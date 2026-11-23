package com.sugarplumstudios.pinkified.client.theme;

import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.util.Colors;

/** Holds the palette currently in use, rebuilt from config whenever settings change. */
public final class Themes {
    private static Palette current = Palette.STRAWBERRY_MILK;
    private static ThemeId currentId = ThemeId.STRAWBERRY_MILK;

    private Themes() {
    }

    public static Palette palette() {
        return current;
    }

    public static ThemeId id() {
        return currentId;
    }

    public static int customPrimary() {
        return Colors.parseHex(PinkConfig.get(PinkConfig.CUSTOM_PRIMARY), 0xFFF48FB1);
    }

    public static int customAccent() {
        return Colors.parseHex(PinkConfig.get(PinkConfig.CUSTOM_ACCENT), 0xFFB39DDB);
    }

    public static void refresh() {
        currentId = PinkConfig.get(PinkConfig.THEME);
        current = Palette.of(currentId, customPrimary(), customAccent());
    }
}
