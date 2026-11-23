package com.sugarplumstudios.pinkified.client.util;

import com.sugarplumstudios.pinkified.PinkifiedMod;
import net.minecraft.resources.ResourceLocation;

/** ResourceLocation factories. */
public final class Ids {
    private Ids() {
    }

    public static ResourceLocation mod(String path) {
        return ResourceLocation.fromNamespaceAndPath(PinkifiedMod.MOD_ID, path);
    }

    public static ResourceLocation vanilla(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }
}
