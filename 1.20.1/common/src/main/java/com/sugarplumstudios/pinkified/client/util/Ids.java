package com.sugarplumstudios.pinkified.client.util;

import com.sugarplumstudios.pinkified.PinkifiedMod;
import net.minecraft.resources.ResourceLocation;

/**
 * ResourceLocation factories. Recent Forge 47 builds deprecate the constructors in favour of
 * static factories that older 47.x builds lack, so the constructors are kept (and the warning
 * silenced) to stay compatible with every Forge 47 release.
 */
@SuppressWarnings("removal")
public final class Ids {
    private Ids() {
    }

    public static ResourceLocation mod(String path) {
        return new ResourceLocation(PinkifiedMod.MOD_ID, path);
    }

    public static ResourceLocation vanilla(String path) {
        return new ResourceLocation("minecraft", path);
    }
}
