package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCustomQueryPacket implements Packet<ClientLoginPacketListener> {
   private static final int MAX_PAYLOAD_SIZE = 1048576;
   private final int transactionId;
   private final ResourceLocation identifier;
   private final FriendlyByteBuf data;

   public ClientboundCustomQueryPacket(int pTransactionId, ResourceLocation pIdentifier, FriendlyByteBuf pData) {
      this.transactionId = pTransactionId;
      this.identifier = pIdentifier;
      this.data = pData;
   }

   public ClientboundCustomQueryPacket(FriendlyByteBuf pBuffer) {
      this.transactionId = pBuffer.readVarInt();
      this.identifier = pBuffer.readResourceLocation();
      int i = pBuffer.readableBytes();
      if (i >= 0 && i <= 1048576) {
         this.data = new FriendlyByteBuf(pBuffer.readBytes(i));
      } else {
         throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
      }
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.transactionId);
      pBuffer.writeResourceLocation(this.identifier);
      pBuffer.writeBytes(this.data.copy());
   }

   public void handle(ClientLoginPacketListener pHandler) {
      pHandler.handleCustomQuery(this);
   }

   public int getTransactionId() {
      return this.transactionId;
   }

   public ResourceLocation getIdentifier() {
      return this.identifier;
   }

   public FriendlyByteBuf getData() {
      return this.data;
   }
}