package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundSetSimulationDistancePacket(int simulationDistance) implements Packet<ClientGamePacketListener> {
   public ClientboundSetSimulationDistancePacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readVarInt());
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.simulationDistance);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetSimulationDistance(this);
   }
}