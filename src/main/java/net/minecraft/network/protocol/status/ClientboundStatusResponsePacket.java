package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundStatusResponsePacket(ServerStatus status) implements Packet<ClientStatusPacketListener> {
   public ClientboundStatusResponsePacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readJsonWithCodec(ServerStatus.CODEC));
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeJsonWithCodec(ServerStatus.CODEC, this.status);
   }

   public void handle(ClientStatusPacketListener pHandler) {
      pHandler.handleStatusResponse(this);
   }
}