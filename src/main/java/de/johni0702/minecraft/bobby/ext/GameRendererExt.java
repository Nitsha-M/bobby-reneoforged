package de.johni0702.minecraft.bobby.ext;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;

public interface GameRendererExt {
    FogRenderer bobby_getSkyFogRenderer();

    static GameRendererExt get(GameRenderer view) {
        return (GameRendererExt) view;
    }
}
