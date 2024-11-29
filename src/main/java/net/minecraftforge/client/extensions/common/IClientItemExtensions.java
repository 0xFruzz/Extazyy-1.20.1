package net.minecraftforge.client.extensions.common;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public interface IClientItemExtensions {
   IClientItemExtensions DUMMY = new IClientItemExtensions() {
   };

   static IClientItemExtensions of(ItemStack itemStack) {
      return DUMMY;
   }

   default boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
      return false;
   }

   default Font getFont(ItemStack stack, IClientItemExtensions.FontContext context) {
      return null;
   }

   default BlockEntityWithoutLevelRenderer getCustomRenderer() {
      return Minecraft.getInstance().getItemRenderer().getBlockEntityRenderer();
   }

   public static enum FontContext {
      ITEM_COUNT,
      TOOLTIP,
      SELECTED_ITEM_NAME;
   }
}