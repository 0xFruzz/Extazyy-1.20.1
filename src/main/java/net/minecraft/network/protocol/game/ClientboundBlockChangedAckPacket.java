package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundBlockChangedAckPacket(int sequence) implements Packet<ClientGamePacketListener> {
   public ClientboundBlockChangedAckPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readVarInt());
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.sequence);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleBlockChangedAck(this);
   }
}