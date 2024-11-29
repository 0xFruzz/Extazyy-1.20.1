package net.minecraft.network.protocol.status;

import net.minecraft.network.PacketListener;

public interface ClientStatusPacketListener extends PacketListener {
   void handleStatusResponse(ClientboundStatusResponsePacket pPacket);

   void handlePongResponse(ClientboundPongResponsePacket pPacket);
}