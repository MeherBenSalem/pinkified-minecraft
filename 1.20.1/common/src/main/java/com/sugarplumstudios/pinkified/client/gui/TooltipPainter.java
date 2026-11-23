package com.sugarplumstudios.pinkified.client.gui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Draws a tooltip background with vanilla's shape in the given colours, the way Forge's tooltip colour event does.
 * Used by loaders that have no such event.
 */
public final class TooltipPainter {
    private TooltipPainter() {
    }

    public static void paint(GuiGraphics g, int x, int y, int width, int height, int z, ScreenHooks.TooltipColors c) {
        int left = x - 3;
        int top = y - 3;
        int w = width + 6;
        int h = height + 6;
        g.fillGradient(left, top - 1, left + w, top, z, c.backgroundStart(), c.backgroundStart());
        g.fillGradient(left, top + h, left + w, top + h + 1, z, c.backgroundEnd(), c.backgroundEnd());
        g.fillGradient(left, top, left + w, top + h, z, c.backgroundStart(), c.backgroundEnd());
        g.fillGradient(left - 1, top, left, top + h, z, c.backgroundStart(), c.backgroundEnd());
        g.fillGradient(left + w, top, left + w + 1, top + h, z, c.backgroundStart(), c.backgroundEnd());
        // the frame
        int fy = top + 1;
        g.fillGradient(left, fy, left + 1, fy + h - 2, z, c.borderStart(), c.borderEnd());
        g.fillGradient(left + w - 1, fy, left + w, fy + h - 2, z, c.borderStart(), c.borderEnd());
        g.fillGradient(left, fy - 1, left + w, fy, z, c.borderStart(), c.borderStart());
        g.fillGradient(left, fy + h - 2, left + w, fy + h - 1, z, c.borderEnd(), c.borderEnd());
    }
}
