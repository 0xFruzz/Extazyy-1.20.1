package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundLoginDisconnectPacket implements Packet<ClientLoginPacketListener> {
   private final Component reason;

   public ClientboundLoginDisconnectPacket(Component pReason) {
      this.reason = pReason;
   }

   public ClientboundLoginDisconnectPacket(FriendlyByteBuf pBuffer) {
      this.reason = Component.Serializer.fromJsonLenient(pBuffer.readUtf(262144));
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeComponent(this.reason);
   }

   public void handle(ClientLoginPacketListener pHandler) {
      pHandler.handleDisconnect(this);
   }

   public Component getReason() {
      return this.reason;
   }
}