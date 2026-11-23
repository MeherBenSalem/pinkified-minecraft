package com.sugarplumstudios.pinkified.forge;

import com.sugarplumstudios.pinkified.PinkifiedMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Forge entry point for Pinkified Minecraft, part of the Sugarplum Studios collection.
 *
 * <p>Every feature is cosmetic and client-side; on a dedicated server the mod does nothing.
 */
@Mod(PinkifiedMod.MOD_ID)
public final class PinkifiedForge {
    @SuppressWarnings("removal") // FMLJavaModLoadingContext.get() keeps the mod working on every Forge 47 build
    public PinkifiedForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            PinkifiedForgeClient.init(FMLJavaModLoadingContext.get().getModEventBus());
        }
    }
}
