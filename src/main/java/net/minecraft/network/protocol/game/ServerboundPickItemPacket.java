package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPickItemPacket implements Packet<ServerGamePacketListener> {
   private final int slot;

   public ServerboundPickItemPacket(int pSlot) {
      this.slot = pSlot;
   }

   public ServerboundPickItemPacket(FriendlyByteBuf pBuffer) {
      this.slot = pBuffer.readVarInt();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.slot);
   }

   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handlePickItem(this);
   }

   public int getSlot() {
      return this.slot;
   }
}