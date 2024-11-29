package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LootItemRandomChanceWithLootingCondition implements LootItemCondition {
   final float percent;
   final float lootingMultiplier;

   LootItemRandomChanceWithLootingCondition(float pPercent, float pLootingMultiplier) {
      this.percent = pPercent;
      this.lootingMultiplier = pLootingMultiplier;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.RANDOM_CHANCE_WITH_LOOTING;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.KILLER_ENTITY);
   }

   public boolean test(LootContext pContext) {
      Entity entity = pContext.getParamOrNull(LootContextParams.KILLER_ENTITY);
      int i = 0;
      if (entity instanceof LivingEntity) {
         i = EnchantmentHelper.getMobLooting((LivingEntity)entity);
      }

      return pContext.getRandom().nextFloat() < this.percent + (float)i * this.lootingMultiplier;
   }

   public static LootItemCondition.Builder randomChanceAndLootingBoost(float pChance, float pLootingMultiplier) {
      return () -> {
         return new LootItemRandomChanceWithLootingCondition(pChance, pLootingMultiplier);
      };
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemRandomChanceWithLootingCondition> {
      public void serialize(JsonObject p_81983_, LootItemRandomChanceWithLootingCondition p_81984_, JsonSerializationContext p_81985_) {
         p_81983_.addProperty("chance", p_81984_.percent);
         p_81983_.addProperty("looting_multiplier", p_81984_.lootingMultiplier);
      }

      public LootItemRandomChanceWithLootingCondition deserialize(JsonObject p_81991_, JsonDeserializationContext p_81992_) {
         return new LootItemRandomChanceWithLootingCondition(GsonHelper.getAsFloat(p_81991_, "chance"), GsonHelper.getAsFloat(p_81991_, "looting_multiplier"));
      }
   }
}