package com.glitchedcrown.client.screen;

import com.glitchedcrown.GlitchedCrownMod;
import com.glitchedcrown.menu.CrownLorebookMenu;
import com.glitchedcrown.logic.CrownLogic;
import com.glitchedcrown.logic.AdvancedCrownLogic;
import com.glitchedcrown.recipe.CrownBuilderRecipe;
import com.glitchedcrown.registry.ModItems;
import com.glitchedcrown.registry.ModRecipes;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.util.FormattedCharSequence;

/**
 * Patchouli/The Acknowledgment-inspired native lorebook.
 * It deliberately does not depend on Patchouli, JEI, EMI, or REI.
 */
public final class CrownLorebookScreen extends AbstractContainerScreen<CrownLorebookMenu> {
    private static final int BOOK_WIDTH = 408;
    private static final int BOOK_HEIGHT = 244;
    private static final int PAGE_WIDTH = 184;
    private static final int PAGE_HEIGHT = 216;
    private static final int GAP = 12;
    private static final int PARCHMENT = 0xFFF1E3BD;
    private static final int PARCHMENT_DARK = 0xFFE2CEA0;
    private static final int INK = 0xFF352817;
    private static final int GOLD = 0xFFFFAA00;
    private static final int LINK = 0xFF00AAAA;
    private static final int LINK_HOVER = 0xFFAA00AA;
    private static final int SLOT_BG = 0xFF8B724A;
    private static final int SLOT_INNER = 0xFFF8EDCE;

    private enum View { HOME, CATEGORY, ENTRY }

    private record Category(String id, Component name, Component description, Supplier<ItemStack> icon) {}
    private record Entry(String id, String category, Supplier<ItemStack> item, ResourceLocation recipeId,
                         int requiredTier, List<String> detailKeys, List<String> noteKeys) {}

    private static final List<Category> CATEGORIES = List.of(
            category("tier1", "gui.glitchedcrown.lorebook.category.tier1", "gui.glitchedcrown.lorebook.category.tier1.desc", () -> new ItemStack(ModItems.BURNING_CROWN.get())),
            category("tier2", "gui.glitchedcrown.lorebook.category.tier2", "gui.glitchedcrown.lorebook.category.tier2.desc", () -> new ItemStack(ModItems.WARRIOR_CROWN.get())),
            category("tier3", "gui.glitchedcrown.lorebook.category.tier3", "gui.glitchedcrown.lorebook.category.tier3.desc", () -> new ItemStack(ModItems.ANGELIC_CROWN.get())),
            category("tier4", "gui.glitchedcrown.lorebook.category.tier4", "gui.glitchedcrown.lorebook.category.tier4.desc", () -> new ItemStack(ModItems.GLITCHED_CROWN.get())),
            category("builders", "gui.glitchedcrown.lorebook.category.builders", "gui.glitchedcrown.lorebook.category.builders.desc", () -> new ItemStack(ModItems.CROWN_BUILDER_T1.get())),
            category("materials", "gui.glitchedcrown.lorebook.category.materials", "gui.glitchedcrown.lorebook.category.materials.desc", () -> new ItemStack(ModItems.COMPRESSED_EMERALD_BLOCK.get())),
            category("special", "gui.glitchedcrown.lorebook.category.special", "gui.glitchedcrown.lorebook.category.special.desc", () -> new ItemStack(ModItems.CURSED_CROWN.get()))
    );

