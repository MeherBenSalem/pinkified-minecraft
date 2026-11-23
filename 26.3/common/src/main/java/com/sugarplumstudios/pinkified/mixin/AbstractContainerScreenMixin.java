package com.sugarplumstudios.pinkified.mixin;

import com.sugarplumstudios.pinkified.client.gui.ScreenHooks;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Container decorations (both loaders). A container screen draws its window texture in extractBackground, before
 * extractRenderState draws the slots and items, so the start of extractRenderState is right on top of the window.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;
    @Shadow
    @Final
    protected int imageWidth;
    @Shadow
    @Final
    protected int imageHeight;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void pinkified$windowDrawn(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ScreenHooks.onContainerBackground((AbstractContainerScreen<?>) (Object) this, graphics, leftPos, topPos, imageWidth, imageHeight);
    }
}
