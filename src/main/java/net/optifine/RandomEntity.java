package net.optifine;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public class RandomEntity implements IRandomEntity {
   private Entity entity;

   public int getId() {
      UUID uuid = this.entity.getUUID();
      long i = uuid.getLeastSignificantBits();
      return (int)(i & 2147483647L);
   }

   public BlockPos getSpawnPosition() {
      return this.entity.getEntityData().spawnPosition;
   }

   public Biome getSpawnBiome() {
      return this.entity.getEntityData().spawnBiome;
   }

   public String getName() {
      return this.entity.hasCustomName() ? this.entity.getCustomName().getString() : null;
   }

   public int getHealth() {
      if (!(this.entity instanceof LivingEntity)) {
         return 0;
      } else {
         LivingEntity livingentity = (LivingEntity)this.entity;
         return (int)livingentity.getHealth();
      }
   }

   public int getMaxHealth() {
      if (!(this.entity instanceof LivingEntity)) {
         return 0;
      } else {
         LivingEntity livingentity = (LivingEntity)this.entity;
         return (int)livingentity.getMaxHealth();
      }
   }

   public Entity getEntity() {
      return this.entity;
   }

   public void setEntity(Entity entity) {
      this.entity = entity;
   }

   public CompoundTag getNbtTag() {
      SynchedEntityData synchedentitydata = this.entity.getEntityData();
      CompoundTag compoundtag = synchedentitydata.nbtTag;
      long i = System.currentTimeMillis();
      if (compoundtag == null || synchedentitydata.nbtTagUpdateMs < i - 1000L) {
         compoundtag = new CompoundTag();
         this.entity.saveWithoutId(compoundtag);
         if (this.entity instanceof TamableAnimal) {
            TamableAnimal tamableanimal = (TamableAnimal)this.entity;
            compoundtag.putBoolean("Sitting", tamableanimal.isInSittingPose());
         }

         synchedentitydata.nbtTag = compoundtag;
         synchedentitydata.nbtTagUpdateMs = i;
      }

      return compoundtag;
   }

   public DyeColor getColor() {
      return RandomEntityRule.getEntityColor(this.entity);
   }

   public BlockState getBlockState() {
      if (this.entity instanceof ItemEntity) {
         ItemEntity itementity = (ItemEntity)this.entity;
         Item item = itementity.getItem().getItem();
         if (item instanceof BlockItem) {
            BlockItem blockitem = (BlockItem)item;
            return blockitem.getBlock().defaultBlockState();
         }
      }

      SynchedEntityData synchedentitydata = this.entity.getEntityData();
      BlockState blockstate = synchedentitydata.blockStateOn;
      long i = System.currentTimeMillis();
      if (blockstate == null || synchedentitydata.blockStateOnUpdateMs < i - 50L) {
         BlockPos blockpos = this.entity.blockPosition();
         blockstate = this.entity.getCommandSenderWorld().getBlockState(blockpos);
         if (blockstate.isAir()) {
            blockstate = this.entity.getCommandSenderWorld().getBlockState(blockpos.below());
         }

         synchedentitydata.blockStateOn = blockstate;
         synchedentitydata.blockStateOnUpdateMs = i;
      }

      return blockstate;
   }

   public String toString() {
      return this.entity.toString();
   }
}