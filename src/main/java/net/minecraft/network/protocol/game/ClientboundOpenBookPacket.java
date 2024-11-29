package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ClientboundOpenBookPacket implements Packet<ClientGamePacketListener> {
   private final InteractionHand hand;

   public ClientboundOpenBookPacket(InteractionHand pHand) {
      this.hand = pHand;
   }

   public ClientboundOpenBookPacket(FriendlyByteBuf pBuffer) {
      this.hand = pBuffer.readEnum(InteractionHand.class);
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeEnum(this.hand);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleOpenBook(this);
   }

   public InteractionHand getHand() {
      return this.hand;
   }
}