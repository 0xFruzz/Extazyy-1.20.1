package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.CombatTracker;

public class ClientboundPlayerCombatEndPacket implements Packet<ClientGamePacketListener> {
   private final int duration;

   public ClientboundPlayerCombatEndPacket(CombatTracker pCombatTracker) {
      this(pCombatTracker.getCombatDuration());
   }

   public ClientboundPlayerCombatEndPacket(int pDuration) {
      this.duration = pDuration;
   }

   public ClientboundPlayerCombatEndPacket(FriendlyByteBuf pBuffer) {
      this.duration = pBuffer.readVarInt();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.duration);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handlePlayerCombatEnd(this);
   }
}