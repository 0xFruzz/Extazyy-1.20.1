package net.optifine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.optifine.util.TileEntityUtils;

public class RandomTileEntity implements IRandomEntity {
   private BlockEntity tileEntity;

   public int getId() {
      return Config.getRandom(this.getSpawnPosition(), 0);
   }

   public BlockPos getSpawnPosition() {
      if (this.tileEntity instanceof BedBlockEntity) {
         BedBlockEntity bedblockentity = (BedBlockEntity)this.tileEntity;
         BlockState blockstate = bedblockentity.getBlockState();
         BedPart bedpart = blockstate.getValue(BedBlock.PART);
         if (bedpart == BedPart.HEAD) {
            Direction direction = blockstate.getValue(BedBlock.FACING);
            return this.tileEntity.getBlockPos().relative(direction.getOpposite());
         }
      }

      return this.tileEntity.getBlockPos();
   }

   public String getName() {
      return TileEntityUtils.getTileEntityName(this.tileEntity);
   }

   public Biome getSpawnBiome() {
      return this.tileEntity.getLevel().getBiome(this.tileEntity.getBlockPos()).value();
   }

   public int getHealth() {
      return -1;
   }

   public int getMaxHealth() {
      return -1;
   }

   public BlockEntity getTileEntity() {
      return this.tileEntity;
   }

   public void setTileEntity(BlockEntity tileEntity) {
      this.tileEntity = tileEntity;
   }

   public CompoundTag getNbtTag() {
      CompoundTag compoundtag = this.tileEntity.nbtTag;
      long i = System.currentTimeMillis();
      if (compoundtag == null || this.tileEntity.nbtTagUpdateMs < i - 1000L) {
         this.tileEntity.nbtTag = this.tileEntity.saveWithoutMetadata();
         this.tileEntity.nbtTagUpdateMs = i;
      }

      return compoundtag;
   }

   public DyeColor getColor() {
      return RandomEntityRule.getBlockEntityColor(this.tileEntity);
   }

   public BlockState getBlockState() {
      return this.tileEntity.getBlockState();
   }

   public String toString() {
      return this.tileEntity.toString();
   }
}