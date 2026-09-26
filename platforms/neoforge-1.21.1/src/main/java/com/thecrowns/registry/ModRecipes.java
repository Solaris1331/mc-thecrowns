package com.thecrowns.registry;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.recipe.GlitchedCrownRecipe;
import com.thecrowns.recipe.UnleashedCrownRecipe;
import com.thecrowns.recipe.DimensionalCrownRecipe;
import com.thecrowns.recipe.WarriorCrownRecipe;
import com.thecrowns.recipe.AngelicCrownRecipe;
import com.thecrowns.recipe.CrownBuilderRecipe;
import com.thecrowns.recipe.GlitchedCrownFusionRecipe;
import com.thecrowns.recipe.Tier2BuilderRecipe;
import com.thecrowns.recipe.Tier3BuilderRecipe;
import com.thecrowns.recipe.Tier4BuilderRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.function.Supplier;

public final class ModRecipes {
    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeType<?>> TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, TheCrownsMod.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, TheCrownsMod.MOD_ID);

    public static final Supplier<RecipeSerializer<GlitchedCrownRecipe>> GLITCHED_CROWN =
            SERIALIZERS.register("glitched_crown",
                    () -> new SimpleCraftingRecipeSerializer<>(GlitchedCrownRecipe::new));

    public static final Supplier<RecipeSerializer<UnleashedCrownRecipe>> UNLEASHED_CROWN =
            SERIALIZERS.register("unleashed_crown",
                    () -> new SimpleCraftingRecipeSerializer<>(UnleashedCrownRecipe::new));

    public static final Supplier<RecipeSerializer<DimensionalCrownRecipe>> DIMENSIONAL_CROWN =
            SERIALIZERS.register("dimensional_crown",
                    () -> new SimpleCraftingRecipeSerializer<>(DimensionalCrownRecipe::new));

    public static final Supplier<RecipeSerializer<WarriorCrownRecipe>> WARRIOR_CROWN =
            SERIALIZERS.register("warrior_crown",
                    () -> new SimpleCraftingRecipeSerializer<>(WarriorCrownRecipe::new));

    public static final Supplier<RecipeSerializer<AngelicCrownRecipe>> ANGELIC_CROWN =
            SERIALIZERS.register("angelic_crown",
                    () -> new SimpleCraftingRecipeSerializer<>(AngelicCrownRecipe::new));


    public static final Supplier<net.minecraft.world.item.crafting.RecipeType<CrownBuilderRecipe>> CROWN_BUILDER_TYPE =
            TYPES.register("crown_builder", () -> new net.minecraft.world.item.crafting.RecipeType<>() {
                @Override public String toString() { return TheCrownsMod.MOD_ID + ":crown_builder"; }
            });

    public static final Supplier<RecipeSerializer<CrownBuilderRecipe>> CROWN_BUILDER =
            SERIALIZERS.register("crown_builder", CrownBuilderRecipe.Serializer::new);

    public static final Supplier<RecipeSerializer<GlitchedCrownFusionRecipe>> GLITCHED_CROWN_FUSION =
            SERIALIZERS.register("glitched_crown_fusion", GlitchedCrownFusionRecipe.Serializer::new);

    public static final Supplier<RecipeSerializer<Tier2BuilderRecipe>> TIER2_BUILDER_UPGRADE =
            SERIALIZERS.register("tier2_builder_upgrade", () -> new SimpleCraftingRecipeSerializer<>(Tier2BuilderRecipe::new));
    public static final Supplier<RecipeSerializer<Tier3BuilderRecipe>> TIER3_BUILDER_UPGRADE =
            SERIALIZERS.register("tier3_builder_upgrade", () -> new SimpleCraftingRecipeSerializer<>(Tier3BuilderRecipe::new));
    public static final Supplier<RecipeSerializer<Tier4BuilderRecipe>> TIER4_BUILDER_UPGRADE =
            SERIALIZERS.register("tier4_builder_upgrade", () -> new SimpleCraftingRecipeSerializer<>(Tier4BuilderRecipe::new));

    private ModRecipes() {
    }
}
