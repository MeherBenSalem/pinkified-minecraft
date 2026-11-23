package com.sugarplumstudios.pinkified.client.gui.widget;

import net.minecraft.client.input.InputWithModifiers;
import com.sugarplumstudios.pinkified.client.gui.Anim;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Cycles through the constants of an enum ("Crosshair: Heart"). Shift-click goes backwards.
 * An optional preview painter draws a small picture of the current value on the right.
 */
public class PinkCycle<E extends Enum<E>> extends AbstractButton {
    public interface Preview<E> {
        void draw(GuiGraphicsExtractor g, E value, int x, int y);
    }

    private final E[] values;
    private final Supplier<E> getter;
    private final Consumer<E> setter;
    private final Function<E, Component> names;
    private final Component label;
    private Preview<E> preview;
    private float hover;
    private long last;

    public PinkCycle(int x, int y, int w, int h, Component label, Component tooltip, E[] values, Supplier<E> getter,
                     Consumer<E> setter, Function<E, Component> names) {
        super(x, y, w, h, label);
        this.label = label;
        this.values = values;
        this.getter = getter;
        this.setter = setter;
        this.names = names;
        if (tooltip != null) setTooltip(Tooltip.create(tooltip));
    }

    public PinkCycle<E> withPreview(Preview<E> preview) {
        this.preview = preview;
        return this;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        int i = getter.get().ordinal();
        int n = values.length;
        int next = Screen.hasShiftDown() ? (i - 1 + n) % n : (i + 1) % n;
        setter.accept(values[next]);
    }

    @Override
    public void playDownSound(SoundManager sm) {
        PinkSounds.click(sm);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        long now = Anim.millis();
        float dt = last == 0 ? 0 : Math.min(0.1f, (now - last) / 1000f);
        last = now;
        hover = Anim.approach(hover, isHoveredOrFocused() ? 1f : 0f, dt, 14f);
        Palette p = Themes.palette();
        int x = getX(), y = getY(), w = width, h = height;
        RenderKit.roundedRect(g, x, y, w, h, 3, Colors.withAlpha(p.ink, Math.round(0x40 + 0x30 * hover)));
        if (hover > 0.01f) RenderKit.roundedOutline(g, x, y, w, h, 3, Colors.withAlpha(p.accentLight, Math.round(0xA0 * hover)));
        Font font = Minecraft.getInstance().font;
        int textY = y + (h - 8) / 2;
        RenderKit.drawString(g, font, label, x + 7, textY + 1, Colors.withAlpha(p.ink, 0xC0), false);
        RenderKit.drawString(g, font, label, x + 6, textY, 0xFFFFFF, false);

        E value = getter.get();
        Component name = names.apply(value);
        int previewW = preview != null ? 20 : 0;
        int pillW = font.width(name) + 16;
        int px = x + w - pillW - 5 - previewW, py = y + 3;
        RenderKit.roundedGradient(g, px, py, pillW, h - 6, 4, Colors.lerp(p.slot, p.accentLight, hover), Colors.lerp(p.shadow, p.accent, hover));
        RenderKit.roundedOutline(g, px, py, pillW, h - 6, 4, p.ink);
        RenderKit.drawString(g, font, "‹", px + 3, textY, Colors.withAlpha(0xFFFFFF, 0xB0), false);
        RenderKit.drawString(g, font, "›", px + pillW - 6, textY, Colors.withAlpha(0xFFFFFF, 0xB0), false);
        RenderKit.text(g, font, name, px + 8, textY, 0xFFFFFF, p.ink);
        if (preview != null) preview.draw(g, value, x + w - 21, y + (h - 16) / 2);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        out.add(NarratedElementType.TITLE, Component.empty().append(label).append(": ").append(names.apply(getter.get())));
    }
}
