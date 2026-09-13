package com.thecrowns.item;

import com.thecrowns.logic.NewCrownLogic;
import com.thecrowns.logic.CrownEquipRules;
import com.thecrowns.logic.AdvancedCrownLogic;
import com.thecrowns.client.model.CrownArmorClient;
import com.thecrowns.client.ClientTooltipContext;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.List;
import java.util.function.Consumer;

/** Standard Tier I-III Crown shell. Unlike Glitched/Unleashed it is genuinely damageable. */
public final class StandardCrownItem extends ArmorItem implements ICurioItem {
    public static final int MAX_DURABILITY = 4096;

    private final String root;
    private final int statLines;
    private final String keybind;

    public StandardCrownItem(ArmorMaterial material, ArmorItem.Type type, Properties properties,
                             String root, int statLines, @Nullable String keybind) {
        super(material, type, properties);
        this.root = root;
        this.statLines = statLines;
        this.keybind = keybind;
    }

    private static boolean isHelmetEnchantment(Enchantment enchantment) {
        if (enchantment == null) return false;
        ItemStack[] proxies = {
                new ItemStack(Items.NETHERITE_HELMET), new ItemStack(Items.DIAMOND_HELMET),
                new ItemStack(Items.LEATHER_HELMET), new ItemStack(Items.TURTLE_HELMET)
        };
        for (ItemStack proxy : proxies) {
            try {
                if (enchantment.canApplyAtEnchantingTable(proxy)) return true;
            } catch (Throwable ignored) {}
        }
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return isHelmetEnchantment(enchantment);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        var enchantments = EnchantmentHelper.getEnchantments(book);
        return !enchantments.isEmpty() && enchantments.keySet().stream().allMatch(StandardCrownItem::isHelmetEnchantment);
    }

    @Override public boolean isEnchantable(ItemStack stack) { return true; }
    @Override public int getEnchantmentValue() { return 1; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!ClientTooltipContext.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.thecrowns.hold_shift").withStyle(ChatFormatting.GRAY));
            return;
        }

        for (int i = 1; i <= statLines; i++) {
            tooltip.add(AdvancedCrownLogic.configuredDescription("tooltip.thecrowns." + root + ".stat." + i).copy()
                    .withStyle(ChatFormatting.BLUE));
        }

