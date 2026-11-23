package com.sugarplumstudios.pinkified.fabric.mixin;

import com.sugarplumstudios.pinkified.client.gui.ScreenHooks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's ContainerScreenEvent.Render.Background: runs right after a container screen drew its background texture. */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Inject(method = "renderBackground", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"))
    private void pinkified$backgroundDrawn(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ScreenHooks.onContainerBackground((AbstractContainerScreen<?>) (Object) this, graphics);
    }
}
