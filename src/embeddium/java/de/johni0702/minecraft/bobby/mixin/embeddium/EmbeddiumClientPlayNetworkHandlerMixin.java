package de.johni0702.minecraft.bobby.mixin.embeddium;

import de.johni0702.minecraft.bobby.FakeChunk;
import de.johni0702.minecraft.bobby.ext.ClientChunkManagerExt;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPacketListener.class, priority = 2200) // higher than Sodium so we get to run after it runs
public abstract class EmbeddiumClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientLevel level;

    @Inject(method = "handleForgetLevelChunk", at = @At("RETURN"))
    private void keepChunkRenderedIfReplacedByFakeChunk(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
        int x = packet.pos().x;
        int z = packet.pos().z;
        LevelChunk chunk = this.level.getChunk(x, z);
        if (chunk instanceof FakeChunk) {
            ((ClientChunkManagerExt) level.getChunkSource()).bobby_onFakeChunkAdded(x, z);
        }
    }
}