        switch (root) {
            case "burning_crown" -> appendBurning(tooltip);
            case "ironforged_crown" -> appendIronforged(stack, level, tooltip);
            case "bloody_crown" -> appendBloody(stack, level, tooltip);
            case "darkened_crown" -> appendSimple(root, 4, tooltip);
            case "warrior_crown" -> appendWarrior(stack, level, tooltip);
            case "crown_of_light" -> appendSimple(root, 4, tooltip);
            case "dimensional_crown" -> appendSimple(root, 5, tooltip);
            case "angelic_crown" -> appendAngelic(stack, level, tooltip);
            case "temporal_crown" -> appendTemporal(stack, level, tooltip);
            case "frost_crown" -> appendSimple(root, 4, tooltip);
            case "divine_crown" -> appendDivine(stack, level, tooltip);
            case "cursed_crown" -> appendCursed(stack, level, tooltip);
            default -> appendSimple(root, 0, tooltip);
        }
    }

    private void appendBurning(List<Component> tooltip) {
        blank(tooltip);
        appendLines(root, "passive", 4, tooltip, ChatFormatting.LIGHT_PURPLE);
        tooltip.add(Component.translatable("tooltip.thecrowns.cooldown.same_target", 1)
                .withStyle(ChatFormatting.YELLOW));
    }

    private void appendIronforged(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        blank(tooltip);
        title("tooltip.thecrowns.ironforged_crown.active.title", tooltip);
        appendLines(root, "active", 5, tooltip, ChatFormatting.LIGHT_PURPLE);
        appendCooldown(stack, level, NewCrownLogic.TAG_IRONFORGED_READY_AT, 120, tooltip);
        appendKey(tooltip);
    }

    private void appendBloody(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        blank(tooltip);
        int stacks = NewCrownLogic.tooltipInt(stack, NewCrownLogic.TAG_BLOODY_STACKS, 0);
        tooltip.add(Component.translatable("tooltip.thecrowns.bloody_crown.blessing", stacks, NewCrownLogic.BLOODY_MAX_STACKS)
                .withStyle(ChatFormatting.DARK_RED));
        appendLines(root, "passive", 1, tooltip, ChatFormatting.LIGHT_PURPLE);
        appendCooldown(stack, level, NewCrownLogic.TAG_BLOODY_READY_AT, 30, tooltip);
        appendLines(root, "passive", 2, 2, tooltip, ChatFormatting.LIGHT_PURPLE);
    }

    private void appendWarrior(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        blank(tooltip);
        appendLines(root, "passive", 1, tooltip, ChatFormatting.LIGHT_PURPLE);
        blank(tooltip);
        title("tooltip.thecrowns.warrior_crown.active.title", tooltip);
        appendLines(root, "active", 5, tooltip, ChatFormatting.LIGHT_PURPLE);
        tooltip.add(Component.translatable("tooltip.thecrowns.duration", 15).withStyle(ChatFormatting.YELLOW));
        appendCooldown(stack, level, NewCrownLogic.TAG_WARRIOR_READY_AT, 120, tooltip);
        appendKey(tooltip);
    }

    private void appendAngelic(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        blank(tooltip);
        long guard = remaining(stack, level, NewCrownLogic.TAG_ANGELIC_GUARD_READY_AT);
        if (guard <= 0L) {
            tooltip.add(Component.translatable("tooltip.thecrowns.angelic_crown.second_chance.ready")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("tooltip.thecrowns.angelic_crown.second_chance.cooldown", seconds(guard))
                    .withStyle(ChatFormatting.YELLOW));
        }
        blank(tooltip);
        appendLines(root, "passive", 3, tooltip, ChatFormatting.LIGHT_PURPLE);
        blank(tooltip);
        title("tooltip.thecrowns.angelic_crown.active.title", tooltip);
        appendLines(root, "active", 1, tooltip, ChatFormatting.LIGHT_PURPLE);
        appendCooldown(stack, level, NewCrownLogic.TAG_ANGELIC_FLIGHT_READY_AT, 120, tooltip);
        appendKey(tooltip);
    }


    private void appendTemporal(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        blank(tooltip);
        appendLines(root, "passive", 2, tooltip, ChatFormatting.LIGHT_PURPLE);
        blank(tooltip);
        title("tooltip.thecrowns.temporal_crown.active.rewind.title", tooltip);
        appendLines(root, "rewind", 3, tooltip, ChatFormatting.LIGHT_PURPLE);
        tooltip.add(Component.translatable(AdvancedCrownLogic.tooltipBoolean(stack, AdvancedCrownLogic.TAG_TIME_SNAPSHOT)
                ? "tooltip.thecrowns.temporal_crown.snapshot.saved"
                : "tooltip.thecrowns.temporal_crown.snapshot.empty").withStyle(ChatFormatting.GRAY));
        appendCooldown(stack, level, AdvancedCrownLogic.TAG_TIME_REWIND_READY_AT,
                com.thecrowns.config.CrownServerConfig.TEMPORAL_REWIND_COOLDOWN_SECONDS.get(), tooltip);
        tooltip.add(Component.translatable("tooltip.thecrowns.temporal_crown.keys.rewind",
                Component.keybind("key.thecrowns.temporal_mark"), Component.keybind("key.thecrowns.temporal_rewind"))
                .withStyle(ChatFormatting.YELLOW));
        blank(tooltip);
        title("tooltip.thecrowns.temporal_crown.active.warp.title", tooltip);
        appendLines(root, "warp", 1, tooltip, ChatFormatting.LIGHT_PURPLE);
        tooltip.add(Component.translatable("tooltip.thecrowns.duration",
                com.thecrowns.config.CrownServerConfig.TEMPORAL_WARP_DURATION_SECONDS.get()).withStyle(ChatFormatting.YELLOW));
        appendCooldown(stack, level, AdvancedCrownLogic.TAG_TIME_WARP_READY_AT,
                com.thecrowns.config.CrownServerConfig.TEMPORAL_WARP_COOLDOWN_SECONDS.get(), tooltip);
        tooltip.add(Component.translatable("tooltip.thecrowns.key.dynamic", Component.keybind("key.thecrowns.time_warp"))
                .withStyle(ChatFormatting.YELLOW));
    }

    private void appendDivine(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        blank(tooltip);
        title("tooltip.thecrowns.divine_crown.passive.title", tooltip);
        appendLines(root, "passive", 1, tooltip, ChatFormatting.LIGHT_PURPLE);
        blank(tooltip);
        title("tooltip.thecrowns.divine_crown.active.title", tooltip);
        appendLines(root, "active", 2, tooltip, ChatFormatting.LIGHT_PURPLE);
        appendCooldown(stack, level, AdvancedCrownLogic.TAG_DIVINE_READY_AT,
                com.thecrowns.config.CrownServerConfig.DIVINE_SANCTIFY_COOLDOWN_SECONDS.get(), tooltip);
        tooltip.add(Component.translatable("tooltip.thecrowns.key.dynamic", Component.keybind("key.thecrowns.divine_sanctify"))
                .withStyle(ChatFormatting.YELLOW));
    }

    private void appendCursed(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        blank(tooltip);
        appendLines(root, "passive", 3, tooltip, ChatFormatting.LIGHT_PURPLE);
        ClientTooltipContext.appendCursedHints(tooltip);
        blank(tooltip);
        title("tooltip.thecrowns.cursed_crown.active.title", tooltip);
        appendLines(root, "active", 1, tooltip, ChatFormatting.LIGHT_PURPLE);
        tooltip.add(Component.translatable("tooltip.thecrowns.duration",
                com.thecrowns.config.CrownServerConfig.CURSED_LIBERATION_DURATION_SECONDS.get()).withStyle(ChatFormatting.YELLOW));
        appendCooldown(stack, level, AdvancedCrownLogic.TAG_CURSED_READY_AT,
                com.thecrowns.config.CrownServerConfig.CURSED_LIBERATION_COOLDOWN_SECONDS.get(), tooltip);
        tooltip.add(Component.translatable("tooltip.thecrowns.key.dynamic", Component.keybind("key.thecrowns.cursed_liberation"))
                .withStyle(ChatFormatting.YELLOW));
    }

    private void appendSimple(String root, int lines, List<Component> tooltip) {
        if (lines <= 0) return;
        blank(tooltip);
        appendLines(root, "passive", lines, tooltip, ChatFormatting.LIGHT_PURPLE);
    }

    private void appendCooldown(ItemStack stack, @Nullable Level level, String tag, int defaultSeconds, List<Component> tooltip) {
        long ticks = remaining(stack, level, tag);
        if (ticks <= 0L) {
            tooltip.add(Component.translatable("tooltip.thecrowns.cooldown.ready").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("tooltip.thecrowns.cooldown.remaining", seconds(ticks))
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    private long remaining(ItemStack stack, @Nullable Level level, String tag) {
        if (level == null) return 0L;
        return Math.max(0L, NewCrownLogic.tooltipLong(stack, tag, 0L) - level.getGameTime());
    }

    private static long seconds(long ticks) { return Math.max(1L, (ticks + 19L) / 20L); }

    private void appendKey(List<Component> tooltip) {
        if (keybind == null) return;
        tooltip.add(Component.translatable("tooltip.thecrowns.key.dynamic", Component.keybind(keybind))
                .withStyle(ChatFormatting.YELLOW));
    }

    private static void title(String key, List<Component> tooltip) {
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GOLD));
    }

    private static void blank(List<Component> tooltip) { tooltip.add(Component.empty()); }

    private static void appendLines(String root, String section, int count, List<Component> tooltip, ChatFormatting color) {
        appendLines(root, section, 1, count, tooltip, color);
    }

    private static void appendLines(String root, String section, int from, int to, List<Component> tooltip, ChatFormatting color) {
        for (int i = from; i <= to; i++) {
            tooltip.add(AdvancedCrownLogic.configuredDescription(
                    "tooltip.thecrowns." + root + "." + section + "." + i).copy().withStyle(color));
        }
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        if (armorType != EquipmentSlot.HEAD) return false;
        return !(entity instanceof LivingEntity living) || CrownEquipRules.canEquipUnique(living, stack);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CrownEquipRules.canEquipUnique(slotContext.entity(), stack);
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        if (!"cursed_crown".equals(root)) return true;
        return AdvancedCrownLogic.canRemoveCursedCrown(slotContext.entity());
    }

    @Override
    public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel,
                                       boolean recentlyHit, ItemStack stack) {
        if ("temporal_crown".equals(root) || "cursed_crown".equals(root)) {
            return ICurio.DropRule.ALWAYS_KEEP;
        }
        return ICurioItem.super.getDropRule(slotContext, source, lootingLevel, recentlyHit, stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.NETHER_STAR);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        CrownArmorClient.register(consumer);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "thecrowns:textures/models/armor/" + root + "_layer_1.png";
    }
}
