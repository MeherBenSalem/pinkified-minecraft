package com.sugarplumstudios.pinkified.platform;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.function.BiConsumer;

/** What the common code needs from the mod loader. One implementation per loader, found with {@link Services}. */
public interface PinkPlatform {
    String name();

    Path configDir();

    boolean isModLoaded(String modId);

    /** The key mapping that opens the Pinkified settings (unbound by default). */
    KeyMapping createSettingsKey(String name, KeyMapping.Category category);

    /** Label of the "Mods" button on the Pinkified main menu, or null when this loader has no mod list to open. */
    @Nullable
    Component modsButtonLabel();

    /** The loader's mod list screen, or null when there is none. */
    @Nullable
    Screen modListScreen(Screen parent);

    /**
     * The version lines the vanilla or loader title screen shows in its bottom-left corner, bottom line first
     * (index 0 is the line nearest the bottom edge).
     */
    void forEachBrandingLine(BiConsumer<Integer, String> consumer);

    /** Lines the loader shows above the copyright text in the bottom-right corner (index 0 is the lowest). */
    void forEachAboveCopyrightLine(BiConsumer<Integer, String> consumer);
}
