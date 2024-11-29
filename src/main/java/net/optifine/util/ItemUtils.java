package net.optifine.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemUtils {
   public static Item getItem(ResourceLocation loc) {
      return !BuiltInRegistries.ITEM.containsKey(loc) ? null : BuiltInRegistries.ITEM.get(loc);
   }

   public static int getId(Item item) {
      return BuiltInRegistries.ITEM.getId(item);
   }
}