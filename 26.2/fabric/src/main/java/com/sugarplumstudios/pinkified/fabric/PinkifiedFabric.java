package com.sugarplumstudios.pinkified.fabric;

import com.sugarplumstudios.pinkified.client.PinkifiedClient;
import com.sugarplumstudios.pinkified.client.gui.ScreenHooks;
import com.sugarplumstudios.pinkified.client.hud.HudHooks;
import com.sugarplumstudios.pinkified.client.particle.PinkEnchantParticle;
import com.sugarplumstudios.pinkified.client.texture.ThemedTextures;
import com.sugarplumstudios.pinkified.client.util.Ids;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.packs.PackType;

/**
 * Fabric entry point for Pinkified Minecraft. Fabric API covers the key binding, ticking, particles, resource reloads,
 * the HUD overlays and screen init/background/render; the mixins stand in for the NeoForge events Fabric API lacks
 * (title screen swap and menu music in {@code fabric.mixin}; container decorations and the game-over screen are
 * shared mixins in {@code pinkified.mixins.json}).
 */
public final class PinkifiedFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PinkifiedClient.init();
        KeyMapping.Category.register(PinkifiedClient.KEY_CATEGORY.id());
        KeyMappingHelper.registerKeyMapping(PinkifiedClient.OPEN_SETTINGS);
        ClientTickEvents.END_CLIENT_TICK.register(client -> PinkifiedClient.onClientTick());
        ParticleProviderRegistry.getInstance().register(ParticleTypes.ENCHANT, PinkEnchantParticle.Provider::new);
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(Ids.mod("themed_textures"), ThemedTextures.INSTANCE);
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, Ids.mod("crosshair"),
                (g, delta) -> HudHooks.renderCrosshair(g, g.guiWidth(), g.guiHeight()));
        HudElementRegistry.attachElementAfter(VanillaHudElements.EXPERIENCE_LEVEL, Ids.mod("experience_level"),
                (g, delta) -> HudHooks.renderXpLevel(g, g.guiWidth(), g.guiHeight()));
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            ScreenHooks.onInit(screen, widget -> Screens.getWidgets(screen).add(widget));
            ScreenEvents.afterBackground(screen).register((s, g, mouseX, mouseY, partialTick) -> ScreenHooks.onBackgroundRendered(s, g));
            ScreenEvents.afterExtract(screen).register((s, g, mouseX, mouseY, partialTick) -> ScreenHooks.onRenderPost(s, g));
        });
    }
}
