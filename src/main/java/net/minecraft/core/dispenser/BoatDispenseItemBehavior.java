package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class BoatDispenseItemBehavior extends DefaultDispenseItemBehavior {
   private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
   private final Boat.Type type;
   private final boolean isChestBoat;

   public BoatDispenseItemBehavior(Boat.Type pType) {
      this(pType, false);
   }

   public BoatDispenseItemBehavior(Boat.Type pType, boolean pIsChestBoat) {
      this.type = pType;
      this.isChestBoat = pIsChestBoat;
   }

   public ItemStack execute(BlockSource pSource, ItemStack pStack) {
      Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
      Level level = pSource.getLevel();
      double d0 = 0.5625D + (double)EntityType.BOAT.getWidth() / 2.0D;
      double d1 = pSource.x() + (double)direction.getStepX() * d0;
      double d2 = pSource.y() + (double)((float)direction.getStepY() * 1.125F);
      double d3 = pSource.z() + (double)direction.getStepZ() * d0;
      BlockPos blockpos = pSource.getPos().relative(direction);
      double d4;
      if (level.getFluidState(blockpos).is(FluidTags.WATER)) {
         d4 = 1.0D;
      } else {
         if (!level.getBlockState(blockpos).isAir() || !level.getFluidState(blockpos.below()).is(FluidTags.WATER)) {
            return this.defaultDispenseItemBehavior.dispense(pSource, pStack);
         }

         d4 = 0.0D;
      }

      Boat boat = (Boat)(this.isChestBoat ? new ChestBoat(level, d1, d2 + d4, d3) : new Boat(level, d1, d2 + d4, d3));
      boat.setVariant(this.type);
      boat.setYRot(direction.toYRot());
      level.addFreshEntity(boat);
      pStack.shrink(1);
      return pStack;
   }

   protected void playSound(BlockSource pSource) {
      pSource.getLevel().levelEvent(1000, pSource.getPos(), 0);
   }
}