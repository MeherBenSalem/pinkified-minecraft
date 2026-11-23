package com.sugarplumstudios.pinkified.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import com.sugarplumstudios.pinkified.client.util.Ids;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Softens status-effect icons into pastel versions. Right after vanilla stitches the mob-effect atlas, the original
 * icons are read from the resource packs; themed pixels are written over each icon's spot in the atlas, so the theme
 * can change at any time without a resource reload.
 */
public final class EffectIconTinter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation MOB_EFFECTS = Ids.vanilla("textures/atlas/mob_effects.png");

    private static TextureAtlas atlas;
    private static final Map<ResourceLocation, int[]> originals = new HashMap<>();

    private EffectIconTinter() {
    }

    public static void onStitched(TextureAtlas a) {
        if (!a.location().equals(MOB_EFFECTS)) return;
        atlas = a;
        originals.clear();
        ResourceManager rm = Minecraft.getInstance().getResourceManager();
        for (Map.Entry<ResourceLocation, Resource> r : rm.listResources("textures/mob_effect", l -> l.getPath().endsWith(".png")).entrySet()) {
            ResourceLocation file = r.getKey();
            String path = file.getPath().substring("textures/mob_effect/".length(), file.getPath().length() - 4);
            ResourceLocation id = new ResourceLocation(file.getNamespace(), path);
            TextureAtlasSprite sprite = a.getSprite(id);
            if (!sprite.contents().name().equals(id)) continue; // not stitched
            int w = sprite.contents().width(), h = sprite.contents().height();
            try (InputStream in = r.getValue().open(); NativeImage img = NativeImage.read(in)) {
                if (img.getWidth() != w || img.getHeight() != h) continue; // animated icons are left alone
                int[] px = new int[w * h];
                for (int y = 0; y < h; y++)
                    for (int x = 0; x < w; x++) px[y * w + x] = Colors.fromAbgr(img.getPixelRGBA(x, y));
                originals.put(id, px);
            } catch (Exception ex) {
                LOGGER.debug("Pinkified could not read effect icon {}", id, ex);
            }
        }
        apply();
    }

    public static void apply() {
        if (atlas == null || originals.isEmpty()) return;
        boolean themed = PinkConfig.on(PinkConfig.THEME_EFFECT_ICONS);
        atlas.bind();
        for (Map.Entry<ResourceLocation, int[]> e : originals.entrySet()) {
            TextureAtlasSprite sprite = atlas.getSprite(e.getKey());
            int w = sprite.contents().width(), h = sprite.contents().height();
            if (w * h != e.getValue().length) continue;
            Canvas c = new Canvas(w, h, e.getValue().clone());
            if (themed) Recolor.effectIcon(c, Themes.palette());
            try (NativeImage img = new NativeImage(w, h, false)) {
                for (int y = 0; y < h; y++)
                    for (int x = 0; x < w; x++) img.setPixelRGBA(x, y, Colors.toAbgr(c.px[y * w + x]));
                img.upload(0, sprite.getX(), sprite.getY(), 0, 0, w, h, false, false, false, false);
            }
        }
    }
}
