package com.sugarplumstudios.pinkified.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import com.sugarplumstudios.pinkified.platform.PinkPlatform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.function.BiConsumer;

public final class FabricPlatform implements PinkPlatform {
    private static final Logger LOGGER = LoggerFactory.getLogger("pinkified");
    private static final String MOD_MENU = "modmenu";

    @Override
    public String name() {
        return "Fabric";
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public KeyMapping createSettingsKey(String name, KeyMapping.Category category) {
        return new KeyMapping(name, InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), category);
    }

    /** Fabric has no mod list of its own; Mod Menu's is offered when it is installed. */
    @Override
    public Component modsButtonLabel() {
        return isModLoaded(MOD_MENU) ? Component.translatable("pinkified.menu.mods") : null;
    }

    @Override
    public Screen modListScreen(Screen parent) {
        if (!isModLoaded(MOD_MENU)) return null;
        try {
            return (Screen) Class.forName("com.terraformersmc.modmenu.gui.ModsScreen").getConstructor(Screen.class).newInstance(parent);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            LOGGER.warn("Pinkified could not open the Mod Menu screen", e);
            return null;
        }
    }

    /** The version line the vanilla title screen draws in its bottom-left corner. */
    @Override
    public void forEachBrandingLine(BiConsumer<Integer, String> consumer) {
        Minecraft mc = Minecraft.getInstance();
        String line = "Minecraft " + SharedConstants.getCurrentVersion().name();
        if (mc.isDemo()) line = line + " Demo";
        if (Minecraft.checkModStatus().shouldReportAsModified()) line = line + I18n.get("menu.modded");
        consumer.accept(0, line);
    }

    @Override
    public void forEachAboveCopyrightLine(BiConsumer<Integer, String> consumer) {
    }
}
