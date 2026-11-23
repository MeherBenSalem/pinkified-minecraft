package com.sugarplumstudios.pinkified.client.gui;

import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.gui.screen.PinkSettingsScreen;
import com.sugarplumstudios.pinkified.client.gui.screen.PinkTitleScreen;
import com.sugarplumstudios.pinkified.client.gui.widget.PinkButton;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import com.sugarplumstudios.pinkified.platform.Services;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Screen-level styling that works with any screen, vanilla or modded, through the loader's screen
 * events (Forge events, or Fabric API events and a few mixins): the pastel main menu, menu backdrops, list panels, pause-menu button, container
 * decorations, the game-over screen, soft transitions and tooltip colours.
 */
public final class ScreenHooks {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean firstTitle = true;
    private static long transitionStart;

    private ScreenHooks() {
    }

    // ------------------------------------------------------------------ main menu

    /** A screen is about to open; returns the screen to open instead (or {@code next} itself). */
    @Nullable
    public static Screen onOpening(@Nullable Screen next) {
        Minecraft mc = Minecraft.getInstance();
        if (next != null && next.getClass() == TitleScreen.class && PinkConfig.on(PinkConfig.CUSTOM_MAIN_MENU) && !mc.isDemo()) {
            next = new PinkTitleScreen(firstTitle);
            firstTitle = false;
        }
        if (mc.level == null || next instanceof PinkSettingsScreen) {
            transitionStart = Anim.millis();
        }
        return next;
    }

    // ------------------------------------------------------------------ init: lists and buttons

    /** After a screen's init; {@code addWidget} adds a widget to the screen (rendered, clickable, narrated). */
    public static void onInit(Screen screen, Consumer<AbstractWidget> addWidget) {
        if (PinkConfig.on(PinkConfig.MENU_BACKGROUNDS) && !(screen instanceof PinkTitleScreen)) {
            for (GuiEventListener child : screen.children()) {
                if (child instanceof AbstractSelectionList<?> list) {
                    list.setRenderBackground(false);
                    list.setRenderTopAndBottom(false);
                }
            }
        }
        boolean pause = screen instanceof PauseScreen;
        boolean vanillaTitle = screen.getClass() == TitleScreen.class;
        if ((pause || vanillaTitle) && PinkConfig.on(PinkConfig.PAUSE_MENU_BUTTON)) {
            // pause menu: left of Options; vanilla title: right of the accessibility button after Quit
            Button anchor = findButton(screen, vanillaTitle ? "menu.quit" : "menu.options");
            if (anchor != null) {
                int x = vanillaTitle ? anchor.getX() + anchor.getWidth() + 28 : anchor.getX() - 24;
                PinkButton heart = new PinkButton(x, anchor.getY(), 20, 20, Component.empty(), PinkTextures.HEART_BUTTON,
                        b -> Minecraft.getInstance().setScreen(new PinkSettingsScreen(screen)));
                heart.setTooltip(Tooltip.create(Component.translatable("pinkified.settings.open")));
                addWidget.accept(heart);
            }
        }
    }

