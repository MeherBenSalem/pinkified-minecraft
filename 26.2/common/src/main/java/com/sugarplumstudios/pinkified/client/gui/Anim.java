package com.sugarplumstudios.pinkified.client.gui;

import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import net.minecraft.util.Util;

/** Time and easing helpers that respect the player's animation settings. */
public final class Anim {
    private Anim() {
    }

    public static boolean enabled() {
        return PinkConfig.on(PinkConfig.ANIMATIONS);
    }

    /** 0 when animations are off, otherwise the configured intensity (0.1 - 2). */
    public static float intensity() {
        return enabled() ? PinkConfig.get(PinkConfig.ANIMATION_INTENSITY).floatValue() : 0f;
    }

    /** Seconds on a monotonic clock, wrapped to keep float precision. */
    public static float seconds() {
        return (Util.getMillis() % 3_600_000L) / 1000f;
    }

    public static long millis() {
        return Util.getMillis();
    }

    /** Frame-rate independent exponential approach toward {@code target}. */
    public static float approach(float current, float target, float dtSeconds, float speed) {
        if (!enabled()) return target;
        float k = 1f - (float) Math.exp(-speed * dtSeconds);
        return current + (target - current) * k;
    }

    public static float clamp01(float t) {
        return t < 0 ? 0 : (t > 1 ? 1 : t);
    }

    public static float easeOutCubic(float t) {
        t = clamp01(t);
        float u = 1 - t;
        return 1 - u * u * u;
    }

    public static float easeOutBack(float t) {
        t = clamp01(t);
        float c1 = 1.70158f, c3 = c1 + 1;
        float u = t - 1;
        return 1 + c3 * u * u * u + c1 * u * u;
    }

    public static float easeInOutSine(float t) {
        t = clamp01(t);
        return (float) (-(Math.cos(Math.PI * t) - 1) / 2);
    }

    /** Smooth 0..1..0 pulse with the given period in seconds. */
    public static float pulse(float period, float phase) {
        return 0.5f + 0.5f * (float) Math.sin((seconds() / period + phase) * Math.PI * 2);
    }
}