    private static final List<Entry> ENTRIES = List.of(
            entry("builder1", "builders", ModItems.CROWN_BUILDER_T1, "crown_builder_t1", 0,
                    builderDetails(1)),
            entry("builder2", "builders", ModItems.CROWN_BUILDER_T2, "crown_builder_t2", 0,
                    builderDetails(2)),
            entry("builder3", "builders", ModItems.CROWN_BUILDER_T3, "crown_builder_t3", 0,
                    builderDetails(3)),
            entry("builder4", "builders", ModItems.CROWN_BUILDER_T4, "crown_builder_t4", 0,
                    builderDetails(4)),

            entry("compressed_emerald", "materials", ModItems.COMPRESSED_EMERALD_BLOCK, "compressed_emerald_block", 0,
                    List.of("gui.glitchedcrown.lorebook.material.compressed_emerald")),
            entry("ironforged_core", "materials", ModItems.IRONFORGED_CORE, "ironforged_core", 0,
                    List.of("gui.glitchedcrown.lorebook.material.ironforged_core")),
            entry("spacetime_stabilizer", "materials", ModItems.SPACETIME_STABILIZER, "spacetime_stabilizer", 0,
                    List.of("gui.glitchedcrown.lorebook.material.spacetime_stabilizer")),
            entry("snow_clump", "materials", ModItems.SNOW_CLUMP, "snow_clump", 0,
                    List.of("gui.glitchedcrown.lorebook.material.snow_clump")),
            entry("rotten_flesh", "materials", ModItems.ROTTEN_FLESH_BLOCK, "rotten_flesh_block", 0,
                    List.of("gui.glitchedcrown.lorebook.material.rotten_flesh")),
            entry("big_rotten_flesh", "materials", ModItems.BIG_ROTTEN_FLESH_BLOCK, "big_rotten_flesh_block", 0,
                    List.of("gui.glitchedcrown.lorebook.material.big_rotten_flesh")),

            crown("burning", "tier1", ModItems.BURNING_CROWN, "burning_crown", 1,
                    "burning_crown", 3, 4, 0, List.of()),
            crown("ironforged", "tier1", ModItems.IRONFORGED_CROWN, "ironforged_crown", 1,
                    "ironforged_crown", 3, 0, 6, List.of()),
            crown("frost", "tier1", ModItems.FROST_CROWN, "frost_crown", 1,
                    "frost_crown", 3, 4, 0, List.of("gui.glitchedcrown.lorebook.note.frost_potion")),

            crown("bloody", "tier2", ModItems.BLOODY_CROWN, "bloody_crown", 2,
                    "bloody_crown", 4, 6, 0, List.of()),
            crown("darkened", "tier2", ModItems.DARKENED_CROWN, "darkened_crown", 2,
                    "darkened_crown", 2, 4, 0, List.of()),
            crown("warrior", "tier2", ModItems.WARRIOR_CROWN, "warrior_crown", 2,
                    "warrior_crown", 4, 1, 5, List.of("gui.glitchedcrown.lorebook.note.strength_ii")),
            crown("divine", "tier2", ModItems.DIVINE_CROWN, "divine_crown", 2,
                    "divine_crown", 5, 1, 2, List.of()),

            crown("light", "tier3", ModItems.CROWN_OF_LIGHT, "crown_of_light", 3,
                    "crown_of_light", 3, 4, 0, List.of()),
            crown("dimensional", "tier3", ModItems.DIMENSIONAL_CROWN, "dimensional_crown", 3,
                    "dimensional_crown", 3, 5, 0, List.of("gui.glitchedcrown.lorebook.note.enchanted_book")),
            crown("angelic", "tier3", ModItems.ANGELIC_CROWN, "angelic_crown", 3,
                    "angelic_crown", 4, 3, 1, List.of("gui.glitchedcrown.lorebook.note.angelic_potions")),
            entry("temporal", "tier3", ModItems.TEMPORAL_CROWN, "temporal_crown", 3,
                    List.of("tooltip.glitchedcrown.temporal_crown.stat.1",
                            "tooltip.glitchedcrown.temporal_crown.stat.2",
                            "tooltip.glitchedcrown.temporal_crown.passive.1",
                            "tooltip.glitchedcrown.temporal_crown.passive.2",
                             "tooltip.glitchedcrown.temporal_crown.active.rewind.title",
                             "tooltip.glitchedcrown.temporal_crown.rewind.1",
                             "tooltip.glitchedcrown.temporal_crown.rewind.2",
                             "tooltip.glitchedcrown.temporal_crown.rewind.3",
                             "gui.glitchedcrown.lorebook.temporal.rewind.meta",
                            "tooltip.glitchedcrown.temporal_crown.active.warp.title",
                            "tooltip.glitchedcrown.temporal_crown.warp.1",
                            "gui.glitchedcrown.lorebook.temporal.warp.meta")),

            crown("glitched", "tier4", ModItems.GLITCHED_CROWN, "glitched_crown", 4,
                    "glitched", 0, 6, 0, List.of("gui.glitchedcrown.lorebook.note.dragon_egg_return")),
            crown("unleashed", "tier4", ModItems.UNLEASHED_CROWN, "unleashed_crown", 4,
                    "unleashed", 0, 9, 0, List.of("gui.glitchedcrown.lorebook.note.unleashed_gate")),

            entry("cursed", "special", ModItems.CURSED_CROWN, null, 0,
                    List.of("tooltip.glitchedcrown.cursed_crown.stat.1",
                            "tooltip.glitchedcrown.cursed_crown.stat.2",
                            "tooltip.glitchedcrown.cursed_crown.stat.3",
                            "tooltip.glitchedcrown.cursed_crown.stat.4",
                            "tooltip.glitchedcrown.cursed_crown.stat.5",
                            "tooltip.glitchedcrown.cursed_crown.stat.6",
                            "tooltip.glitchedcrown.cursed_crown.passive.1",
                             "tooltip.glitchedcrown.cursed_crown.passive.2",
                             "tooltip.glitchedcrown.cursed_crown.passive.3",
                             "tooltip.glitchedcrown.cursed_crown.passive.creative_hint",
                             "tooltip.glitchedcrown.cursed_crown.passive.crown_hint",
                             "tooltip.glitchedcrown.cursed_crown.active.title",
                            "tooltip.glitchedcrown.cursed_crown.active.1",
                            "gui.glitchedcrown.lorebook.cursed.transform"))
    );

