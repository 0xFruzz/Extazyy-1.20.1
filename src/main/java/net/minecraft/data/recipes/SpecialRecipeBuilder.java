package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SpecialRecipeBuilder extends CraftingRecipeBuilder {
   final RecipeSerializer<?> serializer;

   public SpecialRecipeBuilder(RecipeSerializer<?> pSerializer) {
      this.serializer = pSerializer;
   }

   public static SpecialRecipeBuilder special(RecipeSerializer<? extends CraftingRecipe> pSerializer) {
      return new SpecialRecipeBuilder(pSerializer);
   }

   public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, final String pId) {
      pFinishedRecipeConsumer.accept(new CraftingRecipeBuilder.CraftingResult(CraftingBookCategory.MISC) {
         public RecipeSerializer<?> getType() {
            return SpecialRecipeBuilder.this.serializer;
         }

         public ResourceLocation getId() {
            return new ResourceLocation(pId);
         }

         @Nullable
         public JsonObject serializeAdvancement() {
            return null;
         }

         public ResourceLocation getAdvancementId() {
            return new ResourceLocation("");
         }
      });
   }
}