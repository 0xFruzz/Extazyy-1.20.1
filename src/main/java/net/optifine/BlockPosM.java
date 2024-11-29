package net.optifine;

import com.google.common.collect.AbstractIterator;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class BlockPosM extends BlockPos {
   private int mx;
   private int my;
   private int mz;
   private int level;
   private BlockPosM[] facings;
   private boolean needsUpdate;

   public BlockPosM() {
      this(0, 0, 0, 0);
   }

   public BlockPosM(int x, int y, int z) {
      this(x, y, z, 0);
   }

   public BlockPosM(double xIn, double yIn, double zIn) {
      this(Mth.floor(xIn), Mth.floor(yIn), Mth.floor(zIn));
   }

   public BlockPosM(int x, int y, int z, int level) {
      super(0, 0, 0);
      this.mx = x;
      this.my = y;
      this.mz = z;
      this.level = level;
   }

   public int getX() {
      return this.mx;
   }

   public int getY() {
      return this.my;
   }

   public int getZ() {
      return this.mz;
   }

   public void setXyz(int x, int y, int z) {
      this.mx = x;
      this.my = y;
      this.mz = z;
      this.needsUpdate = true;
   }

   public BlockPosM setXyz(double xIn, double yIn, double zIn) {
      this.setXyz(Mth.floor(xIn), Mth.floor(yIn), Mth.floor(zIn));
      return this;
   }

   public BlockPos relative(Direction facing) {
      if (this.level <= 0) {
         return super.relative(facing, 1).immutable();
      } else {
         if (this.facings == null) {
            this.facings = new BlockPosM[Direction.VALUES.length];
         }

         if (this.needsUpdate) {
            this.update();
         }

         int i = facing.get3DDataValue();
         BlockPosM blockposm = this.facings[i];
         if (blockposm == null) {
            int j = this.mx + facing.getStepX();
            int k = this.my + facing.getStepY();
            int l = this.mz + facing.getStepZ();
            blockposm = new BlockPosM(j, k, l, this.level - 1);
            this.facings[i] = blockposm;
         }

         return blockposm;
      }
   }

   public BlockPos relative(Direction facing, int n) {
      return n == 1 ? this.relative(facing) : super.relative(facing, n).immutable();
   }

   public void setPosOffset(BlockPos pos, Direction facing) {
      this.mx = pos.getX() + facing.getStepX();
      this.my = pos.getY() + facing.getStepY();
      this.mz = pos.getZ() + facing.getStepZ();
   }

   public BlockPos setPosOffset(BlockPos pos, Direction facing, Direction facing2) {
      this.mx = pos.getX() + facing.getStepX() + facing2.getStepX();
      this.my = pos.getY() + facing.getStepY() + facing2.getStepY();
      this.mz = pos.getZ() + facing.getStepZ() + facing2.getStepZ();
      return this;
   }

   private void update() {
      for(int i = 0; i < 6; ++i) {
         BlockPosM blockposm = this.facings[i];
         if (blockposm != null) {
            Direction direction = Direction.VALUES[i];
            int j = this.mx + direction.getStepX();
            int k = this.my + direction.getStepY();
            int l = this.mz + direction.getStepZ();
            blockposm.setXyz(j, k, l);
         }
      }

      this.needsUpdate = false;
   }

   public BlockPos immutable() {
      return new BlockPos(this.mx, this.my, this.mz);
   }

   public static Iterable getAllInBoxMutable(BlockPos from, BlockPos to) {
      final BlockPos blockpos = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
      final BlockPos blockpos1 = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
      return new Iterable() {
         public Iterator iterator() {
            return new AbstractIterator() {
               private BlockPosM posM = null;

               protected Object computeNext() {
                  if (this.posM == null) {
                     this.posM = new BlockPosM(blockpos.getX(), blockpos.getY(), blockpos.getZ(), 3);
                     return this.posM;
                  } else if (this.posM.equals(blockpos1)) {
                     return (BlockPosM)this.endOfData();
                  } else {
                     int i = this.posM.getX();
                     int j = this.posM.getY();
                     int k = this.posM.getZ();
                     if (i < blockpos1.getX()) {
                        ++i;
                     } else if (k < blockpos1.getZ()) {
                        i = blockpos.getX();
                        ++k;
                     } else if (j < blockpos1.getY()) {
                        i = blockpos.getX();
                        k = blockpos.getZ();
                        ++j;
                     }

                     this.posM.setXyz(i, j, k);
                     return this.posM;
                  }
               }
            };
         }
      };
   }
}