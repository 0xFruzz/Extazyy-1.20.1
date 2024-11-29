package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundSetEntityLinkPacket implements Packet<ClientGamePacketListener> {
   private final int sourceId;
   private final int destId;

   public ClientboundSetEntityLinkPacket(Entity pSource, @Nullable Entity pDestination) {
      this.sourceId = pSource.getId();
      this.destId = pDestination != null ? pDestination.getId() : 0;
   }

   public ClientboundSetEntityLinkPacket(FriendlyByteBuf pBuffer) {
      this.sourceId = pBuffer.readInt();
      this.destId = pBuffer.readInt();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeInt(this.sourceId);
      pBuffer.writeInt(this.destId);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleEntityLinkPacket(this);
   }

   public int getSourceId() {
      return this.sourceId;
   }

   public int getDestId() {
      return this.destId;
   }
}