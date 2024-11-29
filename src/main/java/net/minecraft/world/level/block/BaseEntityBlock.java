package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseEntityBlock extends Block implements EntityBlock {
   protected BaseEntityBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.INVISIBLE;
   }

   public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
      super.triggerEvent(pState, pLevel, pPos, pId, pParam);
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      return blockentity == null ? false : blockentity.triggerEvent(pId, pParam);
   }

   @Nullable
   public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      return blockentity instanceof MenuProvider ? (MenuProvider)blockentity : null;
   }

   @Nullable
   protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
      return pClientType == pServerType ? (BlockEntityTicker<A>)pTicker : null;
   }
}