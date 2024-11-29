package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundSystemChatPacket(Component content, boolean overlay) implements Packet<ClientGamePacketListener> {
   public ClientboundSystemChatPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readComponent(), pBuffer.readBoolean());
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeComponent(this.content);
      pBuffer.writeBoolean(this.overlay);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSystemChat(this);
   }

   public boolean isSkippable() {
      return true;
   }
}