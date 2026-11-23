package com.sugarplumstudios.pinkified.client.gui.screen;

import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.realmsclient.RealmsMainScreen;
import com.sugarplumstudios.pinkified.client.gui.Anim;
import com.sugarplumstudios.pinkified.client.gui.MenuBackdrop;
import com.sugarplumstudios.pinkified.client.gui.PinkTextures;
import com.sugarplumstudios.pinkified.client.gui.RenderKit;
import com.sugarplumstudios.pinkified.client.gui.widget.PinkButton;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import com.sugarplumstudios.pinkified.client.util.Ids;
import com.sugarplumstudios.pinkified.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;

/**
 * The Pinkified main menu: an animated pastel sky, the Minecraft logo in theme colours with a
 * "Pinkified" ribbon, a rotating cute splash, and soft animated buttons. It offers everything the
 * vanilla title screen does (plus the loader's mod list, when it has one, and Pinkified settings).
 */
public class PinkTitleScreen extends Screen {
    private static final String[] SPLASH_KEYS = new String[16];

    static {
        for (int i = 0; i < SPLASH_KEYS.length; i++) SPLASH_KEYS[i] = "pinkified.splash." + i;
    }

    private static final Component COPYRIGHT_TEXT = Component.literal("Copyright Mojang AB. Do not distribute!");
    private static final net.minecraft.resources.Identifier MINECRAFT_LOGO = Ids.vanilla("textures/gui/title/minecraft.png");

    private final long openedAt = Anim.millis();
    private final boolean fadeIn;
    private String splashKey;

    public PinkTitleScreen() {
        this(true);
    }

    public PinkTitleScreen(boolean fadeIn) {
        super(Component.translatable("narrator.screen.title"));
        this.fadeIn = fadeIn;
    }

    @Override
    protected void init() {
        if (splashKey == null) splashKey = SPLASH_KEYS[RandomSource.create().nextInt(SPLASH_KEYS.length)];
        int cx = width / 2;
        int top = Math.max(height / 4 + 40, 108);
        int bw = 200, gap = 24;
        long d = 70;
        Component mpDisabled = multiplayerDisabledReason();
        Tooltip mpTooltip = mpDisabled != null ? Tooltip.create(mpDisabled) : null;

        addRenderableWidget(new PinkButton(cx - bw / 2, top, bw, 20, Component.translatable("menu.singleplayer"),
                b -> minecraft.gui.setScreen(new SelectWorldScreen(this))).entrance(d, -40));
        PinkButton mp = addRenderableWidget(new PinkButton(cx - bw / 2, top + gap, bw, 20, Component.translatable("menu.multiplayer"),
                b -> minecraft.gui.setScreen(minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this)))
                .entrance(d * 2, 40));
        mp.active = mpDisabled == null;
        mp.setTooltip(mpTooltip);
        Component modsLabel = Services.PLATFORM.modsButtonLabel();
        PinkButton realms = addRenderableWidget(new PinkButton(cx - bw / 2, top + gap * 2, modsLabel != null ? 98 : bw, 20,
                Component.translatable("menu.online"), b -> minecraft.gui.setScreen(new RealmsMainScreen(this))).entrance(d * 3, -40));
        realms.active = mpDisabled == null;
        realms.setTooltip(mpTooltip);
        if (modsLabel != null) {
            addRenderableWidget(new PinkButton(cx + 2, top + gap * 2, 98, 20, modsLabel, b -> {
                Screen mods = Services.PLATFORM.modListScreen(this);
                if (mods != null) minecraft.gui.setScreen(mods);
            }).entrance(d * 3, 40));
        }
        addRenderableWidget(new PinkButton(cx - bw / 2, top + gap * 3 + 8, 98, 20, Component.translatable("menu.options"),
                b -> minecraft.gui.setScreen(new OptionsScreen(this, minecraft.options, false))).entrance(d * 4, -40));
        addRenderableWidget(new PinkButton(cx + 2, top + gap * 3 + 8, 98, 20, Component.translatable("menu.quit"),
                b -> minecraft.stop()).entrance(d * 4, 40));

        int iy = top + gap * 3 + 8;
        PinkButton lang = addRenderableWidget(new PinkButton(cx - bw / 2 - 24, iy, 20, 20, Component.empty(), PinkTextures.LANGUAGE,
                b -> minecraft.gui.setScreen(new LanguageSelectScreen(this, minecraft.options, minecraft.getLanguageManager())))
                .entrance(d * 5, -30));
        lang.setTooltip(Tooltip.create(Component.translatable("narrator.button.language")));
        PinkButton access = addRenderableWidget(new PinkButton(cx + bw / 2 + 4, iy, 20, 20, Component.empty(), PinkTextures.ACCESSIBILITY,
                b -> minecraft.gui.setScreen(new AccessibilityOptionsScreen(this, minecraft.options))).entrance(d * 5, 30));
        access.setTooltip(Tooltip.create(Component.translatable("narrator.button.accessibility")));
        PinkButton settings = addRenderableWidget(new PinkButton(cx + bw / 2 + 4, top + gap * 2, 20, 20, Component.empty(), PinkTextures.HEART_BUTTON,
                b -> minecraft.gui.setScreen(new PinkSettingsScreen(this))).entrance(d * 6, 30));
        settings.setTooltip(Tooltip.create(Component.translatable("pinkified.settings.open")));

