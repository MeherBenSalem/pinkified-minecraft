package com.sugarplumstudios.pinkified.fabric;

import com.sugarplumstudios.pinkified.client.PinkifiedClient;
import com.sugarplumstudios.pinkified.client.gui.ScreenHooks;
import com.sugarplumstudios.pinkified.client.particle.PinkEnchantParticle;
import com.sugarplumstudios.pinkified.client.texture.ThemedTextures;
import com.sugarplumstudios.pinkified.client.util.Ids;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Collection;
import java.util.List;

/**
 * Fabric entry point for Pinkified Minecraft. Fabric API events cover the key binding, ticking, particles, resource
 * reloads and screen init/render; the mixins in {@code fabric.mixin} stand in for the Forge events Fabric API lacks
 * (title screen swap, menu backgrounds, container decorations, the game-over screen, tooltip colours, HUD overlays,
 * menu music and atlas stitching).
 */
public final class PinkifiedFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PinkifiedClient.init();
        KeyBindingHelper.registerKeyBinding(PinkifiedClient.OPEN_SETTINGS);
        ClientTickEvents.END_CLIENT_TICK.register(client -> PinkifiedClient.onClientTick());
        ParticleFactoryRegistry.getInstance().register(ParticleTypes.ENCHANT, PinkEnchantParticle.Provider::new);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Ids.mod("themed_textures");
            }

            @Override
            public Collection<Identifier> getFabricDependencies() {
                return List.of(ResourceReloadListenerKeys.TEXTURES);
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                ThemedTextures.INSTANCE.onResourceManagerReload(manager);
            }
        });
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            ScreenHooks.onInit(screen, widget -> Screens.getButtons(screen).add(widget));
            ScreenEvents.afterRender(screen).register((s, g, mouseX, mouseY, partialTick) -> ScreenHooks.onRenderPost(s, g));
        });
    }
}