    private View view = View.HOME;
    private Category category;
    private Entry entry;
    private int detailScroll;
    private int entryPage;
    private int qualificationScroll;
    private final List<HoverStack> hoverStacks = new ArrayList<>();

    private record HoverStack(ItemStack stack, int x, int y, int size) {}

    public CrownLorebookScreen(CrownLorebookMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = BOOK_WIDTH;
        this.imageHeight = BOOK_HEIGHT;
        this.titleLabelX = -1000;
        this.inventoryLabelX = -1000;
    }

    private static Category category(String id, String name, String desc, Supplier<ItemStack> icon) {
        return new Category(id, Component.translatable(name), Component.translatable(desc), icon);
    }

    private static Entry entry(String id, String category, Supplier<? extends Item> item, String recipe, int tier, List<String> details) {
        return new Entry(id, category, () -> new ItemStack(item.get()), recipe == null ? null : rl(recipe), tier, details, List.of());
    }

    private static Entry crown(String id, String category, Supplier<? extends Item> item, String recipe, int tier,
                               String tooltipRoot, int statLines, int passiveLines, int activeLines, List<String> notes) {
        List<String> details = new ArrayList<>();
        for (int i = 1; i <= statLines; i++) details.add("tooltip.glitchedcrown." + tooltipRoot + ".stat." + i);
        for (int i = 1; i <= passiveLines; i++) details.add("tooltip.glitchedcrown." + tooltipRoot + ".passive." + i);
        if (activeLines > 0) {
            details.add("tooltip.glitchedcrown." + tooltipRoot + ".active.title");
            for (int i = 1; i <= activeLines; i++) details.add("tooltip.glitchedcrown." + tooltipRoot + ".active." + i);
        }
        if (tooltipRoot.equals("glitched")) {
            details.clear();
            for (int i = 1; i <= 6; i++) details.add("tooltip.glitchedcrown.glitched.detail." + i);
        } else if (tooltipRoot.equals("unleashed")) {
            details.clear();
            for (int i = 1; i <= 9; i++) details.add("tooltip.glitchedcrown.unleashed.detail." + i);
        }
        return new Entry(id, category, () -> new ItemStack(item.get()), rl(recipe), tier, List.copyOf(details), notes);
    }

