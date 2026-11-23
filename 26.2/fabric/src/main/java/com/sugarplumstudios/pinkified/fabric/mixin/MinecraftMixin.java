package com.sugarplumstudios.pinkified.fabric.mixin;

import com.sugarplumstudios.pinkified.client.gui.ScreenHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Forge's ScreenEvent.Opening: lets Pinkified swap the screen that is about to open (the title screen). */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    public ClientLevel level;

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen pinkified$onOpening(Screen screen) {
        // vanilla opens a new title screen when asked for no screen outside a world; decide on it here
        if (screen == null && level == null) screen = new TitleScreen();
        return screen == null ? null : ScreenHooks.onOpening(screen);
    }
}
