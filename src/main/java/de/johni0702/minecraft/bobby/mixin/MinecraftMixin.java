package de.johni0702.minecraft.bobby.mixin;

import de.johni0702.minecraft.bobby.FakeChunkManager;
import de.johni0702.minecraft.bobby.FakeChunkStorage;
import de.johni0702.minecraft.bobby.Worlds;
import de.johni0702.minecraft.bobby.ext.ClientChunkManagerExt;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow private ProfilerFiller profiler;

    @Shadow @Final public Options options;

    @Shadow @Nullable public ClientLevel level;

    @Inject(method = "runTick", at = @At(value = "CONSTANT", args = "stringValue=tick"))
    private void bobbyUpdate(CallbackInfo ci) {
        if (level == null) {
            return;
        }
        FakeChunkManager bobbyChunkManager = ((ClientChunkManagerExt) level.getChunkSource()).bobby_getFakeChunkManager();
        if (bobbyChunkManager == null) {
            return;
        }

        profiler.push("bobbyUpdate");

        int maxFps = options.framerateLimit().get();
        long frameTime = 1_000_000_000 / (maxFps == Options.UNLIMITED_FRAMERATE_CUTOFF ? 120 : maxFps);
        // Arbitrarily choosing 1/4 of frame time as our max budget, that way we're hopefully not noticeable.
        long frameBudget = frameTime / 4;
        long timeLimit = Util.getNanos() + frameBudget;
        bobbyChunkManager.update(false, () -> Util.getNanos() < timeLimit);

        profiler.pop();
    }

    @Inject(method = "clearDownloadedResourcePacks", at = @At("RETURN"))
    private void bobbyClose(CallbackInfo ci) {
        Worlds.closeAll();
        FakeChunkStorage.closeAll();
    }
}
