package com.sugarplumstudios.pinkified.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.sugarplumstudios.pinkified.client.particle.ParticleSprites;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import com.sugarplumstudios.pinkified.client.util.Ids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Themes sprites of the GUI atlas (buttons, sliders, container parts, HUD icons, mob-effect icons) in place.
 * The original pixels are read from the resource packs, and the themed ones are written over each sprite's spot in
 * the stitched atlas, so switching themes needs no resource reload. The atlases are watched every client tick: when
 * a reload stitched them again, the sprites are read and themed again.
 */
public final class GuiSpriteThemer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier GUI_PROBE = Ids.vanilla("widget/button");
    private static final Identifier EFFECT_PROBE = Ids.vanilla("mob_effect/speed");
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
        TextureAtlas gui = atlas(AtlasIds.GUI);
        // mob effect icons are stitched into the GUI atlas (as mob_effect/<name>)
        TextureAtlas effects = gui;
        TextureAtlas particles = atlas(AtlasIds.PARTICLES);
        Object g = sprite(gui, GUI_PROBE);
        Object e = sprite(effects, EFFECT_PROBE);
        Object pt = sprite(particles, PARTICLE_PROBE);
        // an atlas exists before its first stitch finishes; wait until it can be read
        if (gui != null && g == null) return;
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
        try {
            return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(id);
        } catch (RuntimeException notLoadedYet) {
            return null;
        }
    }

    /** The sprite, or null while the atlas has not been stitched yet. */
    private static TextureAtlasSprite sprite(TextureAtlas atlas, Identifier id) {
        if (atlas == null) return null;
        try {
            return atlas.getSprite(id);
        } catch (IllegalStateException notStitchedYet) {
            return null;
        }
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
                add(effects, Ids.vanilla("mob_effect/" + path), path, r.getValue(), true);
            }
        }
    }

    private static void add(TextureAtlas atlas, Identifier id, String path, Resource resource, boolean effect) {
        TextureAtlasSprite sprite = sprite(atlas, id);
        if (sprite == null || !sprite.contents().name().equals(id)) return; // not stitched (missing sprite)
        int w = sprite.contents().width(), h = sprite.contents().height();
        try (InputStream in = resource.open(); NativeImage img = NativeImage.read(in)) {
            // animated sprites (taller files) are left alone
            if (img.getWidth() != w || img.getHeight() != h) return;
            Canvas c = new Canvas(w, h);
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++) c.px[y * w + x] = img.getPixel(x, y);
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

    private static ByteBuffer staging;

    /** Writes the ARGB canvas over the sprite's spot in the atlas texture (RGBA8, so the ints are stored as ABGR). */
    private static void write(TextureAtlas atlas, TextureAtlasSprite sprite, Canvas c) {
        int bytes = c.width * c.height * 4;
        if (staging == null || staging.capacity() < bytes) {
            if (staging != null) MemoryUtil.memFree(staging);
            staging = MemoryUtil.memAlloc(Math.max(bytes, 64 * 64 * 4));
        }
        staging.clear();
        IntBuffer ints = staging.asIntBuffer();
        for (int i = 0; i < c.width * c.height; i++) ints.put(Colors.toAbgr(c.px[i]));
        staging.limit(bytes);
        RenderSystem.getDevice().createCommandEncoder()
                .writeToTexture(atlas.getTexture(), staging, 0, 0, sprite.getX(), sprite.getY(), c.width, c.height);
    }
}
