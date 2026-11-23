package com.sugarplumstudios.pinkified.fabric.mixin;

import com.sugarplumstudios.pinkified.client.gui.ScreenHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** NeoForge's ScreenEvent.Opening: lets Pinkified swap the screen that is about to open (the title screen). */
@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen pinkified$onOpening(Screen screen) {
        // vanilla opens a new title screen when asked for no screen outside a world; decide on it here
        if (screen == null && minecraft.level == null) screen = new TitleScreen();
        return screen == null ? null : ScreenHooks.onOpening(screen);
    }
}
