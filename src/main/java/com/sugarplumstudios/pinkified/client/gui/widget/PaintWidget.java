package com.sugarplumstudios.pinkified.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** A non-interactive widget that just paints something (section headers, previews). */
public class PaintWidget extends AbstractWidget {
    public interface Painter {
        void paint(GuiGraphics g, int x, int y, int w, int h);
    }

    private final Painter painter;

    public PaintWidget(int x, int y, int w, int h, Component narration, Painter painter) {
        super(x, y, w, h, narration);
        this.painter = painter;
        this.active = false;
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        painter.paint(g, getX(), getY(), width, height);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        if (!getMessage().getString().isEmpty()) out.add(NarratedElementType.TITLE, getMessage());
    }
}
