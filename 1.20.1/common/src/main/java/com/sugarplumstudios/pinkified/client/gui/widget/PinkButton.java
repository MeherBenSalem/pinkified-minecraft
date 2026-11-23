package com.sugarplumstudios.pinkified.client.gui.widget;

import com.sugarplumstudios.pinkified.client.gui.Anim;
import com.sugarplumstudios.pinkified.client.gui.PinkTextures;
import com.sugarplumstudios.pinkified.client.gui.RenderKit;
import com.sugarplumstudios.pinkified.client.sound.PinkSounds;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * A rounded pastel button that lifts, glows and shows a little heart when hovered, and
 * squishes when pressed. Optionally carries a 16x16 icon.
 */
public class PinkButton extends net.minecraft.client.gui.components.Button {
    @Nullable
    private final ResourceLocation icon;
    private float hover;
    private float press;
    private long last;
    /** Entrance animation: the button slides in after this delay. */
    private long appearAt;
    private float appearOffset;

    public PinkButton(int x, int y, int w, int h, Component message, OnPress onPress) {
        this(x, y, w, h, message, null, onPress);
    }

    public PinkButton(int x, int y, int w, int h, Component message, @Nullable ResourceLocation icon, OnPress onPress) {
        super(x, y, w, h, message, onPress, DEFAULT_NARRATION);
        this.icon = icon;
    }

    /** Makes the button slide in from the side once {@code delayMs} have passed. */
    public PinkButton entrance(long delayMs, float offset) {
        this.appearAt = Anim.millis() + delayMs;
        this.appearOffset = offset;
        return this;
    }

    @Override
    public void onPress() {
        press = 1f;
        super.onPress();
    }

    @Override
    public void playDownSound(SoundManager sm) {
        PinkSounds.click(sm);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long now = Anim.millis();
        float dt = last == 0 ? 0 : Math.min(0.1f, (now - last) / 1000f);
        last = now;
        float intensity = Anim.intensity();
        boolean hot = active && isHoveredOrFocused();
        hover = Anim.approach(hover, hot ? 1f : 0f, dt, 14f);
        press = Anim.approach(press, 0f, dt, 9f);

        float appear = 1f;
        if (appearAt != 0 && intensity > 0) appear = Anim.easeOutBack((now - appearAt) / 450f);
        if (appearAt != 0 && now < appearAt && intensity > 0) appear = 0f;
        float a = this.alpha * Anim.clamp01(appear * 1.4f);
        if (a <= 0.01f) return;

        Palette p = Themes.palette();
        int lift = Math.round(hover * 1.5f * Math.min(1f, intensity));
        float squish = 1f - press * 0.06f * Math.min(1f, intensity);
        int x = getX(), y = getY() - lift, w = width, h = height;

        g.pose().pushPose();
        float slide = (1f - appear) * appearOffset;
        g.pose().translate(x + w / 2f + slide, y + h / 2f, 0);
        g.pose().scale(squish, squish, 1f);
        g.pose().translate(-(x + w / 2f), -(y + h / 2f), 0);

        int radius = Math.min(4, h / 4);
        // shadow
        RenderKit.roundedRect(g, x, y + 2 + lift, w, h, radius, Colors.withAlpha(p.ink, Math.round(0x50 * a)));
        int top, bottom, border;
        if (!active) {
            top = Colors.lerp(p.shadow, p.deep, 0.35f);
            bottom = Colors.lerp(p.shadow, p.deep, 0.6f);
            border = p.deep;
        } else {
            top = Colors.lerp(Colors.lerp(p.slot, p.light, 0.25f), p.accentLight, hover);
            bottom = Colors.lerp(p.slot, p.accent, hover);
            border = Colors.lerp(p.shadow, p.accentDeep, hover);
        }
        RenderKit.roundedGradient(g, x, y, w, h, radius, Colors.fade(top, a), Colors.fade(bottom, a));
        RenderKit.roundedOutline(g, x, y, w, h, radius, Colors.fade(border, a));
        // glossy top highlight
        g.fill(x + radius, y + 1, x + w - radius, y + 2, Colors.withAlpha(0xFFFFFF, Math.round((0x70 + 0x40 * hover) * a)));

        Font font = Minecraft.getInstance().font;
        int textColor = active ? 0xFFFFFF : 0xD8C8D0;
        int shadow = Colors.lerp(p.ink, bottom, 0.25f);
        int textY = y + (h - 8) / 2;
        int iconW = icon != null ? 18 : 0;
        Component msg = getMessage();
        int textW = font.width(msg);
        int contentW = iconW + textW;
        int cx = x + w / 2;
        if (icon != null) {
            int ix = textW > 0 ? cx - contentW / 2 : cx - 8;
            RenderKit.sprite(g, icon, ix, y + (h - 16) / 2, 16, 16, 0xFFFFFFFF, a);
        }
        if (textW > 0) {
            int tx = cx - contentW / 2 + iconW;
            if (textW > w - 8 - iconW) {
                renderScrollingString(g, font, msg, x + 4 + iconW, y, x + w - 4, y + h, Colors.withAlpha(textColor, a));
            } else {
                g.drawString(font, msg, tx + 1, textY + 1, Colors.withAlpha(shadow, a), false);
                g.drawString(font, msg, tx, textY, Colors.withAlpha(textColor, a), false);
            }
        }
        // hover heart that pops in at the left edge of wide buttons
        if (hover > 0.02f && w >= 80 && icon == null) {
            float s = Anim.easeOutBack(hover) * (0.85f + 0.15f * Anim.pulse(0.9f, 0f));
            RenderKit.spriteTransformed(g, PinkTextures.HEART, x + 10, y + h / 2f, 9, 8, -12f, s * 0.9f, p.heart, hover * a);
            RenderKit.spriteTransformed(g, PinkTextures.SPARKLE_SMALL, x + w - 9, y + h / 2f - 2, 5, 5, 0, s, 0xFFFFFFFF, hover * a * Anim.pulse(0.7f, 0.3f));
        }
        g.pose().popPose();
    }
}
