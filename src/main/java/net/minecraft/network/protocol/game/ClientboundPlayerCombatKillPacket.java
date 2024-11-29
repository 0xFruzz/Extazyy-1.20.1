package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundPlayerCombatKillPacket implements Packet<ClientGamePacketListener> {
   private final int playerId;
   private final Component message;

   public ClientboundPlayerCombatKillPacket(int pPlayerId, Component pMessage) {
      this.playerId = pPlayerId;
      this.message = pMessage;
   }

   public ClientboundPlayerCombatKillPacket(FriendlyByteBuf pBuffer) {
      this.playerId = pBuffer.readVarInt();
      this.message = pBuffer.readComponent();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.playerId);
      pBuffer.writeComponent(this.message);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handlePlayerCombatKill(this);
   }

   public boolean isSkippable() {
      return true;
   }

   public int getPlayerId() {
      return this.playerId;
   }

   public Component getMessage() {
      return this.message;
   }
}