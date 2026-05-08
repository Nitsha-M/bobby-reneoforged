package de.johni0702.minecraft.bobby.mixin;

import de.johni0702.minecraft.bobby.ext.WorldChunkExt;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelChunk.class)
public class WorldChunkMixin implements WorldChunkExt {
    @Unique
    private ClientboundLightUpdatePacketData initialLightData;

    @Override
    public void bobby_setInitialLightData(@Nullable ClientboundLightUpdatePacketData data) {
        this.initialLightData = data;
    }

    @Override
    public @Nullable ClientboundLightUpdatePacketData bobby_getInitialLightData() {
        return initialLightData;
    }
}
