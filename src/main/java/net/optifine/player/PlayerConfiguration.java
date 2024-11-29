package net.optifine.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.optifine.Config;

public class PlayerConfiguration {
   private PlayerItemModel[] playerItemModels = new PlayerItemModel[0];
   private boolean initialized = false;

   public void renderPlayerItems(HumanoidModel modelBiped, AbstractClientPlayer player, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, int packedOverlayIn) {
      if (this.initialized) {
         for(int i = 0; i < this.playerItemModels.length; ++i) {
            PlayerItemModel playeritemmodel = this.playerItemModels[i];
            playeritemmodel.render(modelBiped, player, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
         }

      }
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public void setInitialized(boolean initialized) {
      this.initialized = initialized;
   }

   public PlayerItemModel[] getPlayerItemModels() {
      return this.playerItemModels;
   }

   public void addPlayerItemModel(PlayerItemModel playerItemModel) {
      this.playerItemModels = (PlayerItemModel[])Config.addObjectToArray(this.playerItemModels, playerItemModel);
   }
}