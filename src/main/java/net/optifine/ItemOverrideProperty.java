package net.optifine;

import java.util.Arrays;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemOverrideProperty {
   private ResourceLocation location;
   private float[] values;

   public ItemOverrideProperty(ResourceLocation location, float[] values) {
      this.location = location;
      this.values = (float[])values.clone();
      Arrays.sort(this.values);
   }

   public Integer getValueIndex(ItemStack stack, ClientLevel world, LivingEntity entity) {
      Item item = stack.getItem();
      ItemPropertyFunction itempropertyfunction = ItemProperties.getProperty(item, this.location);
      if (itempropertyfunction == null) {
         return null;
      } else {
         float f = itempropertyfunction.call(stack, world, entity, 0);
         int i = Arrays.binarySearch(this.values, f);
         return i;
      }
   }

   public ResourceLocation getLocation() {
      return this.location;
   }

   public float[] getValues() {
      return this.values;
   }

   public String toString() {
      return "location: " + this.location + ", values: [" + Config.arrayToString(this.values) + "]";
   }
}