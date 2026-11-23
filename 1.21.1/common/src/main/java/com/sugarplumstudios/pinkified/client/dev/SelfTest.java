package com.sugarplumstudios.pinkified.client.dev;

import com.mojang.logging.LogUtils;
import com.sugarplumstudios.pinkified.client.gui.screen.PinkSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;

/**
 * CI launch check. Only active when the client is started with the environment variable {@code PINKIFIED_SELFTEST=1}
 * (or {@code -Dpinkified.selftest=true}); it does nothing in normal play. Once the main menu is up it loads every
 * mixin target (a mixin that no longer applies fails here), opens the settings screen, goes back to the menu, logs
 * {@code [pinkified] self-test passed} and quits. Any error is logged as {@code [pinkified] self-test FAILED}.
 */
public final class SelfTest {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String[] MIXIN_TARGETS = {
        "net.minecraft.client.gui.Gui",
        "net.minecraft.client.sounds.SoundEngine",
        "net.minecraft.client.gui.screens.DeathScreen",
        "net.minecraft.client.gui.screens.inventory.AbstractContainerScreen",
        "net.minecraft.client.gui.screens.inventory.InventoryScreen",
        "net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil",
    };
    private static final boolean ENABLED = "1".equals(System.getenv("PINKIFIED_SELFTEST")) || Boolean.getBoolean("pinkified.selftest");

    private static int stage;
    private static int ticks;
    private static boolean done;

    private SelfTest() {
    }

    public static boolean enabled() {
        return ENABLED && !done;
    }

    private static String name(Screen screen) {
        return screen == null ? "none" : screen.getClass().getName();
    }

    /** End of every client tick, only while {@link #enabled()}. */
    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        try {
            switch (stage) {
                case 0 -> {
                    // wait for the loading overlay to go and the menu to settle
                    if (mc.getOverlay() != null || mc.screen == null) {
                        ticks = 0;
                        return;
                    }
                    if (++ticks < 100) return;
                    LOGGER.info("[pinkified] self-test: menu up ({})", name(mc.screen));
                    for (String target : MIXIN_TARGETS) Class.forName(target, true, SelfTest.class.getClassLoader());
                    LOGGER.info("[pinkified] self-test: {} mixin targets loaded", MIXIN_TARGETS.length);
                    mc.setScreen(new PinkSettingsScreen(mc.screen));
                    next();
                }
                case 1 -> {
                    if (++ticks < 60) return;
                    if (!(mc.screen instanceof PinkSettingsScreen)) {
                        throw new IllegalStateException("the settings screen did not stay open: " + name(mc.screen));
                    }
                    LOGGER.info("[pinkified] self-test: settings screen rendered");
                    mc.setScreen(null);
                    next();
                }
                default -> {
                    if (++ticks < 60) return;
                    LOGGER.info("[pinkified] self-test passed (back on {})", name(mc.screen));
                    done = true;
                    mc.stop();
                }
            }
        } catch (Throwable t) {
            LOGGER.error("[pinkified] self-test FAILED", t);
            done = true;
            mc.stop();
        }
    }

    private static void next() {
        stage++;
        ticks = 0;
    }
}
