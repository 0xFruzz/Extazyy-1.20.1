package net.minecraftforge.common.extensions;

import net.minecraftforge.fluids.FluidType;

public interface IForgeLivingEntity extends IForgeEntity {
   default void jumpInFluid(FluidType type) {
   }
}