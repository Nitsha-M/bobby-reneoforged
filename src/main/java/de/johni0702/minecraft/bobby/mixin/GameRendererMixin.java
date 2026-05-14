package de.johni0702.minecraft.bobby.mixin;

import de.johni0702.minecraft.bobby.FakeChunkManager;
import de.johni0702.minecraft.bobby.ext.ClientChunkManagerExt;
import de.johni0702.minecraft.bobby.util.FlawlessFrames;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private float renderDistance;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void blockingBobbyUpdate(CallbackInfo ci) {
        ClientLevel world = this.minecraft.level;
        if (world == null) {
            return;
        }

        FakeChunkManager bobbyChunkManager = ((ClientChunkManagerExt) world.getChunkSource()).bobby_getFakeChunkManager();
        if (bobbyChunkManager == null) {
            return;
        }

        this.minecraft.getProfiler().push("bobbyUpdate");

        if (FlawlessFrames.isActive()) {
            bobbyChunkManager.update(true, () -> true);
        }

        this.minecraft.getProfiler().pop();
    }

    @Inject(method = "getDepthFar", at = @At("HEAD"), cancellable = true)
    private void bobbyLimitDepthFar(CallbackInfoReturnable<Float> cir) {
        // Vanilla normally returns `this.renderDistance * 4.0F`.
        // Mods like Auroras inject at RETURN and multiply this value. 
        // If the render distance is > 32 chunks, the multiplier causes an overflow that breaks frustum culling.
        // We cap the base distance to 32 chunks (512 blocks) so Auroras receives a value it can safely multiply.
        float cappedDistance = Math.min(this.renderDistance, 32.0F * 16.0F);
        cir.setReturnValue(cappedDistance * 4.0F);
    }
}
