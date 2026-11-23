package com.sugarplumstudios.pinkified.fabric.mixin;

import com.sugarplumstudios.pinkified.client.hud.HudHooks;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge's overlays registered above the crosshair and the experience bar. */
@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "renderCrosshair", at = @At("RETURN"))
    private void pinkified$crosshair(GuiGraphics graphics, CallbackInfo ci) {
        HudHooks.renderCrosshair(graphics, graphics.guiWidth(), graphics.guiHeight());
    }

    @Inject(method = "renderExperienceBar", at = @At("RETURN"))
    private void pinkified$experienceLevel(GuiGraphics graphics, int x, CallbackInfo ci) {
        HudHooks.renderXpLevel(graphics, graphics.guiWidth(), graphics.guiHeight());
    }
}
