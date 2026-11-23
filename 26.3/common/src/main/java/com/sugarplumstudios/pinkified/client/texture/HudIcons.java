package com.sugarplumstudios.pinkified.client.texture;

import com.sugarplumstudios.pinkified.client.util.Ids;
import net.minecraft.resources.Identifier;

/** Vanilla HUD sprite ids, used by the settings screen's HUD preview (they are themed in the atlas). */
public final class HudIcons {
    public static final Identifier HEART_CONTAINER = Ids.vanilla("hud/heart/container");
    public static final Identifier HEART_FULL = Ids.vanilla("hud/heart/full");
    public static final Identifier HEART_HALF = Ids.vanilla("hud/heart/half");
    public static final Identifier FOOD_EMPTY = Ids.vanilla("hud/food_empty");
    public static final Identifier FOOD_FULL = Ids.vanilla("hud/food_full");
    public static final Identifier FOOD_HALF = Ids.vanilla("hud/food_half");
    public static final Identifier ARMOR_FULL = Ids.vanilla("hud/armor_full");
    public static final Identifier ARMOR_HALF = Ids.vanilla("hud/armor_half");
    public static final Identifier XP_BACKGROUND = Ids.vanilla("hud/experience_bar_background");
    public static final Identifier XP_PROGRESS = Ids.vanilla("hud/experience_bar_progress");

    private HudIcons() {
    }
}
