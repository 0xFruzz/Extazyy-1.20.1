package net.minecraft.world.item.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public interface SmithingRecipe extends Recipe<Container> {
   default RecipeType<?> getType() {
      return RecipeType.SMITHING;
   }

   default boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth >= 3 && pHeight >= 1;
   }

   default ItemStack getToastSymbol() {
      return new ItemStack(Blocks.SMITHING_TABLE);
   }

   boolean isTemplateIngredient(ItemStack pStack);

   boolean isBaseIngredient(ItemStack pStack);

   boolean isAdditionIngredient(ItemStack pStack);
}