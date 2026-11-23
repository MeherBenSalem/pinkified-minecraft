package com.sugarplumstudios.pinkified.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.theme.CrosshairStyle;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import com.sugarplumstudios.pinkified.client.util.Ids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Serves themed versions of vanilla GUI textures.
 *
 * <p>On every resource reload the original images are read from the active resource packs (so
 * other packs are respected) and kept in memory. Themed copies are uploaded as
 * {@link DynamicTexture}s registered under the vanilla texture ids, which means vanilla and
 * modded screens that draw those textures pick up the theme with no code changes. Switching
 * themes only re-uploads pixels, so it is instant and needs no resource reload.
 */
public final class ThemedTextures implements ResourceManagerReloadListener {
    public static final ThemedTextures INSTANCE = new ThemedTextures();
    public static final ResourceLocation CROSSHAIRS = Ids.mod("dynamic/crosshairs");
    /** Width of one crosshair cell in {@link #CROSSHAIRS}. */
    public static final int CROSSHAIR_CELL = 15;

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final class Entry {
        final ThemedArt.Target target;
        final ResourceLocation id;
        Canvas source;
        boolean blur, clamp;
        DynamicTexture texture;

        Entry(ThemedArt.Target target) {
            this.target = target;
            this.id = Ids.vanilla(target.path());
        }
    }

    private final Map<ResourceLocation, Entry> entries = new LinkedHashMap<>();
    private DynamicTexture crosshairs;
    private boolean loaded;

    private ThemedTextures() {
        for (ThemedArt.Target t : ThemedArt.TARGETS) {
            Entry e = new Entry(t);
            entries.put(e.id, e);
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        for (Entry e : entries.values()) {
            e.source = null;
            Optional<Resource> res = manager.getResource(e.id);
            if (res.isEmpty()) continue;
            try (InputStream in = res.get().open(); NativeImage img = NativeImage.read(in)) {
                e.source = toCanvas(img);
                Optional<TextureMetadataSection> meta = res.get().metadata().getSection(TextureMetadataSection.SERIALIZER);
                e.blur = meta.map(TextureMetadataSection::isBlur).orElse(false);
                e.clamp = meta.map(TextureMetadataSection::isClamp).orElse(false);
            } catch (Exception ex) {
                LOGGER.warn("Pinkified could not read {}; leaving it untouched", e.id, ex);
                e.source = null;
            }
        }
        loaded = true;
        apply();
        GuiSpriteThemer.markDirty();
    }

    /** Rebuilds every themed texture from the cached originals using the current settings. */
    public void apply() {
        if (!loaded) return;
        RenderSystem.assertOnRenderThreadOrInit();
        TextureManager tm = Minecraft.getInstance().getTextureManager();
        Palette p = Themes.palette();
        ThemedArt.Options o = currentOptions();
        for (Entry e : entries.values()) {
            Canvas out = null;
            if (e.source != null) {
                try {
                    out = ThemedArt.process(e.target, e.source, p, o);
                } catch (RuntimeException ex) {
                    LOGGER.warn("Pinkified failed to theme {}", e.id, ex);
                }
            }
            if (out == null) {
                if (e.texture != null) {
                    // hand the id back to vanilla; it reloads the pack texture on next use
                    tm.release(e.id);
                    e.texture = null;
                }
                continue;
            }
            e.texture = upload(tm, e.id, e.texture, out, e.blur, e.clamp);
        }
        crosshairs = upload(tm, CROSSHAIRS, crosshairs, crosshairSheet(p), false, true);
    }

    public static ThemedArt.Options currentOptions() {
        return new ThemedArt.Options(
                PinkConfig.on(PinkConfig.THEME_CONTAINERS),
                PinkConfig.on(PinkConfig.THEME_WIDGETS),
                PinkConfig.on(PinkConfig.MENU_BACKGROUNDS),
                PinkConfig.on(PinkConfig.THEME_HEARTS),
                PinkConfig.on(PinkConfig.THEME_FOOD),
                PinkConfig.get(PinkConfig.FOOD_STYLE),
                PinkConfig.on(PinkConfig.THEME_ARMOR),
                PinkConfig.on(PinkConfig.THEME_XP),
                PinkConfig.on(PinkConfig.THEME_HOTBAR),
                PinkConfig.get(PinkConfig.CROSSHAIR) != CrosshairStyle.VANILLA,
                PinkConfig.on(PinkConfig.PINK_GLINT),
                PinkConfig.on(PinkConfig.THEME_EFFECT_ICONS));
    }

    private static Canvas crosshairSheet(Palette p) {
        CrosshairStyle[] styles = CrosshairStyle.values();
        Canvas sheet = new Canvas(CROSSHAIR_CELL * styles.length, CROSSHAIR_CELL);
        for (CrosshairStyle s : styles) {
            if (s == CrosshairStyle.VANILLA) continue;
            Canvas c = HudSprites.crosshair(s, p);
            for (int y = 0; y < c.height; y++)
                System.arraycopy(c.px, y * c.width, sheet.px, y * sheet.width + s.ordinal() * CROSSHAIR_CELL, c.width);
        }
        return sheet;
    }

    private static DynamicTexture upload(TextureManager tm, ResourceLocation id, DynamicTexture tex, Canvas out, boolean blur, boolean clamp) {
        NativeImage pixels = tex == null ? null : tex.getPixels();
        if (tex == null || pixels == null || pixels.getWidth() != out.width || pixels.getHeight() != out.height) {
            tex = new DynamicTexture(out.width, out.height, false);
            tm.register(id, tex);
            pixels = tex.getPixels();
        }
        for (int y = 0; y < out.height; y++)
            for (int x = 0; x < out.width; x++)
                pixels.setPixelRGBA(x, y, Colors.toAbgr(out.px[y * out.width + x]));
        tex.bind();
        pixels.upload(0, 0, 0, 0, 0, out.width, out.height, blur, clamp, false, false);
        return tex;
    }

    static Canvas toCanvas(NativeImage img) {
        int w = img.getWidth(), h = img.getHeight();
        Canvas c = new Canvas(w, h);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) c.px[y * w + x] = Colors.fromAbgr(img.getPixelRGBA(x, y));
        return c;
    }
}
