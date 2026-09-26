package com.thecrowns.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thecrowns.logic.GlitchedCrownFusion;
import com.thecrowns.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

import java.util.stream.Stream;

/** Nether Star template + Glitched Crown base + supported Crown addition. */
public final class GlitchedCrownFusionRecipe implements SmithingRecipe {
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final ItemStack result;

    public GlitchedCrownFusionRecipe(Ingredient template, Ingredient base, Ingredient addition, ItemStack result) {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        if (!template.test(input.template()) || !base.test(input.base()) || !addition.test(input.addition())) return false;
        ResourceLocation additionId = BuiltInRegistries.ITEM.getKey(input.addition().getItem());
        return !GlitchedCrownFusion.hasFusion(input.base(), additionId);
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        if (!base.test(input.base()) || !addition.test(input.addition())) return ItemStack.EMPTY;
        ItemStack output = input.base().transmuteCopy(result.getItem(), result.getCount());
        output.applyComponents(result.getComponentsPatch());
        ItemStack previous = GlitchedCrownFusion.getFusionStack(output);
        GlitchedCrownFusion.setFusion(output, input.addition());
        if (!previous.isEmpty()) GlitchedCrownFusion.markPendingReturn(output, previous);
        return output;
    }

    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }
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
        private static final MapCodec<GlitchedCrownFusionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("template").forGetter(recipe -> recipe.template),
                Ingredient.CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
                Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> recipe.addition),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
        ).apply(instance, GlitchedCrownFusionRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, GlitchedCrownFusionRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork);

        @Override public MapCodec<GlitchedCrownFusionRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, GlitchedCrownFusionRecipe> streamCodec() { return STREAM_CODEC; }

        private static GlitchedCrownFusionRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            return new GlitchedCrownFusionRecipe(
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                    ItemStack.STREAM_CODEC.decode(buffer));
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, GlitchedCrownFusionRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.template);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.base);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.addition);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    }
}
