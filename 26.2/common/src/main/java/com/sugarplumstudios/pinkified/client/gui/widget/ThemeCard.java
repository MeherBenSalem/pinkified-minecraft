package com.sugarplumstudios.pinkified.client.gui.widget;

import net.minecraft.client.input.InputWithModifiers;
import com.sugarplumstudios.pinkified.client.gui.Anim;
import com.sugarplumstudios.pinkified.client.gui.PinkTextures;
import com.sugarplumstudios.pinkified.client.gui.RenderKit;
import com.sugarplumstudios.pinkified.client.sound.PinkSounds;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.ThemeId;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/** A selectable card that previews a theme with its icon, name and colour swatches. */
public class ThemeCard extends AbstractButton {
    private final ThemeId theme;
    private final Consumer<ThemeId> onSelect;
    private float hover;
    private float selected;
    private long last;

    public ThemeCard(int x, int y, int w, int h, ThemeId theme, Consumer<ThemeId> onSelect) {
        super(x, y, w, h, Component.translatable(theme.translationKey()));
        this.theme = theme;
        this.onSelect = onSelect;
        setTooltip(Tooltip.create(Component.translatable(theme.descriptionKey())));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        onSelect.accept(theme);
    }

    @Override
    public void playDownSound(SoundManager sm) {
        PinkSounds.chime(sm, 1.0f + theme.ordinal() * 0.06f);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        long now = Anim.millis();
        float dt = last == 0 ? 0 : Math.min(0.1f, (now - last) / 1000f);
        last = now;
        boolean isSelected = Themes.id() == theme;
        hover = Anim.approach(hover, isHoveredOrFocused() ? 1f : 0f, dt, 14f);
        selected = Anim.approach(selected, isSelected ? 1f : 0f, dt, 10f);

        Palette own = Palette.of(theme, Themes.customPrimary(), Themes.customAccent());
        Palette ui = Themes.palette();
        int x = getX(), y = getY() - Math.round(hover * Math.min(1f, Anim.intensity())), w = width, h = height;

        RenderKit.roundedRect(g, x, y + 2, w, h, 4, Colors.withAlpha(ui.ink, 0x50));
        RenderKit.roundedGradient(g, x, y, w, h, 4, own.base, own.slot);
        int border = Colors.lerp(own.shadow, ui.light, Math.max(selected, hover * 0.6f));
        RenderKit.roundedOutline(g, x, y, w, h, 4, border);
        if (selected > 0.01f) RenderKit.roundedOutline(g, x + 1, y + 1, w - 2, h - 2, 3, Colors.withAlpha(ui.light, Math.round(0xC0 * selected)));

        RenderKit.sprite(g, PinkTextures.themeIcon(theme), x + 5, y + (h - 16) / 2 - 3, 16, 16, 0xFFFFFFFF, 1f);

        Font font = Minecraft.getInstance().font;
        Component name = getMessage();
        int maxW = w - 28;
        int textX = x + 24, textY = y + 6;
        if (font.width(name) > maxW) {
            RenderKit.scrollingText(g, font, name, textX, textY - 1, textX + maxW, textY + 9, own.ink);
        } else {
            RenderKit.drawString(g, font, name, textX, textY, own.ink, false);
        }
        int[] swatches = {own.heart, own.accent, own.slot, own.light};
        for (int i = 0; i < swatches.length; i++) {
            int sx = textX + i * 9, sy = y + h - 12;
            RenderKit.roundedRect(g, sx, sy, 7, 7, 2, swatches[i]);
            RenderKit.roundedOutline(g, sx, sy, 7, 7, 2, own.ink);
        }
        if (selected > 0.02f) {
            float s = Anim.easeOutBack(selected);
            RenderKit.spriteTransformed(g, PinkTextures.HEART, x + w - 9, y + 8, 9, 8, 0, s, own.heart, selected);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        defaultButtonNarrationText(out);
    }
}
