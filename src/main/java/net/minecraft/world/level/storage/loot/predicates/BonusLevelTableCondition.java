package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class BonusLevelTableCondition implements LootItemCondition {
   final Enchantment enchantment;
   final float[] values;

   BonusLevelTableCondition(Enchantment pEnchantment, float[] pChances) {
      this.enchantment = pEnchantment;
      this.values = pChances;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.TABLE_BONUS;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.TOOL);
   }

   public boolean test(LootContext pContext) {
      ItemStack itemstack = pContext.getParamOrNull(LootContextParams.TOOL);
      int i = itemstack != null ? EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemstack) : 0;
      float f = this.values[Math.min(i, this.values.length - 1)];
      return pContext.getRandom().nextFloat() < f;
   }

   public static LootItemCondition.Builder bonusLevelFlatChance(Enchantment pEnchantment, float... pChances) {
      return () -> {
         return new BonusLevelTableCondition(pEnchantment, pChances);
      };
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<BonusLevelTableCondition> {
      public void serialize(JsonObject p_81537_, BonusLevelTableCondition p_81538_, JsonSerializationContext p_81539_) {
         p_81537_.addProperty("enchantment", BuiltInRegistries.ENCHANTMENT.getKey(p_81538_.enchantment).toString());
         p_81537_.add("chances", p_81539_.serialize(p_81538_.values));
      }

      public BonusLevelTableCondition deserialize(JsonObject p_81547_, JsonDeserializationContext p_81548_) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_81547_, "enchantment"));
         Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(resourcelocation).orElseThrow(() -> {
            return new JsonParseException("Invalid enchantment id: " + resourcelocation);
         });
         float[] afloat = GsonHelper.getAsObject(p_81547_, "chances", p_81548_, float[].class);
         return new BonusLevelTableCondition(enchantment, afloat);
      }
   }
}