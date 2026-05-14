package de.johni0702.minecraft.bobby.mixin.embeddium;

import de.johni0702.minecraft.bobby.ext.ClientChunkManagerExt;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.embeddedt.embeddium.impl.render.chunk.map.ChunkTrackerHolder;
import org.embeddedt.embeddium.impl.render.chunk.map.ChunkStatus;

@Mixin(value = ClientChunkCache.class, priority = 2200)
public abstract class EmbeddiumChunkManagerMixin implements ClientChunkManagerExt {
    @Shadow
    @Final
    ClientLevel level;

    @org.spongepowered.asm.mixin.Unique
    private static final org.apache.logging.log4j.Logger BOBBY_DEBUG_LOGGER = org.apache.logging.log4j.LogManager.getLogger("Bobby/Debug");

    @org.spongepowered.asm.mixin.Unique
    private int bobbyDebugAddCounter = 0;

    @Override
    public void bobby_onFakeChunkAdded(int x, int z) {
        if (Math.abs(x) > 32 || Math.abs(z) > 32) {
            if (bobbyDebugAddCounter++ % 1000 == 0) {
                BOBBY_DEBUG_LOGGER.info("bobby_onFakeChunkAdded: Notified Embeddium of fake chunk at {}, {} (>32 limit)", x, z);
            }
        }
        ChunkTrackerHolder.get(level).onChunkStatusAdded(x, z, ChunkStatus.FLAG_ALL);
    }

    @Override
    public void bobby_onFakeChunkRemoved(int x, int z, boolean willBeReplaced) {
        boolean stillHasLight = willBeReplaced || level.getLightEngine().lightOnInSection(SectionPos.of(x, 0, z));
        ChunkTrackerHolder.get(level).onChunkStatusRemoved(x, z, stillHasLight ? ChunkStatus.FLAG_HAS_BLOCK_DATA : ChunkStatus.FLAG_ALL);
    }
}
