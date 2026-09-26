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
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.event.CurioUnequipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = TheCrownsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
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
    public static void onAttack(LivingAttackEvent event) {
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
    public static void onHurt(LivingHurtEvent event) {
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
    public static void onFinalDamageModifiers(LivingDamageEvent event) {
        if (event.getAmount() <= 0.0F) return;
        float modified = NewCrownLogic.modifyFinalDamage(event.getEntity(), event.getSource(), event.getAmount());
        modified = AdvancedCrownLogic.applyCursedOutgoingPenalty(event.getSource(), modified);
        modified = ShadowAbyssalLogic.applyAmbush(event.getSource(), modified);
        if (event.getEntity() instanceof ServerPlayer player && NewCrownLogic.tryAngelicLethalGuard(player, modified)) {
            event.setAmount(0.0F);
            return;
        }
        event.setAmount(modified);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamageResolved(LivingDamageEvent event) {
        // This is intentionally event-based rather than a LivingEntity Redirect. It runs after
        // other final-damage listeners, preserving the Iron Guardian cap without monopolizing
        // ForgeHooks.onLivingDamage and breaking other mods' Mixins.
        float resolved = NewCrownLogic.capIronforgedFinalDamage(event.getEntity(), event.getAmount());
        event.setAmount(resolved);
        if (resolved <= 0.0F) return;
        ServerPlayer attacker = CrownLogic.crownDamageOwner(event.getSource());
        if (attacker != null && CrownLogic.isFateActive(attacker)
                && CrownLogic.canAffectCrownTarget(attacker, event.getEntity(), CrownAbilityTargetEvent.Ability.FATE_BIND)) {
            CrownLogic.applyFateBind(event.getEntity());
        }
        NewCrownLogic.afterDamageResolved(event.getEntity(), event.getSource(), resolved);
        AdvancedCrownLogic.onDamageResolved(event.getEntity(), event.getSource(), resolved);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        CrownLogic.enforceGlitchedRayHealingCap(event.getEntity());
        CrownLogic.tickFateBind(event.getEntity());
        NewCrownLogic.tickOwnedIronGolemEntity(event.getEntity());
        AdvancedCrownLogic.tickLiving(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTeleport(EntityTeleportEvent event) {
        if (CrownLogic.isFateBound(event.getEntity()) && event.isCancelable()) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDimensionTravel(EntityTravelToDimensionEvent event) {
        if (CrownLogic.isFateBound(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (AdvancedCrownLogic.isTemporalEffectImmune(event.getEntity(), event.getEffectInstance().getEffect())
                || NewCrownLogic.isImmuneToEffect(event.getEntity(), event.getEffectInstance().getEffect())) {
            event.setResult(Event.Result.DENY);
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
        if (CrownLogic.shouldProtectBeneficialEffectRemoval(event.getEntity(), event.getEffect())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWarriorUseItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof ServerPlayer player && AdvancedCrownLogic.isConsumableSpeedBoosted(player, event.getItem())) {
            event.setDuration(AdvancedCrownLogic.acceleratedUseDuration(event.getDuration()));
        }
    }

    @SubscribeEvent
    public static void onTemporalConcentrationInteract(PlayerInteractEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AdvancedCrownLogic.cancelTemporalRewindConcentration(player);
        }
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
    public static void onCurioUnequip(CurioUnequipEvent event) {
        if (event.getStack().is(ModItems.CURSED_CROWN.get())) {
            if (!AdvancedCrownLogic.canRemoveCursedCrown(event.getSlotContext().entity())) {
                event.setResult(Event.Result.DENY);
                return;
            }
            if (event.getSlotContext().entity() instanceof ServerPlayer player) {
                AdvancedCrownLogic.releaseCursedBinding(player);
            }
        }
        if (CrownIntegrity.shouldBlockRemoval(event.getStack())) {
            CrownIntegrity.onBlockedInterference(event.getStack());
            event.setResult(Event.Result.DENY);
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
        if (event.getNewTarget() instanceof ServerPlayer player) {
            if (AdvancedCrownLogic.shouldBlockFrostTarget(mob, player)) {
                event.setNewTarget(null);
                mob.setTarget(null);
                return;
            }
            if (mob instanceof Warden warden && NewCrownLogic.shouldBlockWardenTarget(warden, player)) {
                event.setNewTarget(null);
                mob.setTarget(null);
                return;
            }
            if (mob instanceof IronGolem golem && NewCrownLogic.isOwnedIronGolemTargetBlocked(golem, player)) {
                event.setNewTarget(null);
                mob.setTarget(null);
                return;
            }
            if (NewCrownLogic.shouldBlockDimensionalAcquisition(mob, player)) {
                event.setNewTarget(null);
                mob.setTarget(null);
                return;
            }
            if (ShadowAbyssalLogic.shouldBlockTarget(mob, player)) {
                event.setNewTarget(null);
                mob.setTarget(null);
                return;
            }
            if (CrownLogic.isAuraActive(player)) {
                event.setNewTarget(null);
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

        ResourceLocation id = new ResourceLocation(TheCrownsMod.MOD_ID, "forbidden_domain");
        var forbiddenDomain = earned.getServer().getAdvancements().getAdvancement(id);
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
    public static void onLooting(LootingLevelEvent event) {
        if (event.getDamageSource() != null
                && event.getDamageSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker) {
            if (CrownLogic.isWearing(attacker)) {
                event.setLootingLevel(event.getLootingLevel() + CrownLogic.lootingBonus(attacker));
            }
            int cursed = AdvancedCrownLogic.cursedLootingBonus(attacker);
            if (cursed != 0) event.setLootingLevel(event.getLootingLevel() + cursed);
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

    @Mod.EventBusSubscriber(modid = TheCrownsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void keepTheCrownsItemsExclusiveToModTab(BuildCreativeModeTabContentsEvent event) {
            // The dedicated thecrowns:the_crowns tab is the only creative tab allowed
            // to expose items from this mod. This also catches entries that another tab
            // may add automatically (notably SpawnEggItem-derived entries).
            if (TheCrownsMod.MOD_ID.equals(event.getTabKey().location().getNamespace())) return;

            var iterator = event.getEntries().iterator();
            while (iterator.hasNext()) {
                ItemStack stack = iterator.next().getKey();
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (itemId != null && TheCrownsMod.MOD_ID.equals(itemId.getNamespace())) {
                    iterator.remove();
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
