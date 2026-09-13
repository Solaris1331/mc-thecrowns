package com.glitchedcrown.recipe;

import com.glitchedcrown.logic.GlitchedCrownFusion;
import com.glitchedcrown.registry.ModRecipes;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;

import java.util.stream.Stream;

/**
 * Nether Star template + Glitched Crown base + supported Crown addition.
 * The addition replaces the single active fusion while all base NBT is preserved.
 */
public final class GlitchedCrownFusionRecipe implements SmithingRecipe {
    private final ResourceLocation id;
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final ItemStack result;

    public GlitchedCrownFusionRecipe(ResourceLocation id, Ingredient template, Ingredient base,
                                     Ingredient addition, ItemStack result) {
        this.id = id;
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() < 3
                || !template.test(container.getItem(0))
                || !base.test(container.getItem(1))
                || !addition.test(container.getItem(2))) {
            return false;
        }
        ResourceLocation additionId = BuiltInRegistries.ITEM.getKey(container.getItem(2).getItem());
        return !GlitchedCrownFusion.hasFusion(container.getItem(1), additionId);
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        if (container.getContainerSize() < 3
                || !base.test(container.getItem(1))
                || !addition.test(container.getItem(2))) {
            return ItemStack.EMPTY;
        }
        ItemStack output = result.copy();
        if (container.getItem(1).getTag() != null) {
            output.setTag(container.getItem(1).getTag().copy());
        }
        ItemStack previous = GlitchedCrownFusion.getFusionStack(output);
        GlitchedCrownFusion.setFusion(output, container.getItem(2));
        if (!previous.isEmpty()) GlitchedCrownFusion.markPendingReturn(output, previous);
        return output;
    }

    @Override public ItemStack getResultItem(RegistryAccess registryAccess) { return result.copy(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.GLITCHED_CROWN_FUSION.get(); }
    @Override public boolean isTemplateIngredient(ItemStack stack) { return template.test(stack); }
    @Override public boolean isBaseIngredient(ItemStack stack) { return base.test(stack); }
    @Override public boolean isAdditionIngredient(ItemStack stack) { return addition.test(stack); }
    @Override public boolean isIncomplete() { return Stream.of(template, base, addition).anyMatch(Ingredient::hasNoItems); }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(template);
        ingredients.add(base);
        ingredients.add(addition);
        return ingredients;
    }

    public static final class Serializer implements RecipeSerializer<GlitchedCrownFusionRecipe> {
        @Override
        public GlitchedCrownFusionRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient template = Ingredient.fromJson(GsonHelper.getNonNull(json, "template"));
            Ingredient base = Ingredient.fromJson(GsonHelper.getNonNull(json, "base"));
            Ingredient addition = Ingredient.fromJson(GsonHelper.getNonNull(json, "addition"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new GlitchedCrownFusionRecipe(id, template, base, addition, result);
        }

        @Override
        public GlitchedCrownFusionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient template = Ingredient.fromNetwork(buf);
            Ingredient base = Ingredient.fromNetwork(buf);
            Ingredient addition = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            return new GlitchedCrownFusionRecipe(id, template, base, addition, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, GlitchedCrownFusionRecipe recipe) {
            recipe.template.toNetwork(buf);
            recipe.base.toNetwork(buf);
            recipe.addition.toNetwork(buf);
            buf.writeItem(recipe.result);
        }
    }
}
