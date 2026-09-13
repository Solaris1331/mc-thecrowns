package com.glitchedcrown.emi;

import com.glitchedcrown.GlitchedCrownMod;
import com.glitchedcrown.menu.CrownBuilderMenu;
import com.glitchedcrown.recipe.CrownBuilderRecipe;
import com.glitchedcrown.registry.ModItems;
import com.glitchedcrown.registry.ModMenus;
import com.glitchedcrown.registry.ModRecipes;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.ArrayList;
import java.util.List;

/** Native EMI integration. Loaded by EMI only when EMI is installed. */
@EmiEntrypoint
public final class TheCrownsEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory CROWN_BUILDER = new EmiRecipeCategory(
            new ResourceLocation(GlitchedCrownMod.MOD_ID, "crown_builder"),
            EmiStack.of(ModItems.CROWN_BUILDER_T1.get()));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(CROWN_BUILDER);
        registry.addWorkstation(CROWN_BUILDER, EmiStack.of(ModItems.CROWN_BUILDER_T1.get()));
        registry.addWorkstation(CROWN_BUILDER, EmiStack.of(ModItems.CROWN_BUILDER_T2.get()));
        registry.addWorkstation(CROWN_BUILDER, EmiStack.of(ModItems.CROWN_BUILDER_T3.get()));
        registry.addWorkstation(CROWN_BUILDER, EmiStack.of(ModItems.CROWN_BUILDER_T4.get()));

        List<CrownBuilderRecipe> recipes = registry.getRecipeManager().getAllRecipesFor(ModRecipes.CROWN_BUILDER_TYPE.get()).stream()
                .filter(CrownBuilderRecipe::isRecipeEnabled)
                .filter(recipe -> !recipe.getDisplayResult().is(ModItems.UNLEASHED_CROWN.get()))
                .toList();
        recipes.stream().map(CrownBuilderEmiRecipe::new).forEach(registry::addRecipe);

        registry.addRecipeHandler(ModMenus.CROWN_BUILDER.get(), new CrownBuilderEmiRecipeHandler());
        GlitchedCrownMod.LOGGER.info("EMI: registered Crown Builder category, 4 workstations, {} recipes, and transfer handler",
                recipes.size());
    }

    static final class CrownBuilderEmiRecipe implements EmiRecipe {
        private final CrownBuilderRecipe recipe;
        private final List<EmiIngredient> inputs;
        private final List<EmiStack> outputs;

        CrownBuilderEmiRecipe(CrownBuilderRecipe recipe) {
            this.recipe = recipe;
            this.inputs = new ArrayList<>(9);
            for (int slot = 0; slot < 9; slot++) {
                ItemStack special = recipe.getSpecialDisplayStack(slot);
                if (!special.isEmpty()) inputs.add(EmiStack.of(special));
                else inputs.add(EmiIngredient.of(recipe.getIngredients().get(slot)));
            }
            this.outputs = List.of(EmiStack.of(recipe.getDisplayResult()));
        }

        CrownBuilderRecipe source() { return recipe; }

        @Override public EmiRecipeCategory getCategory() { return CROWN_BUILDER; }
        @Override public ResourceLocation getId() { return recipe.getId(); }
        @Override public List<EmiIngredient> getInputs() { return inputs; }
        @Override public List<EmiStack> getOutputs() { return outputs; }
        @Override public int getDisplayWidth() { return 128; }
        @Override public int getDisplayHeight() { return 78; }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int slot = row * 3 + col;
                    addSlot(widgets, inputs.get(slot), 1 + col * 18, 1 + row * 18);
                }
            }
            addSlot(widgets, outputs.get(0), 91, 19);
            addTierText(widgets, recipe.getRequiredTier());
        }

        private static void addSlot(WidgetHolder widgets, EmiIngredient ingredient, int x, int y) {
            try {
                for (var method : widgets.getClass().getMethods()) {
                    if (method.getName().equals("addSlot") && method.getParameterCount() == 3) {
                        method.invoke(widgets, ingredient, x, y);
                        return;
                    }
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        private static void addTierText(WidgetHolder widgets, int tier) {
            var text = net.minecraft.network.chat.Component.translatable(
                    "jei.glitchedcrown.required_tier", roman(tier));
            try {
                for (var method : widgets.getClass().getMethods()) {
                    if (!method.getName().equals("addText") || method.getParameterCount() != 5) continue;
                    method.invoke(widgets, text, 1, 61, 0x404040, false);
                    return;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        private static String roman(int tier) {
            return switch (tier) { case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; default -> "I"; };
        }
    }

    /** EMI's standard transfer engine handles inventory movement; tier-invalid Builders expose no craft slots. */
    static final class CrownBuilderEmiRecipeHandler implements StandardRecipeHandler<CrownBuilderMenu> {
        @Override
        public List<Slot> getInputSources(CrownBuilderMenu menu) {
            return menu.slots.subList(CrownBuilderMenu.INPUT_SLOT_START,
                    CrownBuilderMenu.PLAYER_SLOT_START + CrownBuilderMenu.PLAYER_SLOT_COUNT);
        }

        @Override
        public List<Slot> getCraftingSlots(CrownBuilderMenu menu) {
            return menu.slots.subList(CrownBuilderMenu.INPUT_SLOT_START,
                    CrownBuilderMenu.INPUT_SLOT_START + CrownBuilderMenu.INPUT_SLOT_COUNT);
        }

        @Override
        public Slot getOutputSlot(CrownBuilderMenu menu) {
            return menu.getSlot(CrownBuilderMenu.RESULT_SLOT);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            if (!(recipe instanceof CrownBuilderEmiRecipe crownRecipe) || !crownRecipe.source().isRecipeEnabled()) return false;
            var player = net.minecraft.client.Minecraft.getInstance().player;
            if (player != null && player.containerMenu instanceof CrownBuilderMenu menu) {
                return menu.getBuilderTier() >= crownRecipe.source().getRequiredTier();
            }
            return true;
        }
    }
}
