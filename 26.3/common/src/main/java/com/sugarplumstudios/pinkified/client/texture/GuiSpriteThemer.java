package com.sugarplumstudios.pinkified.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.sugarplumstudios.pinkified.client.particle.ParticleSprites;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import com.sugarplumstudios.pinkified.client.util.Ids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Themes sprites of the GUI atlas (buttons, sliders, container parts, HUD icons) and the mob-effect atlas in place.
 * The original pixels are read from the resource packs, and the themed ones are written over each sprite's spot in
 * the stitched atlas, so switching themes needs no resource reload. The atlases are watched every client tick: when
 * a reload stitched them again, the sprites are read and themed again.
 */
public final class GuiSpriteThemer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier GUI_ATLAS = Ids.vanilla("textures/atlas/gui.png");
    private static final Identifier EFFECT_ATLAS = Ids.vanilla("textures/atlas/mob_effects.png");
    private static final Identifier GUI_PROBE = Ids.vanilla("widget/button");
    private static final Identifier EFFECT_PROBE = Ids.vanilla("speed");
    private static final Identifier PARTICLE_PROBE = Ids.vanilla("heart");

    private record Entry(TextureAtlas atlas, Identifier sprite, String path, Canvas original, boolean effect) {
    }

    private static final List<Entry> entries = new ArrayList<>();
    private static Object guiSeen, effectSeen, particleSeen;
    private static boolean dirty = true;

    private GuiSpriteThemer() {
    }

    /** Resource packs changed: read every sprite again on the next tick. */
    public static void markDirty() {
        dirty = true;
    }

    @SuppressWarnings("ConstantConditions")
    public static void tick() {
        TextureAtlas gui = atlas(GUI_ATLAS);
        TextureAtlas effects = atlas(EFFECT_ATLAS);
        TextureAtlas particles = atlas(TextureAtlas.LOCATION_PARTICLES);
        Object g = gui == null ? null : gui.getSprite(GUI_PROBE);
        Object e = effects == null ? null : effects.getSprite(EFFECT_PROBE);
        Object pt = particles == null ? null : particles.getSprite(PARTICLE_PROBE);
        if (pt != particleSeen) {
            particleSeen = pt;
            ParticleSprites.invalidate();
        }
        if (!dirty && g == guiSeen && e == effectSeen) return;
        dirty = false;
        guiSeen = g;
        effectSeen = e;
        capture(Minecraft.getInstance().getResourceManager(), gui, effects);
        apply();
    }

    private static TextureAtlas atlas(Identifier id) {
        AbstractTexture t = Minecraft.getInstance().getTextureManager().getTexture(id, null);
        return t instanceof TextureAtlas a ? a : null;
    }

    private static void capture(ResourceManager rm, TextureAtlas gui, TextureAtlas effects) {
        entries.clear();
        if (gui != null) {
            for (Map.Entry<Identifier, Resource> r : rm.listResources("textures/gui/sprites", l -> l.getPath().endsWith(".png")).entrySet()) {
                Identifier file = r.getKey();
                if (!file.getNamespace().equals("minecraft")) continue;
                String path = file.getPath().substring("textures/gui/sprites/".length(), file.getPath().length() - 4);
                if (!SpriteArt.handles(path)) continue;
                add(gui, Ids.vanilla(path), path, r.getValue(), false);
            }
        }
        if (effects != null) {
            for (Map.Entry<Identifier, Resource> r : rm.listResources("textures/mob_effect", l -> l.getPath().endsWith(".png")).entrySet()) {
                Identifier file = r.getKey();
                if (!file.getNamespace().equals("minecraft")) continue;
                String path = file.getPath().substring("textures/mob_effect/".length(), file.getPath().length() - 4);
                add(effects, Ids.vanilla(path), path, r.getValue(), true);
            }
        }
    }

    private static void add(TextureAtlas atlas, Identifier id, String path, Resource resource, boolean effect) {
        TextureAtlasSprite sprite = atlas.getSprite(id);
        if (!sprite.contents().name().equals(id)) return; // not stitched (missing sprite)
        int w = sprite.contents().width(), h = sprite.contents().height();
        try (InputStream in = resource.open(); NativeImage img = NativeImage.read(in)) {
            // animated sprites (taller files) are left alone
            if (img.getWidth() != w || img.getHeight() != h) return;
            Canvas c = new Canvas(w, h);
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++) c.px[y * w + x] = Colors.fromAbgr(img.getPixelRGBA(x, y));
            entries.add(new Entry(atlas, id, path, c, effect));
        } catch (Exception ex) {
            LOGGER.debug("Pinkified could not read sprite {}", id, ex);
        }
    }

    /** Writes every captured sprite again, themed with the current settings (or original when its toggle is off). */
    public static void apply() {
        if (entries.isEmpty()) return;
        SpriteArt.Session session = new SpriteArt.Session(Themes.palette(), ThemedTextures.currentOptions());
        for (Entry e : entries) {
            Canvas out = null;
            try {
                out = e.effect() ? session.effectIcon(e.original()) : session.process(e.path(), e.original());
            } catch (RuntimeException ex) {
                LOGGER.warn("Pinkified failed to theme sprite {}", e.sprite(), ex);
            }
            write(e.atlas(), e.atlas().getSprite(e.sprite()), out != null ? out : e.original());
        }
    }

    private static void write(TextureAtlas atlas, TextureAtlasSprite sprite, Canvas c) {
        try (NativeImage img = new NativeImage(c.width, c.height, false)) {
            for (int y = 0; y < c.height; y++)
                for (int x = 0; x < c.width; x++) img.setPixelRGBA(x, y, Colors.toAbgr(c.px[y * c.width + x]));
            atlas.bind();
            img.upload(0, sprite.getX(), sprite.getY(), 0, 0, c.width, c.height, false, false, false, false);
        }
    }
}
