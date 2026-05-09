package de.johni0702.minecraft.bobby.mixin.sodium06;

import de.johni0702.minecraft.bobby.FakeChunk;
import de.johni0702.minecraft.bobby.ext.ClientChunkCacheExt;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPacketListener.class, priority = 1100) // higher than Sodium so we get to run after it runs
public abstract class SodiumClientPacketListenerMixin {
    @Shadow
    private ClientLevel level;

    @Inject(method = "handleForgetLevelChunk", at = @At("RETURN"))
    private void keepChunkRenderedIfReplacedByFakeChunk(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
        int x = packet.pos().x();
        int z = packet.pos().z();
        LevelChunk chunk = this.level.getChunk(x, z);
        // Sodium removes the block and light flags from the unloaded chunk at the end of this method.
        // We however load our fake chunk at the end of the unload method in ClientChunkManager, so Sodium naturally
        // would get the last word and un-render the chunk. To prevent that, when we have replaced it with a fake chunk,
        // we simply re-add the flags.
        if (chunk instanceof FakeChunk) {
            ((ClientChunkCacheExt) level.getChunkSource()).bobby_onFakeChunkAdded(x, z);
        }
    }
}
