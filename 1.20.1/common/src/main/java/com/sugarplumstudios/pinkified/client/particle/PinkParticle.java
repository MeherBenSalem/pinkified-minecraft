package com.sugarplumstudios.pinkified.client.particle;

import com.sugarplumstudios.pinkified.client.util.Colors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;

/** Pinkified's cosmetic particles: rising hearts, twinkling sparkles, drifting petals and popping flowers. */
public class PinkParticle extends TextureSheetParticle {
    public enum Kind {HEART, SPARKLE, PETAL, FLOWER}

    private static final String[] SPARKLE_FRAMES = {"sparkle_0", "sparkle_1", "sparkle_2", "sparkle_3", "sparkle_2", "sparkle_1"};

    private final Kind kind;
    private final float baseSize;
    private float spin;
    private final float swayPhase;
    private final boolean glow;

    public PinkParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, Kind kind, int tint) {
        super(level, x, y, z);
        this.kind = kind;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        setColor(Colors.red(tint) / 255f, Colors.green(tint) / 255f, Colors.blue(tint) / 255f);
        this.swayPhase = random.nextFloat() * Mth.TWO_PI;
        switch (kind) {
            case HEART -> {
                lifetime = 26 + random.nextInt(14);
                gravity = -0.012f;
                friction = 0.9f;
                hasPhysics = false;
                baseSize = 0.11f + random.nextFloat() * 0.06f;
                spin = 0;
                glow = true;
                sprite("heart");
            }
            case SPARKLE -> {
                lifetime = 18 + random.nextInt(16);
                gravity = 0f;
                friction = 0.92f;
                hasPhysics = false;
                baseSize = 0.07f + random.nextFloat() * 0.07f;
                spin = 0;
                glow = true;
                sprite(SPARKLE_FRAMES[0]);
            }
            case PETAL -> {
                lifetime = 120 + random.nextInt(80);
                gravity = 0.0045f;
                friction = 0.96f;
                hasPhysics = true;
                baseSize = 0.07f + random.nextFloat() * 0.05f;
                spin = (random.nextFloat() - 0.5f) * 0.2f;
                glow = false;
                sprite("petal_" + random.nextInt(4));
            }
            default -> {
                lifetime = 24 + random.nextInt(16);
                gravity = 0.04f;
                friction = 0.9f;
                hasPhysics = true;
                baseSize = 0.08f + random.nextFloat() * 0.04f;
                spin = (random.nextFloat() - 0.5f) * 0.3f;
                glow = false;
                sprite("flower_" + random.nextInt(3));
            }
        }
        this.quadSize = baseSize;
        this.roll = random.nextFloat() * Mth.TWO_PI;
        this.oRoll = roll;
    }

    private void sprite(String name) {
        TextureAtlasSprite s = ParticleSprites.get(name);
        if (s != null) setSprite(s);
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) return;
        oRoll = roll;
        roll += spin;
        float life = age / (float) lifetime;
        switch (kind) {
            case PETAL -> {
                xd += Math.sin(age * 0.12f + swayPhase) * 0.0016;
                zd += Math.cos(age * 0.09f + swayPhase) * 0.0016;
                if (onGround) spin = 0;
            }
            case HEART -> xd += Math.sin(age * 0.25f + swayPhase) * 0.0012;
            case SPARKLE -> {
                int frame = Math.min(SPARKLE_FRAMES.length - 1, (int) (life * SPARKLE_FRAMES.length));
                sprite(SPARKLE_FRAMES[frame]);
            }
            default -> {
            }
        }
        float fadeStart = kind == Kind.PETAL ? 0.8f : 0.6f;
        alpha = life < fadeStart ? 1f : 1f - (life - fadeStart) / (1f - fadeStart);
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float t = (age + partialTicks) / 4f;
        float pop = t >= 1f ? 1f : (float) (1 + 2.7 * Math.pow(t - 1, 3) + 1.7 * Math.pow(t - 1, 2));
        return baseSize * Math.max(0.05f, pop);
    }

    @Override
    public int getLightColor(float partialTick) {
        return glow ? 0xF000F0 : super.getLightColor(partialTick);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
