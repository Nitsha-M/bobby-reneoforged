package de.johni0702.minecraft.bobby.mixin;

import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @ModifyVariable(method = "setupColor", at = @At("HEAD"), argsOnly = true)
    private static int clampMaxValue(int viewDistance) {
        return Math.min(viewDistance, 32);
    }
}
