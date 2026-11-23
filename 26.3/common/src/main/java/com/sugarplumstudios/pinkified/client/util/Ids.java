package com.sugarplumstudios.pinkified.client.util;

import com.sugarplumstudios.pinkified.PinkifiedMod;
import net.minecraft.resources.Identifier;

/** Identifier factories. */
public final class Ids {
    private Ids() {
    }

    public static Identifier mod(String path) {
        return Identifier.fromNamespaceAndPath(PinkifiedMod.MOD_ID, path);
    }

    public static Identifier vanilla(String path) {
        return Identifier.withDefaultNamespace(path);
    }
}
