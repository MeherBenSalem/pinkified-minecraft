package com.sugarplumstudios.pinkified.client.config;

import com.sugarplumstudios.pinkified.client.theme.CrosshairStyle;
import com.sugarplumstudios.pinkified.client.theme.FoodStyle;
import com.sugarplumstudios.pinkified.client.theme.ThemeId;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Client config ({@code config/pinkified-client.toml}). Every value can also be changed live
 * from the in-game Pinkified settings screen. The file is shared by every loader and keeps the
 * layout of the original Forge config, so existing files keep their settings.
 */
public final class PinkConfig {
    public static final String FILE_NAME = "pinkified-client.toml";

    /** Every value, used by "reset to defaults" and by the file reader and writer. */
    private static final List<ConfigFile.Value<?>> ALL = new ArrayList<>();
    private static final List<ConfigFile.Section> SECTIONS = new ArrayList<>();

    private static String section;

    public static final ConfigFile.EnumValue<ThemeId> THEME;
    public static final ConfigFile.StringValue CUSTOM_PRIMARY;
    public static final ConfigFile.StringValue CUSTOM_ACCENT;

    public static final ConfigFile.BoolValue THEME_CONTAINERS;
    public static final ConfigFile.BoolValue THEME_WIDGETS;
    public static final ConfigFile.BoolValue MENU_BACKGROUNDS;
    public static final ConfigFile.BoolValue CUSTOM_MAIN_MENU;
    public static final ConfigFile.BoolValue CONTAINER_DECORATIONS;
    public static final ConfigFile.BoolValue THEME_TOOLTIPS;
    public static final ConfigFile.BoolValue PINK_GLINT;
    public static final ConfigFile.BoolValue THEME_DEATH_SCREEN;
    public static final ConfigFile.BoolValue PAUSE_MENU_BUTTON;

    public static final ConfigFile.BoolValue THEME_HEARTS;
    public static final ConfigFile.BoolValue THEME_FOOD;
    public static final ConfigFile.EnumValue<FoodStyle> FOOD_STYLE;
    public static final ConfigFile.BoolValue THEME_ARMOR;
    public static final ConfigFile.BoolValue THEME_XP;
    public static final ConfigFile.BoolValue THEME_HOTBAR;
    public static final ConfigFile.BoolValue THEME_EFFECT_ICONS;
    public static final ConfigFile.EnumValue<CrosshairStyle> CROSSHAIR;

    public static final ConfigFile.BoolValue PARTICLES;
    public static final ConfigFile.BoolValue AMBIENT_SPARKLES;
    public static final ConfigFile.BoolValue CHERRY_BLOSSOMS;
    public static final ConfigFile.BoolValue HEAL_HEARTS;
    public static final ConfigFile.BoolValue FLOWER_STEPS;
    public static final ConfigFile.BoolValue LEVEL_UP_SPARKLES;
    public static final ConfigFile.BoolValue PINK_ENCHANT_PARTICLES;
    public static final ConfigFile.BoolValue MENU_PETALS;
    public static final ConfigFile.DoubleValue PARTICLE_DENSITY;

    public static final ConfigFile.BoolValue ANIMATIONS;
    public static final ConfigFile.DoubleValue ANIMATION_INTENSITY;
    public static final ConfigFile.BoolValue SCREEN_TRANSITIONS;
    public static final ConfigFile.BoolValue MENU_MUSIC;
    public static final ConfigFile.BoolValue UI_SOUNDS;

    private static final ConfigFile FILE;

