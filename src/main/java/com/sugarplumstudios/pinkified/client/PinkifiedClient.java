package com.sugarplumstudios.pinkified.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.dev.DevSnapshots;
import com.sugarplumstudios.pinkified.client.gui.ScreenHooks;
import com.sugarplumstudios.pinkified.client.gui.screen.PinkSettingsScreen;
import com.sugarplumstudios.pinkified.client.hud.HudHooks;
import com.sugarplumstudios.pinkified.client.particle.AmbientEffects;
import com.sugarplumstudios.pinkified.client.particle.ParticleSprites;
import com.sugarplumstudios.pinkified.client.particle.PinkEnchantParticle;
import com.sugarplumstudios.pinkified.client.sound.MusicHooks;
import com.sugarplumstudios.pinkified.client.texture.EffectIconTinter;
import com.sugarplumstudios.pinkified.client.texture.ThemedTextures;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client entry point: registers config, listeners and event hooks. */
public final class PinkifiedClient {
    public static final KeyMapping OPEN_SETTINGS = new KeyMapping("key.pinkified.settings", KeyConflictContext.UNIVERSAL,
            InputConstants.UNKNOWN, "key.categories.pinkified");

    private PinkifiedClient() {
    }

    @SuppressWarnings("removal") // ModLoadingContext.get() is the only option on older Forge 47 builds
    public static void init(IEventBus modBus) {
        ModLoadingContext ctx = ModLoadingContext.get();
        ctx.registerConfig(ModConfig.Type.CLIENT, PinkConfig.SPEC);
        ctx.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new PinkSettingsScreen(parent)));

        modBus.addListener(PinkifiedClient::onClientSetup);
        modBus.addListener(PinkifiedClient::onConfigLoad);
        modBus.addListener(PinkifiedClient::onConfigReload);
        modBus.addListener(PinkifiedClient::onRegisterReloadListeners);
        modBus.addListener(HudHooks::registerOverlays);
        modBus.addListener(PinkifiedClient::onRegisterParticles);
        modBus.addListener(PinkifiedClient::onRegisterKeys);
        modBus.addListener(PinkifiedClient::onTextureStitched);

        MinecraftForge.EVENT_BUS.register(ScreenHooks.class);
        MinecraftForge.EVENT_BUS.register(HudHooks.class);
        MinecraftForge.EVENT_BUS.register(AmbientEffects.class);
        MinecraftForge.EVENT_BUS.register(MusicHooks.class);
        MinecraftForge.EVENT_BUS.addListener(PinkifiedClient::onClientTick);
        if (DevSnapshots.enabled()) MinecraftForge.EVENT_BUS.register(DevSnapshots.class);
    }

    /** Re-reads the config and refreshes every themed asset. Must run on the render thread. */
    public static void applySettings() {
        Themes.refresh();
        ThemedTextures.INSTANCE.apply();
        EffectIconTinter.apply();
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(Themes::refresh);
    }

    private static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == PinkConfig.SPEC) Themes.refresh();
    }

    private static void onConfigReload(ModConfigEvent.Reloading event) {
        // fired from the file watcher when the toml is edited by hand
        if (event.getConfig().getSpec() == PinkConfig.SPEC) Minecraft.getInstance().execute(PinkifiedClient::applySettings);
    }

    private static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ThemedTextures.INSTANCE);
    }

    private static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleTypes.ENCHANT, PinkEnchantParticle.Provider::new);
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SETTINGS);
    }

    private static void onTextureStitched(TextureStitchEvent.Post event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_PARTICLES)) ParticleSprites.invalidate();
        Themes.refresh();
        EffectIconTinter.onStitched(event);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_SETTINGS.consumeClick()) {
            if (!(mc.screen instanceof PinkSettingsScreen)) mc.setScreen(new PinkSettingsScreen(mc.screen));
        }
    }
}
