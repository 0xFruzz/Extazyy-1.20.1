package net.optifine.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.optifine.reflect.Reflector;

public class TileEntityUtils {
   public static String getTileEntityName(BlockGetter blockAccess, BlockPos blockPos) {
      BlockEntity blockentity = blockAccess.getBlockEntity(blockPos);
      return getTileEntityName(blockentity);
   }

   public static String getTileEntityName(BlockEntity te) {
      if (!(te instanceof Nameable nameable)) {
         return null;
      } else {
         updateTileEntityName(te);
         return !nameable.hasCustomName() ? null : nameable.getCustomName().getString();
      }
   }

   public static void updateTileEntityName(BlockEntity te) {
      BlockPos blockpos = te.getBlockPos();
      Component component = getTileEntityRawName(te);
      if (component == null) {
         Component component1 = getServerTileEntityRawName(blockpos);
         if (component1 == null) {
            component1 = Component.literal("");
         }

         setTileEntityRawName(te, component1);
      }
   }

   public static Component getServerTileEntityRawName(BlockPos blockPos) {
      BlockEntity blockentity = IntegratedServerUtils.getTileEntity(blockPos);
      return blockentity == null ? null : getTileEntityRawName(blockentity);
   }

   public static Component getTileEntityRawName(BlockEntity te) {
      if (te instanceof Nameable) {
         return ((Nameable)te).getCustomName();
      } else {
         return te instanceof BeaconBlockEntity ? (Component)Reflector.getFieldValue(te, Reflector.TileEntityBeacon_customName) : null;
      }
   }

   public static boolean setTileEntityRawName(BlockEntity te, Component name) {
      if (te instanceof BaseContainerBlockEntity) {
         ((BaseContainerBlockEntity)te).setCustomName(name);
         return true;
      } else if (te instanceof BannerBlockEntity) {
         ((BannerBlockEntity)te).setCustomName(name);
         return true;
      } else if (te instanceof EnchantmentTableBlockEntity) {
         ((EnchantmentTableBlockEntity)te).setCustomName(name);
         return true;
      } else if (te instanceof BeaconBlockEntity) {
         ((BeaconBlockEntity)te).setCustomName(name);
         return true;
      } else {
         return false;
      }
   }
}