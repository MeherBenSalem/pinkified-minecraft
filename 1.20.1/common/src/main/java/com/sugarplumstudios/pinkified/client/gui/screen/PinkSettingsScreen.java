package com.sugarplumstudios.pinkified.client.gui.screen;

import com.sugarplumstudios.pinkified.client.PinkifiedClient;
import com.sugarplumstudios.pinkified.client.config.ConfigFile;
import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.gui.Anim;
import com.sugarplumstudios.pinkified.client.gui.MenuBackdrop;
import com.sugarplumstudios.pinkified.client.gui.PinkTextures;
import com.sugarplumstudios.pinkified.client.gui.RenderKit;
import com.sugarplumstudios.pinkified.client.gui.widget.PaintWidget;
import com.sugarplumstudios.pinkified.client.gui.widget.PinkButton;
import com.sugarplumstudios.pinkified.client.gui.widget.PinkCycle;
import com.sugarplumstudios.pinkified.client.gui.widget.PinkSlider;
import com.sugarplumstudios.pinkified.client.gui.widget.PinkToggle;
import com.sugarplumstudios.pinkified.client.gui.widget.ThemeCard;
import com.sugarplumstudios.pinkified.client.texture.ThemedTextures;
import com.sugarplumstudios.pinkified.client.theme.CrosshairStyle;
import com.sugarplumstudios.pinkified.client.theme.FoodStyle;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.ThemeId;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import com.sugarplumstudios.pinkified.client.util.Ids;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;

/** The Pinkified settings screen. Every change previews live; the config is saved on close. */
public class PinkSettingsScreen extends Screen {
    private enum Tab {
        THEME(PinkTextures.TAB_THEME),
        INTERFACE(PinkTextures.TAB_INTERFACE),
        HUD(PinkTextures.TAB_HUD),
        EFFECTS(PinkTextures.TAB_EFFECTS),
        MOTION(PinkTextures.TAB_MOTION);

        final ResourceLocation icon;

        Tab(ResourceLocation icon) {
            this.icon = icon;
        }

        Component title() {
            return Component.translatable("pinkified.settings.tab." + name().toLowerCase(java.util.Locale.ROOT));
        }
    }

    private static final ResourceLocation ICONS = Ids.vanilla("textures/gui/icons.png");
    private static Tab lastTab = Tab.THEME;

    private final Screen parent;
    private Tab tab = lastTab;
    private final List<AbstractWidget> content = new ArrayList<>();
    private final List<Integer> contentBaseY = new ArrayList<>();
    private final List<PinkButton> tabButtons = new ArrayList<>();
    private int px, py, pw, ph;
    private int cx, cy, cw, ch;
    private int scroll, maxScroll;
    private boolean dirty;
    private long lastApply;
    private final long openedAt = Anim.millis();

    public PinkSettingsScreen(Screen parent) {
        super(Component.translatable("pinkified.settings.title"));
        this.parent = parent;
    }

    /** Opens the screen on a specific tab (0 = Theme ... 4 = Motion and Sound). */
    public static PinkSettingsScreen atTab(Screen parent, int tabIndex) {
        PinkSettingsScreen s = new PinkSettingsScreen(parent);
        Tab[] tabs = Tab.values();
        s.tab = tabs[Math.floorMod(tabIndex, tabs.length)];
        return s;
    }

    // ------------------------------------------------------------------ layout

