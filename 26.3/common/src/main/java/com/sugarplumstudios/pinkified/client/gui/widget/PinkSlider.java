package com.sugarplumstudios.pinkified.client.gui.widget;

import com.sugarplumstudios.pinkified.client.gui.Anim;
import com.sugarplumstudios.pinkified.client.gui.PinkTextures;
import com.sugarplumstudios.pinkified.client.gui.RenderKit;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;

/**
 * A slider with a rounded track, a gradient fill and a heart-shaped knob. Values are mapped
 * from [0,1] to [min,max]. An optional colour function paints the track (used for hue pickers).
 */
public class PinkSlider extends AbstractSliderButton {
    private final Component label;
    private final double min, max;
    private final DoubleConsumer setter;
    private final DoubleFunction<Component> format;
    private IntUnaryOperator trackColor;
    private float hover;
    private long last;

    public PinkSlider(int x, int y, int w, int h, Component label, double min, double max, DoubleSupplier getter,
                      DoubleConsumer setter, DoubleFunction<Component> format) {
        super(x, y, w, h, Component.empty(), (getter.getAsDouble() - min) / (max - min));
        this.label = label;
        this.min = min;
        this.max = max;
        this.setter = setter;
        this.format = format;
        updateMessage();
    }

    /** Paints the track with {@code colorAt(percent 0..100)} instead of the theme gradient. */
    public PinkSlider withTrack(IntUnaryOperator colorAt) {
        this.trackColor = colorAt;
        return this;
    }

    public PinkSlider withTooltip(Component tooltip) {
        setTooltip(Tooltip.create(tooltip));
        return this;
    }

    public double realValue() {
        return min + (max - min) * value;
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.empty().append(label).append(": ").append(format.apply(realValue())));
    }

    @Override
    protected void applyValue() {
        setter.accept(realValue());
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        long now = Anim.millis();
        float dt = last == 0 ? 0 : Math.min(0.1f, (now - last) / 1000f);
        last = now;
        hover = Anim.approach(hover, isHoveredOrFocused() ? 1f : 0f, dt, 14f);
        Palette p = Themes.palette();
        int x = getX(), y = getY(), w = width, h = height;
        RenderKit.roundedRect(g, x, y, w, h, 3, Colors.withAlpha(p.ink, Math.round(0x40 + 0x30 * hover)));
        if (hover > 0.01f) RenderKit.roundedOutline(g, x, y, w, h, 3, Colors.withAlpha(p.accentLight, Math.round(0xA0 * hover)));

        Font font = Minecraft.getInstance().font;
        RenderKit.drawString(g, font, getMessage(), x + 7, y + 4, Colors.withAlpha(p.ink, 0xC0), false);
        RenderKit.drawString(g, font, getMessage(), x + 6, y + 3, 0xFFFFFF, false);

        int tx = x + 6, tw = w - 12, ty = y + h - 8, th = 5;
        RenderKit.roundedRect(g, tx, ty, tw, th, 2, p.deep);
        int fillW = (int) Math.round(tw * value);
        if (trackColor != null) {
            for (int i = 0; i < tw; i++) {
                int c = trackColor.applyAsInt(i * 100 / Math.max(1, tw - 1));
                g.fill(tx + i, ty + 1, tx + i + 1, ty + th - 1, c);
            }
        } else if (fillW > 0) {
            for (int i = 0; i < fillW; i++) {
                int c = Colors.lerp(p.accent, p.heart, i / (float) Math.max(1, tw));
                g.fill(tx + i, ty + 1, tx + i + 1, ty + th - 1, c);
            }
        }
        RenderKit.roundedOutline(g, tx, ty, tw, th, 2, p.ink);
        float kx = tx + (float) (tw * value);
        float scale = 1f + 0.15f * hover;
        RenderKit.spriteTransformed(g, PinkTextures.HEART, kx, ty + 2.5f, 9, 8, 0, scale, 0xFFFFFFFF, 1f);
        RenderKit.spriteTransformed(g, PinkTextures.HEART, kx, ty + 2.5f, 9, 8, 0, scale * 0.7f, p.heart, 1f);
    }
}
