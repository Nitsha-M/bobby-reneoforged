package de.johni0702.minecraft.bobby;

import de.johni0702.minecraft.bobby.ext.ChunkLightProviderExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.ticks.LevelChunkTicks;

// Fake chunks are of this subclass, primarily so we have an easy way of identifying them.
public class FakeChunk extends LevelChunk {

    private boolean isTainted;

    // Keeping these around, so we can safely serialize the chunk from any thread
    public DataLayer[] blockLight;
    public DataLayer[] skyLight;
    public ListTag serializedBlockEntities;

    public FakeChunk(Level world, ChunkPos pos, LevelChunkSection[] sections) {
        super(world, pos, UpgradeData.EMPTY, new LevelChunkTicks<>(), new LevelChunkTicks<>(), 0L, sections, null, null);
    }

    public void setTainted(boolean enabled) {
        if (isTainted == enabled) {
            return;
        }
        isTainted = enabled;

        Minecraft client = Minecraft.getInstance();
        double gamma = client.options.gamma().get();
        LevelRenderer worldRenderer = client.levelRenderer;

        LevelLightEngine lightingProvider = getLevel().getLightEngine();
        ChunkLightProviderExt blockLightProvider = ChunkLightProviderExt.get(lightingProvider.getLayerListener(LightLayer.BLOCK));
        ChunkLightProviderExt skyLightProvider = ChunkLightProviderExt.get(lightingProvider.getLayerListener(LightLayer.SKY));

        int blockDelta = enabled ? 5 : 0;
        int skyDelta = enabled ? -3 + (int) (-7 * gamma) : 0;

        int x = getPos().x;
        int z = getPos().z;
        for (int y = getMinSection(); y < getMaxSection(); y++) {
            updateTaintedState(blockLightProvider, x, y, z, blockDelta);
            updateTaintedState(skyLightProvider, x, y, z, skyDelta);
            worldRenderer.setSectionDirty(x, y, z);
        }
    }

    private void updateTaintedState(ChunkLightProviderExt lightProvider, int x, int y, int z, int delta) {
        if (lightProvider == null) {
            return;
        }
        lightProvider.bobby_setTainted(SectionPos.asLong(x, y, z), delta);
    }

    public void setHeightmap(Heightmap.Types type, Heightmap heightmap) {
        this.heightmaps.put(type, heightmap);
    }
}
