package com.sugarplumstudios.pinkified.fabric.mixin;

import com.sugarplumstudios.pinkified.client.gui.ScreenHooks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge's ScreenEvent.BackgroundRendered: runs right after a screen drew its default background. */
@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void pinkified$backgroundRendered(GuiGraphics graphics, CallbackInfo ci) {
        ScreenHooks.onBackgroundRendered((Screen) (Object) this, graphics);
    }
}
