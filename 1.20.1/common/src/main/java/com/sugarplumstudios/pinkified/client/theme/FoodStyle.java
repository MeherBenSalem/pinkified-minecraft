package com.sugarplumstudios.pinkified.client.theme;

/** The treat drawn in place of the vanilla drumstick on the hunger bar. */
public enum FoodStyle {
    /** Use the treat that belongs to the active theme. */
    AUTO,
    STRAWBERRY,
    MACARON,
    DONUT,
    DANGO,
    CUPCAKE,
    BONBON,
    /** Keep the vanilla drumsticks. */
    VANILLA;

    public String translationKey() {
        return "pinkified.food." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
