package com.sugarplumstudios.pinkified.client;

import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.dev.SelfTest;
import com.sugarplumstudios.pinkified.client.gui.screen.PinkSettingsScreen;
import com.sugarplumstudios.pinkified.client.particle.AmbientEffects;
import com.sugarplumstudios.pinkified.client.sound.MusicHooks;
import com.sugarplumstudios.pinkified.client.texture.GuiSpriteThemer;
import com.sugarplumstudios.pinkified.client.texture.ThemedTextures;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Ids;
import com.sugarplumstudios.pinkified.platform.Services;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Client entry point shared by every loader. The loader projects call {@link #init()} once and forward their events
 * (client tick, resource reloads, screen and HUD hooks) to the hook classes.
 */
public final class PinkifiedClient {
    public static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(Ids.mod("pinkified"));
    public static final KeyMapping OPEN_SETTINGS = Services.PLATFORM.createSettingsKey("key.pinkified.settings", KEY_CATEGORY);

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
        GuiSpriteThemer.apply();
    }

    /** End of every client tick. */
    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_SETTINGS.consumeClick()) {
            if (!(mc.gui.screen() instanceof PinkSettingsScreen)) mc.gui.setScreen(new PinkSettingsScreen(mc.gui.screen()));
        }
        // the file watcher: a hand-edited config applies without a restart
        if (++configCheck >= 40) {
            configCheck = 0;
            if (PinkConfig.reloadIfChanged()) applySettings();
        }
        // the GUI atlases are themed in place, again after every resource reload
        GuiSpriteThemer.tick();
        MusicHooks.onClientTick();
        AmbientEffects.onClientTick();
        if (SelfTest.enabled()) SelfTest.onClientTick();
    }
}
