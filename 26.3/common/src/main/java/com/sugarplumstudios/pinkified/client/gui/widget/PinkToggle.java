package com.sugarplumstudios.pinkified.client.gui.widget;

import net.minecraft.client.input.InputWithModifiers;
import com.sugarplumstudios.pinkified.client.gui.Anim;
import com.sugarplumstudios.pinkified.client.gui.PinkTextures;
import com.sugarplumstudios.pinkified.client.gui.RenderKit;
import com.sugarplumstudios.pinkified.client.sound.PinkSounds;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** A labelled on/off switch with a sliding heart knob. */
public class PinkToggle extends AbstractButton {
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private float knob;
    private float hover;
    private long last;

    public PinkToggle(int x, int y, int w, int h, Component label, Component tooltip, BooleanSupplier getter, Consumer<Boolean> setter) {
        super(x, y, w, h, label);
        this.getter = getter;
        this.setter = setter;
        this.knob = getter.getAsBoolean() ? 1f : 0f;
        if (tooltip != null) setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        setter.accept(!getter.getAsBoolean());
    }

    @Override
    public void playDownSound(SoundManager sm) {
        PinkSounds.chime(sm, getter.getAsBoolean() ? 0.9f : 1.15f);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        long now = Anim.millis();
        float dt = last == 0 ? 0 : Math.min(0.1f, (now - last) / 1000f);
        last = now;
        boolean on = getter.getAsBoolean();
        knob = Anim.approach(knob, on ? 1f : 0f, dt, 16f);
        hover = Anim.approach(hover, isHoveredOrFocused() ? 1f : 0f, dt, 14f);
        Palette p = Themes.palette();
        int x = getX(), y = getY(), w = width, h = height;

        // row background
        RenderKit.roundedRect(g, x, y, w, h, 3, Colors.withAlpha(p.ink, Math.round(0x40 + 0x30 * hover)));
        if (hover > 0.01f) RenderKit.roundedOutline(g, x, y, w, h, 3, Colors.withAlpha(p.accentLight, Math.round(0xA0 * hover)));

        Font font = Minecraft.getInstance().font;
        int textY = y + (h - 8) / 2;
        int maxText = w - 44;
        Component label = getMessage();
        if (font.width(label) > maxText) {
            RenderKit.scrollingText(g, font, label, x + 6, y, x + 6 + maxText, y + h, 0xFFFFFF);
        } else {
            RenderKit.drawString(g, font, label, x + 7, textY + 1, Colors.withAlpha(p.ink, 0xC0), false);
            RenderKit.drawString(g, font, label, x + 6, textY, 0xFFFFFF, false);
        }

        // the switch
        int sw = 28, sh = 14;
        int sx = x + w - sw - 5, sy = y + (h - sh) / 2;
        int trackTop = Colors.lerp(p.shadow, p.accent, knob);
        int trackBottom = Colors.lerp(p.deep, p.accentDeep, knob);
        RenderKit.roundedGradient(g, sx, sy, sw, sh, 5, trackTop, trackBottom);
        RenderKit.roundedOutline(g, sx, sy, sw, sh, 5, p.ink);
        int kx = Math.round(sx + 2 + knob * (sw - 14));
        RenderKit.roundedRect(g, kx, sy + 2, 10, 10, 3, p.light);
        RenderKit.roundedOutline(g, kx, sy + 2, 10, 10, 3, Colors.lerp(p.shadow, p.accentDeep, knob));
        if (knob > 0.05f) {
            RenderKit.spriteTransformed(g, PinkTextures.HEART, kx + 5f, sy + 7f, 9, 8, 0, 0.62f * knob, p.heart, knob);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        out.add(NarratedElementType.TITLE, Component.empty().append(getMessage()).append(": ")
                .append(getter.getAsBoolean() ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
        if (active) {
            out.add(NarratedElementType.USAGE, Component.translatable(isFocused() ? "narration.button.usage.focused" : "narration.button.usage.hovered"));
        }
    }
}
