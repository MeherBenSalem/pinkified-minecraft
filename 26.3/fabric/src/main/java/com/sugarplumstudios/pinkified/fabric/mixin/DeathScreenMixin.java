package com.sugarplumstudios.pinkified.fabric.mixin;

import com.sugarplumstudios.pinkified.client.gui.ScreenHooks;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge's cancellable ScreenEvent.Render.Pre, for the one screen Pinkified draws itself: the game-over screen. */
@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void pinkified$render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (ScreenHooks.onRenderPre((DeathScreen) (Object) this, graphics, mouseX, mouseY, partialTick)) ci.cancel();
    }
}
