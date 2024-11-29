package net.optifine;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

public class BetterSnow {
   private static BakedModel modelSnowLayer = null;

   public static void update() {
      modelSnowLayer = Config.getMinecraft().getBlockRenderer().getBlockModelShaper().getBlockModel(Blocks.SNOW.defaultBlockState());
   }

   public static BakedModel getModelSnowLayer() {
      return modelSnowLayer;
   }

   public static BlockState getStateSnowLayer() {
      return Blocks.SNOW.defaultBlockState();
   }

   public static boolean shouldRender(BlockAndTintGetter lightReader, BlockState blockState, BlockPos blockPos) {
      if (!(lightReader instanceof BlockGetter)) {
         return false;
      } else {
         return !checkBlock(lightReader, blockState, blockPos) ? false : hasSnowNeighbours(lightReader, blockPos);
      }
   }

   private static boolean hasSnowNeighbours(BlockGetter blockAccess, BlockPos pos) {
      Block block = Blocks.SNOW;
      if (blockAccess.getBlockState(pos.north()).getBlock() == block || blockAccess.getBlockState(pos.south()).getBlock() == block || blockAccess.getBlockState(pos.west()).getBlock() == block || blockAccess.getBlockState(pos.east()).getBlock() == block) {
         BlockState blockstate = blockAccess.getBlockState(pos.below());
         if (blockstate.isSolidRender(blockAccess, pos)) {
            return true;
         }

         Block block1 = blockstate.getBlock();
         if (block1 instanceof StairBlock) {
            return blockstate.getValue(StairBlock.HALF) == Half.TOP;
         }

         if (block1 instanceof SlabBlock) {
            return blockstate.getValue(SlabBlock.TYPE) == SlabType.TOP;
         }
      }

      return false;
   }

   private static boolean checkBlock(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos) {
      if (blockState.isSolidRender(blockAccess, blockPos)) {
         return false;
      } else {
         Block block = blockState.getBlock();
         if (block == Blocks.SNOW_BLOCK) {
            return false;
         } else if (!(block instanceof BushBlock) || !(block instanceof DoublePlantBlock) && !(block instanceof FlowerBlock) && !(block instanceof MushroomBlock) && !(block instanceof SaplingBlock) && !(block instanceof TallGrassBlock)) {
            if (!(block instanceof FenceBlock) && !(block instanceof FenceGateBlock) && !(block instanceof FlowerPotBlock) && !(block instanceof CrossCollisionBlock) && !(block instanceof SugarCaneBlock) && !(block instanceof WallBlock)) {
               if (block instanceof RedstoneTorchBlock) {
                  return true;
               } else if (block instanceof StairBlock) {
                  return blockState.getValue(StairBlock.HALF) == Half.TOP;
               } else if (block instanceof SlabBlock) {
                  return blockState.getValue(SlabBlock.TYPE) == SlabType.TOP;
               } else if (block instanceof ButtonBlock) {
                  return blockState.getValue(ButtonBlock.FACE) != AttachFace.FLOOR;
               } else if (block instanceof HopperBlock) {
                  return true;
               } else if (block instanceof LadderBlock) {
                  return true;
               } else if (block instanceof LeverBlock) {
                  return true;
               } else if (block instanceof TurtleEggBlock) {
                  return true;
               } else {
                  return block instanceof VineBlock;
               }
            } else {
               return true;
            }
         } else {
            return true;
         }
      }
   }
}