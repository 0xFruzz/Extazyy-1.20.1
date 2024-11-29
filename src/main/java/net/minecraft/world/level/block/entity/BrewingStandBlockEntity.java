package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BrewingStandBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
   private static final int INGREDIENT_SLOT = 3;
   private static final int FUEL_SLOT = 4;
   private static final int[] SLOTS_FOR_UP = new int[]{3};
   private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, 3};
   private static final int[] SLOTS_FOR_SIDES = new int[]{0, 1, 2, 4};
   public static final int FUEL_USES = 20;
   public static final int DATA_BREW_TIME = 0;
   public static final int DATA_FUEL_USES = 1;
   public static final int NUM_DATA_VALUES = 2;
   private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
   int brewTime;
   private boolean[] lastPotionCount;
   private Item ingredient;
   int fuel;
   protected final ContainerData dataAccess = new ContainerData() {
      public int get(int p_59038_) {
         switch (p_59038_) {
            case 0:
               return BrewingStandBlockEntity.this.brewTime;
            case 1:
               return BrewingStandBlockEntity.this.fuel;
            default:
               return 0;
         }
      }

      public void set(int p_59040_, int p_59041_) {
         switch (p_59040_) {
            case 0:
               BrewingStandBlockEntity.this.brewTime = p_59041_;
               break;
            case 1:
               BrewingStandBlockEntity.this.fuel = p_59041_;
         }

      }

      public int getCount() {
         return 2;
      }
   };

   public BrewingStandBlockEntity(BlockPos pPos, BlockState pState) {
      super(BlockEntityType.BREWING_STAND, pPos, pState);
   }

   protected Component getDefaultName() {
      return Component.translatable("container.brewing");
   }

   public int getContainerSize() {
      return this.items.size();
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.items) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, BrewingStandBlockEntity pBlockEntity) {
      ItemStack itemstack = pBlockEntity.items.get(4);
      if (pBlockEntity.fuel <= 0 && itemstack.is(Items.BLAZE_POWDER)) {
         pBlockEntity.fuel = 20;
         itemstack.shrink(1);
         setChanged(pLevel, pPos, pState);
      }

      boolean flag = isBrewable(pBlockEntity.items);
      boolean flag1 = pBlockEntity.brewTime > 0;
      ItemStack itemstack1 = pBlockEntity.items.get(3);
      if (flag1) {
         --pBlockEntity.brewTime;
         boolean flag2 = pBlockEntity.brewTime == 0;
         if (flag2 && flag) {
            doBrew(pLevel, pPos, pBlockEntity.items);
            setChanged(pLevel, pPos, pState);
         } else if (!flag || !itemstack1.is(pBlockEntity.ingredient)) {
            pBlockEntity.brewTime = 0;
            setChanged(pLevel, pPos, pState);
         }
      } else if (flag && pBlockEntity.fuel > 0) {
         --pBlockEntity.fuel;
         pBlockEntity.brewTime = 400;
         pBlockEntity.ingredient = itemstack1.getItem();
         setChanged(pLevel, pPos, pState);
      }

      boolean[] aboolean = pBlockEntity.getPotionBits();
      if (!Arrays.equals(aboolean, pBlockEntity.lastPotionCount)) {
         pBlockEntity.lastPotionCount = aboolean;
         BlockState blockstate = pState;
         if (!(pState.getBlock() instanceof BrewingStandBlock)) {
            return;
         }

         for(int i = 0; i < BrewingStandBlock.HAS_BOTTLE.length; ++i) {
            blockstate = blockstate.setValue(BrewingStandBlock.HAS_BOTTLE[i], Boolean.valueOf(aboolean[i]));
         }

         pLevel.setBlock(pPos, blockstate, 2);
      }

   }

   private boolean[] getPotionBits() {
      boolean[] aboolean = new boolean[3];

      for(int i = 0; i < 3; ++i) {
         if (!this.items.get(i).isEmpty()) {
            aboolean[i] = true;
         }
      }

      return aboolean;
   }

   private static boolean isBrewable(NonNullList<ItemStack> pItems) {
      ItemStack itemstack = pItems.get(3);
      if (itemstack.isEmpty()) {
         return false;
      } else if (!PotionBrewing.isIngredient(itemstack)) {
         return false;
      } else {
         for(int i = 0; i < 3; ++i) {
            ItemStack itemstack1 = pItems.get(i);
            if (!itemstack1.isEmpty() && PotionBrewing.hasMix(itemstack1, itemstack)) {
               return true;
            }
         }

         return false;
      }
   }

   private static void doBrew(Level pLevel, BlockPos pPos, NonNullList<ItemStack> pItems) {
      ItemStack itemstack = pItems.get(3);

      for(int i = 0; i < 3; ++i) {
         pItems.set(i, PotionBrewing.mix(itemstack, pItems.get(i)));
      }

      itemstack.shrink(1);
      if (itemstack.getItem().hasCraftingRemainingItem()) {
         ItemStack itemstack1 = new ItemStack(itemstack.getItem().getCraftingRemainingItem());
         if (itemstack.isEmpty()) {
            itemstack = itemstack1;
         } else {
            Containers.dropItemStack(pLevel, (double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), itemstack1);
         }
      }

      pItems.set(3, itemstack);
      pLevel.levelEvent(1035, pPos, 0);
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      ContainerHelper.loadAllItems(pTag, this.items);
      this.brewTime = pTag.getShort("BrewTime");
      this.fuel = pTag.getByte("Fuel");
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      pTag.putShort("BrewTime", (short)this.brewTime);
      ContainerHelper.saveAllItems(pTag, this.items);
      pTag.putByte("Fuel", (byte)this.fuel);
   }

   public ItemStack getItem(int pIndex) {
      return pIndex >= 0 && pIndex < this.items.size() ? this.items.get(pIndex) : ItemStack.EMPTY;
   }

   public ItemStack removeItem(int pIndex, int pCount) {
      return ContainerHelper.removeItem(this.items, pIndex, pCount);
   }

   public ItemStack removeItemNoUpdate(int pIndex) {
      return ContainerHelper.takeItem(this.items, pIndex);
   }

   public void setItem(int pIndex, ItemStack pStack) {
      if (pIndex >= 0 && pIndex < this.items.size()) {
         this.items.set(pIndex, pStack);
      }

   }

   public boolean stillValid(Player pPlayer) {
      return Container.stillValidBlockEntity(this, pPlayer);
   }

   public boolean canPlaceItem(int pIndex, ItemStack pStack) {
      if (pIndex == 3) {
         return PotionBrewing.isIngredient(pStack);
      } else if (pIndex == 4) {
         return pStack.is(Items.BLAZE_POWDER);
      } else {
         return (pStack.is(Items.POTION) || pStack.is(Items.SPLASH_POTION) || pStack.is(Items.LINGERING_POTION) || pStack.is(Items.GLASS_BOTTLE)) && this.getItem(pIndex).isEmpty();
      }
   }

   public int[] getSlotsForFace(Direction pSide) {
      if (pSide == Direction.UP) {
         return SLOTS_FOR_UP;
      } else {
         return pSide == Direction.DOWN ? SLOTS_FOR_DOWN : SLOTS_FOR_SIDES;
      }
   }

   public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
      return this.canPlaceItem(pIndex, pItemStack);
   }

   public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
      return pIndex == 3 ? pStack.is(Items.GLASS_BOTTLE) : true;
   }

   public void clearContent() {
      this.items.clear();
   }

   protected AbstractContainerMenu createMenu(int pId, Inventory pPlayer) {
      return new BrewingStandMenu(pId, pPlayer, this, this.dataAccess);
   }
}