package com.sugarplumstudios.pinkified.client.sound;

import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.util.Ids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundSource;

import javax.annotation.Nullable;

/**
 * Swaps the vanilla menu music for the Sugarplum Lullaby. Vanilla's music manager keeps doing
 * the scheduling (and the Music volume slider still applies); whenever it tries to start a menu
 * track, our track plays instead, and never twice at once.
 */
public final class MusicHooks {
    private static final Identifier VANILLA_MENU = Ids.vanilla("music.menu");
    private static SoundInstance current;

    private MusicHooks() {
    }

    /** A sound is about to play; returns the sound to play instead, or null to play nothing. */
    @Nullable
    public static SoundInstance onPlaySound(@Nullable SoundInstance sound) {
        if (sound == null || sound == current || sound.getSource() != SoundSource.MUSIC) return sound;
        if (!VANILLA_MENU.equals(sound.getLocation()) || !PinkConfig.on(PinkConfig.MENU_MUSIC)) return sound;
        SoundManager sm = Minecraft.getInstance().getSoundManager();
        if (current != null && sm.isActive(current)) return null;
        current = SimpleSoundInstance.forMusic(PinkSounds.MENU_MUSIC);
        return current;
    }

    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        SoundManager sm = mc.getSoundManager();
        boolean enabled = PinkConfig.on(PinkConfig.MENU_MUSIC);
        if (!enabled) {
            if (current != null) {
                sm.stop(current);
                current = null;
            }
            return;
        }
        // switched on while vanilla menu music is playing: change tracks right away
        if (mc.level == null && mc.getMusicManager().isPlayingMusic(Musics.MENU) && (current == null || !sm.isActive(current))) {
            mc.getMusicManager().stopPlaying();
            current = SimpleSoundInstance.forMusic(PinkSounds.MENU_MUSIC);
            sm.play(current);
        }
    }
}
