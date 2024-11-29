package net.minecraftforge.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class PartEntity<T extends Entity> extends Entity {
   private final T parent;

   public PartEntity(T parent) {
      super(parent.getType(), parent.level());
      this.parent = parent;
   }

   public T getParent() {
      return this.parent;
   }

   public Packet getAddEntityPacket() {
      throw new UnsupportedOperationException();
   }

   protected void defineSynchedData() {
      throw new UnsupportedOperationException();
   }

   protected void readAdditionalSaveData(CompoundTag compound) {
      throw new UnsupportedOperationException();
   }

   protected void addAdditionalSaveData(CompoundTag compound) {
      throw new UnsupportedOperationException();
   }
}