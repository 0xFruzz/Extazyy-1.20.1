package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerPotBlock extends Block {
   private static final Map<Block, Block> POTTED_BY_CONTENT = Maps.newHashMap();
   public static final float AABB_SIZE = 3.0F;
   protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);
   private final Block content;

   public FlowerPotBlock(Block pContent, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.content = pContent;
      POTTED_BY_CONTENT.put(pContent, this);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      Item item = itemstack.getItem();
      BlockState blockstate = (item instanceof BlockItem ? POTTED_BY_CONTENT.getOrDefault(((BlockItem)item).getBlock(), Blocks.AIR) : Blocks.AIR).defaultBlockState();
      boolean flag = blockstate.is(Blocks.AIR);
      boolean flag1 = this.isEmpty();
      if (flag != flag1) {
         if (flag1) {
            pLevel.setBlock(pPos, blockstate, 3);
            pPlayer.awardStat(Stats.POT_FLOWER);
            if (!pPlayer.getAbilities().instabuild) {
               itemstack.shrink(1);
            }
         } else {
            ItemStack itemstack1 = new ItemStack(this.content);
            if (itemstack.isEmpty()) {
               pPlayer.setItemInHand(pHand, itemstack1);
            } else if (!pPlayer.addItem(itemstack1)) {
               pPlayer.drop(itemstack1, false);
            }

            pLevel.setBlock(pPos, Blocks.FLOWER_POT.defaultBlockState(), 3);
         }

         pLevel.gameEvent(pPlayer, GameEvent.BLOCK_CHANGE, pPos);
         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      } else {
         return InteractionResult.CONSUME;
      }
   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return this.isEmpty() ? super.getCloneItemStack(pLevel, pPos, pState) : new ItemStack(this.content);
   }

   private boolean isEmpty() {
      return this.content == Blocks.AIR;
   }

   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public Block getContent() {
      return this.content;
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }
}