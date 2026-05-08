package de.johni0702.minecraft.bobby.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow
    public abstract double getLastViewDistance();

    @ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZF)V"), index = 2)
    private static float clampMaxValue(float viewDistance) {
        return Math.min(viewDistance, 32 * 16);
    }

    @ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;addSkyPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"))
    private GpuBufferSlice clampMaxValue(GpuBufferSlice fogBuffer) {
        if (getLastViewDistance() >= 32) {
            fogBuffer = ((GameRendererExt) minecraft.gameRenderer).bobby_getSkyFogRenderer().getBuffer(FogRenderer.FogMode.WORLD);
        }
        return fogBuffer;
    }
}
