package com.sugarplumstudios.pinkified.client.particle;

import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Spawns Pinkified's cosmetic particles around the local player. Everything here is purely
 * client-side: other players never see these effects and the server is never involved.
 */
public final class AmbientEffects {
    private static LocalPlayer trackedPlayer;
    private static float lastHealth = -1;
    private static int lastLevel = -1;
    private static double lastX, lastZ;
    private static double stepDistance;

    private AmbientEffects() {
    }

    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null || mc.isPaused()) return;
        if (player != trackedPlayer) {
            // new world, dimension change or respawn: start tracking fresh
            trackedPlayer = player;
            lastHealth = player.getHealth();
            lastLevel = player.experienceLevel;
            lastX = player.getX();
            lastZ = player.getZ();
            stepDistance = 0;
            return;
        }
        float health = player.getHealth();
        int xpLevel = player.experienceLevel;
        double moved = Math.sqrt(Mth.square(player.getX() - lastX) + Mth.square(player.getZ() - lastZ));
        lastX = player.getX();
        lastZ = player.getZ();
        float prevHealth = lastHealth;
        int prevLevel = lastLevel;
        lastHealth = health;
        lastLevel = xpLevel;

        if (!PinkConfig.on(PinkConfig.PARTICLES)) return;
        ParticleStatus status = mc.options.particles().get();
        if (status == ParticleStatus.MINIMAL) return;
        float density = PinkConfig.get(PinkConfig.PARTICLE_DENSITY).floatValue() * (status == ParticleStatus.DECREASED ? 0.5f : 1f);
        RandomSource r = level.getRandom();
        Palette p = Themes.palette();

        if (PinkConfig.on(PinkConfig.AMBIENT_SPARKLES) && r.nextFloat() < 0.07f * density) {
            double ox = (r.nextDouble() - 0.5) * 6, oz = (r.nextDouble() - 0.5) * 6;
            if (ox * ox + oz * oz > 1.2) {
                spawn(mc, level, player.getX() + ox, player.getY() + 0.2 + r.nextDouble() * 2.2, player.getZ() + oz,
                        0, 0.004, 0, PinkParticle.Kind.SPARKLE, r.nextBoolean() ? p.sparkle : p.petal(r.nextInt(4)));
            }
        }

        if (PinkConfig.on(PinkConfig.CHERRY_BLOSSOMS) && level.dimensionType().hasSkyLight() && r.nextFloat() < 0.2f * density) {
            double x = player.getX() + (r.nextDouble() - 0.5) * 14;
            double z = player.getZ() + (r.nextDouble() - 0.5) * 14;
            double y = player.getEyeY() + 3 + r.nextDouble() * 4;
            if (level.canSeeSky(BlockPos.containing(x, y, z))) {
                spawn(mc, level, x, y, z, 0.01 + r.nextDouble() * 0.015, -0.01, (r.nextDouble() - 0.5) * 0.02,
                        PinkParticle.Kind.PETAL, p.petal(r.nextInt(4)));
            }
        }

        // In first person, effects centred on the player would sit right on the camera, so they
        // are moved a little ahead of the player and kept below eye level.
        boolean firstPerson = mc.options.getCameraType().isFirstPerson();
        Vec3 look = player.getViewVector(1f).multiply(1, 0, 1);
        look = look.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : look.normalize();
        double cx = player.getX() + (firstPerson ? look.x * 2.0 : 0);
        double cz = player.getZ() + (firstPerson ? look.z * 2.0 : 0);

        if (PinkConfig.on(PinkConfig.HEAL_HEARTS) && prevHealth >= 0 && health > prevHealth + 0.49f && player.isAlive()) {
            int count = Math.max(1, Math.round((2 + r.nextInt(3)) * Math.min(1.5f, density)));
            for (int i = 0; i < count; i++) {
                double a = r.nextDouble() * Mth.TWO_PI;
                double radius = firstPerson ? 0.7 : 0.55;
                double y = player.getY() + (firstPerson ? 0.2 + r.nextDouble() * 0.6 : 0.9 + r.nextDouble() * 0.9);
                spawn(mc, level, cx + Math.cos(a) * radius, y, cz + Math.sin(a) * radius, 0, 0.02, 0,
                        PinkParticle.Kind.HEART, i % 2 == 0 ? p.heart : p.heartShine);
            }
        }

        if (PinkConfig.on(PinkConfig.LEVEL_UP_SPARKLES) && prevLevel >= 0 && xpLevel > prevLevel) {
            int count = Math.round(18 * Math.min(1.5f, density));
            double radius = firstPerson ? 0.9 : 0.6;
            double baseY = player.getY() + (firstPerson ? 0.3 : 1.0);
            for (int i = 0; i < count; i++) {
                double a = i * Mth.TWO_PI / count;
                spawn(mc, level, cx + Math.cos(a) * radius, baseY + r.nextDouble() * 0.6, cz + Math.sin(a) * radius,
                        Math.cos(a) * 0.08, 0.03 + r.nextDouble() * 0.04, Math.sin(a) * 0.08,
                        i % 3 == 0 ? PinkParticle.Kind.HEART : PinkParticle.Kind.SPARKLE, i % 2 == 0 ? p.sparkle : p.heart);
            }
        }

        if (PinkConfig.on(PinkConfig.FLOWER_STEPS) && player.onGround() && !player.isSpectator() && !player.isShiftKeyDown()) {
            stepDistance += moved;
            if (stepDistance > 1.5) {
                stepDistance = 0;
                BlockState below = level.getBlockState(player.getOnPos());
                if (below.is(BlockTags.DIRT) && r.nextFloat() < 0.75f * Math.min(1f, density)) {
                    spawn(mc, level, player.getX() + (r.nextDouble() - 0.5) * 0.5, player.getY() + 0.05, player.getZ() + (r.nextDouble() - 0.5) * 0.5,
                            (r.nextDouble() - 0.5) * 0.04, 0.16, (r.nextDouble() - 0.5) * 0.04, PinkParticle.Kind.FLOWER, p.petal(r.nextInt(4)));
                }
            }
        } else {
            stepDistance = 0;
        }
    }

    private static void spawn(Minecraft mc, ClientLevel level, double x, double y, double z, double vx, double vy, double vz,
                              PinkParticle.Kind kind, int tint) {
        mc.particleEngine.add(new PinkParticle(level, x, y, z, vx, vy, vz, kind, tint));
    }
}
