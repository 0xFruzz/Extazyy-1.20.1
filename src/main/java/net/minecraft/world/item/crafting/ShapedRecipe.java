package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ShapedRecipe implements CraftingRecipe {
   final int width;
   final int height;
   final NonNullList<Ingredient> recipeItems;
   final ItemStack result;
   private final ResourceLocation id;
   final String group;
   final CraftingBookCategory category;
   final boolean showNotification;

   public ShapedRecipe(ResourceLocation pId, String pGroup, CraftingBookCategory pCategory, int pWidth, int pHeight, NonNullList<Ingredient> pRecipeItems, ItemStack pResult, boolean pShowNotification) {
      this.id = pId;
      this.group = pGroup;
      this.category = pCategory;
      this.width = pWidth;
      this.height = pHeight;
      this.recipeItems = pRecipeItems;
      this.result = pResult;
      this.showNotification = pShowNotification;
   }

   public ShapedRecipe(ResourceLocation pId, String pGroup, CraftingBookCategory pCategory, int pWidth, int pHeight, NonNullList<Ingredient> pRecipeItems, ItemStack pResult) {
      this(pId, pGroup, pCategory, pWidth, pHeight, pRecipeItems, pResult, true);
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SHAPED_RECIPE;
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
      return this.recipeItems;
   }

   public boolean showNotification() {
      return this.showNotification;
   }

   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth >= this.width && pHeight >= this.height;
   }

   public boolean matches(CraftingContainer pInv, Level pLevel) {
      for(int i = 0; i <= pInv.getWidth() - this.width; ++i) {
         for(int j = 0; j <= pInv.getHeight() - this.height; ++j) {
            if (this.matches(pInv, i, j, true)) {
               return true;
            }

            if (this.matches(pInv, i, j, false)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean matches(CraftingContainer pCraftingInventory, int pWidth, int pHeight, boolean pMirrored) {
      for(int i = 0; i < pCraftingInventory.getWidth(); ++i) {
         for(int j = 0; j < pCraftingInventory.getHeight(); ++j) {
            int k = i - pWidth;
            int l = j - pHeight;
            Ingredient ingredient = Ingredient.EMPTY;
            if (k >= 0 && l >= 0 && k < this.width && l < this.height) {
               if (pMirrored) {
                  ingredient = this.recipeItems.get(this.width - k - 1 + l * this.width);
               } else {
                  ingredient = this.recipeItems.get(k + l * this.width);
               }
            }

            if (!ingredient.test(pCraftingInventory.getItem(i + j * pCraftingInventory.getWidth()))) {
               return false;
            }
         }
      }

      return true;
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      return this.getResultItem(pRegistryAccess).copy();
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   static NonNullList<Ingredient> dissolvePattern(String[] pPattern, Map<String, Ingredient> pKeys, int pPatternWidth, int pPatternHeight) {
      NonNullList<Ingredient> nonnulllist = NonNullList.withSize(pPatternWidth * pPatternHeight, Ingredient.EMPTY);
      Set<String> set = Sets.newHashSet(pKeys.keySet());
      set.remove(" ");

      for(int i = 0; i < pPattern.length; ++i) {
         for(int j = 0; j < pPattern[i].length(); ++j) {
            String s = pPattern[i].substring(j, j + 1);
            Ingredient ingredient = pKeys.get(s);
            if (ingredient == null) {
               throw new JsonSyntaxException("Pattern references symbol '" + s + "' but it's not defined in the key");
            }

            set.remove(s);
            nonnulllist.set(j + pPatternWidth * i, ingredient);
         }
      }

      if (!set.isEmpty()) {
         throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
      } else {
         return nonnulllist;
      }
   }

   @VisibleForTesting
   static String[] shrink(String... pToShrink) {
      int i = Integer.MAX_VALUE;
      int j = 0;
      int k = 0;
      int l = 0;

      for(int i1 = 0; i1 < pToShrink.length; ++i1) {
         String s = pToShrink[i1];
         i = Math.min(i, firstNonSpace(s));
         int j1 = lastNonSpace(s);
         j = Math.max(j, j1);
         if (j1 < 0) {
            if (k == i1) {
               ++k;
            }

            ++l;
         } else {
            l = 0;
         }
      }

      if (pToShrink.length == l) {
         return new String[0];
      } else {
         String[] astring = new String[pToShrink.length - l - k];

         for(int k1 = 0; k1 < astring.length; ++k1) {
            astring[k1] = pToShrink[k1 + k].substring(i, j + 1);
         }

         return astring;
      }
   }

   public boolean isIncomplete() {
      NonNullList<Ingredient> nonnulllist = this.getIngredients();
      return nonnulllist.isEmpty() || nonnulllist.stream().filter((p_151277_) -> {
         return !p_151277_.isEmpty();
      }).anyMatch((p_151273_) -> {
         return p_151273_.getItems().length == 0;
      });
   }

   private static int firstNonSpace(String pEntry) {
      int i;
      for(i = 0; i < pEntry.length() && pEntry.charAt(i) == ' '; ++i) {
      }

      return i;
   }

   private static int lastNonSpace(String pEntry) {
      int i;
      for(i = pEntry.length() - 1; i >= 0 && pEntry.charAt(i) == ' '; --i) {
      }

      return i;
   }

   static String[] patternFromJson(JsonArray pPatternArray) {
      String[] astring = new String[pPatternArray.size()];
      if (astring.length > 3) {
         throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
      } else if (astring.length == 0) {
         throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
      } else {
         for(int i = 0; i < astring.length; ++i) {
            String s = GsonHelper.convertToString(pPatternArray.get(i), "pattern[" + i + "]");
            if (s.length() > 3) {
               throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
            }

            if (i > 0 && astring[0].length() != s.length()) {
               throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
            }

            astring[i] = s;
         }

         return astring;
      }
   }

   static Map<String, Ingredient> keyFromJson(JsonObject pKeyEntry) {
      Map<String, Ingredient> map = Maps.newHashMap();

      for(Map.Entry<String, JsonElement> entry : pKeyEntry.entrySet()) {
         if (entry.getKey().length() != 1) {
            throw new JsonSyntaxException("Invalid key entry: '" + (String)entry.getKey() + "' is an invalid symbol (must be 1 character only).");
         }

         if (" ".equals(entry.getKey())) {
            throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
         }

         map.put(entry.getKey(), Ingredient.fromJson(entry.getValue(), false));
      }

      map.put(" ", Ingredient.EMPTY);
      return map;
   }

   public static ItemStack itemStackFromJson(JsonObject pStackObject) {
      Item item = itemFromJson(pStackObject);
      if (pStackObject.has("data")) {
         throw new JsonParseException("Disallowed data tag found");
      } else {
         int i = GsonHelper.getAsInt(pStackObject, "count", 1);
         if (i < 1) {
            throw new JsonSyntaxException("Invalid output count: " + i);
         } else {
            return new ItemStack(item, i);
         }
      }
   }

   public static Item itemFromJson(JsonObject pItemObject) {
      String s = GsonHelper.getAsString(pItemObject, "item");
      Item item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.tryParse(s)).orElseThrow(() -> {
         return new JsonSyntaxException("Unknown item '" + s + "'");
      });
      if (item == Items.AIR) {
         throw new JsonSyntaxException("Empty ingredient not allowed here");
      } else {
         return item;
      }
   }

   public static class Serializer implements RecipeSerializer<ShapedRecipe> {
      public ShapedRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
         String s = GsonHelper.getAsString(pJson, "group", "");
         CraftingBookCategory craftingbookcategory = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(pJson, "category", (String)null), CraftingBookCategory.MISC);
         Map<String, Ingredient> map = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(pJson, "key"));
         String[] astring = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(pJson, "pattern")));
         int i = astring[0].length();
         int j = astring.length;
         NonNullList<Ingredient> nonnulllist = ShapedRecipe.dissolvePattern(astring, map, i, j);
         ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
         boolean flag = GsonHelper.getAsBoolean(pJson, "show_notification", true);
         return new ShapedRecipe(pRecipeId, s, craftingbookcategory, i, j, nonnulllist, itemstack, flag);
      }

      public ShapedRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
         int i = pBuffer.readVarInt();
         int j = pBuffer.readVarInt();
         String s = pBuffer.readUtf();
         CraftingBookCategory craftingbookcategory = pBuffer.readEnum(CraftingBookCategory.class);
         NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i * j, Ingredient.EMPTY);

         for(int k = 0; k < nonnulllist.size(); ++k) {
            nonnulllist.set(k, Ingredient.fromNetwork(pBuffer));
         }

         ItemStack itemstack = pBuffer.readItem();
         boolean flag = pBuffer.readBoolean();
         return new ShapedRecipe(pRecipeId, s, craftingbookcategory, i, j, nonnulllist, itemstack, flag);
      }

      public void toNetwork(FriendlyByteBuf pBuffer, ShapedRecipe pRecipe) {
         pBuffer.writeVarInt(pRecipe.width);
         pBuffer.writeVarInt(pRecipe.height);
         pBuffer.writeUtf(pRecipe.group);
         pBuffer.writeEnum(pRecipe.category);

         for(Ingredient ingredient : pRecipe.recipeItems) {
            ingredient.toNetwork(pBuffer);
         }

         pBuffer.writeItem(pRecipe.result);
         pBuffer.writeBoolean(pRecipe.showNotification);
      }
   }
}