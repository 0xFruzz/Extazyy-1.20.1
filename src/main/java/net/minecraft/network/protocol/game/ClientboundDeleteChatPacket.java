package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;

public record ClientboundDeleteChatPacket(MessageSignature.Packed messageSignature) implements Packet<ClientGamePacketListener> {
   public ClientboundDeleteChatPacket(FriendlyByteBuf pBuffer) {
      this(MessageSignature.Packed.read(pBuffer));
   }

   public void write(FriendlyByteBuf pBuffer) {
      MessageSignature.Packed.write(pBuffer, this.messageSignature);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleDeleteChat(this);
   }
}