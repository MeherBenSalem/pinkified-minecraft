package com.sugarplumstudios.pinkified.platform;

import java.util.ServiceLoader;

/** Finds the loader's {@link PinkPlatform} (declared in META-INF/services of each loader jar). */
public final class Services {
    public static final PinkPlatform PLATFORM = load(PinkPlatform.class);

    private Services() {
    }

    private static <T> T load(Class<T> type) {
        return ServiceLoader.load(type, Services.class.getClassLoader()).findFirst()
                .orElseThrow(() -> new IllegalStateException("No " + type.getName() + " implementation for this loader"));
    }
}
