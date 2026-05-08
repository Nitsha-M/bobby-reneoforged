package de.johni0702.minecraft.bobby.ext;

import net.minecraft.world.level.lighting.LevelLightEngine;

public interface LightingProviderExt {
    void bobby_enabledColumn(long pos);
    void bobby_disableColumn(long pos);

    static LightingProviderExt get(LevelLightEngine provider) {
        return (provider instanceof LightingProviderExt) ? (LightingProviderExt) provider : null;
    }
}
