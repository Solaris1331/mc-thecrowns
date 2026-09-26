package com.thecrowns.registry;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.item.GlitchedCrownItem;
import com.thecrowns.item.StandardCrownItem;
import com.thecrowns.item.CrownBuilderBlockItem;
import com.thecrowns.item.CrownLorebookItem;
import com.thecrowns.item.UnleashedCrownItem;
import com.thecrowns.item.UnleashedUnleashedCrownItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, TheCrownsMod.MOD_ID);

    /** PvP-nerfed Crown. */
    public static final RegistryObject<GlitchedCrownItem> GLITCHED_CROWN =
            ITEMS.register("glitched_crown", () -> new GlitchedCrownItem(
                    ModArmorMaterials.GLITCHED,
                    ArmorItem.Type.HELMET,
                    crownProperties(GlitchedCrownItem.GLITCHED_MAX_DURABILITY)
            ));

    /** Original pre-nerf Crown from the 1.0.8 Dragonfyre port. */
    public static final RegistryObject<UnleashedCrownItem> UNLEASHED_CROWN =
            ITEMS.register("unleashed_crown", () -> new UnleashedCrownItem(
                    ModArmorMaterials.GLITCHED,
                    ArmorItem.Type.HELMET,
                    crownProperties(GlitchedCrownItem.UNLEASHED_MAX_DURABILITY)
            ));

    /** Administrator/reference immutable full-power Crown. No recipe or advancement; available in The Crowns creative tab. */
    public static final RegistryObject<UnleashedUnleashedCrownItem> UNLEASHED_UNLEASHED_CROWN =
            ITEMS.register("unleashed_unleashed_crown", () -> new UnleashedUnleashedCrownItem(
                    ModArmorMaterials.GLITCHED,
                    ArmorItem.Type.HELMET,
                    crownProperties(GlitchedCrownItem.UNLEASHED_MAX_DURABILITY)
            ));

    public static final RegistryObject<StandardCrownItem> CROWN_OF_LIGHT = standardCrown(
            "crown_of_light", "crown_of_light", 3, null);
    public static final RegistryObject<StandardCrownItem> BLOODY_CROWN = standardCrown(
            "bloody_crown", "bloody_crown", 4, null);
    public static final RegistryObject<StandardCrownItem> BURNING_CROWN = standardCrown(
            "burning_crown", "burning_crown", 3, null);
    public static final RegistryObject<StandardCrownItem> DARKENED_CROWN = standardCrown(
            "darkened_crown", "darkened_crown", 1, null);
    public static final RegistryObject<StandardCrownItem> IRONFORGED_CROWN = standardCrown(
            "ironforged_crown", "ironforged_crown", 3, "key.thecrowns.summon_iron_golem");
    public static final RegistryObject<StandardCrownItem> DIMENSIONAL_CROWN = standardCrown(
            "dimensional_crown", "dimensional_crown", 3, null);
    public static final RegistryObject<StandardCrownItem> WARRIOR_CROWN = standardCrown(
            "warrior_crown", "warrior_crown", 4, "key.thecrowns.warrior_duel");
    public static final RegistryObject<StandardCrownItem> ANGELIC_CROWN = standardCrown(
            "angelic_crown", "angelic_crown", 4, "key.thecrowns.angelic_flight");

    public static final RegistryObject<StandardCrownItem> TEMPORAL_CROWN = standardCrown(
            "temporal_crown", "temporal_crown", 2, null);
    public static final RegistryObject<StandardCrownItem> FROST_CROWN = standardCrown(
            "frost_crown", "frost_crown", 3, null);
    public static final RegistryObject<StandardCrownItem> DIVINE_CROWN = standardCrown(
            "divine_crown", "divine_crown", 4, null);
    public static final RegistryObject<StandardCrownItem> CURSED_CROWN = standardCrown(
            "cursed_crown", "cursed_crown", 6, null);

    public static final RegistryObject<CrownLorebookItem> CROWN_LOREBOOK = ITEMS.register("crown_lorebook",
            () -> new CrownLorebookItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> IRONFORGED_CORE = ITEMS.register("ironforged_core",
            () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> SPACETIME_STABILIZER = ITEMS.register("spacetime_stabilizer",
            () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> SNOW_CLUMP = ITEMS.register("snow_clump",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<BlockItem> ROTTEN_FLESH_BLOCK = ITEMS.register("rotten_flesh_block",
            () -> new BlockItem(ModBlocks.ROTTEN_FLESH_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> BIG_ROTTEN_FLESH_BLOCK = ITEMS.register("big_rotten_flesh_block",
            () -> new BlockItem(ModBlocks.BIG_ROTTEN_FLESH_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<BlockItem> COMPRESSED_EMERALD_BLOCK = ITEMS.register("compressed_emerald_block",
            () -> new BlockItem(ModBlocks.COMPRESSED_EMERALD_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<CrownBuilderBlockItem> CROWN_BUILDER_T1 = ITEMS.register("crown_builder_t1",
            () -> new CrownBuilderBlockItem(ModBlocks.CROWN_BUILDER_T1.get(), new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<CrownBuilderBlockItem> CROWN_BUILDER_T2 = ITEMS.register("crown_builder_t2",
            () -> new CrownBuilderBlockItem(ModBlocks.CROWN_BUILDER_T2.get(), new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<CrownBuilderBlockItem> CROWN_BUILDER_T3 = ITEMS.register("crown_builder_t3",
            () -> new CrownBuilderBlockItem(ModBlocks.CROWN_BUILDER_T3.get(), new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<CrownBuilderBlockItem> CROWN_BUILDER_T4 = ITEMS.register("crown_builder_t4",
            () -> new CrownBuilderBlockItem(ModBlocks.CROWN_BUILDER_T4.get(), new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));

    private static RegistryObject<StandardCrownItem> standardCrown(String id, String root,
                                                                    int statLines, String keybind) {
        return ITEMS.register(id, () -> new StandardCrownItem(
                ModArmorMaterials.GLITCHED,
                ArmorItem.Type.HELMET,
                crownProperties(StandardCrownItem.MAX_DURABILITY),
                root, statLines, keybind));
    }

    private static Item.Properties crownProperties(int durability) {
        return new Item.Properties()
                .durability(durability)
                .fireResistant()
                .rarity(Rarity.EPIC);
    }

    private ModItems() {}
}
