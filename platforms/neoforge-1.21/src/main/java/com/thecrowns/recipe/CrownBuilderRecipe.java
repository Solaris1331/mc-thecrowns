package com.thecrowns.recipe;

import com.thecrowns.config.CrownServerConfig;
import com.thecrowns.logic.CrownAdvancements;
import com.thecrowns.registry.ModItems;
import com.thecrowns.registry.ModRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/** Datapack-driven fixed 3x3 recipe for Crown Builder I-IV. */
public final class CrownBuilderRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final ItemStack result;
    private final NonNullList<Ingredient> ingredients;
    private final int requiredTier;
    private final Map<Integer, ResourceLocation> potionSlots;
    private final Map<Integer, EffectRequirement> effectSlots;
    private final int enchantedBookSlot;
    private final ResourceLocation requiredAdvancement;
    private final boolean preserveDragonEgg;
    private final CraftingBookCategory category;

    public CrownBuilderRecipe(ResourceLocation id,
                              ItemStack result,
                              NonNullList<Ingredient> ingredients,
                              int requiredTier,
                              Map<Integer, ResourceLocation> potionSlots,
                              Map<Integer, EffectRequirement> effectSlots,
                              int enchantedBookSlot,
                              ResourceLocation requiredAdvancement,
                              boolean preserveDragonEgg,
                              CraftingBookCategory category) {
        this.id = id;
        this.result = result;
        this.ingredients = ingredients;
        this.requiredTier = requiredTier;
        this.potionSlots = Collections.unmodifiableMap(new HashMap<>(potionSlots));
        this.effectSlots = Collections.unmodifiableMap(new HashMap<>(effectSlots));
        this.enchantedBookSlot = enchantedBookSlot;
        this.requiredAdvancement = requiredAdvancement;
        this.preserveDragonEgg = preserveDragonEgg;
        this.category = category;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (!isRecipeEnabled()) return false;
        if (container.getContainerSize() < 9) return false;
        for (int i = 0; i < 9; i++) {
            if (!ingredients.get(i).test(container.getItem(i))) return false;
        }
        for (Map.Entry<Integer, ResourceLocation> entry : potionSlots.entrySet()) {
            int slot = entry.getKey();
            if (slot < 0 || slot >= 9) return false;
            ItemStack stack = container.getItem(slot);
            if (!stack.is(Items.POTION)) return false;
            ResourceLocation actual = BuiltInRegistries.POTION.getKey(PotionUtils.getPotion(stack));
            if (!entry.getValue().equals(actual)) return false;
        }
        for (Map.Entry<Integer, EffectRequirement> entry : effectSlots.entrySet()) {
            int slot = entry.getKey();
            if (slot < 0 || slot >= 9) return false;
            ItemStack stack = container.getItem(slot);
            if (!stack.is(Items.POTION)) return false;
            EffectRequirement requirement = entry.getValue();
            boolean found = PotionUtils.getMobEffects(stack).stream().anyMatch(effect ->
                    requirement.effect().equals(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect()))
                            && effect.getAmplifier() == requirement.amplifier());
            if (!found) return false;
        }
        if (enchantedBookSlot >= 0) {
            ItemStack book = container.getItem(enchantedBookSlot);
            if (!book.is(Items.ENCHANTED_BOOK) || book.getEnchantmentTags().isEmpty()) return false;
        }
        return true;
    }

    /** Existing 1.2.x server recipe toggles remain authoritative after the Builder migration. */
    public boolean isRecipeEnabled() {
        if (result.is(ModItems.GLITCHED_CROWN.get())) return CrownServerConfig.GLITCHED_RECIPE.get();
        if (result.is(ModItems.UNLEASHED_CROWN.get())) return CrownServerConfig.UNLEASHED_RECIPE.get();
        return true;
    }

    public boolean canPlayerCraft(ServerPlayer player, int builderTier) {
        if (!isRecipeEnabled() || player == null || builderTier < requiredTier) return false;
        if (!CrownAdvancements.canCraftTier(player, requiredTier)) return false;
        if (requiredAdvancement == null) return true;
        if (result.is(ModItems.UNLEASHED_CROWN.get())
                && !CrownServerConfig.UNLEASHED_REQUIRE_GLITCHED_ADVANCEMENT.get()) {
            return true;
        }
        var advancement = player.server.getAdvancements().getAdvancement(requiredAdvancement);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public int getRequiredTier() { return requiredTier; }
    public ResourceLocation getRequiredAdvancement() { return requiredAdvancement; }
    public Map<Integer, ResourceLocation> getPotionSlots() { return potionSlots; }
    public Map<Integer, EffectRequirement> getEffectSlots() { return effectSlots; }
    public int getEnchantedBookSlot() { return enchantedBookSlot; }

    public ItemStack getSpecialDisplayStack(int slot) {
        ResourceLocation potionId = potionSlots.get(slot);
        if (potionId != null) {
            ItemStack stack = new ItemStack(Items.POTION);
            BuiltInRegistries.POTION.getOptional(potionId).ifPresent(p -> PotionUtils.setPotion(stack, p));
            return stack;
        }
        EffectRequirement requirement = effectSlots.get(slot);
        if (requirement != null) {
            var effect = BuiltInRegistries.MOB_EFFECT.getOptional(requirement.effect());
            if (effect.isPresent()) {
                ItemStack stack = new ItemStack(Items.POTION);
                PotionUtils.setCustomEffects(stack, List.of(new MobEffectInstance(effect.get(), 20 * 60, requirement.amplifier())));
                return stack;
            }
        }
        if (enchantedBookSlot == slot) return new ItemStack(Items.ENCHANTED_BOOK);
        return ItemStack.EMPTY;
    }

    public record EffectRequirement(ResourceLocation effect, int amplifier) {}
    public boolean preservesDragonEgg() { return preserveDragonEgg; }
    public ItemStack getDisplayResult() { return result.copy(); }

    @Override public ItemStack assemble(Container container, RegistryAccess registryAccess) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width >= 3 && height >= 3; }
    @Override public ItemStack getResultItem(RegistryAccess registryAccess) { return result.copy(); }
    @Override public NonNullList<Ingredient> getIngredients() { return ingredients; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.CROWN_BUILDER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.CROWN_BUILDER_TYPE.get(); }

    @Override
    public NonNullList<ItemStack> getRemainingItems(Container container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < 9; i++) {
            ItemStack input = container.getItem(i);
            if (input.hasCraftingRemainingItem()) remaining.set(i, input.getCraftingRemainingItem());
        }
        if (preserveDragonEgg && container.getItem(4).is(Items.DRAGON_EGG)) {
            remaining.set(4, new ItemStack(Items.DRAGON_EGG));
        }
        return remaining;
    }

    public static final class Serializer implements RecipeSerializer<CrownBuilderRecipe> {
        @Override
        public CrownBuilderRecipe fromJson(ResourceLocation id, JsonObject json) {
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(json, "category", "misc"), CraftingBookCategory.MISC);
            JsonArray patternArray = GsonHelper.getAsJsonArray(json, "pattern");
            if (patternArray.size() != 3) throw new JsonSyntaxException("Crown Builder recipes require exactly 3 rows");

            String[] pattern = new String[3];
            for (int i = 0; i < 3; i++) {
                pattern[i] = GsonHelper.convertToString(patternArray.get(i), "pattern[" + i + "]");
                if (pattern[i].length() != 3) throw new JsonSyntaxException("Every Crown Builder pattern row must be exactly 3 characters");
            }

            JsonObject keyObject = GsonHelper.getAsJsonObject(json, "key");
            Map<Character, Ingredient> key = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : keyObject.entrySet()) {
                String symbol = entry.getKey();
                if (symbol.length() != 1 || " ".equals(symbol)) throw new JsonSyntaxException("Invalid recipe key: " + symbol);
                key.put(symbol.charAt(0), Ingredient.fromJson(entry.getValue()));
            }

            NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    char symbol = pattern[row].charAt(col);
                    if (symbol == ' ') continue;
                    Ingredient ingredient = key.get(symbol);
                    if (ingredient == null) throw new JsonSyntaxException("Pattern uses undefined symbol '" + symbol + "'");
                    ingredients.set(row * 3 + col, ingredient);
                }
            }

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int requiredTier = GsonHelper.getAsInt(json, "required_tier", 1);
            if (requiredTier < 1 || requiredTier > 4) throw new JsonSyntaxException("required_tier must be 1..4");

            Map<Integer, ResourceLocation> potionSlots = new HashMap<>();
            if (json.has("potion_slots")) {
                JsonObject potionObj = GsonHelper.getAsJsonObject(json, "potion_slots");
                for (Map.Entry<String, JsonElement> entry : potionObj.entrySet()) {
                    int slot;
                    try { slot = Integer.parseInt(entry.getKey()); }
                    catch (NumberFormatException ex) { throw new JsonSyntaxException("Potion slot must be an integer: " + entry.getKey()); }
                    if (slot < 0 || slot > 8) throw new JsonSyntaxException("Potion slot must be 0..8: " + slot);
                    potionSlots.put(slot, new ResourceLocation(GsonHelper.convertToString(entry.getValue(), entry.getKey())));
                }
            }

            Map<Integer, EffectRequirement> effectSlots = new HashMap<>();
            if (json.has("effect_slots")) {
                JsonObject effectObj = GsonHelper.getAsJsonObject(json, "effect_slots");
                for (Map.Entry<String, JsonElement> entry : effectObj.entrySet()) {
                    int slot;
                    try { slot = Integer.parseInt(entry.getKey()); }
                    catch (NumberFormatException ex) { throw new JsonSyntaxException("Effect slot must be an integer: " + entry.getKey()); }
                    if (slot < 0 || slot > 8) throw new JsonSyntaxException("Effect slot must be 0..8: " + slot);
                    JsonObject requirement = GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey());
                    ResourceLocation effect = new ResourceLocation(GsonHelper.getAsString(requirement, "effect"));
                    int amplifier = GsonHelper.getAsInt(requirement, "amplifier", 0);
                    if (amplifier < 0 || amplifier > 255) throw new JsonSyntaxException("effect amplifier must be 0..255");
                    effectSlots.put(slot, new EffectRequirement(effect, amplifier));
                }
            }

            int enchantedBookSlot = GsonHelper.getAsInt(json, "require_enchanted_book_slot", -1);
            if (enchantedBookSlot < -1 || enchantedBookSlot > 8) throw new JsonSyntaxException("require_enchanted_book_slot must be -1 or 0..8");
            ResourceLocation requiredAdvancement = json.has("require_advancement")
                    ? new ResourceLocation(GsonHelper.getAsString(json, "require_advancement")) : null;
            boolean preserveDragonEgg = GsonHelper.getAsBoolean(json, "preserve_dragon_egg", false);

            return new CrownBuilderRecipe(id, result, ingredients, requiredTier, potionSlots, effectSlots,
                    enchantedBookSlot, requiredAdvancement, preserveDragonEgg, category);
        }

        @Override
        public CrownBuilderRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
            ItemStack result = buf.readItem();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
            for (int i = 0; i < 9; i++) ingredients.set(i, Ingredient.fromNetwork(buf));
            int requiredTier = buf.readVarInt();
            int potionCount = buf.readVarInt();
            Map<Integer, ResourceLocation> potionSlots = new HashMap<>();
            for (int i = 0; i < potionCount; i++) potionSlots.put(buf.readVarInt(), buf.readResourceLocation());
            int effectCount = buf.readVarInt();
            Map<Integer, EffectRequirement> effectSlots = new HashMap<>();
            for (int i = 0; i < effectCount; i++) {
                effectSlots.put(buf.readVarInt(), new EffectRequirement(buf.readResourceLocation(), buf.readVarInt()));
            }
            int enchantedBookSlot = buf.readVarInt();
            ResourceLocation requiredAdvancement = buf.readBoolean() ? buf.readResourceLocation() : null;
            boolean preserveDragonEgg = buf.readBoolean();
            return new CrownBuilderRecipe(id, result, ingredients, requiredTier, potionSlots, effectSlots,
                    enchantedBookSlot, requiredAdvancement, preserveDragonEgg, category);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CrownBuilderRecipe recipe) {
            buf.writeEnum(recipe.category);
            buf.writeItem(recipe.result);
            for (Ingredient ingredient : recipe.ingredients) ingredient.toNetwork(buf);
            buf.writeVarInt(recipe.requiredTier);
            buf.writeVarInt(recipe.potionSlots.size());
            for (Map.Entry<Integer, ResourceLocation> entry : recipe.potionSlots.entrySet()) {
                buf.writeVarInt(entry.getKey());
                buf.writeResourceLocation(entry.getValue());
            }
            buf.writeVarInt(recipe.effectSlots.size());
            for (Map.Entry<Integer, EffectRequirement> entry : recipe.effectSlots.entrySet()) {
                buf.writeVarInt(entry.getKey());
                buf.writeResourceLocation(entry.getValue().effect());
                buf.writeVarInt(entry.getValue().amplifier());
            }
            buf.writeVarInt(recipe.enchantedBookSlot);
            buf.writeBoolean(recipe.requiredAdvancement != null);
            if (recipe.requiredAdvancement != null) buf.writeResourceLocation(recipe.requiredAdvancement);
            buf.writeBoolean(recipe.preserveDragonEgg);
        }
    }
}
