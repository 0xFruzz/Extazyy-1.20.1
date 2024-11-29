package net.minecraft.network.protocol.login;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundHelloPacket(String name, Optional<UUID> profileId) implements Packet<ServerLoginPacketListener> {
   public ServerboundHelloPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readUtf(16), pBuffer.readOptional(FriendlyByteBuf::readUUID));
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeUtf(this.name, 16);
      pBuffer.writeOptional(this.profileId, FriendlyByteBuf::writeUUID);
   }

   public void handle(ServerLoginPacketListener pHandler) {
      pHandler.handleHello(this);
   }
}