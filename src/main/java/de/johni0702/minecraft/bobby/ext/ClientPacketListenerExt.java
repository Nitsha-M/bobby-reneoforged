package de.johni0702.minecraft.bobby.ext;

import net.minecraft.client.multiplayer.ClientPacketListener;

public interface ClientPacketListenerExt {
    void bobby_queueUnloadFakeLightDataTask(Runnable runnable);

    static ClientPacketListenerExt get(ClientPacketListener handler) {
        return (ClientPacketListenerExt) handler;
    }
}