    private static List<String> builderDetails(int tier) {
        return List.of(
                "tooltip.glitchedcrown.builder.workstation",
                "tooltip.glitchedcrown.builder.blast_simple",
                "tooltip.glitchedcrown.builder.mine.t" + tier,
                "tooltip.glitchedcrown.builder.protection.t" + tier,
                "tooltip.glitchedcrown.builder.craftable.t" + tier
        );
    }

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(GlitchedCrownMod.MOD_ID, path);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x + 12, y + 10, x + 12 + PAGE_WIDTH, y + 10 + PAGE_HEIGHT, PARCHMENT);
        graphics.fill(x + 12 + PAGE_WIDTH + GAP, y + 10, x + 12 + PAGE_WIDTH * 2 + GAP, y + 10 + PAGE_HEIGHT, PARCHMENT);
        graphics.fill(x + 12 + PAGE_WIDTH - 2, y + 10, x + 12 + PAGE_WIDTH + GAP + 2, y + 10 + PAGE_HEIGHT, 0xFF5B4025);
        graphics.fill(x + 15, y + 13, x + 12 + PAGE_WIDTH - 3, y + 15, PARCHMENT_DARK);
        graphics.fill(x + 15 + PAGE_WIDTH + GAP, y + 13, x + 12 + PAGE_WIDTH * 2 + GAP - 3, y + 15, PARCHMENT_DARK);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        hoverStacks.clear();
        switch (view) {
            case HOME -> renderHome(graphics, mouseX - leftPos, mouseY - topPos);
            case CATEGORY -> renderCategory(graphics, mouseX - leftPos, mouseY - topPos);
            case ENTRY -> renderEntry(graphics, mouseX - leftPos, mouseY - topPos);
        }
    }

    private void renderHome(GuiGraphics g, int mouseX, int mouseY) {
        int lx = 24;
        int rx = 24 + PAGE_WIDTH + GAP;
        drawCentered(g, Component.translatable("gui.glitchedcrown.lorebook.title"), lx, 25, PAGE_WIDTH - 24, GOLD);

        ItemStack book = new ItemStack(ModItems.CROWN_LOREBOOK.get());
        int homeContentWidth = PAGE_WIDTH - 24;
        int bookIconX = lx + (homeContentWidth - 32) / 2;
        int bookIconY = 48;
        g.pose().pushPose();
        g.pose().translate(bookIconX, bookIconY, 0);
        g.pose().scale(2.0F, 2.0F, 1.0F);
        g.renderItem(book, 0, 0);
        g.pose().popPose();
        hoverStacks.add(new HoverStack(book, bookIconX, bookIconY, 32));

        drawCentered(g, Component.translatable("gui.glitchedcrown.lorebook.landing"),
                lx, 101, homeContentWidth, INK);

        g.drawString(font, Component.translatable("gui.glitchedcrown.lorebook.index"), rx, 25, GOLD, false);
        int baseY = 43;
        List<Category> visibleCategories = visibleCategories();
        for (int i = 0; i < visibleCategories.size(); i++) {
            Category c = visibleCategories.get(i);
            int rowY = baseY + i * 21;
            boolean hover = inside(mouseX, mouseY, rx - 3, rowY - 2, PAGE_WIDTH - 20, 19);
            ItemStack icon = c.icon().get();
            g.renderItem(icon, rx, rowY);
            hoverStacks.add(new HoverStack(icon, rx, rowY, 16));
            g.drawString(font, c.name(), rx + 22, rowY + 4, hover ? LINK_HOVER : LINK, false);
        }
        drawWrapped(g, Component.translatable("gui.glitchedcrown.lorebook.footer"), rx, 203, PAGE_WIDTH - 24, 0xFF80664A);
    }

    private void renderCategory(GuiGraphics g, int mouseX, int mouseY) {
        int lx = 24;
        int rx = 24 + PAGE_WIDTH + GAP;
        drawBackHome(g, mouseX, mouseY);
        drawCentered(g, category.name(), lx, 25, PAGE_WIDTH - 24, GOLD);
        drawWrapped(g, category.description(), lx, 44, PAGE_WIDTH - 24, INK);

        List<Entry> entries = visibleEntries(category);
        int y = 29;
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            int rowY = y + i * 25;
            boolean hover = inside(mouseX, mouseY, rx - 4, rowY - 3, PAGE_WIDTH - 20, 22);
            boolean locked = isLocked(e);
            ItemStack stack = locked ? new ItemStack(Items.BARRIER) : e.item().get();
            g.renderItem(stack, rx, rowY);
            if (!locked) hoverStacks.add(new HoverStack(stack, rx, rowY, 16));
            Component name = locked
                    ? e.item().get().getHoverName().copy().withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.DARK_PURPLE)
                    : e.item().get().getHoverName();
            g.drawString(font, name, rx + 22, rowY + 4, hover ? LINK_HOVER : LINK, false);
        }
    }

    private void renderEntry(GuiGraphics g, int mouseX, int mouseY) {
        if (isGlitchedQualificationPage()) {
            renderGlitchedQualifications(g, mouseX, mouseY);
            return;
        }
        int lx = 24;
        int rx = 24 + PAGE_WIDTH + GAP;
        drawBackHome(g, mouseX, mouseY);
        ItemStack output = entry.item().get();
        drawCentered(g, output.getHoverName(), lx, 23, PAGE_WIDTH - 24, GOLD);

        RenderSystem.enableBlend();
        int entryContentWidth = PAGE_WIDTH - 24;
        int entryIconX = lx + (entryContentWidth - 32) / 2;
        int entryIconY = 41;
        g.pose().pushPose();
        g.pose().translate(entryIconX, entryIconY, 0);
        g.pose().scale(2.0F, 2.0F, 1.0F);
        g.renderItem(output, 0, 0);
        g.pose().popPose();
        hoverStacks.add(new HoverStack(output, entryIconX, entryIconY, 32));

        if (entry.requiredTier() > 0) {
            Component tier = Component.translatable("gui.glitchedcrown.lorebook.required_tier", roman(entry.requiredTier()));
            drawCentered(g, tier, lx, 78, PAGE_WIDTH - 24, 0xFF7A4F18);
        }

        Recipe<?> recipe = findRecipe(entry.recipeId());
        if (recipe != null) {
            renderRecipe(g, recipe, lx + 30, 91, mouseX, mouseY);
        } else {
            drawCentered(g, Component.translatable("gui.glitchedcrown.lorebook.no_recipe"), lx, 112, PAGE_WIDTH - 24, 0xFF8A382E);
        }

        g.drawString(font, Component.translatable("gui.glitchedcrown.lorebook.record"), rx, 27, GOLD, false);
        List<FormattedCharSequence> detailLines = buildEntryDetailLines(entry, PAGE_WIDTH - 26);
        int visible = 14;
        int maxScroll = Math.max(0, detailLines.size() - visible);
        detailScroll = Math.max(0, Math.min(detailScroll, maxScroll));
        int y = 45;
        for (int i = detailScroll; i < Math.min(detailLines.size(), detailScroll + visible); i++) {
            g.drawString(font, detailLines.get(i), rx, y, INK, false);
            y += 11;
        }
        if (maxScroll > 0) {
            Component scroll = Component.translatable("gui.glitchedcrown.lorebook.scroll", detailScroll + 1, maxScroll + 1);
            g.drawString(font, scroll, rx, 204, 0xFF80664A, false);
        }
        if (isGlitchedEntry()) drawEntryPageButton(g, mouseX, mouseY, false);
    }

    private boolean isGlitchedEntry() {
        return entry != null && "glitched".equals(entry.id());
    }

    private boolean isGlitchedQualificationPage() {
        return isGlitchedEntry() && entryPage == 1;
    }

    private void renderGlitchedQualifications(GuiGraphics g, int mouseX, int mouseY) {
        int lx = 24;
        int rx = 24 + PAGE_WIDTH + GAP;
        drawBackHome(g, mouseX, mouseY);

        drawCentered(g, Component.translatable("gui.glitchedcrown.lorebook.qualification.title"), lx, 24, PAGE_WIDTH - 24, GOLD);
        int y = 43;
        y += drawWrapped(g, Component.translatable("gui.glitchedcrown.lorebook.qualification.intro"),
                lx, y, PAGE_WIDTH - 24, INK);
        y += 8;
        g.drawString(font, Component.translatable("gui.glitchedcrown.lorebook.qualification.core"), lx, y, 0xFF7A4F18, false);
        y += 15;
        for (var status : menu.coreStatus()) {
            drawAdvancementStatus(g, status, lx, y, PAGE_WIDTH - 24);
            y += 18;
        }

        int ry = 25;
        ry += drawWrapped(g, Component.translatable("gui.glitchedcrown.lorebook.qualification.substitute_intro"),
                rx, ry, PAGE_WIDTH - 24, INK);
        ry += 8;
        g.drawString(font, Component.translatable("gui.glitchedcrown.lorebook.qualification.substitutes"), rx, ry, 0xFF7A4F18, false);
        ry += 15;

        List<com.glitchedcrown.logic.CrownAdvancementGate.AdvancementStatus> substitutes = menu.substituteStatus();
        int visible = qualificationVisibleSubstitutes();
        int maxScroll = Math.max(0, substitutes.size() - visible);
        qualificationScroll = Math.max(0, Math.min(qualificationScroll, maxScroll));
        int listY = ry;
        for (int i = qualificationScroll; i < Math.min(substitutes.size(), qualificationScroll + visible); i++) {
            drawAdvancementStatus(g, substitutes.get(i), rx, listY, PAGE_WIDTH - 24);
            listY += 15;
        }
        if (maxScroll > 0) {
            Component scroll = Component.translatable("gui.glitchedcrown.lorebook.scroll", qualificationScroll + 1, maxScroll + 1);
            g.drawString(font, scroll, rx, 204, 0xFF80664A, false);
        }
        drawEntryPageButton(g, mouseX, mouseY, true);
    }

    private int qualificationVisibleSubstitutes() {
        int introLines = font.split(Component.translatable("gui.glitchedcrown.lorebook.qualification.substitute_intro"), PAGE_WIDTH - 24).size();
        int listStart = 25 + introLines * 10 + 8 + 15;
        return Math.max(3, (200 - listStart) / 15);
    }

    private void drawAdvancementStatus(GuiGraphics g,
                                       com.glitchedcrown.logic.CrownAdvancementGate.AdvancementStatus status,
                                       int x, int y, int width) {
        Component marker = Component.literal(status.done() ? "V" : "X")
                .withStyle(status.done() ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD);
        int markerWidth = font.width(marker);
        int textWidth = Math.max(20, width - markerWidth - 8);
        List<FormattedCharSequence> title = font.split(status.title(), textWidth);
        if (!title.isEmpty()) g.drawString(font, title.get(0), x, y, INK, false);
        g.drawString(font, marker, x + width - markerWidth, y, status.done() ? 0xFF00AA00 : 0xFFAA0000, false);
    }

    private void drawEntryPageButton(GuiGraphics g, int mouseX, int mouseY, boolean previous) {
        int rx = 24 + PAGE_WIDTH + GAP;
        Component label = Component.translatable(previous
                ? "gui.glitchedcrown.lorebook.previous"
                : "gui.glitchedcrown.lorebook.next");
        int width = font.width(label);
        int x = rx + PAGE_WIDTH - 24 - width;
        int y = 217;
        boolean hover = inside(mouseX, mouseY, x - 3, y - 2, width + 6, 13);
        g.drawString(font, label, x, y, hover ? LINK_HOVER : LINK, false);
    }

    private Recipe<?> findRecipe(ResourceLocation id) {
        if (id == null || minecraft == null || minecraft.level == null) return null;
        return minecraft.level.getRecipeManager().byKey(id).orElse(null);
    }

    private void renderRecipe(GuiGraphics g, Recipe<?> recipe, int x, int y, int mouseX, int mouseY) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        for (int slot = 0; slot < 9; slot++) {
            int sx = x + (slot % 3) * 22;
            int sy = y + (slot / 3) * 22;
            g.fill(sx - 2, sy - 2, sx + 18, sy + 18, SLOT_BG);
            g.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_INNER);
            ItemStack stack = displayIngredient(recipe, ingredients, slot);
            if (!stack.isEmpty()) {
                g.renderItem(stack, sx, sy);
                hoverStacks.add(new HoverStack(stack, sx, sy, 16));
            }
        }
        int ox = x + 78;
        int oy = y + 22;
        g.drawString(font, "→", x + 65, y + 27, 0xFF6A4A23, false);
        g.fill(ox - 2, oy - 2, ox + 18, oy + 18, SLOT_BG);
        g.fill(ox - 1, oy - 1, ox + 17, oy + 17, SLOT_INNER);
        ItemStack result = recipe.getResultItem(minecraft.level.registryAccess());
        if (recipe instanceof CrownBuilderRecipe builderRecipe) result = builderRecipe.getDisplayResult();
        if (!result.isEmpty()) {
            g.renderItem(result, ox, oy);
            hoverStacks.add(new HoverStack(result, ox, oy, 16));
        }
    }

    private ItemStack displayIngredient(Recipe<?> recipe, NonNullList<Ingredient> ingredients, int slot) {
        if (recipe instanceof CrownBuilderRecipe builderRecipe) {
            ItemStack special = builderRecipe.getSpecialDisplayStack(slot);
            if (!special.isEmpty()) return special;
        }
        if (slot >= ingredients.size()) return ItemStack.EMPTY;
        ItemStack[] options = ingredients.get(slot).getItems();
        return options.length == 0 ? ItemStack.EMPTY : options[0].copy();
    }

    private List<FormattedCharSequence> buildEntryDetailLines(Entry current, int width) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (String key : current.detailKeys()) {
            if (!shouldShowDetailKey(current, key)) continue;
            Component detail = detailComponent(current, key);
            lines.addAll(font.split(detail, width));
            lines.add(FormattedCharSequence.EMPTY);
        }
        for (String key : current.noteKeys()) {
            lines.addAll(font.split(Component.translatable(key), width));
            lines.add(FormattedCharSequence.EMPTY);
        }
        while (!lines.isEmpty() && font.width(lines.get(lines.size() - 1)) == 0) lines.remove(lines.size() - 1);
        return lines;
    }

    private boolean shouldShowDetailKey(Entry current, String key) {
        if (!"cursed".equals(current.id())) return true;
        if ("tooltip.glitchedcrown.cursed_crown.passive.creative_hint".equals(key)) {
            return minecraft != null && minecraft.player != null && minecraft.player.isCreative();
        }
        if ("tooltip.glitchedcrown.cursed_crown.passive.crown_hint".equals(key)) {
            return minecraft != null && minecraft.player != null
                    && (CrownLogic.isWearingGlitched(minecraft.player)
                    || CrownLogic.isWearingUnleashed(minecraft.player));
        }
        return true;
    }

    private Component detailComponent(Entry current, String key) {
        if ("builder4".equals(current.id()) && "tooltip.glitchedcrown.builder.craftable.t4".equals(key)) {
            MutableComponent line = Component.translatable("tooltip.glitchedcrown.builder.craftable.t4.prefix");
            MutableComponent unleashed = ModItems.UNLEASHED_CROWN.get().getDescription().copy();
            if (!menu.isUnleashedUnlocked()) {
                unleashed.withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.DARK_PURPLE);
            }
            return line.append(unleashed);
        }
        return AdvancedCrownLogic.configuredDescription(key);
    }

    private List<FormattedCharSequence> buildDetailLines(List<String> keys, int width) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (String key : keys) {
            lines.addAll(font.split(Component.translatable(key), width));
            lines.add(FormattedCharSequence.EMPTY);
        }
        while (!lines.isEmpty() && font.width(lines.get(lines.size() - 1)) == 0) lines.remove(lines.size() - 1);
        return lines;
    }

    private int drawWrapped(GuiGraphics g, Component text, int x, int y, int width, int color) {
        List<net.minecraft.util.FormattedCharSequence> lines = font.split(text, width);
        for (int i = 0; i < lines.size(); i++) g.drawString(font, lines.get(i), x, y + i * 10, color, false);
        return lines.size() * 10;
    }

    private void drawCentered(GuiGraphics g, Component text, int x, int y, int width, int color) {
        g.drawString(font, text, x + (width - font.width(text)) / 2, y, color, false);
    }

    private void drawBackHome(GuiGraphics g, int mouseX, int mouseY) {
        boolean homeHover = inside(mouseX, mouseY, 22, 217, 44, 12);
        boolean backHover = inside(mouseX, mouseY, 70, 217, 44, 12);
        g.drawString(font, Component.translatable("gui.glitchedcrown.lorebook.home"), 22, 217, homeHover ? LINK_HOVER : LINK, false);
        g.drawString(font, Component.translatable("gui.glitchedcrown.lorebook.back"), 70, 217, backHover ? LINK_HOVER : LINK, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (int) mouseX - leftPos;
        int y = (int) mouseY - topPos;
        if (button == 1) {
            if (view == View.ENTRY && entryPage > 0) {
                entryPage = 0;
                qualificationScroll = 0;
                return true;
            }
            if (view == View.ENTRY) {
                view = View.CATEGORY;
                entry = null;
                detailScroll = 0;
                entryPage = 0;
                qualificationScroll = 0;
                return true;
            }
            if (view == View.CATEGORY) {
                view = View.HOME;
                category = null;
                detailScroll = 0;
                return true;
            }
            onClose();
            return true;
        }
        if (button == 0) {
            if (view == View.ENTRY && isGlitchedEntry()) {
                int rx = 24 + PAGE_WIDTH + GAP;
                Component label = Component.translatable(entryPage == 1
                        ? "gui.glitchedcrown.lorebook.previous"
                        : "gui.glitchedcrown.lorebook.next");
                int width = font.width(label);
                int bx = rx + PAGE_WIDTH - 24 - width;
                if (inside(x, y, bx - 3, 215, width + 6, 16)) {
                    entryPage = entryPage == 0 ? 1 : 0;
                    detailScroll = 0;
                    qualificationScroll = 0;
                    return true;
                }
            }
            if (view != View.HOME && inside(x, y, 22, 215, 44, 16)) {
                view = View.HOME; category = null; entry = null; detailScroll = 0; entryPage = 0; qualificationScroll = 0; return true;
            }
            if (view != View.HOME && inside(x, y, 68, 215, 48, 16)) {
                if (view == View.ENTRY && entryPage > 0) { entryPage = 0; qualificationScroll = 0; }
                else if (view == View.ENTRY) { view = View.CATEGORY; entry = null; detailScroll = 0; entryPage = 0; qualificationScroll = 0; }
                else { view = View.HOME; category = null; }
                return true;
            }
            if (view == View.HOME) {
                int rx = 24 + PAGE_WIDTH + GAP, baseY = 43;
                List<Category> visibleCategories = visibleCategories();
                for (int i = 0; i < visibleCategories.size(); i++) {
                    int rowY = baseY + i * 21;
                    if (inside(x, y, rx - 3, rowY - 2, PAGE_WIDTH - 20, 19)) {
                        category = visibleCategories.get(i); view = View.CATEGORY; return true;
                    }
                }
            } else if (view == View.CATEGORY && category != null) {
                int rx = 24 + PAGE_WIDTH + GAP, baseY = 29;
                List<Entry> entries = visibleEntries(category);
                for (int i = 0; i < entries.size(); i++) {
                    int rowY = baseY + i * 25;
                    if (inside(x, y, rx - 4, rowY - 3, PAGE_WIDTH - 20, 22)) {
                        Entry selected = entries.get(i);
                        if (isLocked(selected)) return true;
                        entry = selected; view = View.ENTRY; detailScroll = 0; entryPage = 0; qualificationScroll = 0; return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isGlitchedQualificationPage()) {
            int maxScroll = Math.max(0, menu.substituteStatus().size() - qualificationVisibleSubstitutes());
            if (maxScroll > 0) {
                qualificationScroll = Math.max(0, Math.min(maxScroll, qualificationScroll + (delta < 0 ? 1 : -1)));
                return true;
            }
        }
        if (view == View.ENTRY && entry != null) {
            List<FormattedCharSequence> detailLines = buildEntryDetailLines(entry, PAGE_WIDTH - 26);
            int maxScroll = Math.max(0, detailLines.size() - 14);
            if (maxScroll > 0) {
                detailScroll = Math.max(0, Math.min(maxScroll, detailScroll + (delta < 0 ? 1 : -1)));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private List<Category> visibleCategories() {
        if (menu.isCursedUnlocked()) return CATEGORIES;
        return CATEGORIES.stream().filter(c -> !"special".equals(c.id())).toList();
    }

    private List<Entry> visibleEntries(Category selected) {
        if (selected == null) return List.of();
        return ENTRIES.stream()
                .filter(e -> e.category().equals(selected.id()))
                .filter(e -> menu.isCursedUnlocked() || !"cursed".equals(e.id()))
                .toList();
    }

    private boolean isLocked(Entry candidate) {
        return candidate != null && "unleashed".equals(candidate.id()) && !menu.isUnleashedUnlocked();
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static String roman(int tier) {
        return switch (tier) { case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; default -> "I"; };
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        for (HoverStack hover : hoverStacks) {
            int x = leftPos + hover.x();
            int y = topPos + hover.y();
            if (mouseX >= x && mouseX < x + hover.size() && mouseY >= y && mouseY < y + hover.size()) {
                graphics.renderTooltip(font, hover.stack(), mouseX, mouseY);
                break;
            }
        }
    }
}
