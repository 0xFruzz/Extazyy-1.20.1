package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundContainerButtonClickPacket implements Packet<ServerGamePacketListener> {
   private final int containerId;
   private final int buttonId;

   public ServerboundContainerButtonClickPacket(int pContainerId, int pButtonId) {
      this.containerId = pContainerId;
      this.buttonId = pButtonId;
   }

   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleContainerButtonClick(this);
   }

   public ServerboundContainerButtonClickPacket(FriendlyByteBuf pBuffer) {
      this.containerId = pBuffer.readByte();
      this.buttonId = pBuffer.readByte();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeByte(this.buttonId);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getButtonId() {
      return this.buttonId;
   }
}