package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ServerboundPlaceRecipePacket implements Packet<ServerGamePacketListener> {
   private final int containerId;
   private final ResourceLocation recipe;
   private final boolean shiftDown;

   public ServerboundPlaceRecipePacket(int pContainerId, Recipe<?> pRecipe, boolean pShiftDown) {
      this.containerId = pContainerId;
      this.recipe = pRecipe.getId();
      this.shiftDown = pShiftDown;
   }

   public ServerboundPlaceRecipePacket(FriendlyByteBuf pBuffer) {
      this.containerId = pBuffer.readByte();
      this.recipe = pBuffer.readResourceLocation();
      this.shiftDown = pBuffer.readBoolean();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeResourceLocation(this.recipe);
      pBuffer.writeBoolean(this.shiftDown);
   }

   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handlePlaceRecipe(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public ResourceLocation getRecipe() {
      return this.recipe;
   }

   public boolean isShiftDown() {
      return this.shiftDown;
   }
}