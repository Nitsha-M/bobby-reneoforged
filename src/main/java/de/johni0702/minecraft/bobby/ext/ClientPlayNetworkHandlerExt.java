package de.johni0702.minecraft.bobby.ext;

import net.minecraft.client.multiplayer.ClientPacketListener;

public interface ClientPlayNetworkHandlerExt {
    void bobby_queueUnloadFakeLightDataTask(Runnable runnable);

    static ClientPlayNetworkHandlerExt get(ClientPacketListener handler) {
        return (ClientPlayNetworkHandlerExt) handler;
    }
}
