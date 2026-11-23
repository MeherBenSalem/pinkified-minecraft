package com.sugarplumstudios.pinkified.fabric.mixin;

import com.sugarplumstudios.pinkified.client.PinkifiedClient;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge's TextureStitchEvent.Post: runs once an atlas has been stitched and uploaded. */
@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin {
    @Inject(method = "upload", at = @At("TAIL"))
    private void pinkified$stitched(SpriteLoader.Preparations preparations, CallbackInfo ci) {
        PinkifiedClient.onAtlasStitched((TextureAtlas) (Object) this);
    }
}
