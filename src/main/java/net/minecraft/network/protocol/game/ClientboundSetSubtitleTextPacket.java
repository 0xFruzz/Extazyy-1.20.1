package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetSubtitleTextPacket implements Packet<ClientGamePacketListener> {
   private final Component text;

   public ClientboundSetSubtitleTextPacket(Component pText) {
      this.text = pText;
   }

   public ClientboundSetSubtitleTextPacket(FriendlyByteBuf pBuffer) {
      this.text = pBuffer.readComponent();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeComponent(this.text);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.setSubtitleText(this);
   }

   public Component getText() {
      return this.text;
   }
}