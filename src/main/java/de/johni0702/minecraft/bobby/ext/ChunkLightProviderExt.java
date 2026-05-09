package de.johni0702.minecraft.bobby.ext;

import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;

public interface ChunkLightProviderExt {
    void bobby_addSectionData(long pos, DataLayer data);
    void bobby_removeSectionData(long pos);

    void bobby_setTainted(long pos, int delta);

    static ChunkLightProviderExt get(LayerLightEventListener view) {
        return (view instanceof ChunkLightProviderExt) ? (ChunkLightProviderExt) view : null;
    }
}
