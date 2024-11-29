package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class WeightedBakedModel implements BakedModel, IDynamicBakedModel {
   private final int totalWeight;
   private final List<WeightedEntry.Wrapper<BakedModel>> list;
   private final BakedModel wrapped;

   public WeightedBakedModel(List<WeightedEntry.Wrapper<BakedModel>> pList) {
      this.list = pList;
      this.totalWeight = WeightedRandom.getTotalWeight(pList);
      this.wrapped = pList.get(0).getData();
   }

   public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, RandomSource pRandom) {
      WeightedEntry.Wrapper<BakedModel> wrapper = getWeightedItem(this.list, Math.abs((int)pRandom.nextLong()) % this.totalWeight);
      return wrapper == null ? Collections.emptyList() : wrapper.getData().getQuads(pState, pDirection, pRandom);
   }

   public static <T extends WeightedEntry> T getWeightedItem(List<T> items, int targetWeight) {
      for(int i = 0; i < items.size(); ++i) {
         T t = items.get(i);
         targetWeight -= t.getWeight().asInt();
         if (targetWeight < 0) {
            return t;
         }
      }

      return (T)null;
   }

   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData modelData, RenderType renderType) {
      WeightedEntry.Wrapper<BakedModel> wrapper = getWeightedItem(this.list, Math.abs((int)rand.nextLong()) % this.totalWeight);
      return wrapper == null ? Collections.emptyList() : wrapper.getData().getQuads(state, side, rand, modelData, renderType);
   }

   public boolean useAmbientOcclusion(BlockState state) {
      return this.wrapped.useAmbientOcclusion(state);
   }

   public boolean useAmbientOcclusion(BlockState state, RenderType renderType) {
      return this.wrapped.useAmbientOcclusion(state, renderType);
   }

   public TextureAtlasSprite getParticleIcon(ModelData modelData) {
      return this.wrapped.getParticleIcon(modelData);
   }

   public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
      return this.wrapped.applyTransform(transformType, poseStack, applyLeftHandTransform);
   }

   public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
      WeightedEntry.Wrapper<BakedModel> wrapper = getWeightedItem(this.list, Math.abs((int)rand.nextLong()) % this.totalWeight);
      return wrapper == null ? ChunkRenderTypeSet.none() : wrapper.getData().getRenderTypes(state, rand, data);
   }

   public boolean useAmbientOcclusion() {
      return this.wrapped.useAmbientOcclusion();
   }

   public boolean isGui3d() {
      return this.wrapped.isGui3d();
   }

   public boolean usesBlockLight() {
      return this.wrapped.usesBlockLight();
   }

   public boolean isCustomRenderer() {
      return this.wrapped.isCustomRenderer();
   }

   public TextureAtlasSprite getParticleIcon() {
      return this.wrapped.getParticleIcon();
   }

   public ItemTransforms getTransforms() {
      return this.wrapped.getTransforms();
   }

   public ItemOverrides getOverrides() {
      return this.wrapped.getOverrides();
   }

   public static class Builder {
      private final List<WeightedEntry.Wrapper<BakedModel>> list = Lists.newArrayList();

      public WeightedBakedModel.Builder add(@Nullable BakedModel pModel, int pWeight) {
         if (pModel != null) {
            this.list.add(WeightedEntry.wrap(pModel, pWeight));
         }

         return this;
      }

      @Nullable
      public BakedModel build() {
         if (this.list.isEmpty()) {
            return null;
         } else {
            return (BakedModel)(this.list.size() == 1 ? this.list.get(0).getData() : new WeightedBakedModel(this.list));
         }
      }
   }
}