package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundSetCameraPacket implements Packet<ClientGamePacketListener> {
   private final int cameraId;

   public ClientboundSetCameraPacket(Entity pCameraEntity) {
      this.cameraId = pCameraEntity.getId();
   }

   public ClientboundSetCameraPacket(FriendlyByteBuf pBuffer) {
      this.cameraId = pBuffer.readVarInt();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.cameraId);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetCamera(this);
   }

   @Nullable
   public Entity getEntity(Level pLevel) {
      return pLevel.getEntity(this.cameraId);
   }
}