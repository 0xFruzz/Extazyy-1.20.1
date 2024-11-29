package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderWarningDistancePacket implements Packet<ClientGamePacketListener> {
   private final int warningBlocks;

   public ClientboundSetBorderWarningDistancePacket(WorldBorder pWorldBorder) {
      this.warningBlocks = pWorldBorder.getWarningBlocks();
   }

   public ClientboundSetBorderWarningDistancePacket(FriendlyByteBuf pBuffer) {
      this.warningBlocks = pBuffer.readVarInt();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.warningBlocks);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetBorderWarningDistance(this);
   }

   public int getWarningBlocks() {
      return this.warningBlocks;
   }
}