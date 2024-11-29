package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundTagQueryPacket implements Packet<ClientGamePacketListener> {
   private final int transactionId;
   @Nullable
   private final CompoundTag tag;

   public ClientboundTagQueryPacket(int pTransactionId, @Nullable CompoundTag pTag) {
      this.transactionId = pTransactionId;
      this.tag = pTag;
   }

   public ClientboundTagQueryPacket(FriendlyByteBuf pBuffer) {
      this.transactionId = pBuffer.readVarInt();
      this.tag = pBuffer.readNbt();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.transactionId);
      pBuffer.writeNbt(this.tag);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleTagQueryPacket(this);
   }

   public int getTransactionId() {
      return this.transactionId;
   }

   @Nullable
   public CompoundTag getTag() {
      return this.tag;
   }

   public boolean isSkippable() {
      return true;
   }
}