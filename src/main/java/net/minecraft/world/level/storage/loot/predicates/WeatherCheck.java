package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;

public class WeatherCheck implements LootItemCondition {
   @Nullable
   final Boolean isRaining;
   @Nullable
   final Boolean isThundering;

   WeatherCheck(@Nullable Boolean pIsRaining, @Nullable Boolean pIsThundering) {
      this.isRaining = pIsRaining;
      this.isThundering = pIsThundering;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.WEATHER_CHECK;
   }

   public boolean test(LootContext pContext) {
      ServerLevel serverlevel = pContext.getLevel();
      if (this.isRaining != null && this.isRaining != serverlevel.isRaining()) {
         return false;
      } else {
         return this.isThundering == null || this.isThundering == serverlevel.isThundering();
      }
   }

   public static WeatherCheck.Builder weather() {
      return new WeatherCheck.Builder();
   }

   public static class Builder implements LootItemCondition.Builder {
      @Nullable
      private Boolean isRaining;
      @Nullable
      private Boolean isThundering;

      public WeatherCheck.Builder setRaining(@Nullable Boolean pIsRaining) {
         this.isRaining = pIsRaining;
         return this;
      }

      public WeatherCheck.Builder setThundering(@Nullable Boolean pIsThundering) {
         this.isThundering = pIsThundering;
         return this;
      }

      public WeatherCheck build() {
         return new WeatherCheck(this.isRaining, this.isThundering);
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<WeatherCheck> {
      public void serialize(JsonObject p_82079_, WeatherCheck p_82080_, JsonSerializationContext p_82081_) {
         p_82079_.addProperty("raining", p_82080_.isRaining);
         p_82079_.addProperty("thundering", p_82080_.isThundering);
      }

      public WeatherCheck deserialize(JsonObject p_82087_, JsonDeserializationContext p_82088_) {
         Boolean obool = p_82087_.has("raining") ? GsonHelper.getAsBoolean(p_82087_, "raining") : null;
         Boolean obool1 = p_82087_.has("thundering") ? GsonHelper.getAsBoolean(p_82087_, "thundering") : null;
         return new WeatherCheck(obool, obool1);
      }
   }
}