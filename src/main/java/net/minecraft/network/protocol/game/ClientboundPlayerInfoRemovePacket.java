package net.minecraft.network.protocol.game;

import java.util.List;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerInfoRemovePacket(List<UUID> profileIds) implements Packet<ClientGamePacketListener> {
   public ClientboundPlayerInfoRemovePacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readList(FriendlyByteBuf::readUUID));
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeCollection(this.profileIds, FriendlyByteBuf::writeUUID);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handlePlayerInfoRemove(this);
   }
}