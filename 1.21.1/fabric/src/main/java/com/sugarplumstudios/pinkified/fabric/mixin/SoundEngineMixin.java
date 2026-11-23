package com.sugarplumstudios.pinkified.fabric.mixin;

import com.sugarplumstudios.pinkified.client.sound.MusicHooks;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge's PlaySoundEvent: the menu music is swapped for the Sugarplum Lullaby. */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void pinkified$play(SoundInstance sound, CallbackInfo ci) {
        SoundInstance replacement = MusicHooks.onPlaySound(sound);
        if (replacement == sound) return;
        ci.cancel();
        if (replacement != null) ((SoundEngine) (Object) this).play(replacement);
    }
}
