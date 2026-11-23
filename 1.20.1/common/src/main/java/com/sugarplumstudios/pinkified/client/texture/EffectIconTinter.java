package com.sugarplumstudios.pinkified.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import com.sugarplumstudios.pinkified.client.util.Ids;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Softens status-effect icons into pastel versions. The mob-effect atlas is captured right
 * after vanilla stitches it; pixels are rewritten in place and re-uploaded, so the theme can
 * change at any time without a resource reload.
 */
public final class EffectIconTinter {
    private static final ResourceLocation MOB_EFFECTS = Ids.vanilla("textures/atlas/mob_effects.png");

    private static TextureAtlas atlas;
    private static final Map<ResourceLocation, int[]> originals = new HashMap<>();

    private EffectIconTinter() {
    }

    public static void onStitched(TextureAtlas a) {
        if (!a.location().equals(MOB_EFFECTS)) return;
        atlas = a;
        originals.clear();
        for (ResourceLocation id : a.getTextureLocations()) {
            NativeImage img = a.getSprite(id).contents().getOriginalImage();
            int w = img.getWidth(), h = img.getHeight();
            int[] px = new int[w * h];
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++) px[y * w + x] = Colors.fromAbgr(img.getPixelRGBA(x, y));
            originals.put(id, px);
        }
        apply();
    }

    public static void apply() {
        if (atlas == null || originals.isEmpty()) return;
        boolean themed = PinkConfig.on(PinkConfig.THEME_EFFECT_ICONS);
        atlas.bind();
        for (Map.Entry<ResourceLocation, int[]> e : originals.entrySet()) {
            TextureAtlasSprite sprite = atlas.getSprite(e.getKey());
            NativeImage img = sprite.contents().getOriginalImage();
            int w = img.getWidth(), h = img.getHeight();
            if (w * h != e.getValue().length) continue;
            Canvas c = new Canvas(w, h, e.getValue().clone());
            if (themed) Recolor.effectIcon(c, Themes.palette());
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++) img.setPixelRGBA(x, y, Colors.toAbgr(c.px[y * w + x]));
            sprite.uploadFirstFrame();
        }
    }
}
