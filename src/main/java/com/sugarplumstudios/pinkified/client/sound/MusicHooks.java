package com.sugarplumstudios.pinkified.client.sound;

import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.util.Ids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Swaps the vanilla menu music for the Sugarplum Lullaby. Vanilla's music manager keeps doing
 * the scheduling (and the Music volume slider still applies); whenever it tries to start a menu
 * track, our track plays instead, and never twice at once.
 */
public final class MusicHooks {
    private static final ResourceLocation VANILLA_MENU = Ids.vanilla("music.menu");
    private static SoundInstance current;

    private MusicHooks() {
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null || sound == current || sound.getSource() != SoundSource.MUSIC) return;
        if (!VANILLA_MENU.equals(sound.getLocation()) || !PinkConfig.on(PinkConfig.MENU_MUSIC)) return;
        SoundManager sm = Minecraft.getInstance().getSoundManager();
        if (current != null && sm.isActive(current)) {
            event.setSound(null);
            return;
        }
        current = SimpleSoundInstance.forMusic(PinkSounds.MENU_MUSIC);
        event.setSound(current);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
