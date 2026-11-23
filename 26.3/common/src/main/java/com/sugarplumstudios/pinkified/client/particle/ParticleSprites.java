package com.sugarplumstudios.pinkified.client.particle;

import com.sugarplumstudios.pinkified.client.util.Ids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.HashMap;
import java.util.Map;

/**
 * Looks up Pinkified's particle sprites. Vanilla's particle atlas stitches every texture under
 * {@code textures/particle/}, so no particle types need to be registered (which keeps the mod
 * client-only and compatible with servers that do not have it).
 */
public final class ParticleSprites {
    private static final Map<String, TextureAtlasSprite> CACHE = new HashMap<>();

    private ParticleSprites() {
    }

    public static TextureAtlasSprite get(String name) {
        TextureAtlasSprite cached = CACHE.get(name);
        if (cached != null) return cached;
        TextureAtlas atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES);
        TextureAtlasSprite sprite = atlas.getSprite(Ids.mod(name));
        if (sprite == null) sprite = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
        CACHE.put(name, sprite);
        return sprite;
    }

    /** Called after the particle atlas is rebuilt (resource reload). */
    public static void invalidate() {
        CACHE.clear();
    }
}
