package com.thecrowns.recipe;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.config.CrownServerConfig;
import com.thecrowns.logic.CrownAdvancements;
import com.thecrowns.registry.ModItems;
import com.thecrowns.registry.ModRecipes;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.thecrowns.util.PotionUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

/** Datapack-driven fixed 3x3 recipe for Crown Builder I-IV. */
public final class CrownBuilderRecipe implements Recipe<CrownBuilderRecipeInput> {
    private static final ResourceLocation UNKNOWN_RECIPE_ID = ResourceLocation.fromNamespaceAndPath(TheCrownsMod.MOD_ID, "unknown_crown_builder_recipe");
    private final ResourceLocation id;
    private final ShapedRecipePattern pattern;
    private final ItemStack result;
    private final NonNullList<Ingredient> ingredients;
    private final int requiredTier;
    private final Map<Integer, ResourceLocation> potionSlots;
    private final Map<Integer, EffectRequirement> effectSlots;
    private final int enchantedBookSlot;
    private final ResourceLocation requiredAdvancement;
    private final boolean preserveDragonEgg;
    private final CraftingBookCategory category;

    private CrownBuilderRecipe(ResourceLocation id,
                              ShapedRecipePattern pattern,
                              ItemStack result,
                              int requiredTier,
                              Map<Integer, ResourceLocation> potionSlots,
                              Map<Integer, EffectRequirement> effectSlots,
                              int enchantedBookSlot,
                              ResourceLocation requiredAdvancement,
                              boolean preserveDragonEgg,
                              CraftingBookCategory category) {
        this.id = id;
        if (pattern.width() != 3 || pattern.height() != 3) {
            throw new IllegalArgumentException("Crown Builder recipes require a 3x3 pattern");
        }
        this.pattern = pattern;
        this.result = result;
        this.ingredients = NonNullList.create();
        this.ingredients.addAll(pattern.ingredients());
        this.requiredTier = requiredTier;
        this.potionSlots = Collections.unmodifiableMap(new HashMap<>(potionSlots));
        this.effectSlots = Collections.unmodifiableMap(new HashMap<>(effectSlots));
        this.enchantedBookSlot = enchantedBookSlot;
        this.requiredAdvancement = requiredAdvancement;
        this.preserveDragonEgg = preserveDragonEgg;
        this.category = category;
    }

    private CrownBuilderRecipe withId(ResourceLocation id) {
        return new CrownBuilderRecipe(id, pattern, result, requiredTier, potionSlots, effectSlots,
                enchantedBookSlot, requiredAdvancement, preserveDragonEgg, category);
    }

