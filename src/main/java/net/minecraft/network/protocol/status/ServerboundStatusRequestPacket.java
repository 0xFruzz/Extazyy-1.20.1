package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundStatusRequestPacket implements Packet<ServerStatusPacketListener> {
   public ServerboundStatusRequestPacket() {
   }

   public ServerboundStatusRequestPacket(FriendlyByteBuf pBuffer) {
   }

   public void write(FriendlyByteBuf pBuffer) {
   }

   public void handle(ServerStatusPacketListener pHandler) {
      pHandler.handleStatusRequest(this);
   }
}