package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public interface WorldlyContainer extends Container {
   int[] getSlotsForFace(Direction pSide);

   boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection);

   boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection);
}