    @Nullable
    private static Button findButton(Screen screen, String key) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof Button b && b.getMessage().getContents() instanceof TranslatableContents tc && key.equals(tc.getKey())) {
                return b;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ backgrounds

    /** Right after a screen drew its default background. */
    public static void onBackgroundRendered(Screen screen, GuiGraphics g) {
        if (!PinkConfig.on(PinkConfig.MENU_BACKGROUNDS)) return;
        if (Minecraft.getInstance().level == null) {
            MenuBackdrop.INSTANCE.renderMenu(g, screen.width, screen.height);
        } else {
            MenuBackdrop.INSTANCE.renderInWorld(g, screen.width, screen.height);
        }
        Palette p = Themes.palette();
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractSelectionList<?> list) {
                int[] b = Services.PLATFORM.listBounds(list);
                int left = Math.max(b[0], list.getRowLeft() - 8);
                int right = Math.min(b[1], list.getRowLeft() + list.getRowWidth() + 14);
                int top = b[2], bottom = b[3];
                if (right - left < 20 || bottom - top < 10) continue;
                RenderKit.roundedRect(g, left, top, right - left, bottom - top, 5, Colors.withAlpha(p.ink, 0x9C));
                RenderKit.roundedOutline(g, left, top, right - left, bottom - top, 5, Colors.withAlpha(p.light, 0x70));
            }
        }
    }

    // ------------------------------------------------------------------ container decorations

    /** Right after a container screen drew its background texture (window at left, top, size w x h). */
    public static void onContainerBackground(AbstractContainerScreen<?> screen, GuiGraphics g, int left, int top, int w, int h) {
        if (!PinkConfig.on(PinkConfig.CONTAINER_DECORATIONS) || !PinkConfig.on(PinkConfig.THEME_CONTAINERS)) return;
        if (screen instanceof CreativeModeInventoryScreen) return;
        if (w < 80 || h < 60) return;
        Palette p = Themes.palette();
        float sway = Anim.intensity() > 0 ? (float) Math.sin(Anim.seconds() * 1.7f) * 4f * Math.min(1f, Anim.intensity()) : 0f;
        RenderKit.spriteTransformed(g, PinkTextures.BOW, left + w - 9, top + 1, 15, 11, 12 + sway, 1f, p.accent, 1f);
        RenderKit.spriteTransformed(g, PinkTextures.HEART, left + 2, top + h - 2, 9, 8, -14, 0.85f, p.heart, 1f);
    }

    // ------------------------------------------------------------------ game over

    private static Field causeField, scoreField;
    private static boolean fieldsResolved;

    /** Before a screen renders; returns true when Pinkified drew the screen itself and vanilla must not. */
    public static boolean onRenderPre(Screen screen, GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (screen instanceof DeathScreen death && PinkConfig.on(PinkConfig.THEME_DEATH_SCREEN)) {
            renderDeath(death, g, mouseX, mouseY, partialTick);
            return true;
        }
        return false;
    }

    private static void resolveDeathFields() {
        if (fieldsResolved) return;
        fieldsResolved = true;
        // DeathScreen has two Component fields: the final cause of death and the score line
        for (Field f : DeathScreen.class.getDeclaredFields()) {
            if (f.getType() != Component.class || Modifier.isStatic(f.getModifiers())) continue;
            try {
                f.setAccessible(true);
                if (Modifier.isFinal(f.getModifiers())) causeField = f;
                else scoreField = f;
            } catch (RuntimeException e) {
                LOGGER.debug("Pinkified could not access DeathScreen field {}", f.getName(), e);
            }
        }
    }

    @Nullable
    private static Component read(@Nullable Field f, Object target) {
        if (f == null) return null;
        try {
            return (Component) f.get(target);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static final List<float[]> deathHearts = new ArrayList<>();

    private static void renderDeath(DeathScreen screen, GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        resolveDeathFields();
        Palette p = Themes.palette();
        Font font = Minecraft.getInstance().font;
        int w = screen.width, h = screen.height;
        g.fillGradient(0, 0, w, h, Colors.withAlpha(Colors.lerp(p.deep, p.heart, 0.25f), 0x90), Colors.withAlpha(p.ink, 0xD8));

        // slow, falling hearts
        if (deathHearts.isEmpty()) {
            java.util.Random r = new java.util.Random(7);
            for (int i = 0; i < 18; i++) deathHearts.add(new float[]{r.nextFloat(), r.nextFloat(), 0.4f + r.nextFloat() * 0.6f, r.nextFloat() * 6.28f});
        }
        float t = Anim.seconds() * Math.max(0.15f, Anim.intensity());
        for (float[] hrt : deathHearts) {
            float y = ((hrt[1] + t * 0.03f * hrt[2]) % 1.1f) * h - 10;
            float x = hrt[0] * w + (float) Math.sin(t + hrt[3]) * 8f;
            RenderKit.spriteTransformed(g, PinkTextures.HEART, x, y, 9, 8, (float) Math.sin(t * 0.7f + hrt[3]) * 20f,
                    0.7f + hrt[2], p.petal((int) (hrt[3] * 3)), 0.35f);
        }

        g.pose().pushPose();
        g.pose().scale(2.0F, 2.0F, 2.0F);
        RenderKit.centeredText(g, font, screen.getTitle(), w / 4, 30, p.light, Colors.lerp(p.ink, p.heart, 0.3f));
        g.pose().popPose();
        RenderKit.spriteTransformed(g, PinkTextures.HEART, w / 2f, 52, 9, 8, 0, 1.4f + 0.1f * Anim.pulse(1.2f, 0), p.heart, 1f);

        Component cause = read(causeField, screen);
        if (cause != null) g.drawCenteredString(font, cause, w / 2, 85, 0xFFFFFF);
        Component score = read(scoreField, screen);
        if (score != null) g.drawCenteredString(font, score, w / 2, 100, 0xFFFFFF);
        if (cause != null && mouseY > 85 && mouseY < 85 + font.lineHeight) {
            int textW = font.width(cause);
            int x0 = w / 2 - textW / 2;
            if (mouseX >= x0 && mouseX <= x0 + textW) {
                g.renderComponentHoverEffect(font, font.getSplitter().componentStyleAtWidth(cause, mouseX - x0), mouseX, mouseY);
            }
        }
        for (GuiEventListener child : screen.children()) {
            if (child instanceof Renderable r) r.render(g, mouseX, mouseY, partialTick);
        }
    }

    // ------------------------------------------------------------------ transitions

    /** After a screen rendered. */
    public static void onRenderPost(Screen s, GuiGraphics g) {
        if (!PinkConfig.on(PinkConfig.SCREEN_TRANSITIONS) || Anim.intensity() <= 0) return;
        if (s instanceof PinkTitleScreen) return;
        long age = Anim.millis() - transitionStart;
        if (age < 0 || age > 260) return;
        float f = 1f - Anim.easeOutCubic(age / 260f);
        g.fill(0, 0, s.width, s.height, Colors.withAlpha(Themes.palette().skyBottom, f * 0.55f));
    }

    // ------------------------------------------------------------------ tooltips

    private static final int VANILLA_TOOLTIP_BG = 0xF0100010;
    private static final int VANILLA_BORDER_START = 0x505000FF;
    private static final int VANILLA_BORDER_END = 0x5028007F;

    /** Theme colours for a tooltip, or null to keep the given ones (custom colours from other mods are kept). */
    @Nullable
    public static TooltipColors tooltipColors(int backgroundStart, int borderStart, int borderEnd) {
        if (!PinkConfig.on(PinkConfig.THEME_TOOLTIPS)) return null;
        if (backgroundStart != VANILLA_TOOLTIP_BG || borderStart != VANILLA_BORDER_START || borderEnd != VANILLA_BORDER_END) {
            return null;
        }
        Palette p = Themes.palette();
        return new TooltipColors(Colors.withAlpha(p.tooltipBg, 0xF0),
                Colors.withAlpha(Colors.lerp(p.tooltipBg, p.deep, 0.3f), 0xF0),
                Colors.withAlpha(p.tooltipBorderTop, 0xD0),
                Colors.withAlpha(p.tooltipBorderBottom, 0xB0));
    }

    /** Colours for the four tooltip gradients. */
    public record TooltipColors(int backgroundStart, int backgroundEnd, int borderStart, int borderEnd) {
    }

    /** Vanilla's tooltip colours, as Forge reports them before any mod changed them. */
    public static int vanillaBackground() {
        return VANILLA_TOOLTIP_BG;
    }

    public static int vanillaBorderStart() {
        return VANILLA_BORDER_START;
    }

    public static int vanillaBorderEnd() {
        return VANILLA_BORDER_END;
    }
}
