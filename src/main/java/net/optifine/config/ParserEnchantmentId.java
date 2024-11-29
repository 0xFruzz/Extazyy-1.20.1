package net.optifine.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.optifine.util.EnchantmentUtils;

public class ParserEnchantmentId implements IParserInt {
   public int parse(String str, int defVal) {
      ResourceLocation resourcelocation = new ResourceLocation(str);
      Enchantment enchantment = EnchantmentUtils.getEnchantment(resourcelocation);
      return enchantment == null ? defVal : BuiltInRegistries.ENCHANTMENT.getId(enchantment);
   }
}