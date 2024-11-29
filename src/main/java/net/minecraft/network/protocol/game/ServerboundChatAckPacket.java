package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatAckPacket(int offset) implements Packet<ServerGamePacketListener> {
   public ServerboundChatAckPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readVarInt());
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.offset);
   }

   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleChatAck(this);
   }
}