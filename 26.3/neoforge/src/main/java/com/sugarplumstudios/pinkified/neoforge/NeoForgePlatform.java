package com.sugarplumstudios.pinkified.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import com.sugarplumstudios.pinkified.platform.PinkPlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.modlist.ModListScreen;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.internal.BrandingControl;

import java.nio.file.Path;
import java.util.function.BiConsumer;

public final class NeoForgePlatform implements PinkPlatform {
    @Override
    public String name() {
        return "NeoForge";
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public KeyMapping createSettingsKey(String name, KeyMapping.Category category) {
        KeyMapping key = new KeyMapping(name, InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), category);
        key.setKeyConflictContext(KeyConflictContext.UNIVERSAL);
        return key;
    }

    @Override
    public Component modsButtonLabel() {
        return Component.translatable("fml.menu.mods");
    }

    @Override
    public Screen modListScreen(Screen parent) {
        return ModListScreen.create(parent);
    }

    @Override
    public void forEachBrandingLine(BiConsumer<Integer, String> consumer) {
        // NeoForge's version lines stay visible on the main menu
        BrandingControl.forEachLine(true, true, consumer);
    }

    @Override
    public void forEachAboveCopyrightLine(BiConsumer<Integer, String> consumer) {
        BrandingControl.forEachAboveCopyrightLine(consumer);
    }
}
