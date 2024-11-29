package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LimitCount extends LootItemConditionalFunction {
   final IntRange limiter;

   LimitCount(LootItemCondition[] pConditions, IntRange pCountLimit) {
      super(pConditions);
      this.limiter = pCountLimit;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.LIMIT_COUNT;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.limiter.getReferencedContextParams();
   }

   public ItemStack run(ItemStack pStack, LootContext pContext) {
      int i = this.limiter.clamp(pContext, pStack.getCount());
      pStack.setCount(i);
      return pStack;
   }

   public static LootItemConditionalFunction.Builder<?> limitCount(IntRange pCountLimit) {
      return simpleBuilder((p_165219_) -> {
         return new LimitCount(p_165219_, pCountLimit);
      });
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<LimitCount> {
      public void serialize(JsonObject pJson, LimitCount pLimitCount, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pLimitCount, pSerializationContext);
         pJson.add("limit", pSerializationContext.serialize(pLimitCount.limiter));
      }

      public LimitCount deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         IntRange intrange = GsonHelper.getAsObject(pObject, "limit", pDeserializationContext, IntRange.class);
         return new LimitCount(pConditions, intrange);
      }
   }
}