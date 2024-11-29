package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetTitleTextPacket implements Packet<ClientGamePacketListener> {
   private final Component text;

   public ClientboundSetTitleTextPacket(Component pText) {
      this.text = pText;
   }

   public ClientboundSetTitleTextPacket(FriendlyByteBuf pBuffer) {
      this.text = pBuffer.readComponent();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeComponent(this.text);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.setTitleText(this);
   }

   public Component getText() {
      return this.text;
   }
}