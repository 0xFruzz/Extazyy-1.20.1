package net.minecraft.world.item.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapelessRecipe implements CraftingRecipe {
   private final ResourceLocation id;
   final String group;
   final CraftingBookCategory category;
   final ItemStack result;
   final NonNullList<Ingredient> ingredients;

   public ShapelessRecipe(ResourceLocation pId, String pGroup, CraftingBookCategory pCategory, ItemStack pResult, NonNullList<Ingredient> pIngredients) {
      this.id = pId;
      this.group = pGroup;
      this.category = pCategory;
      this.result = pResult;
      this.ingredients = pIngredients;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SHAPELESS_RECIPE;
   }

   public String getGroup() {
      return this.group;
   }

   public CraftingBookCategory category() {
      return this.category;
   }

   public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
      return this.result;
   }

   public NonNullList<Ingredient> getIngredients() {
      return this.ingredients;
   }

   public boolean matches(CraftingContainer pInv, Level pLevel) {
      StackedContents stackedcontents = new StackedContents();
      int i = 0;

      for(int j = 0; j < pInv.getContainerSize(); ++j) {
         ItemStack itemstack = pInv.getItem(j);
         if (!itemstack.isEmpty()) {
            ++i;
            stackedcontents.accountStack(itemstack, 1);
         }
      }

      return i == this.ingredients.size() && stackedcontents.canCraft(this, (IntList)null);
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      return this.result.copy();
   }

   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth * pHeight >= this.ingredients.size();
   }

   public static class Serializer implements RecipeSerializer<ShapelessRecipe> {
      public ShapelessRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
         String s = GsonHelper.getAsString(pJson, "group", "");
         CraftingBookCategory craftingbookcategory = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(pJson, "category", (String)null), CraftingBookCategory.MISC);
         NonNullList<Ingredient> nonnulllist = itemsFromJson(GsonHelper.getAsJsonArray(pJson, "ingredients"));
         if (nonnulllist.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
         } else if (nonnulllist.size() > 9) {
            throw new JsonParseException("Too many ingredients for shapeless recipe");
         } else {
            ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
            return new ShapelessRecipe(pRecipeId, s, craftingbookcategory, itemstack, nonnulllist);
         }
      }

      private static NonNullList<Ingredient> itemsFromJson(JsonArray pIngredientArray) {
         NonNullList<Ingredient> nonnulllist = NonNullList.create();

         for(int i = 0; i < pIngredientArray.size(); ++i) {
            Ingredient ingredient = Ingredient.fromJson(pIngredientArray.get(i), false);
            if (!ingredient.isEmpty()) {
               nonnulllist.add(ingredient);
            }
         }

         return nonnulllist;
      }

      public ShapelessRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
         String s = pBuffer.readUtf();
         CraftingBookCategory craftingbookcategory = pBuffer.readEnum(CraftingBookCategory.class);
         int i = pBuffer.readVarInt();
         NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

         for(int j = 0; j < nonnulllist.size(); ++j) {
            nonnulllist.set(j, Ingredient.fromNetwork(pBuffer));
         }

         ItemStack itemstack = pBuffer.readItem();
         return new ShapelessRecipe(pRecipeId, s, craftingbookcategory, itemstack, nonnulllist);
      }

      public void toNetwork(FriendlyByteBuf pBuffer, ShapelessRecipe pRecipe) {
         pBuffer.writeUtf(pRecipe.group);
         pBuffer.writeEnum(pRecipe.category);
         pBuffer.writeVarInt(pRecipe.ingredients.size());

         for(Ingredient ingredient : pRecipe.ingredients) {
            ingredient.toNetwork(pBuffer);
         }

         pBuffer.writeItem(pRecipe.result);
      }
   }
}