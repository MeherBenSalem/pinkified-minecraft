package com.sugarplumstudios.pinkified.forge;

import com.sugarplumstudios.pinkified.client.PinkifiedClient;
import com.sugarplumstudios.pinkified.client.gui.ScreenHooks;
import com.sugarplumstudios.pinkified.client.gui.screen.PinkSettingsScreen;
import com.sugarplumstudios.pinkified.client.hud.HudHooks;
import com.sugarplumstudios.pinkified.client.particle.PinkEnchantParticle;
import com.sugarplumstudios.pinkified.client.sound.MusicHooks;
import com.sugarplumstudios.pinkified.client.texture.ThemedTextures;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-side wiring for Forge: forwards Forge's events to the shared hook classes. */
final class PinkifiedForgeClient {
    private PinkifiedForgeClient() {
    }

    @SuppressWarnings("removal") // ModLoadingContext.get() is the only option on older Forge 47 builds
    static void init(IEventBus modBus) {
        PinkifiedClient.init();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new PinkSettingsScreen(parent)));

        modBus.addListener(EventPriority.NORMAL, false, FMLClientSetupEvent.class, event -> event.enqueueWork(Themes::refresh));
        modBus.addListener(EventPriority.NORMAL, false, RegisterClientReloadListenersEvent.class,
                event -> event.registerReloadListener(ThemedTextures.INSTANCE));
        modBus.addListener(EventPriority.NORMAL, false, RegisterGuiOverlaysEvent.class, event -> {
            event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "crosshair",
                    (gui, g, partialTick, width, height) -> HudHooks.renderCrosshair(g, width, height));
            event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "experience_level",
                    (gui, g, partialTick, width, height) -> HudHooks.renderXpLevel(g, width, height));
        });
        modBus.addListener(EventPriority.NORMAL, false, RegisterParticleProvidersEvent.class,
                event -> event.registerSpriteSet(ParticleTypes.ENCHANT, PinkEnchantParticle.Provider::new));
        modBus.addListener(EventPriority.NORMAL, false, RegisterKeyMappingsEvent.class,
                event -> event.register(PinkifiedClient.OPEN_SETTINGS));
        modBus.addListener(EventPriority.NORMAL, false, TextureStitchEvent.Post.class,
                event -> PinkifiedClient.onAtlasStitched(event.getAtlas()));

        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(EventPriority.LOW, false, ScreenEvent.Opening.class, event -> {
            Screen next = ScreenHooks.onOpening(event.getNewScreen());
            if (next != event.getNewScreen()) event.setNewScreen(next);
        });
        bus.addListener(EventPriority.NORMAL, false, ScreenEvent.Init.Post.class,
                event -> ScreenHooks.onInit(event.getScreen(), event::addListener));
        bus.addListener(EventPriority.NORMAL, false, ScreenEvent.BackgroundRendered.class,
                event -> ScreenHooks.onBackgroundRendered(event.getScreen(), event.getGuiGraphics()));
        bus.addListener(EventPriority.NORMAL, false, ContainerScreenEvent.Render.Background.class,
                event -> ScreenHooks.onContainerBackground(event.getContainerScreen(), event.getGuiGraphics()));
        bus.addListener(EventPriority.NORMAL, false, ScreenEvent.Render.Pre.class, event -> {
            if (ScreenHooks.onRenderPre(event.getScreen(), event.getGuiGraphics(), event.getMouseX(), event.getMouseY(),
                    event.getPartialTick())) {
                event.setCanceled(true);
            }
        });
        bus.addListener(EventPriority.NORMAL, false, ScreenEvent.Render.Post.class,
                event -> ScreenHooks.onRenderPost(event.getScreen(), event.getGuiGraphics()));
        bus.addListener(EventPriority.HIGH, false, RenderTooltipEvent.Color.class, event -> {
            ScreenHooks.TooltipColors c = ScreenHooks.tooltipColors(event.getOriginalBackgroundStart(),
                    event.getOriginalBorderStart(), event.getOriginalBorderEnd());
            if (c == null) return;
            event.setBackgroundStart(c.backgroundStart());
            event.setBackgroundEnd(c.backgroundEnd());
            event.setBorderStart(c.borderStart());
            event.setBorderEnd(c.borderEnd());
        });
        // remembers whether another mod hid the vanilla element, so ours stays hidden too
        bus.addListener(EventPriority.LOWEST, true, RenderGuiOverlayEvent.Pre.class, event -> {
            if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) HudHooks.setCrosshairCancelled(event.isCanceled());
            else if (event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) HudHooks.setXpCancelled(event.isCanceled());
        });
        bus.addListener(EventPriority.NORMAL, false, PlaySoundEvent.class, event -> {
            SoundInstance sound = MusicHooks.onPlaySound(event.getSound());
            if (sound != event.getSound()) event.setSound(sound);
        });
        bus.addListener(EventPriority.NORMAL, false, TickEvent.ClientTickEvent.class, event -> {
            if (event.phase == TickEvent.Phase.END) PinkifiedClient.onClientTick();
        });
    }
}
