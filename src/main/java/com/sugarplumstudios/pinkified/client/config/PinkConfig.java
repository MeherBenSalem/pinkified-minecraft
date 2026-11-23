package com.sugarplumstudios.pinkified.client.config;

import com.sugarplumstudios.pinkified.client.theme.CrosshairStyle;
import com.sugarplumstudios.pinkified.client.theme.FoodStyle;
import com.sugarplumstudios.pinkified.client.theme.ThemeId;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Client config ({@code config/pinkified-client.toml}). Every value can also be changed live
 * from the in-game Pinkified settings screen.
 */
public final class PinkConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.EnumValue<ThemeId> THEME;
    public static final ForgeConfigSpec.ConfigValue<String> CUSTOM_PRIMARY;
    public static final ForgeConfigSpec.ConfigValue<String> CUSTOM_ACCENT;

    public static final ForgeConfigSpec.BooleanValue THEME_CONTAINERS;
    public static final ForgeConfigSpec.BooleanValue THEME_WIDGETS;
    public static final ForgeConfigSpec.BooleanValue MENU_BACKGROUNDS;
    public static final ForgeConfigSpec.BooleanValue CUSTOM_MAIN_MENU;
    public static final ForgeConfigSpec.BooleanValue CONTAINER_DECORATIONS;
    public static final ForgeConfigSpec.BooleanValue THEME_TOOLTIPS;
    public static final ForgeConfigSpec.BooleanValue PINK_GLINT;
    public static final ForgeConfigSpec.BooleanValue THEME_DEATH_SCREEN;
    public static final ForgeConfigSpec.BooleanValue PAUSE_MENU_BUTTON;

    public static final ForgeConfigSpec.BooleanValue THEME_HEARTS;
    public static final ForgeConfigSpec.BooleanValue THEME_FOOD;
    public static final ForgeConfigSpec.EnumValue<FoodStyle> FOOD_STYLE;
    public static final ForgeConfigSpec.BooleanValue THEME_ARMOR;
    public static final ForgeConfigSpec.BooleanValue THEME_XP;
    public static final ForgeConfigSpec.BooleanValue THEME_HOTBAR;
    public static final ForgeConfigSpec.BooleanValue THEME_EFFECT_ICONS;
    public static final ForgeConfigSpec.EnumValue<CrosshairStyle> CROSSHAIR;

    public static final ForgeConfigSpec.BooleanValue PARTICLES;
    public static final ForgeConfigSpec.BooleanValue AMBIENT_SPARKLES;
    public static final ForgeConfigSpec.BooleanValue CHERRY_BLOSSOMS;
    public static final ForgeConfigSpec.BooleanValue HEAL_HEARTS;
    public static final ForgeConfigSpec.BooleanValue FLOWER_STEPS;
    public static final ForgeConfigSpec.BooleanValue LEVEL_UP_SPARKLES;
    public static final ForgeConfigSpec.BooleanValue PINK_ENCHANT_PARTICLES;
    public static final ForgeConfigSpec.BooleanValue MENU_PETALS;
    public static final ForgeConfigSpec.DoubleValue PARTICLE_DENSITY;

    public static final ForgeConfigSpec.BooleanValue ANIMATIONS;
    public static final ForgeConfigSpec.DoubleValue ANIMATION_INTENSITY;
    public static final ForgeConfigSpec.BooleanValue SCREEN_TRANSITIONS;
    public static final ForgeConfigSpec.BooleanValue MENU_MUSIC;
    public static final ForgeConfigSpec.BooleanValue UI_SOUNDS;

    /** Every value, used by "reset to defaults". */
    private static final List<ForgeConfigSpec.ConfigValue<?>> ALL = new ArrayList<>();

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Theme selection").push("theme");
        THEME = track(b.comment("Active theme. CUSTOM builds a palette from customPrimary and customAccent.")
                .defineEnum("theme", ThemeId.STRAWBERRY_MILK));
        CUSTOM_PRIMARY = track(b.comment("Primary colour of the CUSTOM theme, as #RRGGBB.")
                .define("customPrimary", "#F48FB1"));
        CUSTOM_ACCENT = track(b.comment("Accent colour of the CUSTOM theme (hover states, highlights), as #RRGGBB.")
                .define("customAccent", "#B39DDB"));
        b.pop();

        b.comment("Menus and inventories").push("interface");
        THEME_CONTAINERS = track(b.comment("Recolour inventories, chests, furnaces, anvils, enchanting tables, the creative menu and other containers.")
                .define("themeContainers", true));
        THEME_WIDGETS = track(b.comment("Recolour buttons, sliders, checkboxes, lists and the title logo, with softly rounded corners.")
                .define("themeWidgets", true));
        MENU_BACKGROUNDS = track(b.comment("Replace dirt menu backgrounds with an animated pastel sky, and tint in-game menu backdrops.")
                .define("menuBackgrounds", true));
        CUSTOM_MAIN_MENU = track(b.comment("Use the Pinkified main menu instead of the vanilla title screen.")
                .define("customMainMenu", true));
        CONTAINER_DECORATIONS = track(b.comment("Add a little bow and heart to container windows.")
                .define("containerDecorations", true));
        THEME_TOOLTIPS = track(b.comment("Draw default item tooltips in theme colours (custom tooltip colours from other mods are kept).")
                .define("themeTooltips", true));
        PINK_GLINT = track(b.comment("Tint the enchantment glint in the theme colour.")
                .define("pinkGlint", true));
        THEME_DEATH_SCREEN = track(b.comment("Use a soft pastel game-over screen.")
                .define("themeDeathScreen", true));
        PAUSE_MENU_BUTTON = track(b.comment("Show a heart button in the pause menu that opens Pinkified settings.")
                .define("pauseMenuButton", true));
        b.pop();

        b.comment("Heads-up display").push("hud");
        THEME_HEARTS = track(b.comment("Themed health hearts (including hardcore, absorption, poison, wither, frozen, mount) and air bubbles.")
                .define("themeHearts", true));
        THEME_FOOD = track(b.comment("Replace hunger drumsticks with treats.")
                .define("themeFood", true));
        FOOD_STYLE = track(b.comment("Which treat to draw. AUTO follows the theme.")
                .defineEnum("foodStyle", FoodStyle.AUTO));
        THEME_ARMOR = track(b.comment("Themed armor icons.")
                .define("themeArmor", true));
        THEME_XP = track(b.comment("Themed experience bar, level number and horse jump bar.")
                .define("themeXpBar", true));
        THEME_HOTBAR = track(b.comment("Themed hotbar and selection frame.")
                .define("themeHotbar", true));
        THEME_EFFECT_ICONS = track(b.comment("Pastel status-effect icons and effect frames.")
                .define("themeEffectIcons", true));
        CROSSHAIR = track(b.comment("Crosshair shape. VANILLA keeps the original inverted crosshair.")
                .defineEnum("crosshair", CrosshairStyle.HEART));
        b.pop();

        b.comment("Cosmetic particles (client-side only, nobody else sees them)").push("effects");
        PARTICLES = track(b.comment("Master switch for all Pinkified particles.")
                .define("particles", true));
        AMBIENT_SPARKLES = track(b.comment("Occasional sparkles twinkling around you.")
                .define("ambientSparkles", true));
        CHERRY_BLOSSOMS = track(b.comment("Cherry blossom petals drifting down around you outdoors.")
                .define("cherryBlossoms", true));
        HEAL_HEARTS = track(b.comment("Little hearts float up when you heal.")
                .define("healHearts", true));
        FLOWER_STEPS = track(b.comment("Tiny flowers pop up where you walk on grass and dirt.")
                .define("flowerSteps", true));
        LEVEL_UP_SPARKLES = track(b.comment("A burst of sparkles when you gain an experience level.")
                .define("levelUpSparkles", true));
        PINK_ENCHANT_PARTICLES = track(b.comment("Enchanting table glyphs glow in the theme colour.")
                .define("pinkEnchantParticles", true));
        MENU_PETALS = track(b.comment("Floating petals and sparkles in menus.")
                .define("menuPetals", true));
        PARTICLE_DENSITY = track(b.comment("Multiplier for how many particles spawn. Vanilla's particle setting is also respected.")
                .defineInRange("particleDensity", 1.0D, 0.1D, 2.0D));
        b.pop();

        b.comment("Motion and sound").push("motion");
        ANIMATIONS = track(b.comment("Animated buttons, logo and menu backgrounds.")
                .define("animations", true));
        ANIMATION_INTENSITY = track(b.comment("How lively animations are.")
                .defineInRange("animationIntensity", 1.0D, 0.1D, 2.0D));
        SCREEN_TRANSITIONS = track(b.comment("Soft fade when opening menus.")
                .define("screenTransitions", true));
        MENU_MUSIC = track(b.comment("Play the Sugarplum Lullaby in menus instead of the vanilla menu music. Uses the Music volume slider.")
                .define("menuMusic", true));
        UI_SOUNDS = track(b.comment("Gentle chime sounds on Pinkified buttons.")
                .define("uiSounds", true));
        b.pop();

        SPEC = b.build();
    }

    private static <T extends ForgeConfigSpec.ConfigValue<?>> T track(T value) {
        ALL.add(value);
        return value;
    }

    private PinkConfig() {
    }

    /** Reads a value, falling back to its default if the config file is not loaded yet. */
    public static <T> T get(ForgeConfigSpec.ConfigValue<T> value) {
        try {
            return value.get();
        } catch (IllegalStateException notLoaded) {
            return value.getDefault();
        }
    }

    public static boolean on(ForgeConfigSpec.BooleanValue value) {
        return get(value);
    }

    public static <T> void set(ForgeConfigSpec.ConfigValue<T> value, T newValue) {
        try {
            value.set(newValue);
        } catch (IllegalStateException ignored) {
            // config not loaded yet; nothing to write to
        }
    }

    public static void save() {
        try {
            SPEC.save();
        } catch (IllegalStateException ignored) {
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void resetAll() {
        for (ForgeConfigSpec.ConfigValue v : ALL) set(v, v.getDefault());
    }
}
