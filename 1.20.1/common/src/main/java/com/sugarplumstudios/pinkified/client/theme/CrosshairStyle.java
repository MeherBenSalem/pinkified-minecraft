package com.sugarplumstudios.pinkified.client.theme;

import java.util.Locale;

public enum CrosshairStyle {
    VANILLA,
    HEART,
    SPARKLE,
    FLOWER,
    BOW,
    DOT,
    CROSS;

    public String translationKey() {
        return "pinkified.crosshair." + name().toLowerCase(Locale.ROOT);
    }
}
