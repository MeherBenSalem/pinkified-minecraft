package com.sugarplumstudios.pinkified.mixin;

import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.gui.ScreenHooks;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The pastel game-over screen (both loaders): Pinkified draws the whole screen, and vanilla's red backdrop is skipped. */
@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void pinkified$render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (ScreenHooks.onRenderPre((DeathScreen) (Object) this, graphics, mouseX, mouseY, partialTick)) ci.cancel();
    }

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void pinkified$background(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (PinkConfig.on(PinkConfig.THEME_DEATH_SCREEN)) ci.cancel();
    }
}
