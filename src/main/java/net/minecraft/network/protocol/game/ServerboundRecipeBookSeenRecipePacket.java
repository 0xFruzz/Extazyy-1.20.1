package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ServerboundRecipeBookSeenRecipePacket implements Packet<ServerGamePacketListener> {
   private final ResourceLocation recipe;

   public ServerboundRecipeBookSeenRecipePacket(Recipe<?> pRecipe) {
      this.recipe = pRecipe.getId();
   }

   public ServerboundRecipeBookSeenRecipePacket(FriendlyByteBuf pBuffer) {
      this.recipe = pBuffer.readResourceLocation();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeResourceLocation(this.recipe);
   }

   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleRecipeBookSeenRecipePacket(this);
   }

   public ResourceLocation getRecipe() {
      return this.recipe;
   }
}