package de.johni0702.minecraft.bobby.ext;

import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public interface WorldChunkExt {
    void bobby_setInitialLightData(@Nullable ClientboundLightUpdatePacketData data);
    @Nullable ClientboundLightUpdatePacketData bobby_getInitialLightData();

    static WorldChunkExt get(LevelChunk chunk) {
        return (chunk instanceof WorldChunkExt) ? (WorldChunkExt) chunk : null;
    }
}
