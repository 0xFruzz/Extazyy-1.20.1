package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public interface Recipe<C extends Container> {
   boolean matches(C pContainer, Level pLevel);

   ItemStack assemble(C pContainer, RegistryAccess pRegistryAccess);

   boolean canCraftInDimensions(int pWidth, int pHeight);

   ItemStack getResultItem(RegistryAccess pRegistryAccess);

   default NonNullList<ItemStack> getRemainingItems(C pContainer) {
      NonNullList<ItemStack> nonnulllist = NonNullList.withSize(pContainer.getContainerSize(), ItemStack.EMPTY);

      for(int i = 0; i < nonnulllist.size(); ++i) {
         Item item = pContainer.getItem(i).getItem();
         if (item.hasCraftingRemainingItem()) {
            nonnulllist.set(i, new ItemStack(item.getCraftingRemainingItem()));
         }
      }

      return nonnulllist;
   }

   default NonNullList<Ingredient> getIngredients() {
      return NonNullList.create();
   }

   default boolean isSpecial() {
      return false;
   }

   default boolean showNotification() {
      return true;
   }

   default String getGroup() {
      return "";
   }

   default ItemStack getToastSymbol() {
      return new ItemStack(Blocks.CRAFTING_TABLE);
   }

   ResourceLocation getId();

   RecipeSerializer<?> getSerializer();

   RecipeType<?> getType();

   default boolean isIncomplete() {
      NonNullList<Ingredient> nonnulllist = this.getIngredients();
      return nonnulllist.isEmpty() || nonnulllist.stream().anyMatch((p_151268_) -> {
         return p_151268_.getItems().length == 0;
      });
   }
}