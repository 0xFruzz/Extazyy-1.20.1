package net.minecraftforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

public interface IForgeBakedModel {
   default BakedModel getBakedModel() {
      return (BakedModel)this;
   }

   default List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData) {
      return this.getBakedModel().getQuads(state, side, rand);
   }

   default List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType renderType) {
      return this.getBakedModel().getQuads(state, side, rand);
   }

   default boolean isAmbientOcclusion(BlockState state) {
      return this.getBakedModel().useAmbientOcclusion();
   }

   default boolean useAmbientOcclusion(BlockState state) {
      return this.getBakedModel().useAmbientOcclusion();
   }

   default boolean useAmbientOcclusion(BlockState state, RenderType renderType) {
      return this.isAmbientOcclusion(state);
   }

   default ModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData tileData) {
      return tileData;
   }

   default TextureAtlasSprite getParticleTexture(ModelData data) {
      return this.getBakedModel().getParticleIcon();
   }

   default TextureAtlasSprite getParticleIcon(ModelData data) {
      return this.self().getParticleIcon();
   }

   default List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
      return List.of(this.self());
   }

   default List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
      return List.of();
   }

   private BakedModel self() {
      return (BakedModel)this;
   }

   default ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
      return null;
   }

   default BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
      this.self().getTransforms().getTransform(transformType).apply(applyLeftHandTransform, poseStack);
      return this.self();
   }
}