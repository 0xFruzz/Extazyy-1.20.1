package net.optifine.util;

import it.unimi.dsi.fastutil.longs.Long2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.optifine.Config;
import net.optifine.render.RenderEnv;

public class BlockUtils {
   private static final ThreadLocal<BlockUtils.RenderSideCacheKey> threadLocalKey = ThreadLocal.withInitial(() -> {
      return new BlockUtils.RenderSideCacheKey((BlockState)null, (BlockState)null, (Direction)null);
   });
   private static final ThreadLocal<Object2ByteLinkedOpenHashMap<BlockUtils.RenderSideCacheKey>> threadLocalMap = ThreadLocal.withInitial(() -> {
      Object2ByteLinkedOpenHashMap<BlockUtils.RenderSideCacheKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<BlockUtils.RenderSideCacheKey>(200) {
         protected void rehash(int p_rehash_1_) {
         }
      };
      object2bytelinkedopenhashmap.defaultReturnValue((byte)127);
      return object2bytelinkedopenhashmap;
   });

   public static boolean shouldSideBeRendered(BlockState blockStateIn, BlockGetter blockReaderIn, BlockPos blockPosIn, Direction facingIn, RenderEnv renderEnv) {
      BlockPos blockpos = blockPosIn.relative(facingIn);
      BlockState blockstate = blockReaderIn.getBlockState(blockpos);
      if (blockstate.isCacheOpaqueCube() && !(blockStateIn.getBlock() instanceof PowderSnowBlock)) {
         return false;
      } else if (blockStateIn.skipRendering(blockstate, facingIn)) {
         return false;
      } else {
         return blockstate.canOcclude() ? shouldSideBeRenderedCached(blockStateIn, blockReaderIn, blockPosIn, facingIn, renderEnv, blockstate, blockpos) : true;
      }
   }

   public static boolean shouldSideBeRenderedCached(BlockState blockStateIn, BlockGetter blockReaderIn, BlockPos blockPosIn, Direction facingIn, RenderEnv renderEnv, BlockState stateNeighbourIn, BlockPos posNeighbourIn) {
      long i = (long)blockStateIn.getBlockStateId() << 36 | (long)stateNeighbourIn.getBlockStateId() << 4 | (long)facingIn.ordinal();
      Long2ByteLinkedOpenHashMap long2bytelinkedopenhashmap = renderEnv.getRenderSideMap();
      byte b0 = long2bytelinkedopenhashmap.getAndMoveToFirst(i);
      if (b0 != 0) {
         return b0 > 0;
      } else {
         VoxelShape voxelshape = blockStateIn.getFaceOcclusionShape(blockReaderIn, blockPosIn, facingIn);
         if (voxelshape.isEmpty()) {
            return true;
         } else {
            VoxelShape voxelshape1 = stateNeighbourIn.getFaceOcclusionShape(blockReaderIn, posNeighbourIn, facingIn.getOpposite());
            boolean flag = Shapes.joinIsNotEmpty(voxelshape, voxelshape1, BooleanOp.ONLY_FIRST);
            if (long2bytelinkedopenhashmap.size() > 400) {
               long2bytelinkedopenhashmap.removeLastByte();
            }

            long2bytelinkedopenhashmap.putAndMoveToFirst(i, (byte)(flag ? 1 : -1));
            return flag;
         }
      }
   }

   public static int getBlockId(Block block) {
      return BuiltInRegistries.BLOCK.getId(block);
   }

   public static Block getBlock(ResourceLocation loc) {
      return !BuiltInRegistries.BLOCK.containsKey(loc) ? null : BuiltInRegistries.BLOCK.get(loc);
   }

   public static int getMetadata(BlockState blockState) {
      Block block = blockState.getBlock();
      StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
      List<BlockState> list = statedefinition.getPossibleStates();
      return list.indexOf(blockState);
   }

   public static int getMetadataCount(Block block) {
      StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
      List<BlockState> list = statedefinition.getPossibleStates();
      return list.size();
   }

   public static BlockState getBlockState(Block block, int metadata) {
      StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
      List<BlockState> list = statedefinition.getPossibleStates();
      return metadata >= 0 && metadata < list.size() ? list.get(metadata) : null;
   }

   public static List<BlockState> getBlockStates(Block block) {
      StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
      List<BlockState> list = statedefinition.getPossibleStates();
      return list;
   }

   public static boolean isFullCube(BlockState stateIn, BlockGetter blockReaderIn, BlockPos posIn) {
      return stateIn.isCacheOpaqueCollisionShape();
   }

   public static Collection<Property<?>> getProperties(BlockState blockState) {
      return blockState.getProperties();
   }

   public static boolean isPropertyTrue(BlockState blockState, BooleanProperty prop) {
      Boolean obool = (Boolean)blockState.getValues().get(prop);
      return Config.isTrue(obool);
   }

   public static boolean isPropertyFalse(BlockState blockState, BooleanProperty prop) {
      Boolean obool = (Boolean)blockState.getValues().get(prop);
      return Config.isFalse(obool);
   }

   public static final class RenderSideCacheKey {
      private BlockState blockState1;
      private BlockState blockState2;
      private Direction facing;
      private int hashCode;

      private RenderSideCacheKey(BlockState blockState1In, BlockState blockState2In, Direction facingIn) {
         this.blockState1 = blockState1In;
         this.blockState2 = blockState2In;
         this.facing = facingIn;
      }

      private void init(BlockState blockState1In, BlockState blockState2In, Direction facingIn) {
         this.blockState1 = blockState1In;
         this.blockState2 = blockState2In;
         this.facing = facingIn;
         this.hashCode = 0;
      }

      public BlockUtils.RenderSideCacheKey duplicate() {
         return new BlockUtils.RenderSideCacheKey(this.blockState1, this.blockState2, this.facing);
      }

      public boolean equals(Object p_equals_1_) {
         if (this == p_equals_1_) {
            return true;
         } else if (!(p_equals_1_ instanceof BlockUtils.RenderSideCacheKey)) {
            return false;
         } else {
            BlockUtils.RenderSideCacheKey blockutils$rendersidecachekey = (BlockUtils.RenderSideCacheKey)p_equals_1_;
            return this.blockState1 == blockutils$rendersidecachekey.blockState1 && this.blockState2 == blockutils$rendersidecachekey.blockState2 && this.facing == blockutils$rendersidecachekey.facing;
         }
      }

      public int hashCode() {
         if (this.hashCode == 0) {
            this.hashCode = 31 * this.hashCode + this.blockState1.hashCode();
            this.hashCode = 31 * this.hashCode + this.blockState2.hashCode();
            this.hashCode = 31 * this.hashCode + this.facing.hashCode();
         }

         return this.hashCode;
      }
   }
}
