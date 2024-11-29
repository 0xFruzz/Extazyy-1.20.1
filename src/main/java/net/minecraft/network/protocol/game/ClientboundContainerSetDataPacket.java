package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundContainerSetDataPacket implements Packet<ClientGamePacketListener> {
   private final int containerId;
   private final int id;
   private final int value;

   public ClientboundContainerSetDataPacket(int pContainerId, int pId, int pValue) {
      this.containerId = pContainerId;
      this.id = pId;
      this.value = pValue;
   }

   public ClientboundContainerSetDataPacket(FriendlyByteBuf pBuffer) {
      this.containerId = pBuffer.readUnsignedByte();
      this.id = pBuffer.readShort();
      this.value = pBuffer.readShort();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeShort(this.id);
      pBuffer.writeShort(this.value);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleContainerSetData(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getId() {
      return this.id;
   }

   public int getValue() {
      return this.value;
   }
}