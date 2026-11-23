package com.sugarplumstudios.pinkified.fabric.mixin;

import com.sugarplumstudios.pinkified.client.sound.MusicHooks;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NeoForge's PlaySoundEvent: the menu music is swapped for the Sugarplum Lullaby. */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void pinkified$play(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        SoundInstance replacement = MusicHooks.onPlaySound(sound);
        if (replacement == sound) return;
        cir.setReturnValue(replacement != null ? ((SoundEngine) (Object) this).play(replacement) : SoundEngine.PlayResult.NOT_STARTED);
    }
}
