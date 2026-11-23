package com.sugarplumstudios.pinkified.neoforge;

import com.sugarplumstudios.pinkified.PinkifiedMod;
import com.sugarplumstudios.pinkified.client.PinkifiedClient;
import com.sugarplumstudios.pinkified.client.gui.ScreenHooks;
import com.sugarplumstudios.pinkified.client.gui.screen.PinkSettingsScreen;
import com.sugarplumstudios.pinkified.client.hud.HudHooks;
import com.sugarplumstudios.pinkified.client.particle.PinkEnchantParticle;
import com.sugarplumstudios.pinkified.client.sound.MusicHooks;
import com.sugarplumstudios.pinkified.client.texture.ThemedTextures;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Ids;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge entry point for Pinkified Minecraft, part of the Sugarplum Studios collection. Every feature is cosmetic
 * and client-side, so the mod only loads on the client. NeoForge's events are forwarded to the shared hook classes.
 */
@Mod(value = PinkifiedMod.MOD_ID, dist = Dist.CLIENT)
public final class PinkifiedNeoForge {
    public PinkifiedNeoForge(IEventBus modBus, ModContainer container) {
        PinkifiedClient.init();
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new PinkSettingsScreen(parent));

        modBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(Themes::refresh));
        modBus.addListener(RegisterClientReloadListenersEvent.class, event -> event.registerReloadListener(ThemedTextures.INSTANCE));
        modBus.addListener(RegisterGuiLayersEvent.class, event -> {
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, Ids.mod("crosshair"),
                    (g, delta) -> HudHooks.renderCrosshair(g, g.guiWidth(), g.guiHeight()));
            event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, Ids.mod("experience_level"),
                    (g, delta) -> HudHooks.renderXpLevel(g, g.guiWidth(), g.guiHeight()));
        });
        modBus.addListener(RegisterParticleProvidersEvent.class,
                event -> event.registerSpriteSet(ParticleTypes.ENCHANT, PinkEnchantParticle.Provider::new));
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> event.register(PinkifiedClient.OPEN_SETTINGS));

        IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(EventPriority.LOW, false, ScreenEvent.Opening.class, event -> {
            Screen next = ScreenHooks.onOpening(event.getNewScreen());
            if (next != event.getNewScreen()) event.setNewScreen(next);
        });
        bus.addListener(ScreenEvent.Init.Post.class, event -> ScreenHooks.onInit(event.getScreen(), event::addListener));
        bus.addListener(ScreenEvent.BackgroundRendered.class,
                event -> ScreenHooks.onBackgroundRendered(event.getScreen(), event.getGuiGraphics()));
        bus.addListener(ContainerScreenEvent.Render.Background.class,
                event -> {
                    AbstractContainerScreen<?> s = event.getContainerScreen();
                    ScreenHooks.onContainerBackground(s, event.getGuiGraphics(), s.getGuiLeft(), s.getGuiTop(), s.getXSize(), s.getYSize());
                });
        bus.addListener(ScreenEvent.Render.Pre.class, event -> {
            if (ScreenHooks.onRenderPre(event.getScreen(), event.getGuiGraphics(), event.getMouseX(), event.getMouseY(),
                    event.getPartialTick())) {
                event.setCanceled(true);
            }
        });
        bus.addListener(ScreenEvent.Render.Post.class, event -> ScreenHooks.onRenderPost(event.getScreen(), event.getGuiGraphics()));
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
        bus.addListener(EventPriority.LOWEST, true, RenderGuiLayerEvent.Pre.class, event -> {
            if (event.getName().equals(VanillaGuiLayers.CROSSHAIR)) HudHooks.setCrosshairCancelled(event.isCanceled());
            else if (event.getName().equals(VanillaGuiLayers.EXPERIENCE_LEVEL)) HudHooks.setXpCancelled(event.isCanceled());
        });
        bus.addListener(PlaySoundEvent.class, event -> {
            SoundInstance sound = MusicHooks.onPlaySound(event.getSound());
            if (sound != event.getSound()) event.setSound(sound);
        });
        bus.addListener(ClientTickEvent.Post.class, event -> PinkifiedClient.onClientTick());
    }
}
