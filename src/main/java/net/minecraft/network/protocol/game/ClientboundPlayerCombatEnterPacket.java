package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPlayerCombatEnterPacket implements Packet<ClientGamePacketListener> {
   public ClientboundPlayerCombatEnterPacket() {
   }

   public ClientboundPlayerCombatEnterPacket(FriendlyByteBuf pBuffer) {
   }

   public void write(FriendlyByteBuf pBuffer) {
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handlePlayerCombatEnter(this);
   }
}