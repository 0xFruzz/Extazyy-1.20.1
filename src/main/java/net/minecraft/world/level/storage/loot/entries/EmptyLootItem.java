package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EmptyLootItem extends LootPoolSingletonContainer {
   EmptyLootItem(int p_79519_, int p_79520_, LootItemCondition[] p_79521_, LootItemFunction[] p_79522_) {
      super(p_79519_, p_79520_, p_79521_, p_79522_);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.EMPTY;
   }

   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
   }

   public static LootPoolSingletonContainer.Builder<?> emptyItem() {
      return simpleBuilder(EmptyLootItem::new);
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<EmptyLootItem> {
      public EmptyLootItem deserialize(JsonObject pObject, JsonDeserializationContext pContext, int pWeight, int pQuality, LootItemCondition[] pConditions, LootItemFunction[] pFunctions) {
         return new EmptyLootItem(pWeight, pQuality, pConditions, pFunctions);
      }
   }
}