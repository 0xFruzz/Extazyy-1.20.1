package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class LootingEnchantFunction extends LootItemConditionalFunction {
   public static final int NO_LIMIT = 0;
   final NumberProvider value;
   final int limit;

   LootingEnchantFunction(LootItemCondition[] pConditions, NumberProvider pLootingMultiplier, int pCountLimit) {
      super(pConditions);
      this.value = pLootingMultiplier;
      this.limit = pCountLimit;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.LOOTING_ENCHANT;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return Sets.union(ImmutableSet.of(LootContextParams.KILLER_ENTITY), this.value.getReferencedContextParams());
   }

   boolean hasLimit() {
      return this.limit > 0;
   }

   public ItemStack run(ItemStack pStack, LootContext pContext) {
      Entity entity = pContext.getParamOrNull(LootContextParams.KILLER_ENTITY);
      if (entity instanceof LivingEntity) {
         int i = EnchantmentHelper.getMobLooting((LivingEntity)entity);
         if (i == 0) {
            return pStack;
         }

         float f = (float)i * this.value.getFloat(pContext);
         pStack.grow(Math.round(f));
         if (this.hasLimit() && pStack.getCount() > this.limit) {
            pStack.setCount(this.limit);
         }
      }

      return pStack;
   }

   public static LootingEnchantFunction.Builder lootingMultiplier(NumberProvider pLootingMultiplier) {
      return new LootingEnchantFunction.Builder(pLootingMultiplier);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<LootingEnchantFunction.Builder> {
      private final NumberProvider count;
      private int limit = 0;

      public Builder(NumberProvider pLootingMultiplier) {
         this.count = pLootingMultiplier;
      }

      protected LootingEnchantFunction.Builder getThis() {
         return this;
      }

      public LootingEnchantFunction.Builder setLimit(int pLimit) {
         this.limit = pLimit;
         return this;
      }

      public LootItemFunction build() {
         return new LootingEnchantFunction(this.getConditions(), this.count, this.limit);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<LootingEnchantFunction> {
      public void serialize(JsonObject pJson, LootingEnchantFunction pLootingEnchantFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pLootingEnchantFunction, pSerializationContext);
         pJson.add("count", pSerializationContext.serialize(pLootingEnchantFunction.value));
         if (pLootingEnchantFunction.hasLimit()) {
            pJson.add("limit", pSerializationContext.serialize(pLootingEnchantFunction.limit));
         }

      }

      public LootingEnchantFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         int i = GsonHelper.getAsInt(pObject, "limit", 0);
         return new LootingEnchantFunction(pConditions, GsonHelper.getAsObject(pObject, "count", pDeserializationContext, NumberProvider.class), i);
      }
   }
}