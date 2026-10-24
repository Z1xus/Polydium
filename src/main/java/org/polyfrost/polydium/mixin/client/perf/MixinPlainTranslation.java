package org.polyfrost.polydium.mixin.client.perf;

import org.polyfrost.polydium.client.PolydiumToggles;

//? if = 1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.resource.language.Locale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Locale.class)
public class MixinPlainTranslation {
    @WrapOperation(
        method = "translate",
        at = @At(value = "INVOKE", target = "Ljava/lang/String;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;")
    )
    private String polydium$skipFormat(String format, Object[] args, Operation<String> original) {
        if (!PolydiumToggles.plainTranslation || format.indexOf('%') >= 0) {
            return original.call(format, args);
        }
        return format;
    }
}
*///?}
