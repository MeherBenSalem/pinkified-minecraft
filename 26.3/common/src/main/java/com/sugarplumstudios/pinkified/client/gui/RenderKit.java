package com.sugarplumstudios.pinkified.client.gui;

import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.math.Axis;
import com.sugarplumstudios.pinkified.client.util.Colors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

/** Drawing helpers for Pinkified's rounded, pastel interface elements. */
public final class RenderKit {
    private RenderKit() {
    }

    /** Horizontal inset of each row near a corner, for pixel-art rounding of radius 1..5. */
    private static final int[][] INSETS = {
            {},
            {1},
            {2, 1},
            {3, 1, 1},
            {4, 2, 1, 1},
            {5, 3, 2, 1, 1},
    };

    private static int[] insets(int radius) {
        return INSETS[Math.max(0, Math.min(INSETS.length - 1, radius))];
    }

    public static void roundedRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int color) {
        roundedGradient(g, x, y, w, h, radius, color, color);
    }

    /** Rounded rectangle with a vertical gradient from {@code top} to {@code bottom}. */
    public static void roundedGradient(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int top, int bottom) {
        if (w <= 0 || h <= 0) return;
        int[] in = insets(Math.min(radius, Math.min(w, h) / 2));
        int r = in.length;
        for (int i = 0; i < r; i++) {
            int c0 = Colors.lerp(top, bottom, i / (float) Math.max(1, h - 1));
            int c1 = Colors.lerp(top, bottom, (h - 1 - i) / (float) Math.max(1, h - 1));
            g.fill(x + in[i], y + i, x + w - in[i], y + i + 1, c0);
            g.fill(x + in[i], y + h - 1 - i, x + w - in[i], y + h - i, c1);
        }
        if (h - 2 * r > 0) {
            if (top == bottom) g.fill(x, y + r, x + w, y + h - r, top);
            else g.fillGradient(x, y + r, x + w, y + h - r, Colors.lerp(top, bottom, r / (float) h), Colors.lerp(top, bottom, (h - r) / (float) h));
        }
    }

    /** One-pixel rounded outline. */
    public static void roundedOutline(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int color) {
        if (w <= 1 || h <= 1) return;
        int[] in = insets(Math.min(radius, Math.min(w, h) / 2));
        int r = in.length;
        if (r == 0) {
            g.outline(x, y, w, h, color);
            return;
        }
        g.fill(x + in[0], y, x + w - in[0], y + 1, color);
        g.fill(x + in[0], y + h - 1, x + w - in[0], y + h, color);
        g.fill(x, y + r, x + 1, y + h - r, color);
        g.fill(x + w - 1, y + r, x + w, y + h - r, color);
        for (int i = 1; i < r; i++) {
            int a = in[i], prev = in[i - 1];
            // left and right edge pixels that connect one row's inset to the next
            g.fill(x + a, y + i, x + Math.max(a + 1, prev), y + i + 1, color);
            g.fill(x + w - Math.max(a + 1, prev), y + i, x + w - a, y + i + 1, color);
            g.fill(x + a, y + h - 1 - i, x + Math.max(a + 1, prev), y + h - i, color);
            g.fill(x + w - Math.max(a + 1, prev), y + h - 1 - i, x + w - a, y + h - i, color);
        }
    }

    /** A soft drop shadow made of stacked translucent rounded rects. */
    public static void softShadow(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int color, int spread) {
        for (int i = spread; i >= 1; i--) {
            int c = Colors.fade(color, 0.35f / i);
            roundedRect(g, x - i + 1, y - i + 2, w + 2 * i - 2, h + 2 * i - 2, radius + i, c);
        }
    }

    /** Draws a whole texture at its natural size, tinted (RGB) and faded (alpha). */
    public static void sprite(GuiGraphicsExtractor g, Identifier tex, int x, int y, int w, int h, int tint, float alpha) {
        if (alpha <= 0.003f) return;
        g.setColor(Colors.red(tint) / 255f, Colors.green(tint) / 255f, Colors.blue(tint) / 255f, alpha);
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0, 0, w, h, w, h);
        g.setColor(1f, 1f, 1f, 1f);
    }

    /** Sprite centred on (cx, cy), rotated by {@code degrees} and scaled. */
    public static void spriteTransformed(GuiGraphicsExtractor g, Identifier tex, float cx, float cy, int w, int h,
                                         float degrees, float scale, int tint, float alpha) {
        if (alpha <= 0.003f || scale <= 0.01f) return;
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        if (degrees != 0) g.pose().rotate((float) Math.toRadians(degrees));
        g.pose().scale(scale, scale);
        g.pose().translate(-w / 2f, -h / 2f);
        sprite(g, tex, 0, 0, w, h, tint, alpha);
        g.pose().popMatrix();
    }

    /** Text centred at x with a soft coloured shadow instead of vanilla's grey one. */
    public static void centeredText(GuiGraphicsExtractor g, Font font, Component text, int cx, int y, int color, int shadow) {
        FormattedCharSequence seq = text.getVisualOrderText();
        int x = cx - font.width(seq) / 2;
        RenderKit.drawString(g, font, seq, x + 1, y + 1, shadow, false);
        RenderKit.drawString(g, font, seq, x, y, color, false);
    }

    public static void text(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int color, int shadow) {
        FormattedCharSequence seq = text.getVisualOrderText();
        RenderKit.drawString(g, font, seq, x + 1, y + 1, shadow, false);
        RenderKit.drawString(g, font, seq, x, y, color, false);
    }

    /** Text with a full one-pixel outline, as used on the experience level number. */
    public static void outlinedText(GuiGraphicsExtractor g, Font font, String s, int x, int y, int color, int outline) {
        RenderKit.drawString(g, font, s, x + 1, y, outline, false);
        RenderKit.drawString(g, font, s, x - 1, y, outline, false);
        RenderKit.drawString(g, font, s, x, y + 1, outline, false);
        RenderKit.drawString(g, font, s, x, y - 1, outline, false);
        RenderKit.drawString(g, font, s, x, y, color, false);
    }
}
