package com.sugarplumstudios.pinkified.forge;

import com.mojang.blaze3d.platform.InputConstants;
import com.sugarplumstudios.pinkified.platform.PinkPlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.internal.BrandingControl;

import java.nio.file.Path;
import java.util.function.BiConsumer;

public final class ForgePlatform implements PinkPlatform {
    @Override
    public String name() {
        return "Forge";
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
    public KeyMapping createSettingsKey(String name, String category) {
        return new KeyMapping(name, KeyConflictContext.UNIVERSAL, InputConstants.UNKNOWN, category);
    }

    @Override
    public Component modsButtonLabel() {
        return Component.translatable("fml.menu.mods");
    }

    @Override
    public Screen modListScreen(Screen parent) {
        return new ModListScreen(parent);
    }

    @Override
    public void forEachBrandingLine(BiConsumer<Integer, String> consumer) {
        // Forge requires its version lines to stay visible on the main menu
        BrandingControl.forEachLine(true, true, consumer);
    }

    @Override
    public void forEachAboveCopyrightLine(BiConsumer<Integer, String> consumer) {
        BrandingControl.forEachAboveCopyrightLine(consumer);
    }

    @Override
    public int[] listBounds(AbstractSelectionList<?> list) {
        return new int[]{list.getLeft(), list.getRight(), list.getTop(), list.getBottom()};
    }
}
