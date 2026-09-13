package com.thecrowns.item;

import com.thecrowns.config.CrownServerConfig;

import com.thecrowns.client.model.CrownArmorClient;
import net.minecraft.ChatFormatting;
import com.thecrowns.client.ClientTooltipContext;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
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

import java.util.List;
import java.util.function.Consumer;

import com.thecrowns.logic.CrownLogic;
import com.thecrowns.logic.CrownEquipRules;
import com.thecrowns.logic.GlitchedCrownFusion;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * PvP-nerfed Glitched Crown.  UnleashedCrownItem subclasses this class so both
 * variants keep the same equipment/enchantment compatibility while their
 * gameplay logic is selected by CrownLogic from the concrete registry item.
 */
public class GlitchedCrownItem extends ArmorItem implements ICurioItem {
    public static final int GLITCHED_MAX_DURABILITY = 32_767;
    public static final int UNLEASHED_MAX_DURABILITY = 16_777_216;

    public GlitchedCrownItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!stack.getOrCreateTag().getBoolean("Unbreakable")) {
            stack.getOrCreateTag().putBoolean("Unbreakable", true);
        }
        // Custom Shift tooltip owns the stat/unbreakable presentation. Keep enchantments visible.
        int hideFlags = stack.getOrCreateTag().getInt("HideFlags");
        stack.getOrCreateTag().putInt("HideFlags", hideFlags | 0x02 | 0x04);
        if (!level.isClientSide && entity instanceof Player player) returnReplacedFusion(stack, player);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        if (source.is(DamageTypeTags.IS_EXPLOSION)) return false;
        return super.canBeHurtBy(source);
    }

    /**
     * Tests enchantment compatibility against ordinary helmet stacks instead
     * of asking the enchantment about this Crown stack again.  Forge's
     * Enchantment#canEnchant -> canApplyAtEnchantingTable -> ItemStack path
     * calls back into this method for the Crown and would recurse forever.
     *
     * Multiple vanilla helmet materials are used so enchantments that make a
     * material-specific helmet check still have a chance to qualify.
     */
    private static boolean isHelmetEnchantment(Enchantment enchantment) {
        if (enchantment == null) return false;

        ItemStack[] helmetProxies = {
                new ItemStack(Items.NETHERITE_HELMET),
                new ItemStack(Items.DIAMOND_HELMET),
                new ItemStack(Items.LEATHER_HELMET),
                new ItemStack(Items.TURTLE_HELMET)
        };

        for (ItemStack helmet : helmetProxies) {
            try {
                if (enchantment.canApplyAtEnchantingTable(helmet)) return true;
            } catch (Throwable ignored) {
                // A badly-behaved optional/modded enchantment must not crash
                // the entire tag/enchantment scan. Try the next proxy.
            }
        }
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        // 1.1.4: allow anything that is actually applicable to a helmet,
        // without recursively asking the enchantment about the Crown itself.
        return isHelmetEnchantment(enchantment);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        var enchantments = EnchantmentHelper.getEnchantments(book);
        return !enchantments.isEmpty()
                && enchantments.keySet().stream().allMatch(GlitchedCrownItem::isHelmetEnchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    protected String tooltipRoot() {
        return "tooltip.thecrowns.glitched";
    }

    protected int statLineCount() {
        return 9;
    }

    protected boolean usesFixedFullPowerStats() { return false; }

    protected void appendSharedStats(List<Component> tooltip) {
        boolean fixed = usesFixedFullPowerStats();
        double health = fixed ? 80.0D : CrownServerConfig.BONUS_MAX_HEALTH.get();
        double armor = fixed ? 40.0D : CrownServerConfig.BONUS_ARMOR.get();
        double toughness = fixed ? 40.0D : CrownServerConfig.BONUS_TOUGHNESS.get();
        double luck = fixed ? 7.0D : CrownServerConfig.BONUS_LUCK.get();
        int looting = fixed ? 7 : CrownServerConfig.BONUS_LOOTING.get();
        double damage = fixed ? 40.0D : CrownServerConfig.BONUS_ATTACK_DAMAGE.get();
        double reach = fixed ? 3.5D : CrownServerConfig.BONUS_INTERACTION_REACH.get();
        double kbPct = (fixed ? 1.0D : CrownServerConfig.BONUS_KNOCKBACK_RESISTANCE.get()) * 100.0D;
        Object[] values = { fmt(health), fmt(armor), fmt(toughness), fmt(luck), looting, fmt(damage), fmt(reach), fmt(reach), fmt(kbPct) };
        for (int i = 1; i <= statLineCount(); i++) {
            tooltip.add(Component.translatable("tooltip.thecrowns.stat." + i, values[i - 1])
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    private static String fmt(double value) {
        if (Math.rint(value) == value) return Long.toString((long) value);
        return String.format(java.util.Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (!ClientTooltipContext.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.thecrowns.hold_shift").withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable("tooltip.thecrowns.glitched.section.shield").withStyle(ChatFormatting.AQUA));
        int charges = CrownLogic.getNerfedBarrierCharges(stack);
        boolean shieldEnabled = CrownLogic.isNerfedShieldEnabled(stack);
        if (shieldEnabled) {
            tooltip.add(Component.translatable("tooltip.thecrowns.glitched.runtime.shield_value",
                            charges, CrownLogic.glitchedBarrierMax()).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.thecrowns.glitched.runtime.shield_disabled")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.thecrowns.glitched.section.revive").withStyle(ChatFormatting.AQUA));
        boolean reviveEnabled = CrownLogic.isNerfedReviveEnabled(stack);
        long reviveTicks = level == null ? 0L : CrownLogic.getNerfedReviveCooldownTicks(stack, level.getGameTime());
        if (!reviveEnabled) {
            tooltip.add(Component.translatable("tooltip.thecrowns.glitched.runtime.revive_disabled_short").withStyle(ChatFormatting.RED));
        } else if (reviveTicks <= 0L) {
            tooltip.add(Component.translatable("tooltip.thecrowns.glitched.runtime.revive_ready").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("tooltip.thecrowns.glitched.runtime.revive_cooldown",
                    Math.max(1L, (reviveTicks + 19L) / 20L)).withStyle(ChatFormatting.YELLOW));
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.thecrowns.glitched.section.stats").withStyle(ChatFormatting.AQUA));
        appendSharedStats(tooltip);
        for (int i = 1; i <= 7; i++) {
            tooltip.add(Component.translatable("tooltip.thecrowns.glitched.passive." + i)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.thecrowns.glitched.active.title").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.thecrowns.glitched.active.1.dynamic",
                fmt(CrownServerConfig.GLITCHED_REMOVAL_RAY_RANGE.get()),
                fmt(CrownServerConfig.GLITCHED_RAY_NORMAL_FRACTION.get() * 100.0D)).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.thecrowns.glitched.active.2.dynamic",
                fmt(CrownServerConfig.GLITCHED_RAY_PLAYER_FRACTION.get() * 100.0D)).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.thecrowns.glitched.active.3.dynamic",
                fmt(CrownServerConfig.GLITCHED_RAY_BOSS_FRACTION.get() * 100.0D)).withStyle(ChatFormatting.LIGHT_PURPLE));

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.thecrowns.glitched.section.cooldown").withStyle(ChatFormatting.AQUA));
        boolean laserEnabled = CrownLogic.isNerfedLaserEnabled(stack);
        boolean useCooldown = CrownLogic.isNerfedLaserCooldownEnabled(stack);
        if (!laserEnabled) {
            tooltip.add(Component.translatable("tooltip.thecrowns.glitched.active.disabled").withStyle(ChatFormatting.RED));
        } else if (!useCooldown) {
            tooltip.add(Component.translatable("tooltip.thecrowns.glitched.active.cooldown_off").withStyle(ChatFormatting.GREEN));
        } else {
            long laserTicks = level == null ? 0L : CrownLogic.getNerfedLaserCooldownTicks(stack, level.getGameTime());
            if (laserTicks <= 0L) {
                tooltip.add(Component.translatable("tooltip.thecrowns.cooldown.ready").withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.translatable("tooltip.thecrowns.cooldown.remaining",
                        Math.max(1L, (laserTicks + 19L) / 20L)).withStyle(ChatFormatting.YELLOW));
            }
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.thecrowns.glitched.section.key").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.thecrowns.key.dynamic",
                        Component.keybind("key.thecrowns.fire_laser"))
                .withStyle(ChatFormatting.YELLOW));

        net.minecraft.resources.ResourceLocation fusion = GlitchedCrownFusion.getFusion(stack);
        if (fusion != null) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.thecrowns.glitched.section.fusions")
                    .withStyle(ChatFormatting.AQUA));
            BuiltInRegistries.ITEM.getOptional(fusion).ifPresent(item ->
                    tooltip.add(Component.translatable("tooltip.thecrowns.glitched.fusion.active",
                                    new ItemStack(item).getHoverName())
                            .withStyle(ChatFormatting.DARK_PURPLE)));
            String path = fusion.getPath();
            if ("ironforged_crown".equals(path) || "cursed_crown".equals(path)) {
                tooltip.add(Component.translatable("tooltip.thecrowns.glitched.fusion.j_key",
                                Component.keybind("key.thecrowns.toggle_aura"))
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        if (!level.isClientSide) returnReplacedFusion(stack, player);
    }

    private static void returnReplacedFusion(ItemStack stack, Player player) {
        ItemStack returned = GlitchedCrownFusion.takePendingReturn(stack);
        if (returned.isEmpty()) return;
        if (!player.getInventory().add(returned)) player.drop(returned, false);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        CrownArmorClient.register(consumer);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "thecrowns:textures/models/armor/glitched_layer_1.png";
    }
}
