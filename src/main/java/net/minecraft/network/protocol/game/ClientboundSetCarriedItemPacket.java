package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetCarriedItemPacket implements Packet<ClientGamePacketListener> {
   private final int slot;

   public ClientboundSetCarriedItemPacket(int pSlot) {
      this.slot = pSlot;
   }

   public ClientboundSetCarriedItemPacket(FriendlyByteBuf pBuffer) {
      this.slot = pBuffer.readByte();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.slot);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetCarriedItem(this);
   }

   public int getSlot() {
      return this.slot;
   }
}