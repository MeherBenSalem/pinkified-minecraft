package com.sugarplumstudios.pinkified.client.texture;

import com.sugarplumstudios.pinkified.client.util.Ids;
import net.minecraft.resources.ResourceLocation;

/** Vanilla HUD sprite ids, used by the settings screen's HUD preview (they are themed in the atlas). */
public final class HudIcons {
    public static final ResourceLocation HEART_CONTAINER = Ids.vanilla("hud/heart/container");
    public static final ResourceLocation HEART_FULL = Ids.vanilla("hud/heart/full");
    public static final ResourceLocation HEART_HALF = Ids.vanilla("hud/heart/half");
    public static final ResourceLocation FOOD_EMPTY = Ids.vanilla("hud/food_empty");
    public static final ResourceLocation FOOD_FULL = Ids.vanilla("hud/food_full");
    public static final ResourceLocation FOOD_HALF = Ids.vanilla("hud/food_half");
    public static final ResourceLocation ARMOR_FULL = Ids.vanilla("hud/armor_full");
    public static final ResourceLocation ARMOR_HALF = Ids.vanilla("hud/armor_half");
    public static final ResourceLocation XP_BACKGROUND = Ids.vanilla("hud/experience_bar_background");
    public static final ResourceLocation XP_PROGRESS = Ids.vanilla("hud/experience_bar_progress");

    private HudIcons() {
    }
}
