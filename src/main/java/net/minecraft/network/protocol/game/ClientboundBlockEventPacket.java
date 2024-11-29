package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;

public class ClientboundBlockEventPacket implements Packet<ClientGamePacketListener> {
   private final BlockPos pos;
   private final int b0;
   private final int b1;
   private final Block block;

   public ClientboundBlockEventPacket(BlockPos pPos, Block pBlock, int pB0, int pB1) {
      this.pos = pPos;
      this.block = pBlock;
      this.b0 = pB0;
      this.b1 = pB1;
   }

   public ClientboundBlockEventPacket(FriendlyByteBuf pBuffer) {
      this.pos = pBuffer.readBlockPos();
      this.b0 = pBuffer.readUnsignedByte();
      this.b1 = pBuffer.readUnsignedByte();
      this.block = pBuffer.readById(BuiltInRegistries.BLOCK);
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeByte(this.b0);
      pBuffer.writeByte(this.b1);
      pBuffer.writeId(BuiltInRegistries.BLOCK, this.block);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleBlockEvent(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int getB0() {
      return this.b0;
   }

   public int getB1() {
      return this.b1;
   }

   public Block getBlock() {
      return this.block;
   }
}