package net.minecraft.client.renderer.block.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.optifine.Config;
import net.optifine.ItemOverrideCache;

public class ItemOverrides {
   public static final ItemOverrides EMPTY = new ItemOverrides();
   public static final float NO_OVERRIDE = Float.NEGATIVE_INFINITY;
   private final ItemOverrides.BakedOverride[] overrides;
   private final ResourceLocation[] properties;
   private ItemOverrideCache itemOverrideCache;
   public static ResourceLocation lastModelLocation = null;

   private ItemOverrides() {
      this.overrides = new ItemOverrides.BakedOverride[0];
      this.properties = new ResourceLocation[0];
   }

   public ItemOverrides(ModelBaker pBaker, BlockModel pModel, List<ItemOverride> pOverrides) {
      this(pBaker, pModel, pOverrides, pBaker.getModelTextureGetter());
   }

   public ItemOverrides(ModelBaker modelBakeryIn, UnbakedModel blockModelIn, List<ItemOverride> itemOverridesIn, Function<Material, TextureAtlasSprite> spriteGetter) {
      this.properties = itemOverridesIn.stream().flatMap(ItemOverride::getPredicates).map(ItemOverride.Predicate::getProperty).distinct().toArray((p_173478_0_) -> {
         return new ResourceLocation[p_173478_0_];
      });
      Object2IntMap<ResourceLocation> object2intmap = new Object2IntOpenHashMap<>();

      for(int i = 0; i < this.properties.length; ++i) {
         object2intmap.put(this.properties[i], i);
      }

      List<ItemOverrides.BakedOverride> list = Lists.newArrayList();

      for(int j = itemOverridesIn.size() - 1; j >= 0; --j) {
         ItemOverride itemoverride = itemOverridesIn.get(j);
         BakedModel bakedmodel = this.bakeModel(modelBakeryIn, blockModelIn, itemoverride, spriteGetter);
         ItemOverrides.PropertyMatcher[] aitemoverrides$propertymatcher = itemoverride.getPredicates().map((p_173475_1_) -> {
            int k = object2intmap.getInt(p_173475_1_.getProperty());
            return new ItemOverrides.PropertyMatcher(k, p_173475_1_.getValue());
         }).toArray((p_173462_0_) -> {
            return new ItemOverrides.PropertyMatcher[p_173462_0_];
         });
         list.add(new ItemOverrides.BakedOverride(aitemoverrides$propertymatcher, bakedmodel));
         ItemOverrides.BakedOverride itemoverrides$bakedoverride = list.get(list.size() - 1);
         itemoverrides$bakedoverride.location = itemoverride.getModel();
      }

      this.overrides = list.toArray(new ItemOverrides.BakedOverride[0]);
      if (itemOverridesIn.size() > 65) {
         this.itemOverrideCache = ItemOverrideCache.make(itemOverridesIn);
      }

   }

   @Nullable
   private BakedModel bakeModel(ModelBaker modelBakeryIn, UnbakedModel blockModelIn, ItemOverride itemOverrideIn, Function<Material, TextureAtlasSprite> spriteGetter) {
      UnbakedModel unbakedmodel = modelBakeryIn.getModel(itemOverrideIn.getModel());
      return Objects.equals(unbakedmodel, blockModelIn) ? null : modelBakeryIn.bake(itemOverrideIn.getModel(), BlockModelRotation.X0_Y0, spriteGetter);
   }

   @Nullable
   public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
      boolean flag = Config.isCustomItems();
      if (flag) {
         lastModelLocation = null;
      }

      if (this.overrides.length != 0) {
         if (this.itemOverrideCache != null) {
            Integer integer = this.itemOverrideCache.getModelIndex(pStack, pLevel, pEntity);
            if (integer != null) {
               int k = integer;
               if (k >= 0 && k < this.overrides.length) {
                  if (flag) {
                     lastModelLocation = this.overrides[k].location;
                  }

                  BakedModel bakedmodel = this.overrides[k].model;
                  if (bakedmodel != null) {
                     return bakedmodel;
                  }
               }

               return pModel;
            }
         }

         Item item = pStack.getItem();
         int i = this.properties.length;
         float[] afloat = new float[i];

         for(int j = 0; j < i; ++j) {
            ResourceLocation resourcelocation = this.properties[j];
            ItemPropertyFunction itempropertyfunction = ItemProperties.getProperty(item, resourcelocation);
            if (itempropertyfunction != null) {
               afloat[j] = itempropertyfunction.call(pStack, pLevel, pEntity, pSeed);
            } else {
               afloat[j] = Float.NEGATIVE_INFINITY;
            }
         }

         for(int l = 0; l < this.overrides.length; ++l) {
            ItemOverrides.BakedOverride itemoverrides$bakedoverride = this.overrides[l];
            if (itemoverrides$bakedoverride.test(afloat)) {
               BakedModel bakedmodel1 = itemoverrides$bakedoverride.model;
               if (flag) {
                  lastModelLocation = itemoverrides$bakedoverride.location;
               }

               if (this.itemOverrideCache != null) {
                  this.itemOverrideCache.putModelIndex(pStack, pLevel, pEntity, l);
               }

               if (bakedmodel1 == null) {
                  return pModel;
               }

               return bakedmodel1;
            }
         }
      }

      return pModel;
   }

   public ImmutableList<ItemOverrides.BakedOverride> getOverrides() {
      return ImmutableList.copyOf(this.overrides);
   }

   static class BakedOverride {
      private final ItemOverrides.PropertyMatcher[] matchers;
      @Nullable
      final BakedModel model;
      private ResourceLocation location;

      BakedOverride(ItemOverrides.PropertyMatcher[] pMatchers, @Nullable BakedModel pModel) {
         this.matchers = pMatchers;
         this.model = pModel;
      }

      boolean test(float[] pProperties) {
         for(ItemOverrides.PropertyMatcher itemoverrides$propertymatcher : this.matchers) {
            float f = pProperties[itemoverrides$propertymatcher.index];
            if (f < itemoverrides$propertymatcher.value) {
               return false;
            }
         }

         return true;
      }
   }

   static class PropertyMatcher {
      public final int index;
      public final float value;

      PropertyMatcher(int pIndex, float pValue) {
         this.index = pIndex;
         this.value = pValue;
      }
   }
}