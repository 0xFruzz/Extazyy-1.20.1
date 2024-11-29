package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class CatSitOnBlockGoal extends MoveToBlockGoal {
   private final Cat cat;

   public CatSitOnBlockGoal(Cat pCat, double pSpeedModifier) {
      super(pCat, pSpeedModifier, 8);
      this.cat = pCat;
   }

   public boolean canUse() {
      return this.cat.isTame() && !this.cat.isOrderedToSit() && super.canUse();
   }

   public void start() {
      super.start();
      this.cat.setInSittingPose(false);
   }

   public void stop() {
      super.stop();
      this.cat.setInSittingPose(false);
   }

   public void tick() {
      super.tick();
      this.cat.setInSittingPose(this.isReachedTarget());
   }

   protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
      if (!pLevel.isEmptyBlock(pPos.above())) {
         return false;
      } else {
         BlockState blockstate = pLevel.getBlockState(pPos);
         if (blockstate.is(Blocks.CHEST)) {
            return ChestBlockEntity.getOpenCount(pLevel, pPos) < 1;
         } else {
            return blockstate.is(Blocks.FURNACE) && blockstate.getValue(FurnaceBlock.LIT) ? true : blockstate.is(BlockTags.BEDS, (p_25156_) -> {
               return p_25156_.getOptionalValue(BedBlock.PART).map((p_148084_) -> {
                  return p_148084_ != BedPart.HEAD;
               }).orElse(true);
            });
         }
      }
   }
}