    static {
        push("theme", "Theme selection");
        THEME = track(new ConfigFile.EnumValue<>(section, "theme",
                "Active theme. CUSTOM builds a palette from customPrimary and customAccent.", ThemeId.STRAWBERRY_MILK));
        CUSTOM_PRIMARY = track(new ConfigFile.StringValue(section, "customPrimary",
                "Primary colour of the CUSTOM theme, as #RRGGBB.", "#F48FB1"));
        CUSTOM_ACCENT = track(new ConfigFile.StringValue(section, "customAccent",
                "Accent colour of the CUSTOM theme (hover states, highlights), as #RRGGBB.", "#B39DDB"));

        push("interface", "Menus and inventories");
        THEME_CONTAINERS = bool("themeContainers",
                "Recolour inventories, chests, furnaces, anvils, enchanting tables, the creative menu and other containers.", true);
        THEME_WIDGETS = bool("themeWidgets",
                "Recolour buttons, sliders, checkboxes, lists and the title logo, with softly rounded corners.", true);
        MENU_BACKGROUNDS = bool("menuBackgrounds",
                "Replace dirt menu backgrounds with an animated pastel sky, and tint in-game menu backdrops.", true);
        CUSTOM_MAIN_MENU = bool("customMainMenu", "Use the Pinkified main menu instead of the vanilla title screen.", true);
        CONTAINER_DECORATIONS = bool("containerDecorations", "Add a little bow and heart to container windows.", true);
        THEME_TOOLTIPS = bool("themeTooltips",
                "Draw default item tooltips in theme colours (custom tooltip colours from other mods are kept).", true);
        PINK_GLINT = bool("pinkGlint", "Tint the enchantment glint in the theme colour.", true);
        THEME_DEATH_SCREEN = bool("themeDeathScreen", "Use a soft pastel game-over screen.", true);
        PAUSE_MENU_BUTTON = bool("pauseMenuButton", "Show a heart button in the pause menu that opens Pinkified settings.", true);

        push("hud", "Heads-up display");
        THEME_HEARTS = bool("themeHearts",
                "Themed health hearts (including hardcore, absorption, poison, wither, frozen, mount) and air bubbles.", true);
        THEME_FOOD = bool("themeFood", "Replace hunger drumsticks with treats.", true);
        FOOD_STYLE = track(new ConfigFile.EnumValue<>(section, "foodStyle", "Which treat to draw. AUTO follows the theme.", FoodStyle.AUTO));
        THEME_ARMOR = bool("themeArmor", "Themed armor icons.", true);
        THEME_XP = bool("themeXpBar", "Themed experience bar, level number and horse jump bar.", true);
        THEME_HOTBAR = bool("themeHotbar", "Themed hotbar and selection frame.", true);
        THEME_EFFECT_ICONS = bool("themeEffectIcons", "Pastel status-effect icons and effect frames.", true);
        CROSSHAIR = track(new ConfigFile.EnumValue<>(section, "crosshair",
                "Crosshair shape. VANILLA keeps the original inverted crosshair.", CrosshairStyle.HEART));

        push("effects", "Cosmetic particles (client-side only, nobody else sees them)");
        PARTICLES = bool("particles", "Master switch for all Pinkified particles.", true);
        AMBIENT_SPARKLES = bool("ambientSparkles", "Occasional sparkles twinkling around you.", true);
        CHERRY_BLOSSOMS = bool("cherryBlossoms", "Cherry blossom petals drifting down around you outdoors.", true);
        HEAL_HEARTS = bool("healHearts", "Little hearts float up when you heal.", true);
        FLOWER_STEPS = bool("flowerSteps", "Tiny flowers pop up where you walk on grass and dirt.", true);
        LEVEL_UP_SPARKLES = bool("levelUpSparkles", "A burst of sparkles when you gain an experience level.", true);
        PINK_ENCHANT_PARTICLES = bool("pinkEnchantParticles", "Enchanting table glyphs glow in the theme colour.", true);
        MENU_PETALS = bool("menuPetals", "Floating petals and sparkles in menus.", true);
        PARTICLE_DENSITY = track(new ConfigFile.DoubleValue(section, "particleDensity",
                "Multiplier for how many particles spawn. Vanilla's particle setting is also respected.", 1.0D, 0.1D, 2.0D));

        push("motion", "Motion and sound");
        ANIMATIONS = bool("animations", "Animated buttons, logo and menu backgrounds.", true);
        ANIMATION_INTENSITY = track(new ConfigFile.DoubleValue(section, "animationIntensity", "How lively animations are.", 1.0D, 0.1D, 2.0D));
        SCREEN_TRANSITIONS = bool("screenTransitions", "Soft fade when opening menus.", true);
        MENU_MUSIC = bool("menuMusic",
                "Play the Sugarplum Lullaby in menus instead of the vanilla menu music. Uses the Music volume slider.", true);
        UI_SOUNDS = bool("uiSounds", "Gentle chime sounds on Pinkified buttons.", true);

        FILE = new ConfigFile(SECTIONS, ALL);
    }

    private static void push(String name, String comment) {
        section = name;
        SECTIONS.add(new ConfigFile.Section(name, comment));
    }

    private static ConfigFile.BoolValue bool(String key, String comment, boolean def) {
        return track(new ConfigFile.BoolValue(section, key, comment, def));
    }

    private static <T extends ConfigFile.Value<?>> T track(T value) {
        ALL.add(value);
        return value;
    }

    private PinkConfig() {
    }

    /** Reads the config file (writing a fresh one when it is missing or incomplete). */
    public static void load(Path configDir) {
        FILE.load(configDir.resolve(FILE_NAME));
    }

    /** True when the file was edited by hand since it was last read or written; the new values are read in. */
    public static boolean reloadIfChanged() {
        return FILE.reloadIfChanged();
    }

    /** Reads a value, falling back to its default if the config file is not loaded yet. */
    public static <T> T get(ConfigFile.Value<T> value) {
        return value.get();
    }

    public static boolean on(ConfigFile.BoolValue value) {
        return value.get();
    }

    public static <T> void set(ConfigFile.Value<T> value, T newValue) {
        value.set(newValue);
    }

    public static void save() {
        FILE.save();
    }

    public static void resetAll() {
        for (ConfigFile.Value<?> v : ALL) v.reset();
    }
}