    @Override
    protected void init() {
        pw = Math.min(width - 16, 460);
        ph = Math.min(height - 16, 280);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        int sidebar = pw >= 360 ? 118 : 26;
        cx = px + sidebar + 10;
        cy = py + 30;
        cw = pw - sidebar - 20;
        ch = ph - 30 - 34;

        tabButtons.clear();
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            Tab t = tabs[i];
            Component label = sidebar > 30 ? t.title() : Component.empty();
            PinkButton b = addRenderableWidget(new PinkButton(px + 6, py + 30 + i * 24, sidebar - 6, 20, label, t.icon, btn -> switchTab(t)));
            b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(t.title()));
            tabButtons.add(b);
        }
        addRenderableWidget(new PinkButton(px + 6, py + ph - 26, Math.max(60, sidebar - 6), 20,
                Component.translatable("pinkified.settings.reset"), b -> {
            PinkConfig.resetAll();
            changed();
            rebuildContent();
        }));
        addRenderableWidget(new PinkButton(px + pw - 106, py + ph - 26, 100, 20, CommonComponents.GUI_DONE, b -> onClose()));
        rebuildContent();
    }

    private void switchTab(Tab t) {
        if (tab == t) return;
        tab = t;
        lastTab = t;
        scroll = 0;
        rebuildContent();
    }

    private void rebuildContent() {
        for (AbstractWidget w : content) removeWidget(w);
        content.clear();
        contentBaseY.clear();
        Builder b = new Builder();
        switch (tab) {
            case THEME -> buildTheme(b);
            case INTERFACE -> buildInterface(b);
            case HUD -> buildHud(b);
            case EFFECTS -> buildEffects(b);
            case MOTION -> buildMotion(b);
        }
        maxScroll = Math.max(0, b.y - cy - ch + 4);
        scroll = Math.min(scroll, maxScroll);
        layoutContent();
    }

    private void layoutContent() {
        for (int i = 0; i < content.size(); i++) {
            AbstractWidget w = content.get(i);
            int y = contentBaseY.get(i) - scroll;
            w.setY(y);
            w.visible = y + w.getHeight() > cy && y < cy + ch;
        }
    }

    /** Places content widgets top to bottom. */
    private final class Builder {
        int y = cy + 2;

        <T extends AbstractWidget> T add(T w, int height) {
            w.setY(y);
            content.add(w);
            contentBaseY.add(y);
            addWidget(w);
            y += height;
            return w;
        }

        void header(String key) {
            Component text = Component.translatable(key);
            add(new PaintWidget(cx, y, cw, 14, text, (g, x, yy, w, h) -> {
                Palette p = Themes.palette();
                RenderKit.spriteTransformed(g, PinkTextures.HEART, x + 5, yy + 6, 9, 8, 0, 0.8f, p.heart, 1f);
                RenderKit.text(g, font, text, x + 13, yy + 2, p.light, p.ink);
                g.fill(x + 15 + font.width(text), yy + 6, x + w, yy + 7, Colors.withAlpha(p.light, 0x40));
            }), 16);
        }

        void toggle(String key, ConfigFile.BoolValue value) {
            add(new PinkToggle(cx, y, cw, 20, Component.translatable(key), Component.translatable(key + ".tooltip"),
                    () -> PinkConfig.on(value), v -> {
                PinkConfig.set(value, v);
                changed();
            }), 23);
        }

        void gap(int px) {
            y += px;
        }
    }

    private void buildTheme(Builder b) {
        b.header("pinkified.settings.section.themes");
        ThemeId[] ids = ThemeId.values();
        int cols = cw >= 300 ? 3 : 2;
        int gapX = 6;
        int cardW = (cw - gapX * (cols - 1)) / cols, cardH = 34;
        int rowY = b.y;
        for (int i = 0; i < ids.length; i++) {
            ThemeId id = ids[i];
            int col = i % cols, row = i / cols;
            ThemeCard card = new ThemeCard(cx + col * (cardW + gapX), 0, cardW, cardH, id, chosen -> {
                PinkConfig.set(PinkConfig.THEME, chosen);
                changed();
                rebuildContent();
            });
            card.setY(rowY + row * (cardH + 6));
            content.add(card);
            contentBaseY.add(rowY + row * (cardH + 6));
            addWidget(card);
        }
        b.y = rowY + ((ids.length + cols - 1) / cols) * (cardH + 6) + 4;
        if (Themes.id() == ThemeId.CUSTOM) {
            b.header("pinkified.settings.section.custom");
            float[] prim = Colors.toHsl(Themes.customPrimary());
            float[] acc = Colors.toHsl(Themes.customAccent());
            b.add(new PinkSlider(cx, b.y, cw, 24, Component.translatable("pinkified.settings.custom.primary_hue"), 0, 359,
                    () -> Colors.toHsl(Themes.customPrimary())[0], v -> setCustom(true, (float) v, -1),
                    v -> Component.literal(Math.round(v) + "°"))
                    .withTrack(pct -> Colors.fromHsl(pct * 3.59f, Math.max(0.35f, prim[1]), 0.72f)), 27);
            b.add(new PinkSlider(cx, b.y, cw, 24, Component.translatable("pinkified.settings.custom.primary_saturation"), 10, 100,
                    () -> Colors.toHsl(Themes.customPrimary())[1] * 100, v -> setCustom(true, -1, (float) v / 100f),
                    v -> Component.literal(Math.round(v) + "%"))
                    .withTrack(pct -> Colors.fromHsl(Colors.toHsl(Themes.customPrimary())[0], 0.1f + pct * 0.009f, 0.72f)), 27);
            b.add(new PinkSlider(cx, b.y, cw, 24, Component.translatable("pinkified.settings.custom.accent_hue"), 0, 359,
                    () -> Colors.toHsl(Themes.customAccent())[0], v -> setCustom(false, (float) v, -1),
                    v -> Component.literal(Math.round(v) + "°"))
                    .withTrack(pct -> Colors.fromHsl(pct * 3.59f, Math.max(0.35f, acc[1]), 0.72f)), 27);
            b.add(new PinkSlider(cx, b.y, cw, 24, Component.translatable("pinkified.settings.custom.accent_saturation"), 10, 100,
                    () -> Colors.toHsl(Themes.customAccent())[1] * 100, v -> setCustom(false, -1, (float) v / 100f),
                    v -> Component.literal(Math.round(v) + "%"))
                    .withTrack(pct -> Colors.fromHsl(Colors.toHsl(Themes.customAccent())[0], 0.1f + pct * 0.009f, 0.72f)), 27);
        }
        b.header("pinkified.settings.section.preview");
        b.add(new PaintWidget(cx, b.y, cw, 30, Component.empty(), this::paintHudPreview), 34);
    }

    private void setCustom(boolean primary, float hue, float sat) {
        int current = primary ? Themes.customPrimary() : Themes.customAccent();
        float[] hsl = Colors.toHsl(current);
        float h = hue >= 0 ? hue : hsl[0];
        float s = sat >= 0 ? sat : Math.max(0.1f, hsl[1]);
        String hex = Colors.toHex(Colors.fromHsl(h, s, 0.74f));
        PinkConfig.set(primary ? PinkConfig.CUSTOM_PRIMARY : PinkConfig.CUSTOM_ACCENT, hex);
        Themes.refresh();
        requestApply();
    }

    private void buildInterface(Builder b) {
        b.header("pinkified.settings.section.menus");
        b.toggle("pinkified.option.themeWidgets", PinkConfig.THEME_WIDGETS);
        b.toggle("pinkified.option.menuBackgrounds", PinkConfig.MENU_BACKGROUNDS);
        b.toggle("pinkified.option.customMainMenu", PinkConfig.CUSTOM_MAIN_MENU);
        b.toggle("pinkified.option.pauseMenuButton", PinkConfig.PAUSE_MENU_BUTTON);
        b.toggle("pinkified.option.themeDeathScreen", PinkConfig.THEME_DEATH_SCREEN);
        b.header("pinkified.settings.section.inventories");
        b.toggle("pinkified.option.themeContainers", PinkConfig.THEME_CONTAINERS);
        b.toggle("pinkified.option.containerDecorations", PinkConfig.CONTAINER_DECORATIONS);
        b.toggle("pinkified.option.themeTooltips", PinkConfig.THEME_TOOLTIPS);
        b.toggle("pinkified.option.pinkGlint", PinkConfig.PINK_GLINT);
    }

    private void buildHud(Builder b) {
        b.header("pinkified.settings.section.preview");
        b.add(new PaintWidget(cx, b.y, cw, 30, Component.empty(), this::paintHudPreview), 34);
        b.header("pinkified.settings.section.hud");
        b.toggle("pinkified.option.themeHearts", PinkConfig.THEME_HEARTS);
        b.toggle("pinkified.option.themeFood", PinkConfig.THEME_FOOD);
        b.add(new PinkCycle<>(cx, b.y, cw, 20, Component.translatable("pinkified.option.foodStyle"),
                Component.translatable("pinkified.option.foodStyle.tooltip"), FoodStyle.values(),
                () -> PinkConfig.get(PinkConfig.FOOD_STYLE), v -> {
            PinkConfig.set(PinkConfig.FOOD_STYLE, v);
            changed();
        }, v -> Component.translatable(v.translationKey()))
                .withPreview((g, v, x, y) -> {
                    g.pose().pushPose();
                    g.pose().translate(x + 1, y + 1, 0);
                    g.pose().scale(1.5f, 1.5f, 1f);
                    g.blit(ICONS, 0, 0, 52, 27, 9, 9);
                    g.pose().popPose();
                }), 23);
        b.toggle("pinkified.option.themeArmor", PinkConfig.THEME_ARMOR);
        b.toggle("pinkified.option.themeXpBar", PinkConfig.THEME_XP);
        b.toggle("pinkified.option.themeHotbar", PinkConfig.THEME_HOTBAR);
        b.toggle("pinkified.option.themeEffectIcons", PinkConfig.THEME_EFFECT_ICONS);
        b.add(new PinkCycle<>(cx, b.y, cw, 20, Component.translatable("pinkified.option.crosshair"),
                Component.translatable("pinkified.option.crosshair.tooltip"), CrosshairStyle.values(),
                () -> PinkConfig.get(PinkConfig.CROSSHAIR), v -> {
            PinkConfig.set(PinkConfig.CROSSHAIR, v);
            changed();
        }, v -> Component.translatable(v.translationKey()))
                .withPreview((g, v, x, y) -> {
                    if (v == CrosshairStyle.VANILLA) {
                        g.fill(x + 7, y + 3, x + 8, y + 13, 0xFFFFFFFF);
                        g.fill(x + 3, y + 7, x + 13, y + 8, 0xFFFFFFFF);
                    } else {
                        int c = ThemedTextures.CROSSHAIR_CELL;
                        g.blit(ThemedTextures.CROSSHAIRS, x, y, v.ordinal() * c, 0, c, c, c * CrosshairStyle.values().length, c);
                    }
                }), 23);
    }

    private void buildEffects(Builder b) {
        b.header("pinkified.settings.section.particles");
        b.toggle("pinkified.option.particles", PinkConfig.PARTICLES);
        b.toggle("pinkified.option.ambientSparkles", PinkConfig.AMBIENT_SPARKLES);
        b.toggle("pinkified.option.cherryBlossoms", PinkConfig.CHERRY_BLOSSOMS);
        b.toggle("pinkified.option.healHearts", PinkConfig.HEAL_HEARTS);
        b.toggle("pinkified.option.flowerSteps", PinkConfig.FLOWER_STEPS);
        b.toggle("pinkified.option.levelUpSparkles", PinkConfig.LEVEL_UP_SPARKLES);
        b.toggle("pinkified.option.pinkEnchantParticles", PinkConfig.PINK_ENCHANT_PARTICLES);
        b.toggle("pinkified.option.menuPetals", PinkConfig.MENU_PETALS);
        b.add(new PinkSlider(cx, b.y, cw, 24, Component.translatable("pinkified.option.particleDensity"), 0.1, 2.0,
                () -> PinkConfig.get(PinkConfig.PARTICLE_DENSITY), v -> PinkConfig.set(PinkConfig.PARTICLE_DENSITY, Math.round(v * 20) / 20.0),
                v -> Component.literal(Math.round(v * 100) + "%"))
                .withTooltip(Component.translatable("pinkified.option.particleDensity.tooltip")), 27);
    }

    private void buildMotion(Builder b) {
        b.header("pinkified.settings.section.motion");
        b.toggle("pinkified.option.animations", PinkConfig.ANIMATIONS);
        b.add(new PinkSlider(cx, b.y, cw, 24, Component.translatable("pinkified.option.animationIntensity"), 0.1, 2.0,
                () -> PinkConfig.get(PinkConfig.ANIMATION_INTENSITY), v -> PinkConfig.set(PinkConfig.ANIMATION_INTENSITY, Math.round(v * 20) / 20.0),
                v -> Component.literal(Math.round(v * 100) + "%"))
                .withTooltip(Component.translatable("pinkified.option.animationIntensity.tooltip")), 27);
        b.toggle("pinkified.option.screenTransitions", PinkConfig.SCREEN_TRANSITIONS);
        b.header("pinkified.settings.section.audio");
        b.toggle("pinkified.option.menuMusic", PinkConfig.MENU_MUSIC);
        b.toggle("pinkified.option.uiSounds", PinkConfig.UI_SOUNDS);
        OptionInstance<Double> music = minecraft.options.getSoundSourceOptionInstance(SoundSource.MUSIC);
        b.add(new PinkSlider(cx, b.y, cw, 24, Component.translatable("soundCategory.music"), 0, 1,
                music::get, music::set, v -> Component.literal(Math.round(v * 100) + "%")), 27);
    }

    /** Hearts, treats, armor and the XP bar exactly as the HUD will draw them right now. */
    private void paintHudPreview(GuiGraphics g, int x, int y, int w, int h) {
        Palette p = Themes.palette();
        RenderKit.roundedRect(g, x, y, w, h, 4, Colors.withAlpha(p.ink, 0x50));
        int ix = x + 6, iy = y + 5;
        for (int i = 0; i < 5; i++) {
            g.blit(ICONS, ix + i * 8, iy, 16, 0, 9, 9);
            g.blit(ICONS, ix + i * 8, iy, i < 3 ? 52 : (i == 3 ? 61 : 16), 0, 9, 9);
        }
        int fx = ix + 48;
        for (int i = 0; i < 5; i++) {
            g.blit(ICONS, fx + i * 8, iy, 16, 27, 9, 9);
            g.blit(ICONS, fx + i * 8, iy, i < 4 ? 52 : 61, 27, 9, 9);
        }
        int ax = fx + 48;
        for (int i = 0; i < 4; i++) g.blit(ICONS, ax + i * 8, iy, i < 3 ? 34 : 25, 9, 9, 9);
        int barW = Math.min(182, w - 12);
        g.blit(ICONS, ix, iy + 13, 0, 64, barW, 5);
        g.blit(ICONS, ix, iy + 13, 0, 69, Math.round(barW * 0.62f), 5);
    }

    // ------------------------------------------------------------------ behaviour

    private void changed() {
        PinkifiedClient.applySettings();
    }

    private void requestApply() {
        dirty = true;
    }

    @Override
    public void tick() {
        if (dirty && Anim.millis() - lastApply > 120) {
            dirty = false;
            lastApply = Anim.millis();
            PinkifiedClient.applySettings();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll > 0 && mouseX >= cx && mouseX < cx + cw && mouseY >= cy && mouseY < cy + ch) {
            scroll = (int) Math.max(0, Math.min(maxScroll, scroll - delta * 18));
            layoutContent();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // content scrolled out of the viewport must not catch clicks
        boolean inView = mouseX >= cx && mouseX < cx + cw && mouseY >= cy && mouseY < cy + ch;
        if (!inView) {
            for (AbstractWidget w : content) {
                if (w.visible && w.isMouseOver(mouseX, mouseY)) return false;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        if (dirty) PinkifiedClient.applySettings();
        PinkConfig.save();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void renderBackground(GuiGraphics g) {
        if (minecraft.level == null) {
            MenuBackdrop.INSTANCE.renderMenu(g, width, height);
        } else {
            super.renderBackground(g);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        Palette p = Themes.palette();
        float appear = Anim.intensity() > 0 ? Anim.easeOutCubic((Anim.millis() - openedAt) / 350f) : 1f;
        g.pose().pushPose();
        g.pose().translate(0, (1f - appear) * 12f, 0);

        RenderKit.softShadow(g, px, py, pw, ph, 8, p.ink, 4);
        int top = Colors.withAlpha(Colors.lerp(p.deep, p.shadow, 0.25f), 0xEC);
        int bottom = Colors.withAlpha(Colors.lerp(p.deep, p.ink, 0.45f), 0xF2);
        RenderKit.roundedGradient(g, px, py, pw, ph, 8, top, bottom);
        RenderKit.roundedOutline(g, px, py, pw, ph, 8, Colors.withAlpha(p.light, 0xA0));
        RenderKit.roundedOutline(g, px + 1, py + 1, pw - 2, ph - 2, 7, Colors.withAlpha(p.accentLight, 0x40));

        // title bar
        RenderKit.spriteTransformed(g, PinkTextures.HEART, px + 14, py + 13, 9, 8, -10, 1.1f, p.heart, 1f);
        RenderKit.text(g, font, title, px + 24, py + 9, p.light, p.ink);
        Component sub = Component.translatable("pinkified.settings.current", Component.translatable(Themes.id().translationKey()));
        int sw = font.width(sub);
        RenderKit.text(g, font, sub, px + pw - sw - 10, py + 9, Colors.lerp(p.accentLight, p.light, 0.5f), p.ink);
        g.fill(px + 8, py + 23, px + pw - 8, py + 24, Colors.withAlpha(p.light, 0x50));

        // sidebar separator and selected-tab marker
        g.fill(cx - 6, py + 28, cx - 5, py + ph - 32, Colors.withAlpha(p.light, 0x30));
        PinkButton sel = tabButtons.get(tab.ordinal());
        RenderKit.roundedOutline(g, sel.getX() - 2, sel.getY() - 2, sel.getWidth() + 4, sel.getHeight() + 4, 5, p.light);

        super.render(g, mouseX, mouseY, partialTick);

        // content, clipped to the viewport
        g.enableScissor(cx - 2, cy, cx + cw + 2, cy + ch);
        for (AbstractWidget w : content) {
            if (w.visible) w.render(g, mouseX, mouseY, partialTick);
        }
        g.disableScissor();
        if (maxScroll > 0) {
            int trackH = ch - 4;
            int thumbH = Math.max(16, trackH * ch / (ch + maxScroll));
            int thumbY = cy + 2 + (trackH - thumbH) * scroll / maxScroll;
            RenderKit.roundedRect(g, cx + cw + 3, cy + 2, 3, trackH, 1, Colors.withAlpha(p.ink, 0x80));
            RenderKit.roundedRect(g, cx + cw + 3, thumbY, 3, thumbH, 1, p.accentLight);
        }
        g.pose().popPose();
    }
}
