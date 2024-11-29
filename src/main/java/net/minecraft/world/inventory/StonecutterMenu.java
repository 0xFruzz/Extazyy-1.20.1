package net.minecraft.world.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class StonecutterMenu extends AbstractContainerMenu {
   public static final int INPUT_SLOT = 0;
   public static final int RESULT_SLOT = 1;
   private static final int INV_SLOT_START = 2;
   private static final int INV_SLOT_END = 29;
   private static final int USE_ROW_SLOT_START = 29;
   private static final int USE_ROW_SLOT_END = 38;
   private final ContainerLevelAccess access;
   private final DataSlot selectedRecipeIndex = DataSlot.standalone();
   private final Level level;
   private List<StonecutterRecipe> recipes = Lists.newArrayList();
   private ItemStack input = ItemStack.EMPTY;
   long lastSoundTime;
   final Slot inputSlot;
   final Slot resultSlot;
   Runnable slotUpdateListener = () -> {
   };
   public final Container container = new SimpleContainer(1) {
      public void setChanged() {
         super.setChanged();
         StonecutterMenu.this.slotsChanged(this);
         StonecutterMenu.this.slotUpdateListener.run();
      }
   };
   final ResultContainer resultContainer = new ResultContainer();

   public StonecutterMenu(int pContainerId, Inventory pPlayerInventory) {
      this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
   }

   public StonecutterMenu(int pContainerId, Inventory pPlayerInventory, final ContainerLevelAccess pAccess) {
      super(MenuType.STONECUTTER, pContainerId);
      this.access = pAccess;
      this.level = pPlayerInventory.player.level();
      this.inputSlot = this.addSlot(new Slot(this.container, 0, 20, 33));
      this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, 143, 33) {
         public boolean mayPlace(ItemStack p_40362_) {
            return false;
         }

         public void onTake(Player p_150672_, ItemStack p_150673_) {
            p_150673_.onCraftedBy(p_150672_.level(), p_150672_, p_150673_.getCount());
            StonecutterMenu.this.resultContainer.awardUsedRecipes(p_150672_, this.getRelevantItems());
            ItemStack itemstack = StonecutterMenu.this.inputSlot.remove(1);
            if (!itemstack.isEmpty()) {
               StonecutterMenu.this.setupResultSlot();
            }

            pAccess.execute((p_40364_, p_40365_) -> {
               long l = p_40364_.getGameTime();
               if (StonecutterMenu.this.lastSoundTime != l) {
                  p_40364_.playSound((Player)null, p_40365_, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                  StonecutterMenu.this.lastSoundTime = l;
               }

            });
            super.onTake(p_150672_, p_150673_);
         }

         private List<ItemStack> getRelevantItems() {
            return List.of(StonecutterMenu.this.inputSlot.getItem());
         }
      });

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 142));
      }

      this.addDataSlot(this.selectedRecipeIndex);
   }

   public int getSelectedRecipeIndex() {
      return this.selectedRecipeIndex.get();
   }

   public List<StonecutterRecipe> getRecipes() {
      return this.recipes;
   }

   public int getNumRecipes() {
      return this.recipes.size();
   }

   public boolean hasInputItem() {
      return this.inputSlot.hasItem() && !this.recipes.isEmpty();
   }

   public boolean stillValid(Player pPlayer) {
      return stillValid(this.access, pPlayer, Blocks.STONECUTTER);
   }

   public boolean clickMenuButton(Player pPlayer, int pId) {
      if (this.isValidRecipeIndex(pId)) {
         this.selectedRecipeIndex.set(pId);
         this.setupResultSlot();
      }

      return true;
   }

   private boolean isValidRecipeIndex(int pRecipeIndex) {
      return pRecipeIndex >= 0 && pRecipeIndex < this.recipes.size();
   }

   public void slotsChanged(Container pInventory) {
      ItemStack itemstack = this.inputSlot.getItem();
      if (!itemstack.is(this.input.getItem())) {
         this.input = itemstack.copy();
         this.setupRecipeList(pInventory, itemstack);
      }

   }

   private void setupRecipeList(Container pContainer, ItemStack pStack) {
      this.recipes.clear();
      this.selectedRecipeIndex.set(-1);
      this.resultSlot.set(ItemStack.EMPTY);
      if (!pStack.isEmpty()) {
         this.recipes = this.level.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, pContainer, this.level);
      }

   }

   void setupResultSlot() {
      if (!this.recipes.isEmpty() && this.isValidRecipeIndex(this.selectedRecipeIndex.get())) {
         StonecutterRecipe stonecutterrecipe = this.recipes.get(this.selectedRecipeIndex.get());
         ItemStack itemstack = stonecutterrecipe.assemble(this.container, this.level.registryAccess());
         if (itemstack.isItemEnabled(this.level.enabledFeatures())) {
            this.resultContainer.setRecipeUsed(stonecutterrecipe);
            this.resultSlot.set(itemstack);
         } else {
            this.resultSlot.set(ItemStack.EMPTY);
         }
      } else {
         this.resultSlot.set(ItemStack.EMPTY);
      }

      this.broadcastChanges();
   }

   public MenuType<?> getType() {
      return MenuType.STONECUTTER;
   }

   public void registerUpdateListener(Runnable pListener) {
      this.slotUpdateListener = pListener;
   }

   public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
      return pSlot.container != this.resultContainer && super.canTakeItemForPickAll(pStack, pSlot);
   }

   public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(pIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         Item item = itemstack1.getItem();
         itemstack = itemstack1.copy();
         if (pIndex == 1) {
            item.onCraftedBy(itemstack1, pPlayer.level(), pPlayer);
            if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (pIndex == 0) {
            if (!this.moveItemStackTo(itemstack1, 2, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.level.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, new SimpleContainer(itemstack1), this.level).isPresent()) {
            if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 2 && pIndex < 29) {
            if (!this.moveItemStackTo(itemstack1, 29, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 29 && pIndex < 38 && !this.moveItemStackTo(itemstack1, 2, 29, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         }

         slot.setChanged();
         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(pPlayer, itemstack1);
         this.broadcastChanges();
      }

      return itemstack;
   }

   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.resultContainer.removeItemNoUpdate(1);
      this.access.execute((p_40313_, p_40314_) -> {
         this.clearContainer(pPlayer, this.container);
      });
   }
}