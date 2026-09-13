package com.glitchedcrown.client;

import com.glitchedcrown.GlitchedCrownMod;
import com.glitchedcrown.compat.RecipeViewerCompat;
import com.glitchedcrown.logic.CrownLogic;
import com.glitchedcrown.client.model.CrownArmorModel;
import com.glitchedcrown.client.screen.CrownBuilderScreen;
import com.glitchedcrown.client.screen.CrownLorebookScreen;
import com.glitchedcrown.registry.ModMenus;
import com.glitchedcrown.network.AnnihilatePayload;
import com.glitchedcrown.network.FireLaserPayload;
import com.glitchedcrown.network.ModNetworking;
import com.glitchedcrown.network.ToggleAuraPayload;
import com.glitchedcrown.network.ToggleFatePayload;
import com.glitchedcrown.network.SummonIronGolemPayload;
import com.glitchedcrown.network.WarriorDuelPayload;
import com.glitchedcrown.network.AngelicFlightPayload;
import com.glitchedcrown.network.TemporalMarkPayload;
import com.glitchedcrown.network.TemporalRewindPayload;
import com.glitchedcrown.network.TimeWarpPayload;
import com.glitchedcrown.network.DivineSanctifyPayload;
import com.glitchedcrown.network.CursedLiberationPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {
    private static final KeyMapping FIRE_LASER = key("fire_laser", GLFW.GLFW_KEY_G);
    private static final KeyMapping TOGGLE_FATE = key("toggle_fate", GLFW.GLFW_KEY_H);
    private static final KeyMapping TOGGLE_AURA = key("toggle_aura", GLFW.GLFW_KEY_J);
    private static final KeyMapping ANNIHILATE = key("annihilate", GLFW.GLFW_KEY_K);
    private static final KeyMapping SUMMON_IRON_GOLEM = key("summon_iron_golem", GLFW.GLFW_KEY_K);
    private static final KeyMapping WARRIOR_DUEL = key("warrior_duel", GLFW.GLFW_KEY_K);
    private static final KeyMapping ANGELIC_FLIGHT = key("angelic_flight", GLFW.GLFW_KEY_K);
    private static final KeyMapping TEMPORAL_MARK = key("temporal_mark", GLFW.GLFW_KEY_H);
    private static final KeyMapping TEMPORAL_REWIND = key("temporal_rewind", GLFW.GLFW_KEY_J);
    private static final KeyMapping TIME_WARP = key("time_warp", GLFW.GLFW_KEY_K);
    private static final KeyMapping DIVINE_SANCTIFY = key("divine_sanctify", GLFW.GLFW_KEY_K);
    private static final KeyMapping CURSED_LIBERATION = key("cursed_liberation", GLFW.GLFW_KEY_K);

    private ClientEvents() {
    }

    private static KeyMapping key(String name, int code) {
        return new KeyMapping("key.glitchedcrown." + name, InputConstants.Type.KEYSYM, code,
                "key.categories.glitchedcrown");
    }

    @Mod.EventBusSubscriber(modid = GlitchedCrownMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(FIRE_LASER);
            event.register(TOGGLE_FATE);
            event.register(TOGGLE_AURA);
            event.register(ANNIHILATE);
            event.register(SUMMON_IRON_GOLEM);
            event.register(WARRIOR_DUEL);
            event.register(ANGELIC_FLIGHT);
            event.register(TEMPORAL_MARK);
            event.register(TEMPORAL_REWIND);
            event.register(TIME_WARP);
            event.register(DIVINE_SANCTIFY);
            event.register(CURSED_LIBERATION);
        }

        @SubscribeEvent
        public static void registerScreens(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenus.CROWN_BUILDER.get(), CrownBuilderScreen::new);
                MenuScreens.register(ModMenus.CROWN_LOREBOOK.get(), CrownLorebookScreen::new);
                RecipeViewerCompat.logDetected();
            });
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(CrownArmorModel.LAYER_LOCATION, CrownArmorModel::createBodyLayer);
        }
    }

    @Mod.EventBusSubscriber(modid = GlitchedCrownMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeBus {
        private ForgeBus() {
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            while (FIRE_LASER.consumeClick()) ModNetworking.CHANNEL.sendToServer(new FireLaserPayload());
            while (TOGGLE_FATE.consumeClick()) ModNetworking.CHANNEL.sendToServer(new ToggleFatePayload());
            while (TOGGLE_AURA.consumeClick()) ModNetworking.CHANNEL.sendToServer(new ToggleAuraPayload());
            while (ANNIHILATE.consumeClick()) ModNetworking.CHANNEL.sendToServer(new AnnihilatePayload());
            while (SUMMON_IRON_GOLEM.consumeClick()) ModNetworking.CHANNEL.sendToServer(new SummonIronGolemPayload());
            while (WARRIOR_DUEL.consumeClick()) ModNetworking.CHANNEL.sendToServer(new WarriorDuelPayload());
            while (ANGELIC_FLIGHT.consumeClick()) ModNetworking.CHANNEL.sendToServer(new AngelicFlightPayload());
            while (TEMPORAL_MARK.consumeClick()) ModNetworking.CHANNEL.sendToServer(new TemporalMarkPayload());
            while (TEMPORAL_REWIND.consumeClick()) ModNetworking.CHANNEL.sendToServer(new TemporalRewindPayload());
            while (TIME_WARP.consumeClick()) ModNetworking.CHANNEL.sendToServer(new TimeWarpPayload());
            while (DIVINE_SANCTIFY.consumeClick()) ModNetworking.CHANNEL.sendToServer(new DivineSanctifyPayload());
            while (CURSED_LIBERATION.consumeClick()) ModNetworking.CHANNEL.sendToServer(new CursedLiberationPayload());
            ClientCrownState.tick(Minecraft.getInstance());
        }


        @SubscribeEvent
        public static void hidePlayer(RenderPlayerEvent.Pre event) {
            if (event.getEntity().isInvisible() && event.getEntity().isSilent()
                    && CrownLogic.isAuraActive(event.getEntity())) {
                event.setCanceled(true);
            }
        }

    }
}
