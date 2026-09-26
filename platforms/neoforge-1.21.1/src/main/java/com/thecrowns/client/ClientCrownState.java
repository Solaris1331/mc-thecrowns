package com.thecrowns.client;

import com.thecrowns.logic.CrownLogic;
import com.thecrowns.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Client-only Crown visuals. */
public final class ClientCrownState {
    private static final List<AnnihilationVisual> ANNIHILATION_VISUALS = new ArrayList<>();
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));

    private ClientCrownState() {
    }

    public static void tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            ANNIHILATION_VISUALS.clear();
            return;
        }

        long now = level.getGameTime();
        Iterator<AnnihilationVisual> iterator = ANNIHILATION_VISUALS.iterator();
        while (iterator.hasNext()) {
            AnnihilationVisual visual = iterator.next();
            long age = now - visual.startGameTime;
            if (age < 0L) continue;
            if (age > CrownLogic.ANNIHILATION_WAVE_TICKS) {
                iterator.remove();
                continue;
            }
            if (visual.lastParticleTick == now) continue;
            visual.lastParticleTick = now;

            // The visible front follows the lethal server shell, but is made of
            // short-lived one-shot particles rather than a screen-filling quad sphere.
            double progress = Math.min(1.0D,
                    (age + 1.0D) / (double) CrownLogic.ANNIHILATION_WAVE_TICKS);
            double radius = visual.maxRadius * progress;
            spawnAnnihilationParticleShell(minecraft, level, visual.origin, radius, (int) age);
        }
    }

    public static void showGlitchedReviveActivation() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null) return;
        minecraft.gameRenderer.displayItemActivation(new ItemStack(ModItems.GLITCHED_CROWN.get()));
    }

    public static void showAngelicGuardActivation() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null) return;
        minecraft.gameRenderer.displayItemActivation(new ItemStack(ModItems.ANGELIC_CROWN.get()));
    }

    public static void startAnnihilation(double x, double y, double z, double radius) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) return;
        ANNIHILATION_VISUALS.add(new AnnihilationVisual(new Vec3(x, y, z), level.getGameTime(), radius));
        if (ANNIHILATION_VISUALS.size() > 16) ANNIHILATION_VISUALS.remove(0);
    }

    /**
     * Emits a roughly uniform point membrane on the current expanding sphere.
     * ELECTRIC_SPARK is deliberately short-lived, so old radii do not remain as
     * a solid yellow volume and the player's screen is never covered by a full
     * translucent surface. Points too close to the camera are skipped as an
     * additional first-person visibility safeguard.
     */
    private static void spawnAnnihilationParticleShell(Minecraft minecraft, ClientLevel level,
                                                        Vec3 origin, double radius, int age) {
        if (radius <= 0.05D) return;

        int points = Math.min(280, 104 + (int) Math.ceil(radius * 2.1D));
        double phase = age * 0.47D;
        Vec3 camera = minecraft.gameRenderer == null
                ? Vec3.ZERO
                : minecraft.gameRenderer.getMainCamera().getPosition();

        for (int i = 0; i < points; i++) {
            double yUnit = 1.0D - 2.0D * (i + 0.5D) / points;
            double ring = Math.sqrt(Math.max(0.0D, 1.0D - yUnit * yUnit));
            double theta = GOLDEN_ANGLE * i + phase;
            double xUnit = Math.cos(theta) * ring;
            double zUnit = Math.sin(theta) * ring;

            double x = origin.x + xUnit * radius;
            double y = origin.y + yUnit * radius;
            double z = origin.z + zUnit * radius;

            // Do not spawn a particle directly in the first-person camera.
            double dx = x - camera.x;
            double dy = y - camera.y;
            double dz = z - camera.z;
            if (dx * dx + dy * dy + dz * dz < 4.0D) continue;

            // Tiny outward velocity makes the membrane sparkle without turning
            // into a persistent fog volume.
            level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    x, y, z,
                    xUnit * 0.015D, yUnit * 0.015D, zUnit * 0.015D);
        }
    }

    private static final class AnnihilationVisual {
        private final Vec3 origin;
        private final long startGameTime;
        private final double maxRadius;
        private long lastParticleTick = Long.MIN_VALUE;

        private AnnihilationVisual(Vec3 origin, long startGameTime, double maxRadius) {
            this.origin = origin;
            this.startGameTime = startGameTime;
            this.maxRadius = maxRadius;
        }
    }
}
