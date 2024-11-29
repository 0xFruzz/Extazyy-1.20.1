package net.optifine.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.optifine.util.ItemUtils;

public class ItemLocator implements IObjectLocator<Item> {
   public Item getObject(ResourceLocation loc) {
      return ItemUtils.getItem(loc);
   }
}