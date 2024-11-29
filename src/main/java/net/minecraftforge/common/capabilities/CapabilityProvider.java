package net.minecraftforge.common.capabilities;

import net.minecraft.nbt.CompoundTag;

public abstract class CapabilityProvider<B> {
   protected CapabilityProvider(Class<B> baseClass) {
   }

   public final void gatherCapabilities() {
   }

   protected final CapabilityDispatcher getCapabilities() {
      return null;
   }

   protected final void deserializeCaps(CompoundTag tag) {
   }

   protected final CompoundTag serializeCaps() {
      return null;
   }

   public void invalidateCaps() {
   }
}