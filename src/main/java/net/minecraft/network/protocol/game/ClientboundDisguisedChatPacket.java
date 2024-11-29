package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundDisguisedChatPacket(Component message, ChatType.BoundNetwork chatType) implements Packet<ClientGamePacketListener> {
   public ClientboundDisguisedChatPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readComponent(), new ChatType.BoundNetwork(pBuffer));
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeComponent(this.message);
      this.chatType.write(pBuffer);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleDisguisedChat(this);
   }

   public boolean isSkippable() {
      return true;
   }
}