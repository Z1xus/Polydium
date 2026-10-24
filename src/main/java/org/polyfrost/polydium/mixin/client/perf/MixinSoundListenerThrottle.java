package org.polyfrost.polydium.mixin.client.perf;

import org.polyfrost.polydium.client.PolydiumToggles;

//? if >= 1.21.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.audio.ListenerTransform;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundEngineExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class MixinSoundListenerThrottle {
    @Unique
    private static final long POLYDIUM$MIN_INTERVAL_NANOS = 5_000_000L;

    @Unique
    private ListenerTransform polydium$sentTransform;
    @Unique
    private long polydium$sentNanos;

    @WrapOperation(
        method = "updateSource",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundEngineExecutor;execute(Ljava/lang/Runnable;)V")
    )
    private void polydium$throttleListener(SoundEngineExecutor executor, Runnable task, Operation<Void> original, @Local ListenerTransform transform) {
        if (!PolydiumToggles.soundListenerThrottle) {
            this.polydium$sentTransform = null;
            original.call(executor, task);
            return;
        }
        ListenerTransform sent = this.polydium$sentTransform;
        if (sent != null && transform.position().equals(sent.position()) && transform.forward().equals(sent.forward()) && transform.up().equals(sent.up())) {
            return;
        }
        long now = System.nanoTime();
        if (sent != null && now - this.polydium$sentNanos < POLYDIUM$MIN_INTERVAL_NANOS) {
            return;
        }
        original.call(executor, task);
        this.polydium$sentTransform = transform;
        this.polydium$sentNanos = now;
    }

    @Inject(method = {"loadLibrary", "stopAll"}, at = @At("HEAD"))
    private void polydium$forgetSentListener(CallbackInfo ci) {
        this.polydium$sentTransform = null;
    }
}
//?} else {
/*import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.sound.system.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.SoundSystem;

@Mixin(SoundEngine.class)
public class MixinSoundListenerThrottle {
    @Unique
    private final float[] polydium$sentPosition = new float[3];
    @Unique
    private final float[] polydium$sentOrientation = new float[6];
    @Unique
    private boolean polydium$sent;

    @WrapWithCondition(
        method = "update",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/system/SoundEngine$System;setListenerPosition(FFF)V", remap = false)
    )
    private boolean polydium$throttlePosition(@Coerce SoundSystem system, float x, float y, float z) {
        float[] sent = this.polydium$sentPosition;
        if (PolydiumToggles.soundListenerThrottle && this.polydium$sent && sent[0] == x && sent[1] == y && sent[2] == z) {
            return false;
        }
        sent[0] = x;
        sent[1] = y;
        sent[2] = z;
        return true;
    }

    @WrapWithCondition(
        method = "update",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/system/SoundEngine$System;setListenerOrientation(FFFFFF)V", remap = false)
    )
    private boolean polydium$throttleOrientation(@Coerce SoundSystem system, float lookX, float lookY, float lookZ, float upX, float upY, float upZ) {
        float[] sent = this.polydium$sentOrientation;
        if (PolydiumToggles.soundListenerThrottle && this.polydium$sent && sent[0] == lookX && sent[1] == lookY && sent[2] == lookZ
                && sent[3] == upX && sent[4] == upY && sent[5] == upZ) {
            return false;
        }
        sent[0] = lookX;
        sent[1] = lookY;
        sent[2] = lookZ;
        sent[3] = upX;
        sent[4] = upY;
        sent[5] = upZ;
        this.polydium$sent = PolydiumToggles.soundListenerThrottle;
        return true;
    }

    @Inject(method = "start", at = @At("HEAD"))
    private void polydium$forgetSentListener(CallbackInfo ci) {
        this.polydium$sent = false;
    }
}
*///?}
