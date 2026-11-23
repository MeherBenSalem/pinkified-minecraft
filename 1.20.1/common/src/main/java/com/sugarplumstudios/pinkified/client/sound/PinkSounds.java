package com.sugarplumstudios.pinkified.client.sound;

import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.util.Ids;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Pinkified's sounds. They are deliberately not registered in the sound-event registry: the
 * client resolves them from {@code sounds.json} by id, which keeps the mod 100% client-side and
 * safe to use on any server.
 */
public final class PinkSounds {
    public static final SoundEvent MENU_MUSIC = event("music.menu");
    public static final SoundEvent UI_CLICK = event("ui.click");
    public static final SoundEvent UI_CHIME = event("ui.chime");

    private PinkSounds() {
    }

    private static SoundEvent event(String path) {
        return SoundEvent.createVariableRangeEvent(Ids.mod(path));
    }

    public static void click(SoundManager sm) {
        if (PinkConfig.on(PinkConfig.UI_SOUNDS)) sm.play(SimpleSoundInstance.forUI(UI_CLICK, 1.0F, 0.7F));
        else sm.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public static void chime(SoundManager sm, float pitch) {
        if (PinkConfig.on(PinkConfig.UI_SOUNDS)) sm.play(SimpleSoundInstance.forUI(UI_CHIME, pitch, 0.6F));
        else sm.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
