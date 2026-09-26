package com.thecrowns.event;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.command.GTCommand;
import com.thecrowns.command.CrownCommand;
import com.thecrowns.api.event.CrownAbilityTargetEvent;
import com.thecrowns.compat.CrownCurios;
import com.thecrowns.logic.CrownIntegrity;
import com.thecrowns.logic.CrownAdvancementGate;
import com.thecrowns.logic.CrownAdvancements;
import com.thecrowns.logic.CrownLogic;
import com.thecrowns.logic.NewCrownLogic;
import com.thecrowns.logic.AdvancedCrownLogic;
import com.thecrowns.logic.GlitchedFusionLogic;
import com.thecrowns.logic.ShadowAbyssalLogic;
import com.thecrowns.registry.ModItems;
import com.thecrowns.item.StandardCrownItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import top.theillusivec4.curios.api.event.CurioCanUnequipEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.List;

@EventBusSubscriber(modid = TheCrownsMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class CommonEvents {
    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        GTCommand.register(event.getDispatcher());
        CrownCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CrownLogic.copyNerfedPlayerSettings(event.getOriginal(), event.getEntity());
        NewCrownLogic.copyPermanentPlayerData(event.getOriginal(), event.getEntity());
        AdvancedCrownLogic.copyPersistentPlayerData(event.getOriginal(), event.getEntity());
        GlitchedFusionLogic.onPlayerClone(event.getOriginal(), event.getEntity());
        if (event.isWasDeath()) AdvancedCrownLogic.clearTemporalSnapshot(event.getEntity());
        if (event.getOriginal().getPersistentData().getBoolean("thecrowns_cursed_died_zero_xp")) {
            event.getEntity().totalExperience = 0;
            event.getEntity().experienceLevel = 0;
            event.getEntity().experienceProgress = 0.0F;
            event.getEntity().getPersistentData().remove("thecrowns_cursed_died_zero_xp");
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CrownLogic.tickPlayer(player);
            NewCrownLogic.tickPlayer(player);
            AdvancedCrownLogic.tickPlayer(player);
            ShadowAbyssalLogic.tickPlayer(player);
            GlitchedFusionLogic.tickPlayer(player);
            CrownAdvancements.tickSpecial(player);
            CrownCurios.syncSlotCount(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        ShadowAbyssalLogic.noteAttack(event.getEntity(), event.getSource());
        if (AdvancedCrownLogic.shouldCancelFrozenAttack(event.getSource())
                || AdvancedCrownLogic.shouldCancelTimeStoppedAttack(event.getSource())
                || NewCrownLogic.shouldBlockWarriorOutgoingDamage(event.getSource())) {
            event.setCanceled(true);
            return;
        }
        if (AdvancedCrownLogic.isFrostDamageImmune(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
            return;
        }
        NewCrownLogic.noteDarkenedWardenAttack(event.getEntity(), event.getSource());
        net.minecraft.world.entity.LivingEntity target = event.getEntity();
        if (event.getSource().getEntity() instanceof ServerPlayer owner
                && NewCrownLogic.hasIronforgedGuardianPower(owner)) {
            NewCrownLogic.commandOwnedIronGolem(owner, target);
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        net.minecraft.world.entity.Entity responsibleForOrder = event.getSource().getEntity();
        if (responsibleForOrder == null) responsibleForOrder = event.getSource().getDirectEntity();
        if (NewCrownLogic.hasIronforgedGuardianPower(player)
                && responsibleForOrder instanceof net.minecraft.world.entity.LivingEntity attacker) {
            NewCrownLogic.commandOwnedIronGolem(player, attacker);
        }

        if (NewCrownLogic.isWarriorDuelActive(player) && !NewCrownLogic.warriorAllowsDamage(player, event.getSource())) {
            NewCrownLogic.noteWarriorBlockedDamage(player);
            event.setCanceled(true);
            return;
        }

        if (NewCrownLogic.shouldAvoidAttack(player, event.getSource())) {
            event.setCanceled(true);
            return;
        }

        net.minecraft.world.entity.Entity source = event.getSource().getEntity();
        if (source == null) source = event.getSource().getDirectEntity();
        if (source != null && CrownLogic.isUnleashedIntegrityActive(player)) {
            CrownIntegrity.notePotentialInterferer(player, source);
        }

        if (CrownLogic.hasAnyDamageInvulnerability(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamageModifiers(LivingIncomingDamageEvent event) {
        float raw = NewCrownLogic.applyIronforgedRawDamageRules(event.getEntity(), event.getSource(), event.getAmount());
        if (CrownLogic.applyResolvedAbsoluteDamage(event.getEntity(), event.getSource(), raw)) {
            event.setCanceled(true);
            return;
        }
        event.setAmount(raw);
        event.setAmount(AdvancedCrownLogic.addFrostDamage(event.getSource(), event.getAmount()));
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        net.minecraft.world.entity.Entity source = event.getSource().getEntity();
        if (source == null) source = event.getSource().getDirectEntity();
        if (source != null && CrownLogic.isUnleashedIntegrityActive(player)) {
            CrownIntegrity.notePotentialInterferer(player, source);
        }

        if (CrownLogic.hasAnyDamageInvulnerability(player)) {
            event.setCanceled(true);
            return;
        }

        if (event.getAmount() > 0.0F && CrownLogic.consumeNerfedBarrierCharge(player)) {
            event.setAmount(event.getAmount() * CrownLogic.glitchedBarrierDamageMultiplier());
            GlitchedFusionLogic.onBarrierConsumed(player);
            player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK,
                    SoundSource.PLAYERS, 0.85F, 1.65F);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFinalDamageModifiers(LivingDamageEvent.Pre event) {
        if (event.getNewDamage() <= 0.0F) return;
        float modified = NewCrownLogic.modifyFinalDamage(event.getEntity(), event.getSource(), event.getNewDamage());
        modified = AdvancedCrownLogic.applyCursedOutgoingPenalty(event.getSource(), modified);
        modified = ShadowAbyssalLogic.applyAmbush(event.getSource(), modified);
        if (event.getEntity() instanceof ServerPlayer player && NewCrownLogic.tryAngelicLethalGuard(player, modified)) {
            event.setNewDamage(0.0F);
            return;
        }
        event.setNewDamage(modified);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void capIronGuardianFinalDamage(LivingDamageEvent.Pre event) {
        // Run after other final-damage listeners rather than redirecting CommonHooks. This
        // preserves compatibility with mods that also instrument the vanilla damage method.
        event.setNewDamage(NewCrownLogic.capIronforgedFinalDamage(event.getEntity(), event.getNewDamage()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamageResolved(LivingDamageEvent.Post event) {
        if (event.getNewDamage() <= 0.0F) return;
        ServerPlayer attacker = CrownLogic.crownDamageOwner(event.getSource());
        if (attacker != null && CrownLogic.isFateActive(attacker)
                && CrownLogic.canAffectCrownTarget(attacker, event.getEntity(), CrownAbilityTargetEvent.Ability.FATE_BIND)) {
            CrownLogic.applyFateBind(event.getEntity());
        }
        NewCrownLogic.afterDamageResolved(event.getEntity(), event.getSource(), event.getNewDamage());
        AdvancedCrownLogic.onDamageResolved(event.getEntity(), event.getSource(), event.getNewDamage());
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.LivingEntity living)) return;
        CrownLogic.enforceGlitchedRayHealingCap(living);
        CrownLogic.tickFateBind(living);
        NewCrownLogic.tickOwnedIronGolemEntity(living);
        AdvancedCrownLogic.tickLiving(living);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTeleport(EntityTeleportEvent event) {
        if (CrownLogic.isFateBound(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDimensionTravel(EntityTravelToDimensionEvent event) {
        if (CrownLogic.isFateBound(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (AdvancedCrownLogic.isTemporalEffectImmune(event.getEntity(), event.getEffectInstance().getEffect().value())
                || NewCrownLogic.isImmuneToEffect(event.getEntity(), event.getEffectInstance().getEffect())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        AdvancedCrownLogic.onNegativeEffectAdded(event.getEntity(), event.getEffectInstance());
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        event.setAmount(AdvancedCrownLogic.modifyHealing(event.getEntity(), event.getAmount()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMobEffectRemove(MobEffectEvent.Remove event) {
        if (CrownLogic.shouldProtectBeneficialEffectRemoval(event.getEntity(), event.getEffect().value())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWarriorUseItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof ServerPlayer player && AdvancedCrownLogic.isConsumableSpeedBoosted(player, event.getItem())) {
            event.setDuration(AdvancedCrownLogic.acceleratedUseDuration(event.getDuration()));
        }
    }

    private static void cancelTemporalConcentration(PlayerInteractEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AdvancedCrownLogic.cancelTemporalRewindConcentration(player);
        }
    }

    @SubscribeEvent
    public static void onTemporalConcentrationRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        cancelTemporalConcentration(event);
    }

    @SubscribeEvent
    public static void onTemporalConcentrationRightClickItem(PlayerInteractEvent.RightClickItem event) {
        cancelTemporalConcentration(event);
    }

    @SubscribeEvent
    public static void onTemporalConcentrationRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        cancelTemporalConcentration(event);
    }

    @SubscribeEvent
    public static void onTemporalConcentrationLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        cancelTemporalConcentration(event);
    }

    @SubscribeEvent
    public static void onTemporalConcentrationLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        cancelTemporalConcentration(event);
    }

    @SubscribeEvent
    public static void onTemporalConcentrationEntityInteract(PlayerInteractEvent.EntityInteract event) {
        cancelTemporalConcentration(event);
    }

    @SubscribeEvent
    public static void onTemporalConcentrationEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        cancelTemporalConcentration(event);
    }

    @SubscribeEvent
    public static void onTemporalConcentrationBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            AdvancedCrownLogic.cancelTemporalRewindConcentration(player);
        }
    }

    @SubscribeEvent
    public static void onTemporalConcentrationBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AdvancedCrownLogic.cancelTemporalRewindConcentration(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCurioUnequip(CurioCanUnequipEvent event) {
        if (event.getStack().is(ModItems.CURSED_CROWN.get())) {
            if (!AdvancedCrownLogic.canRemoveCursedCrown(event.getSlotContext().entity())) {
                event.setUnequipResult(TriState.FALSE);
                return;
            }
            if (event.getSlotContext().entity() instanceof ServerPlayer player) {
                AdvancedCrownLogic.releaseCursedBinding(player);
            }
        }
        if (CrownIntegrity.shouldBlockRemoval(event.getStack())) {
            CrownIntegrity.onBlockedInterference(event.getStack());
            event.setUnequipResult(TriState.FALSE);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCursedDeathEarly(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && AdvancedCrownLogic.hasCursedCrownPendingDeath(player)
                && !CrownLogic.isWearingGlitched(player)) {
            // Clear XP before grave/death-storage listeners can snapshot it. If a Glitched-family
            // Crown is also worn, defer until the normal revive check so a successful revive does not erase XP.
            AdvancedCrownLogic.wipeCursedDeathExperience(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && CrownLogic.tryRevive(player)) {
            event.setCanceled(true);
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            // ForgeHooksMixin normally creates this ticket before LivingDeathEvent is posted.
            // Keep this idempotent fallback for environments where that optional injection is
            // unavailable; it never creates a second ticket or replacement stack.
            AdvancedCrownLogic.prepareCrownDeathRetention(player);
            AdvancedCrownLogic.wipeCursedDeathExperience(player);
            AdvancedCrownLogic.prepareCursedCrownDeath(player);
        }
        NewCrownLogic.onLivingKilled(event.getEntity(), event.getSource());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && (NewCrownLogic.shouldCancelWarriorKnockback(player)
                || ShadowAbyssalLogic.hasShadowHook(player))) {
            event.setCanceled(true);
            return;
        }
        if (CrownLogic.isMovementLocked(event.getEntity()) || CrownLogic.isFateBound(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer player) {
            if (AdvancedCrownLogic.shouldBlockFrostTarget(mob, player)) {
                event.setNewAboutToBeSetTarget(null);
                mob.setTarget(null);
                return;
            }
            if (mob instanceof Warden warden && NewCrownLogic.shouldBlockWardenTarget(warden, player)) {
                event.setNewAboutToBeSetTarget(null);
                mob.setTarget(null);
                return;
            }
            if (mob instanceof IronGolem golem && NewCrownLogic.isOwnedIronGolemTargetBlocked(golem, player)) {
                event.setNewAboutToBeSetTarget(null);
                mob.setTarget(null);
                return;
            }
            if (NewCrownLogic.shouldBlockDimensionalAcquisition(mob, player)) {
                event.setNewAboutToBeSetTarget(null);
                mob.setTarget(null);
                return;
            }
            if (ShadowAbyssalLogic.shouldBlockTarget(mob, player)) {
                event.setNewAboutToBeSetTarget(null);
                mob.setTarget(null);
                return;
            }
            if (CrownLogic.isAuraActive(player)) {
                event.setNewAboutToBeSetTarget(null);
                mob.setTarget(null);
            }
        }
    }

    @SubscribeEvent
    public static void onGameEvent(VanillaGameEvent event) {
        if (event.getCause() instanceof ServerPlayer player && CrownLogic.isAuraActive(player)) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onStandardCrownRepair(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!(left.getItem() instanceof StandardCrownItem) || !right.is(net.minecraft.world.item.Items.NETHER_STAR)) return;
        if (!left.isDamaged()) return;

        ItemStack output = left.copy();
        int repairAmount = Math.max(1, (int) Math.ceil(output.getMaxDamage() * 0.25D));
        output.setDamageValue(Math.max(0, output.getDamageValue() - repairAmount));
        event.setOutput(output);
        event.setMaterialCost(1);
        event.setCost(10);
    }

    @SubscribeEvent
    public static void onForbiddenDomainEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer earned)) return;
        if (earned.getServer() == null) return;

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TheCrownsMod.MOD_ID, "forbidden_domain");
        var forbiddenDomain = earned.getServer().getAdvancements().get(id);
        if (forbiddenDomain == null || event.getAdvancement() != forbiddenDomain) return;

        for (ServerPlayer viewer : earned.getServer().getPlayerList().getPlayers()) {
            Component title = CrownLogic.ownsCrown(viewer)
                    ? Component.translatable("advancement.thecrowns.forbidden_domain.revealed_title")
                            .withStyle(ChatFormatting.LIGHT_PURPLE)
                    : Component.literal("CROWNLOCK!")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.OBFUSCATED);
            viewer.sendSystemMessage(Component.translatable(
                    "chat.type.advancement.challenge", earned.getDisplayName(), title));
        }
    }

    @SubscribeEvent
    public static void onExperienceGain(PlayerXpEvent.XpChange event) {
        if (event.getAmount() <= 0) return;
        double bonus = AdvancedCrownLogic.cursedExperienceBonusFraction(event.getEntity());
        if (bonus <= 0.0D) return;
        long adjusted = Math.min((long) Integer.MAX_VALUE,
                Math.round(event.getAmount() * (1.0D + bonus)));
        event.setAmount((int) adjusted);
    }

    @EventBusSubscriber(modid = TheCrownsMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void keepTheCrownsItemsExclusiveToModTab(BuildCreativeModeTabContentsEvent event) {
            // The dedicated thecrowns:the_crowns tab is the only creative tab allowed
            // to expose items from this mod. This also catches entries that another tab
            // may add automatically (notably SpawnEggItem-derived entries).
            if (TheCrownsMod.MOD_ID.equals(event.getTabKey().location().getNamespace())) return;

            for (ItemStack stack : List.copyOf(event.getParentEntries())) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (itemId != null && TheCrownsMod.MOD_ID.equals(itemId.getNamespace())) {
                    event.remove(stack, net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }
        }

        @SubscribeEvent
        public static void addEntityAttributes(EntityAttributeModificationEvent event) {
            // Vanilla iron golems do not expose toughness by default on all Forge builds.
            if (!event.has(EntityType.IRON_GOLEM, Attributes.ARMOR_TOUGHNESS)) {
                event.add(EntityType.IRON_GOLEM, Attributes.ARMOR_TOUGHNESS);
            }
        }
    }
}
