package com.sugarplumstudios.pinkified.fabric.mixin;

import com.sugarplumstudios.pinkified.client.gui.ScreenHooks;
import com.sugarplumstudios.pinkified.client.gui.TooltipPainter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge's RenderTooltipEvent.Color: draws item tooltips in the theme colours. Fabric has no API for other mods'
 * tooltip colours, so every tooltip drawn with the vanilla background is themed.
 */
@Mixin(TooltipRenderUtil.class)
public abstract class TooltipRenderUtilMixin {
    @Inject(method = "renderTooltipBackground", at = @At("HEAD"), cancellable = true)
    private static void pinkified$themedBackground(GuiGraphics graphics, int x, int y, int width, int height, int z, CallbackInfo ci) {
        ScreenHooks.TooltipColors colors = ScreenHooks.tooltipColors(ScreenHooks.vanillaBackground(),
                ScreenHooks.vanillaBorderStart(), ScreenHooks.vanillaBorderEnd());
        if (colors == null) return;
        TooltipPainter.paint(graphics, x, y, width, height, z, colors);
        ci.cancel();
    }
}