    @Override
    public boolean matches(CrownBuilderRecipeInput container, Level level) {
        if (!isRecipeEnabled()) return false;
        if (container.size() < 9) return false;
        for (int i = 0; i < 9; i++) {
            if (!ingredients.get(i).test(container.getItem(i))) return false;
        }
        for (Map.Entry<Integer, ResourceLocation> entry : potionSlots.entrySet()) {
            int slot = entry.getKey();
            if (slot < 0 || slot >= 9) return false;
            ItemStack stack = container.getItem(slot);
            if (!stack.is(Items.POTION)) return false;
            ResourceLocation actual = BuiltInRegistries.POTION.getKey(PotionUtils.getPotion(stack).value());
            if (!entry.getValue().equals(actual)) return false;
        }
        for (Map.Entry<Integer, EffectRequirement> entry : effectSlots.entrySet()) {
            int slot = entry.getKey();
            if (slot < 0 || slot >= 9) return false;
            ItemStack stack = container.getItem(slot);
            if (!stack.is(Items.POTION)) return false;
            EffectRequirement requirement = entry.getValue();
            boolean found = PotionUtils.getMobEffects(stack).stream().anyMatch(effect ->
                    requirement.effect().equals(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()))
                            && effect.getAmplifier() == requirement.amplifier());
            if (!found) return false;
        }
        if (enchantedBookSlot >= 0) {
            ItemStack book = container.getItem(enchantedBookSlot);
            if (!book.is(Items.ENCHANTED_BOOK) || book.getOrDefault(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS,
                    net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY).isEmpty()) return false;
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
        var advancement = player.server.getAdvancements().get(requiredAdvancement);
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
            BuiltInRegistries.POTION.getOptional(potionId).ifPresent(p -> PotionUtils.setPotion(stack, BuiltInRegistries.POTION.wrapAsHolder(p)));
            return stack;
        }
        EffectRequirement requirement = effectSlots.get(slot);
        if (requirement != null) {
            var effect = BuiltInRegistries.MOB_EFFECT.getOptional(requirement.effect());
            if (effect.isPresent()) {
                ItemStack stack = new ItemStack(Items.POTION);
                PotionUtils.setCustomEffects(stack, List.of(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get()), 20 * 60, requirement.amplifier())));
                return stack;
            }
        }
        if (enchantedBookSlot == slot) return new ItemStack(Items.ENCHANTED_BOOK);
        return ItemStack.EMPTY;
    }

    public boolean preservesDragonEgg() { return preserveDragonEgg; }
    public ItemStack getDisplayResult() { return result.copy(); }

    @Override public ItemStack assemble(CrownBuilderRecipeInput container, HolderLookup.Provider registries) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width >= 3 && height >= 3; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }
    @Override public NonNullList<Ingredient> getIngredients() { return ingredients; }
    /**
     * Runtime recipe codecs are wrapped by {@code RecipeHolder}, so a custom Recipe no longer
     * receives its datapack id directly. Viewer APIs still require a stable display id; built-in
     * Builder outputs are one-to-one with their recipes, which makes this a deterministic fallback.
     */
    public ResourceLocation getId() {
        if (!UNKNOWN_RECIPE_ID.equals(id)) return id;
        ResourceLocation resultId = BuiltInRegistries.ITEM.getKey(result.getItem());
        return ResourceLocation.fromNamespaceAndPath(TheCrownsMod.MOD_ID,
                "crown_builder/" + resultId.getNamespace() + "/" + resultId.getPath());
    }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.CROWN_BUILDER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.CROWN_BUILDER_TYPE.get(); }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CrownBuilderRecipeInput container) {
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

    private static final Codec<Map<Integer, ResourceLocation>> POTION_SLOTS_CODEC =
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).xmap(CrownBuilderRecipe::parseSlotMap, CrownBuilderRecipe::stringifySlotMap);
    private static final Codec<Map<Integer, EffectRequirement>> EFFECT_SLOTS_CODEC =
            Codec.unboundedMap(Codec.STRING, EffectRequirement.CODEC).xmap(CrownBuilderRecipe::parseSlotMap, CrownBuilderRecipe::stringifySlotMap);

    private static <T> Map<Integer, T> parseSlotMap(Map<String, T> source) {
        Map<Integer, T> slots = new HashMap<>();
        source.forEach((slot, value) -> {
            int index = Integer.parseInt(slot);
            if (index < 0 || index > 8) throw new IllegalArgumentException("Crown Builder slot must be 0..8: " + index);
            slots.put(index, value);
        });
        return slots;
    }

    private static <T> Map<String, T> stringifySlotMap(Map<Integer, T> source) {
        Map<String, T> slots = new HashMap<>();
        source.forEach((slot, value) -> slots.put(Integer.toString(slot), value));
        return slots;
    }

    public record EffectRequirement(ResourceLocation effect, int amplifier) {
        private static final Codec<EffectRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("effect").forGetter(EffectRequirement::effect),
                Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(EffectRequirement::amplifier)
        ).apply(instance, EffectRequirement::new));
    }

    public static final class Serializer implements RecipeSerializer<CrownBuilderRecipe> {
        private static final MapCodec<CrownBuilderRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                Codec.intRange(1, 4).optionalFieldOf("required_tier", 1).forGetter(recipe -> recipe.requiredTier),
                POTION_SLOTS_CODEC.optionalFieldOf("potion_slots", Map.of()).forGetter(recipe -> recipe.potionSlots),
                EFFECT_SLOTS_CODEC.optionalFieldOf("effect_slots", Map.of()).forGetter(recipe -> recipe.effectSlots),
                Codec.intRange(-1, 8).optionalFieldOf("require_enchanted_book_slot", -1).forGetter(recipe -> recipe.enchantedBookSlot),
                ResourceLocation.CODEC.optionalFieldOf("require_advancement").forGetter(recipe -> Optional.ofNullable(recipe.requiredAdvancement)),
                Codec.BOOL.optionalFieldOf("preserve_dragon_egg", false).forGetter(recipe -> recipe.preserveDragonEgg),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(recipe -> recipe.category)
        ).apply(instance, (pattern, result, tier, potionSlots, effectSlots, bookSlot, advancement, preserveEgg, category) ->
                new CrownBuilderRecipe(UNKNOWN_RECIPE_ID, pattern, result, tier, potionSlots, effectSlots,
                        bookSlot, advancement.orElse(null), preserveEgg, category)));

        private static final StreamCodec<RegistryFriendlyByteBuf, CrownBuilderRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork);

        @Override public MapCodec<CrownBuilderRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, CrownBuilderRecipe> streamCodec() { return STREAM_CODEC; }

        /** Used only by the bundled JEI fallback; runtime datapacks are decoded through {@link #codec()}. */
        public CrownBuilderRecipe fromJson(ResourceLocation id, JsonObject json) {
            return CODEC.codec().parse(JsonOps.INSTANCE, json)
                    .getOrThrow(message -> { throw new JsonSyntaxException(message); })
                    .withId(id);
        }

        private static CrownBuilderRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            int tier = buffer.readVarInt();
            Map<Integer, ResourceLocation> potionSlots = readPotionSlots(buffer);
            Map<Integer, EffectRequirement> effectSlots = readEffectSlots(buffer);
            int bookSlot = buffer.readVarInt();
            ResourceLocation advancement = buffer.readBoolean() ? buffer.readResourceLocation() : null;
            boolean preserveEgg = buffer.readBoolean();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            return new CrownBuilderRecipe(UNKNOWN_RECIPE_ID, pattern, result, tier, potionSlots, effectSlots,
                    bookSlot, advancement, preserveEgg, category);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, CrownBuilderRecipe recipe) {
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeVarInt(recipe.requiredTier);
            writePotionSlots(buffer, recipe.potionSlots);
            writeEffectSlots(buffer, recipe.effectSlots);
            buffer.writeVarInt(recipe.enchantedBookSlot);
            buffer.writeBoolean(recipe.requiredAdvancement != null);
            if (recipe.requiredAdvancement != null) buffer.writeResourceLocation(recipe.requiredAdvancement);
            buffer.writeBoolean(recipe.preserveDragonEgg);
            buffer.writeEnum(recipe.category);
        }

        private static Map<Integer, ResourceLocation> readPotionSlots(RegistryFriendlyByteBuf buffer) {
            Map<Integer, ResourceLocation> slots = new HashMap<>();
            for (int count = buffer.readVarInt(); count > 0; count--) slots.put(buffer.readVarInt(), buffer.readResourceLocation());
            return slots;
        }

        private static Map<Integer, EffectRequirement> readEffectSlots(RegistryFriendlyByteBuf buffer) {
            Map<Integer, EffectRequirement> slots = new HashMap<>();
            for (int count = buffer.readVarInt(); count > 0; count--) {
                slots.put(buffer.readVarInt(), new EffectRequirement(buffer.readResourceLocation(), buffer.readVarInt()));
            }
            return slots;
        }

        private static void writePotionSlots(RegistryFriendlyByteBuf buffer, Map<Integer, ResourceLocation> slots) {
            buffer.writeVarInt(slots.size());
            slots.forEach((slot, potion) -> { buffer.writeVarInt(slot); buffer.writeResourceLocation(potion); });
        }

        private static void writeEffectSlots(RegistryFriendlyByteBuf buffer, Map<Integer, EffectRequirement> slots) {
            buffer.writeVarInt(slots.size());
            slots.forEach((slot, effect) -> {
                buffer.writeVarInt(slot);
                buffer.writeResourceLocation(effect.effect());
                buffer.writeVarInt(effect.amplifier());
            });
        }
    }
}
