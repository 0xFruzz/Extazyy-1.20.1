package net.optifine;

import com.google.common.primitives.Floats;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.optifine.reflect.Reflector;
import net.optifine.util.CompoundKey;

public class ItemOverrideCache {
   private ItemOverrideProperty[] itemOverrideProperties;
   private Map<CompoundKey, Integer> mapModelIndexes = new HashMap<>();
   public static final Integer INDEX_NONE = new Integer(-1);

   public ItemOverrideCache(ItemOverrideProperty[] itemOverrideProperties) {
      this.itemOverrideProperties = itemOverrideProperties;
   }

   public Integer getModelIndex(ItemStack stack, ClientLevel world, LivingEntity entity) {
      CompoundKey compoundkey = this.getValueKey(stack, world, entity);
      return compoundkey == null ? null : this.mapModelIndexes.get(compoundkey);
   }

   public void putModelIndex(ItemStack stack, ClientLevel world, LivingEntity entity, Integer index) {
      CompoundKey compoundkey = this.getValueKey(stack, world, entity);
      if (compoundkey != null) {
         this.mapModelIndexes.put(compoundkey, index);
      }
   }

   private CompoundKey getValueKey(ItemStack stack, ClientLevel world, LivingEntity entity) {
      Integer[] ainteger = new Integer[this.itemOverrideProperties.length];

      for(int i = 0; i < ainteger.length; ++i) {
         Integer integer = this.itemOverrideProperties[i].getValueIndex(stack, world, entity);
         if (integer == null) {
            return null;
         }

         ainteger[i] = integer;
      }

      return new CompoundKey(ainteger);
   }

   public static ItemOverrideCache make(List<ItemOverride> overrides) {
      if (overrides.isEmpty()) {
         return null;
      } else if (!Reflector.ItemOverride_listResourceValues.exists()) {
         return null;
      } else {
         Map<ResourceLocation, Set<Float>> map = new LinkedHashMap<>();

         for(ItemOverride itemoverride : overrides) {
            for(ItemOverride.Predicate itemoverride$predicate : (List<ItemOverride.Predicate>)Reflector.getFieldValue(itemoverride, Reflector.ItemOverride_listResourceValues)) {
               ResourceLocation resourcelocation = itemoverride$predicate.getProperty();
               float f = itemoverride$predicate.getValue();
               Set<Float> set = map.get(resourcelocation);
               if (set == null) {
                  set = new HashSet<>();
                  map.put(resourcelocation, set);
               }

               set.add(f);
            }
         }

         List<ItemOverrideProperty> list = new ArrayList<>();

         for(ResourceLocation resourcelocation1 : map.keySet()) {
            Set<Float> set1 = map.get(resourcelocation1);
            float[] afloat = Floats.toArray(set1);
            ItemOverrideProperty itemoverrideproperty = new ItemOverrideProperty(resourcelocation1, afloat);
            list.add(itemoverrideproperty);
         }

         ItemOverrideProperty[] aitemoverrideproperty = list.toArray(new ItemOverrideProperty[list.size()]);
         ItemOverrideCache itemoverridecache = new ItemOverrideCache(aitemoverrideproperty);
         logCache(aitemoverrideproperty, overrides);
         return itemoverridecache;
      }
   }

   private static void logCache(ItemOverrideProperty[] props, List<ItemOverride> overrides) {
      StringBuffer stringbuffer = new StringBuffer();

      for(int i = 0; i < props.length; ++i) {
         ItemOverrideProperty itemoverrideproperty = props[i];
         if (stringbuffer.length() > 0) {
            stringbuffer.append(", ");
         }

         stringbuffer.append(itemoverrideproperty.getLocation() + "=" + itemoverrideproperty.getValues().length);
      }

      if (overrides.size() > 0) {
         stringbuffer.append(" -> " + overrides.get(0).getModel() + " ...");
      }

      Config.dbg("ItemOverrideCache: " + stringbuffer.toString());
   }

   public String toString() {
      return "properties: " + this.itemOverrideProperties.length + ", modelIndexes: " + this.mapModelIndexes.size();
   }
}
