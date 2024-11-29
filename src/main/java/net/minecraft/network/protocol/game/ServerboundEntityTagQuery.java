package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundEntityTagQuery implements Packet<ServerGamePacketListener> {
   private final int transactionId;
   private final int entityId;

   public ServerboundEntityTagQuery(int pTransactionId, int pEntityId) {
      this.transactionId = pTransactionId;
      this.entityId = pEntityId;
   }

   public ServerboundEntityTagQuery(FriendlyByteBuf pBuffer) {
      this.transactionId = pBuffer.readVarInt();
      this.entityId = pBuffer.readVarInt();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.transactionId);
      pBuffer.writeVarInt(this.entityId);
   }

   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleEntityTagQuery(this);
   }

   public int getTransactionId() {
      return this.transactionId;
   }

   public int getEntityId() {
      return this.entityId;
   }
}