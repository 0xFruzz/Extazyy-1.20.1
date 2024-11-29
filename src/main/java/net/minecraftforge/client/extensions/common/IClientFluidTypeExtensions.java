package net.minecraftforge.client.extensions.common;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidType;

public interface IClientFluidTypeExtensions {
   IClientFluidTypeExtensions DUMMY = new IClientFluidTypeExtensions() {
   };

   static IClientFluidTypeExtensions of(FluidState fluidState) {
      return DUMMY;
   }

   static IClientFluidTypeExtensions of(FluidType fluidType) {
      return DUMMY;
   }

   default int getColorTint() {
      return -1;
   }

   default void renderOverlay(Minecraft mc, PoseStack stack) {
   }

   default int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
      return this.getColorTint();
   }
}