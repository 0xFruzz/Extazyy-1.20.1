package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ServerboundUseItemPacket implements Packet<ServerGamePacketListener> {
   private final InteractionHand hand;
   private final int sequence;

   public ServerboundUseItemPacket(InteractionHand pHand, int pSequence) {
      this.hand = pHand;
      this.sequence = pSequence;
   }

   public ServerboundUseItemPacket(FriendlyByteBuf pBuffer) {
      this.hand = pBuffer.readEnum(InteractionHand.class);
      this.sequence = pBuffer.readVarInt();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeEnum(this.hand);
      pBuffer.writeVarInt(this.sequence);
   }

   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleUseItem(this);
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   public int getSequence() {
      return this.sequence;
   }
}