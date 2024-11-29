package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundGameProfilePacket implements Packet<ClientLoginPacketListener> {
   private final GameProfile gameProfile;

   public ClientboundGameProfilePacket(GameProfile pGameProfile) {
      this.gameProfile = pGameProfile;
   }

   public ClientboundGameProfilePacket(FriendlyByteBuf pBuffer) {
      this.gameProfile = pBuffer.readGameProfile();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeGameProfile(this.gameProfile);
   }

   public void handle(ClientLoginPacketListener pHandler) {
      pHandler.handleGameProfile(this);
   }

   public GameProfile getGameProfile() {
      return this.gameProfile;
   }
}