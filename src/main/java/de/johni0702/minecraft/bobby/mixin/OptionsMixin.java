package de.johni0702.minecraft.bobby.mixin;

import de.johni0702.minecraft.bobby.Bobby;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public class OptionsMixin {
    @Shadow
    private @Final OptionInstance<Integer> renderDistance;

    @Inject(method = "getEffectiveRenderDistance", at = @At("HEAD"), cancellable = true)
    private void forceClientDistanceWhenBobbyIsActive(CallbackInfoReturnable<Integer> ci) {
        if (Bobby.getInstance() != null && Bobby.getInstance().isEnabled()) {
            int dist = this.renderDistance.get();
            ci.setReturnValue(dist);
        }
    }

    @ModifyArg(
            method = "<init>",
            slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=options.renderDistance")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance$IntRange;<init>(IIZ)V", ordinal = 0),
            index = 1
    )
    private int considerBobbyMaxRenderDistanceSetting(int vanillaSetting) {
        int bobbySetting = Bobby.getConfigStatic().getMaxRenderDistance();
        return Math.max(vanillaSetting, bobbySetting);
    }
}
