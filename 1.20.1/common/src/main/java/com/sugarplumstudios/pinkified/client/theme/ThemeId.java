package com.sugarplumstudios.pinkified.client.theme;

import java.util.Locale;

/** The selectable visual themes. {@link #CUSTOM} is built from the player's own colours. */
public enum ThemeId {
    STRAWBERRY_MILK,
    LAVENDER_DREAMS,
    PINK_COQUETTE,
    CHERRY_BLOSSOM,
    ANGELCORE,
    PASTEL_PRINCESS,
    CUSTOM;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "pinkified.theme." + id();
    }

    public String descriptionKey() {
        return "pinkified.theme." + id() + ".desc";
    }

    /** Path of the theme's icon inside {@code assets/pinkified/textures/}. */
    public String iconPath() {
        return "textures/gui/icons/theme_" + id() + ".png";
    }
}
