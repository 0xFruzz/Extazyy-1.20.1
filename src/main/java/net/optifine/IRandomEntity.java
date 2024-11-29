package net.optifine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public interface IRandomEntity {
   int getId();

   BlockPos getSpawnPosition();

   Biome getSpawnBiome();

   String getName();

   int getHealth();

   int getMaxHealth();

   CompoundTag getNbtTag();

   DyeColor getColor();

   BlockState getBlockState();
}