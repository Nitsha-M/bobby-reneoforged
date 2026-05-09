package de.johni0702.minecraft.bobby.ext;

import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;

public interface LightEngineExt {
    void bobby_addSectionData(long pos, DataLayer data);
    void bobby_removeSectionData(long pos);

    void bobby_setTainted(long pos, int delta);

    static LightEngineExt get(LayerLightEventListener view) {
        return (view instanceof LightEngineExt) ? (LightEngineExt) view : null;
    }
}
