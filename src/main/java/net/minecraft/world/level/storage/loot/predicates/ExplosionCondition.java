package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ExplosionCondition implements LootItemCondition {
   static final ExplosionCondition INSTANCE = new ExplosionCondition();

   private ExplosionCondition() {
   }

   public LootItemConditionType getType() {
      return LootItemConditions.SURVIVES_EXPLOSION;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.EXPLOSION_RADIUS);
   }

   public boolean test(LootContext pContext) {
      Float f = pContext.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
      if (f != null) {
         RandomSource randomsource = pContext.getRandom();
         float f1 = 1.0F / f;
         return randomsource.nextFloat() <= f1;
      } else {
         return true;
      }
   }

   public static LootItemCondition.Builder survivesExplosion() {
      return () -> {
         return INSTANCE;
      };
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ExplosionCondition> {
      public void serialize(JsonObject p_81671_, ExplosionCondition p_81672_, JsonSerializationContext p_81673_) {
      }

      public ExplosionCondition deserialize(JsonObject p_81679_, JsonDeserializationContext p_81680_) {
         return ExplosionCondition.INSTANCE;
      }
   }
}