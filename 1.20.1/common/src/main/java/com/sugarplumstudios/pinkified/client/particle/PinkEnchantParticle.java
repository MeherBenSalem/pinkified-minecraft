package com.sugarplumstudios.pinkified.client.particle;

import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * The enchanting-table glyph particle, behaving exactly like vanilla's but tinted in the theme
 * colour when enabled. Registered as the provider for {@code minecraft:enchant}; with the option
 * off it uses vanilla's colours.
 */
public class PinkEnchantParticle extends TextureSheetParticle {
    private final double xStart, yStart, zStart;

    protected PinkEnchantParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.xStart = x;
        this.yStart = y;
        this.zStart = z;
        this.xo = x + xd;
        this.yo = y + yd;
        this.zo = z + zd;
        this.x = this.xo;
        this.y = this.yo;
        this.z = this.zo;
        this.quadSize = 0.1F * (random.nextFloat() * 0.5F + 0.2F);
        float f = random.nextFloat() * 0.6F + 0.4F;
        if (PinkConfig.on(PinkConfig.PINK_ENCHANT_PARTICLES)) {
            Palette p = Themes.palette();
            int c = Colors.lerp(Colors.lerp(p.heart, p.light, 0.45f), p.accentLight, random.nextFloat() * 0.5f);
            float k = 0.55f + f * 0.45f;
            this.rCol = Colors.red(c) / 255f * k;
            this.gCol = Colors.green(c) / 255f * k;
            this.bCol = Colors.blue(c) / 255f * k;
        } else {
            this.rCol = 0.9F * f;
            this.gCol = 0.9F * f;
            this.bCol = f;
        }
        this.hasPhysics = false;
        this.lifetime = (int) (Math.random() * 10.0) + 30;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double dx, double dy, double dz) {
        setBoundingBox(getBoundingBox().move(dx, dy, dz));
        setLocationFromBoundingbox();
    }

    @Override
    public int getLightColor(float partialTick) {
        int i = super.getLightColor(partialTick);
        float f = (float) age / (float) lifetime;
        f *= f;
        f *= f;
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        k += (int) (f * 15.0F * 16.0F);
        if (k > 240) k = 240;
        return j | k << 16;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float f = (float) this.age / (float) this.lifetime;
            f = 1.0F - f;
            float f1 = 1.0F - f;
            f1 *= f1;
            f1 *= f1;
            this.x = this.xStart + this.xd * (double) f;
            this.y = this.yStart + this.yd * (double) f - (double) (f1 * 1.2F);
            this.z = this.zStart + this.zd * (double) f;
            this.setPos(this.x, this.y, this.z);
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            PinkEnchantParticle particle = new PinkEnchantParticle(level, x, y, z, xd, yd, zd);
            particle.pickSprite(sprites);
            return particle;
        }
    }
}
