package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundRemoveEntitiesPacket implements Packet<ClientGamePacketListener> {
   private final IntList entityIds;

   public ClientboundRemoveEntitiesPacket(IntList pEntityIds) {
      this.entityIds = new IntArrayList(pEntityIds);
   }

   public ClientboundRemoveEntitiesPacket(int... pEntityIds) {
      this.entityIds = new IntArrayList(pEntityIds);
   }

   public ClientboundRemoveEntitiesPacket(FriendlyByteBuf pBuffer) {
      this.entityIds = pBuffer.readIntIdList();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeIntIdList(this.entityIds);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleRemoveEntities(this);
   }

   public IntList getEntityIds() {
      return this.entityIds;
   }
}