        Component copyright = COPYRIGHT_TEXT;
        int cw = font.width(copyright);
        addRenderableWidget(new PlainTextButton(width - cw - 2, height - 10, cw, 10, copyright,
                b -> minecraft.gui.setScreen(new CreditsAndAttributionScreen(this)), font));
    }

    @Nullable
    private Component multiplayerDisabledReason() {
        if (minecraft.allowsMultiplayer()) return null;
        BanDetails ban = minecraft.multiplayerBan();
        if (ban != null) {
            return ban.expires() != null
                    ? Component.translatable("title.multiplayer.disabled.banned.temporary")
                    : Component.translatable("title.multiplayer.disabled.banned.permanent");
        }
        return Component.translatable("title.multiplayer.disabled");
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    /** The sky, the logo, the splash, the button card and the branding; vanilla's render draws the buttons over it. */
    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        MenuBackdrop.INSTANCE.renderMenu(g, width, height);
        Palette p = Themes.palette();
        float intensity = Anim.intensity();
        float t = Anim.seconds();
        int cx = width / 2;
        int top = Math.max(height / 4 + 40, 108);

        // logo: the Minecraft wordmark (recoloured by the theme) gently bobbing, with the
        // "Pinkified" ribbon tucked under it where the edition text normally sits
        int logoY = Math.max(6, top - 96);
        float bob = intensity > 0 ? (float) Math.sin(t * 1.4f) * 1.6f * Math.min(1.5f, intensity) : 0f;
        g.pose().pushMatrix();
        g.pose().translate(0, bob);
        g.blit(RenderPipelines.GUI_TEXTURED, MINECRAFT_LOGO, cx - 128, logoY, 0.0F, 0.0F, 256, 44, 256, 64);
        int ribbonY = logoY + 46;
        float wobble = intensity > 0 ? (float) Math.sin(t * 2.1f) * 1.5f * Math.min(1f, intensity) : 0f;
        int lw = PinkTextures.LOGO_W, lh = PinkTextures.LOGO_H;
        RenderKit.spriteTransformed(g, PinkTextures.LOGO_BASE, cx, ribbonY + lh / 2f, lw, lh, wobble, 1.25f, p.logo, 1f);
        RenderKit.spriteTransformed(g, PinkTextures.LOGO_GLOSS, cx, ribbonY + lh / 2f, lw, lh, wobble, 1.25f, 0xFFFFFFFF, 1f);
        RenderKit.spriteTransformed(g, PinkTextures.BOW, cx - lw * 0.68f, ribbonY + lh / 2f + 2, 15, 11, -18 + wobble * 2, 1.1f, p.accent, 1f);
        // twinkles around the logo
        for (int i = 0; i < 5; i++) {
            float phase = i * 0.37f;
            float tw = intensity > 0 ? Anim.pulse(1.8f + i * 0.3f, phase) : 0.6f;
            float sx = cx - 120 + i * 60 + (i % 2) * 14;
            float sy = logoY + (i % 2 == 0 ? -2 : 46);
            RenderKit.spriteTransformed(g, PinkTextures.SPARKLE_BIG, sx, sy, 9, 9, 0, 0.5f + 0.5f * tw, p.sparkle, tw);
        }
        g.pose().popMatrix();

        // splash
        Component splash = Component.translatable(splashKey);
        float pulse = intensity > 0 ? 1.8f - Math.abs((float) Math.sin((t % 1f) * Math.PI * 2) * 0.1f) : 1.8f;
        pulse = pulse * 100f / (font.width(splash) + 32);
        g.pose().pushMatrix();
        g.pose().translate(cx + 128, logoY + 50 + bob);
        g.pose().rotate((float) Math.toRadians(-16.0F));
        g.pose().scale(pulse, pulse);
        RenderKit.centeredText(g, font, splash, 0, -8, Colors.lerp(p.light, 0xFFFFFFFF, 0.5f), p.accentDeep);
        g.pose().popMatrix();

        // a soft card behind the buttons
        int cardTop = top - 8, cardBottom = top + 24 * 3 + 36;
        RenderKit.roundedRect(g, cx - 112, cardTop, 224, cardBottom - cardTop, 6, Colors.withAlpha(p.ink, 0x30));
        RenderKit.roundedOutline(g, cx - 112, cardTop, 224, cardBottom - cardTop, 6, Colors.withAlpha(p.light, 0x60));

        // branding (the loader's version lines stay visible, as on the vanilla title screen)
        Services.PLATFORM.forEachBrandingLine((line, text) ->
                RenderKit.drawString(g, font, text, 2, height - (10 + line * (font.lineHeight + 1)), 0xFFFFFF));
        Services.PLATFORM.forEachAboveCopyrightLine((line, text) ->
                RenderKit.drawString(g, font, text, width - font.width(text), height - (10 + (line + 1) * (font.lineHeight + 1)), 0xFFFFFF));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        Palette p = Themes.palette();
        float intensity = Anim.intensity();

        // fade in from the theme colour when the menu first opens
        if (fadeIn && intensity > 0) {
            float f = 1f - Anim.easeInOutSine((Anim.millis() - openedAt) / 900f);
            if (f > 0.001f) g.fill(0, 0, width, height, Colors.withAlpha(p.light, f));
        }
    }
}
