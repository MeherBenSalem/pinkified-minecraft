package com.sugarplumstudios.pinkified;

import com.sugarplumstudios.pinkified.client.PinkifiedClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Pinkified Minecraft, part of the Sugarplum Studios collection.
 *
 * <p>Every feature is cosmetic and client-side; on a dedicated server the mod does nothing.
 */
@Mod(PinkifiedMod.MOD_ID)
public final class PinkifiedMod {
    public static final String MOD_ID = "pinkified";

    @SuppressWarnings("removal") // FMLJavaModLoadingContext.get() keeps the mod working on every Forge 47 build
    public PinkifiedMod() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            PinkifiedClient.init(FMLJavaModLoadingContext.get().getModEventBus());
        }
    }
}
