package net.minecraft.world.entity.boss;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;

public class EnderDragonPart extends Entity {
   public final EnderDragon parentMob;
   public final String name;
   private final EntityDimensions size;

   public EnderDragonPart(EnderDragon pParentMob, String pName, float pWidth, float pHeight) {
      super(pParentMob.getType(), pParentMob.level());
      this.size = EntityDimensions.scalable(pWidth, pHeight);
      this.refreshDimensions();
      this.parentMob = pParentMob;
      this.name = pName;
   }

   protected void defineSynchedData() {
   }

   protected void readAdditionalSaveData(CompoundTag pCompound) {
   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
   }

   public boolean isPickable() {
      return true;
   }

   @Nullable
   public ItemStack getPickResult() {
      return this.parentMob.getPickResult();
   }

   public boolean hurt(DamageSource pSource, float pAmount) {
      return this.isInvulnerableTo(pSource) ? false : this.parentMob.hurt(this, pSource, pAmount);
   }

   public boolean is(Entity pEntity) {
      return this == pEntity || this.parentMob == pEntity;
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      throw new UnsupportedOperationException();
   }

   public EntityDimensions getDimensions(Pose pPose) {
      return this.size;
   }

   public boolean shouldBeSaved() {
      return false;
   }
}