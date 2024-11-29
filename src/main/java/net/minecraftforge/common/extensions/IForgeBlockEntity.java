package net.minecraftforge.common.extensions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;

public interface IForgeBlockEntity {
   AABB INFINITE_EXTENT_AABB = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

   CompoundTag getPersistentData();

   default void requestModelDataUpdate() {
   }

   default void onChunkUnloaded() {
   }
}