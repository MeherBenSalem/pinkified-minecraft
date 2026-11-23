package com.sugarplumstudios.pinkified.fabric.mixin;

import com.sugarplumstudios.pinkified.client.hud.HudHooks;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The overlays NeoForge registers above the crosshair and the experience level. */
@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "renderCrosshair", at = @At("RETURN"))
    private void pinkified$crosshair(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        HudHooks.renderCrosshair(graphics, graphics.guiWidth(), graphics.guiHeight());
    }

    @Inject(method = "renderExperienceLevel", at = @At("RETURN"))
    private void pinkified$experienceLevel(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        HudHooks.renderXpLevel(graphics, graphics.guiWidth(), graphics.guiHeight());
    }
}
