package com.sugarplumstudios.pinkified.client;

import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.dev.DevSnapshots;
import com.sugarplumstudios.pinkified.client.gui.screen.PinkSettingsScreen;
import com.sugarplumstudios.pinkified.client.particle.AmbientEffects;
import com.sugarplumstudios.pinkified.client.particle.ParticleSprites;
import com.sugarplumstudios.pinkified.client.sound.MusicHooks;
import com.sugarplumstudios.pinkified.client.texture.EffectIconTinter;
import com.sugarplumstudios.pinkified.client.texture.ThemedTextures;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.platform.Services;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;

/**
 * Client entry point shared by every loader. The loader projects call {@link #init()} once and forward their events
 * (client tick, atlas stitching, screen and HUD hooks) to the hook classes.
 */
public final class PinkifiedClient {
    public static final KeyMapping OPEN_SETTINGS = Services.PLATFORM.createSettingsKey("key.pinkified.settings", "key.categories.pinkified");

    private static int configCheck;

    private PinkifiedClient() {
    }

    /** Reads the config. Safe to call before the game window exists. */
    public static void init() {
        PinkConfig.load(Services.PLATFORM.configDir());
        Themes.refresh();
    }

    /** Re-reads the config and refreshes every themed asset. Must run on the render thread. */
    public static void applySettings() {
        Themes.refresh();
        ThemedTextures.INSTANCE.apply();
        EffectIconTinter.apply();
    }

    /** Called when any texture atlas has been stitched and uploaded. */
    public static void onAtlasStitched(TextureAtlas atlas) {
        if (atlas.location().equals(TextureAtlas.LOCATION_PARTICLES)) ParticleSprites.invalidate();
        Themes.refresh();
        EffectIconTinter.onStitched(atlas);
    }

    /** End of every client tick. */
    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_SETTINGS.consumeClick()) {
            if (!(mc.screen instanceof PinkSettingsScreen)) mc.setScreen(new PinkSettingsScreen(mc.screen));
        }
        // the file watcher: a hand-edited config applies without a restart
        if (++configCheck >= 40) {
            configCheck = 0;
            if (PinkConfig.reloadIfChanged()) applySettings();
        }
        MusicHooks.onClientTick();
        AmbientEffects.onClientTick();
        if (DevSnapshots.enabled()) DevSnapshots.onClientTick();
    }
}
