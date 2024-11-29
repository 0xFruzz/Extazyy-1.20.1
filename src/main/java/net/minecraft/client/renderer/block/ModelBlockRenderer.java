package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.optifine.BetterSnow;
import net.optifine.BlockPosM;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.EmissiveTextures;
import net.optifine.model.BlockModelCustomizer;
import net.optifine.model.ListQuadsOverlay;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;
import net.optifine.render.LightCacheOF;
import net.optifine.render.RenderEnv;
import net.optifine.render.RenderTypes;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.shaders.Shaders;
import net.optifine.util.BlockUtils;

public class ModelBlockRenderer {
   private static final int FACE_CUBIC = 0;
   private static final int FACE_PARTIAL = 1;
   static final Direction[] DIRECTIONS = Direction.values();
   private final BlockColors blockColors;
   private static final int CACHE_SIZE = 100;
   static final ThreadLocal<ModelBlockRenderer.Cache> CACHE = ThreadLocal.withInitial(ModelBlockRenderer.Cache::new);
   private static float aoLightValueOpaque = 0.2F;
   private static boolean separateAoLightValue = false;
   private static final LightCacheOF LIGHT_CACHE_OF = new LightCacheOF();
   private static final RenderType[] OVERLAY_LAYERS = new RenderType[]{RenderTypes.CUTOUT, RenderTypes.CUTOUT_MIPPED, RenderTypes.TRANSLUCENT};
   private boolean forge = Reflector.ForgeHooksClient.exists();

   public ModelBlockRenderer(BlockColors pBlockColors) {
      this.blockColors = pBlockColors;
   }

   public void tesselateBlock(BlockAndTintGetter pLevel, BakedModel pModel, BlockState pState, BlockPos pPos, PoseStack pPoseStack, VertexConsumer pConsumer, boolean pCheckSides, RandomSource pRandom, long pSeed, int pPackedOverlay) {
      this.tesselateBlock(pLevel, pModel, pState, pPos, pPoseStack, pConsumer, pCheckSides, pRandom, pSeed, pPackedOverlay, ModelData.EMPTY, (RenderType)null);
   }

   public void tesselateBlock(BlockAndTintGetter worldIn, BakedModel modelIn, BlockState stateIn, BlockPos posIn, PoseStack matrixIn, VertexConsumer buffer, boolean checkSides, RandomSource randomIn, long rand, int combinedOverlayIn, ModelData modelData, RenderType renderType) {
      boolean flag = Minecraft.useAmbientOcclusion() && stateIn.getLightEmission(worldIn, posIn) == 0 && modelIn.useAmbientOcclusion(stateIn, renderType);
      Vec3 vec3 = stateIn.getOffset(worldIn, posIn);
      matrixIn.translate(vec3.x, vec3.y, vec3.z);

      try {
         if (Config.isShaders()) {
            SVertexBuilder.pushEntity(stateIn, buffer);
         }

         if (!Config.isAlternateBlocks()) {
            rand = 0L;
         }

         RenderEnv renderenv = buffer.getRenderEnv(stateIn, posIn);
         modelIn = BlockModelCustomizer.getRenderModel(modelIn, stateIn, renderenv);
         int i = buffer.getVertexCount();
         if (flag) {
            this.renderModelSmooth(worldIn, modelIn, stateIn, posIn, matrixIn, buffer, checkSides, randomIn, rand, combinedOverlayIn, modelData, renderType);
         } else {
            this.renderModelFlat(worldIn, modelIn, stateIn, posIn, matrixIn, buffer, checkSides, randomIn, rand, combinedOverlayIn, modelData, renderType);
         }

         if (buffer.getVertexCount() != i) {
            this.renderOverlayModels(worldIn, modelIn, stateIn, posIn, matrixIn, buffer, combinedOverlayIn, checkSides, randomIn, rand, renderenv, flag, vec3);
         }

         if (Config.isShaders()) {
            SVertexBuilder.popEntity(buffer);
         }

      } catch (Throwable throwable1) {
         CrashReport crashreport = CrashReport.forThrowable(throwable1, "Tesselating block model");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block model being tesselated");
         CrashReportCategory.populateBlockDetails(crashreportcategory, worldIn, posIn, stateIn);
         crashreportcategory.setDetail("Using AO", flag);
         throw new ReportedException(crashreport);
      }
   }

   public void tesselateWithAO(BlockAndTintGetter pLevel, BakedModel pModel, BlockState pState, BlockPos pPos, PoseStack pPoseStack, VertexConsumer pConsumer, boolean pCheckSides, RandomSource pRandom, long pSeed, int pPackedOverlay) {
      this.renderModelSmooth(pLevel, pModel, pState, pPos, pPoseStack, pConsumer, pCheckSides, pRandom, pSeed, pPackedOverlay, ModelData.EMPTY, (RenderType)null);
   }

   public void renderModelSmooth(BlockAndTintGetter worldIn, BakedModel modelIn, BlockState stateIn, BlockPos posIn, PoseStack matrixStackIn, VertexConsumer buffer, boolean checkSides, RandomSource randomIn, long rand, int combinedOverlayIn, ModelData modelData, RenderType renderType) {
      RenderEnv renderenv = buffer.getRenderEnv(stateIn, posIn);
      RenderType rendertype = buffer.getRenderType();

      for(Direction direction : DIRECTIONS) {
         if (!checkSides || BlockUtils.shouldSideBeRendered(stateIn, worldIn, posIn, direction, renderenv)) {
            randomIn.setSeed(rand);
            List<BakedQuad> list = this.forge ? modelIn.getQuads(stateIn, direction, randomIn, modelData, renderType) : modelIn.getQuads(stateIn, direction, randomIn);
            list = BlockModelCustomizer.getRenderQuads(list, worldIn, stateIn, posIn, direction, rendertype, rand, renderenv);
            if (!list.isEmpty()) {
               this.renderQuadsSmooth(worldIn, stateIn, posIn, matrixStackIn, buffer, list, combinedOverlayIn, renderenv);
            }
         }
      }

      randomIn.setSeed(rand);
      List<BakedQuad> list1 = this.forge ? modelIn.getQuads(stateIn, (Direction)null, randomIn, modelData, renderType) : modelIn.getQuads(stateIn, (Direction)null, randomIn);
      if (!list1.isEmpty()) {
         list1 = BlockModelCustomizer.getRenderQuads(list1, worldIn, stateIn, posIn, (Direction)null, rendertype, rand, renderenv);
         this.renderQuadsSmooth(worldIn, stateIn, posIn, matrixStackIn, buffer, list1, combinedOverlayIn, renderenv);
      }

   }

   public void tesselateWithoutAO(BlockAndTintGetter pLevel, BakedModel pModel, BlockState pState, BlockPos pPos, PoseStack pPoseStack, VertexConsumer pConsumer, boolean pCheckSides, RandomSource pRandom, long pSeed, int pPackedOverlay) {
      this.renderModelFlat(pLevel, pModel, pState, pPos, pPoseStack, pConsumer, pCheckSides, pRandom, pSeed, pPackedOverlay, ModelData.EMPTY, (RenderType)null);
   }

   public void renderModelFlat(BlockAndTintGetter worldIn, BakedModel modelIn, BlockState stateIn, BlockPos posIn, PoseStack matrixStackIn, VertexConsumer buffer, boolean checkSides, RandomSource randomIn, long rand, int combinedOverlayIn, ModelData modelData, RenderType renderType) {
      RenderEnv renderenv = buffer.getRenderEnv(stateIn, posIn);
      RenderType rendertype = buffer.getRenderType();

      for(Direction direction : DIRECTIONS) {
         if (!checkSides || BlockUtils.shouldSideBeRendered(stateIn, worldIn, posIn, direction, renderenv)) {
            randomIn.setSeed(rand);
            List<BakedQuad> list = this.forge ? modelIn.getQuads(stateIn, direction, randomIn, modelData, renderType) : modelIn.getQuads(stateIn, direction, randomIn);
            list = BlockModelCustomizer.getRenderQuads(list, worldIn, stateIn, posIn, direction, rendertype, rand, renderenv);
            if (!list.isEmpty()) {
               BlockPos.MutableBlockPos blockpos$mutableblockpos = renderenv.getRenderMutableBlockPos();
               blockpos$mutableblockpos.setWithOffset(posIn, direction);
               int i = LevelRenderer.getLightColor(worldIn, stateIn, blockpos$mutableblockpos);
               this.renderQuadsFlat(worldIn, stateIn, posIn, i, combinedOverlayIn, false, matrixStackIn, buffer, list, renderenv);
            }
         }
      }

      randomIn.setSeed(rand);
      List<BakedQuad> list1 = this.forge ? modelIn.getQuads(stateIn, (Direction)null, randomIn, modelData, renderType) : modelIn.getQuads(stateIn, (Direction)null, randomIn);
      if (!list1.isEmpty()) {
         list1 = BlockModelCustomizer.getRenderQuads(list1, worldIn, stateIn, posIn, (Direction)null, rendertype, rand, renderenv);
         this.renderQuadsFlat(worldIn, stateIn, posIn, -1, combinedOverlayIn, true, matrixStackIn, buffer, list1, renderenv);
      }

   }

   private void renderQuadsSmooth(BlockAndTintGetter blockAccessIn, BlockState stateIn, BlockPos posIn, PoseStack matrixStackIn, VertexConsumer buffer, List<BakedQuad> list, int combinedOverlayIn, RenderEnv renderEnv) {
      float[] afloat = renderEnv.getQuadBounds();
      BitSet bitset = renderEnv.getBoundsFlags();
      ModelBlockRenderer.AmbientOcclusionFace modelblockrenderer$ambientocclusionface = renderEnv.getAoFace();
      int i = list.size();

      for(int j = 0; j < i; ++j) {
         BakedQuad bakedquad = list.get(j);
         this.calculateShape(blockAccessIn, stateIn, posIn, bakedquad.getVertices(), bakedquad.getDirection(), afloat, bitset);
         if (bakedquad.hasAmbientOcclusion() || !ReflectorForge.calculateFaceWithoutAO(blockAccessIn, stateIn, posIn, bakedquad, bitset.get(0), modelblockrenderer$ambientocclusionface.brightness, modelblockrenderer$ambientocclusionface.lightmap)) {
            modelblockrenderer$ambientocclusionface.calculate(blockAccessIn, stateIn, posIn, bakedquad.getDirection(), afloat, bitset, bakedquad.isShade());
         }

         if (bakedquad.getSprite().isSpriteEmissive) {
            modelblockrenderer$ambientocclusionface.setMaxBlockLight();
         }

         this.renderQuadSmooth(blockAccessIn, stateIn, posIn, buffer, matrixStackIn.last(), bakedquad, modelblockrenderer$ambientocclusionface.brightness[0], modelblockrenderer$ambientocclusionface.brightness[1], modelblockrenderer$ambientocclusionface.brightness[2], modelblockrenderer$ambientocclusionface.brightness[3], modelblockrenderer$ambientocclusionface.lightmap[0], modelblockrenderer$ambientocclusionface.lightmap[1], modelblockrenderer$ambientocclusionface.lightmap[2], modelblockrenderer$ambientocclusionface.lightmap[3], combinedOverlayIn, renderEnv);
      }

   }

   private void renderQuadSmooth(BlockAndTintGetter blockAccessIn, BlockState stateIn, BlockPos posIn, VertexConsumer buffer, PoseStack.Pose matrixEntry, BakedQuad quadIn, float colorMul0, float colorMul1, float colorMul2, float colorMul3, int brightness0, int brightness1, int brightness2, int brightness3, int combinedOverlayIn, RenderEnv renderEnv) {
      int i = CustomColors.getColorMultiplier(quadIn, stateIn, blockAccessIn, posIn, renderEnv);
      float f;
      float f1;
      float f2;
      if (!quadIn.isTinted() && i == -1) {
         f = 1.0F;
         f1 = 1.0F;
         f2 = 1.0F;
      } else {
         int j = i != -1 ? i : this.blockColors.getColor(stateIn, blockAccessIn, posIn, quadIn.getTintIndex());
         f = (float)(j >> 16 & 255) / 255.0F;
         f1 = (float)(j >> 8 & 255) / 255.0F;
         f2 = (float)(j & 255) / 255.0F;
      }

      buffer.putBulkData(matrixEntry, quadIn, buffer.getTempFloat4(colorMul0, colorMul1, colorMul2, colorMul3), f, f1, f2, buffer.getTempInt4(brightness0, brightness1, brightness2, brightness3), combinedOverlayIn, true);
   }

   private void calculateShape(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, int[] pVertices, Direction pDirection, @Nullable float[] pShape, BitSet pShapeFlags) {
      float f = 32.0F;
      float f1 = 32.0F;
      float f2 = 32.0F;
      float f3 = -32.0F;
      float f4 = -32.0F;
      float f5 = -32.0F;
      int i = pVertices.length / 4;

      for(int j = 0; j < 4; ++j) {
         float f6 = Float.intBitsToFloat(pVertices[j * i]);
         float f7 = Float.intBitsToFloat(pVertices[j * i + 1]);
         float f8 = Float.intBitsToFloat(pVertices[j * i + 2]);
         f = Math.min(f, f6);
         f1 = Math.min(f1, f7);
         f2 = Math.min(f2, f8);
         f3 = Math.max(f3, f6);
         f4 = Math.max(f4, f7);
         f5 = Math.max(f5, f8);
      }

      if (pShape != null) {
         pShape[Direction.WEST.get3DDataValue()] = f;
         pShape[Direction.EAST.get3DDataValue()] = f3;
         pShape[Direction.DOWN.get3DDataValue()] = f1;
         pShape[Direction.UP.get3DDataValue()] = f4;
         pShape[Direction.NORTH.get3DDataValue()] = f2;
         pShape[Direction.SOUTH.get3DDataValue()] = f5;
         int k = DIRECTIONS.length;
         pShape[Direction.WEST.get3DDataValue() + k] = 1.0F - f;
         pShape[Direction.EAST.get3DDataValue() + k] = 1.0F - f3;
         pShape[Direction.DOWN.get3DDataValue() + k] = 1.0F - f1;
         pShape[Direction.UP.get3DDataValue() + k] = 1.0F - f4;
         pShape[Direction.NORTH.get3DDataValue() + k] = 1.0F - f2;
         pShape[Direction.SOUTH.get3DDataValue() + k] = 1.0F - f5;
      }

      float f9 = 1.0E-4F;
      float f10 = 0.9999F;
      switch (pDirection) {
         case DOWN:
            pShapeFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
            pShapeFlags.set(0, f1 == f4 && (f1 < 1.0E-4F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
            break;
         case UP:
            pShapeFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
            pShapeFlags.set(0, f1 == f4 && (f4 > 0.9999F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
            break;
         case NORTH:
            pShapeFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
            pShapeFlags.set(0, f2 == f5 && (f2 < 1.0E-4F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
            break;
         case SOUTH:
            pShapeFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
            pShapeFlags.set(0, f2 == f5 && (f5 > 0.9999F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
            break;
         case WEST:
            pShapeFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
            pShapeFlags.set(0, f == f3 && (f < 1.0E-4F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
            break;
         case EAST:
            pShapeFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
            pShapeFlags.set(0, f == f3 && (f3 > 0.9999F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
      }

   }

   private void renderQuadsFlat(BlockAndTintGetter blockAccessIn, BlockState stateIn, BlockPos posIn, int brightnessIn, int combinedOverlayIn, boolean ownBrightness, PoseStack matrixStackIn, VertexConsumer buffer, List<BakedQuad> list, RenderEnv renderEnv) {
      BitSet bitset = renderEnv.getBoundsFlags();
      int i = list.size();

      for(int j = 0; j < i; ++j) {
         BakedQuad bakedquad = list.get(j);
         if (ownBrightness) {
            this.calculateShape(blockAccessIn, stateIn, posIn, bakedquad.getVertices(), bakedquad.getDirection(), (float[])null, bitset);
            BlockPos blockpos = bitset.get(0) ? posIn.relative(bakedquad.getDirection()) : posIn;
            brightnessIn = LevelRenderer.getLightColor(blockAccessIn, stateIn, blockpos);
         }

         if (bakedquad.getSprite().isSpriteEmissive) {
            brightnessIn = LightTexture.MAX_BRIGHTNESS;
         }

         float f = blockAccessIn.getShade(bakedquad.getDirection(), bakedquad.isShade());
         this.renderQuadSmooth(blockAccessIn, stateIn, posIn, buffer, matrixStackIn.last(), bakedquad, f, f, f, f, brightnessIn, brightnessIn, brightnessIn, brightnessIn, combinedOverlayIn, renderEnv);
      }

   }

   public void renderModel(PoseStack.Pose pPose, VertexConsumer pConsumer, @Nullable BlockState pState, BakedModel pModel, float pRed, float pGreen, float pBlue, int pPackedLight, int pPackedOverlay) {
      this.renderModel(pPose, pConsumer, pState, pModel, pRed, pGreen, pBlue, pPackedLight, pPackedOverlay, ModelData.EMPTY, (RenderType)null);
   }

   public void renderModel(PoseStack.Pose matrixEntry, VertexConsumer buffer, @Nullable BlockState state, BakedModel modelIn, float red, float green, float blue, int combinedLightIn, int combinedOverlayIn, ModelData modelData, RenderType renderType) {
      RandomSource randomsource = RandomSource.create();
      long i = 42L;

      for(Direction direction : DIRECTIONS) {
         randomsource.setSeed(42L);
         if (this.forge) {
            renderQuadList(matrixEntry, buffer, red, green, blue, modelIn.getQuads(state, direction, randomsource, modelData, renderType), combinedLightIn, combinedOverlayIn);
         } else {
            renderQuadList(matrixEntry, buffer, red, green, blue, modelIn.getQuads(state, direction, randomsource), combinedLightIn, combinedOverlayIn);
         }
      }

      randomsource.setSeed(42L);
      if (this.forge) {
         renderQuadList(matrixEntry, buffer, red, green, blue, modelIn.getQuads(state, (Direction)null, randomsource, modelData, renderType), combinedLightIn, combinedOverlayIn);
      } else {
         renderQuadList(matrixEntry, buffer, red, green, blue, modelIn.getQuads(state, (Direction)null, randomsource), combinedLightIn, combinedOverlayIn);
      }

   }

   private static void renderQuadList(PoseStack.Pose pPose, VertexConsumer pConsumer, float pRed, float pGreen, float pBlue, List<BakedQuad> pQuads, int pPackedLight, int pPackedOverlay) {
      boolean flag = EmissiveTextures.isActive();

      for(BakedQuad bakedquad : pQuads) {
         if (flag) {
            bakedquad = EmissiveTextures.getEmissiveQuad(bakedquad);
            if (bakedquad == null) {
               continue;
            }
         }

         float f;
         float f1;
         float f2;
         if (bakedquad.isTinted()) {
            f = Mth.clamp(pRed, 0.0F, 1.0F);
            f1 = Mth.clamp(pGreen, 0.0F, 1.0F);
            f2 = Mth.clamp(pBlue, 0.0F, 1.0F);
         } else {
            f = 1.0F;
            f1 = 1.0F;
            f2 = 1.0F;
         }

         pConsumer.putBulkData(pPose, bakedquad, f, f1, f2, pPackedLight, pPackedOverlay);
      }

   }

   public static void enableCaching() {
      CACHE.get().enable();
   }

   public static void clearCache() {
      CACHE.get().disable();
   }

   public static float fixAoLightValue(float val) {
      return val == 0.2F ? aoLightValueOpaque : val;
   }

   public static void updateAoLightValue() {
      aoLightValueOpaque = 1.0F - Config.getAmbientOcclusionLevel() * 0.8F;
      separateAoLightValue = Config.isShaders() && Shaders.isSeparateAo();
   }

   public static boolean isSeparateAoLightValue() {
      return separateAoLightValue;
   }

   private void renderOverlayModels(BlockAndTintGetter worldIn, BakedModel modelIn, BlockState stateIn, BlockPos posIn, PoseStack matrixStackIn, VertexConsumer buffer, int combinedOverlayIn, boolean checkSides, RandomSource random, long rand, RenderEnv renderEnv, boolean smooth, Vec3 renderOffset) {
      if (renderEnv.isOverlaysRendered()) {
         for(int i = 0; i < OVERLAY_LAYERS.length; ++i) {
            RenderType rendertype = OVERLAY_LAYERS[i];
            ListQuadsOverlay listquadsoverlay = renderEnv.getListQuadsOverlay(rendertype);
            if (listquadsoverlay.size() > 0) {
               ChunkBufferBuilderPack chunkbufferbuilderpack = renderEnv.getRegionRenderCacheBuilder();
               if (chunkbufferbuilderpack != null) {
                  BufferBuilder bufferbuilder = chunkbufferbuilderpack.builder(rendertype);
                  if (!bufferbuilder.building()) {
                     bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
                  }

                  for(int j = 0; j < listquadsoverlay.size(); ++j) {
                     BakedQuad bakedquad = listquadsoverlay.getQuad(j);
                     List<BakedQuad> list = listquadsoverlay.getListQuadsSingle(bakedquad);
                     BlockState blockstate = listquadsoverlay.getBlockState(j);
                     if (bakedquad.getQuadEmissive() != null) {
                        listquadsoverlay.addQuad(bakedquad.getQuadEmissive(), blockstate);
                     }

                     renderEnv.reset(blockstate, posIn);
                     if (smooth) {
                        this.renderQuadsSmooth(worldIn, blockstate, posIn, matrixStackIn, bufferbuilder, list, combinedOverlayIn, renderEnv);
                     } else {
                        int k = LevelRenderer.getLightColor(worldIn, blockstate, posIn.relative(bakedquad.getDirection()));
                        this.renderQuadsFlat(worldIn, blockstate, posIn, k, combinedOverlayIn, false, matrixStackIn, bufferbuilder, list, renderEnv);
                     }
                  }
               }

               listquadsoverlay.clear();
            }
         }
      }

      if (Config.isBetterSnow() && !renderEnv.isBreakingAnimation() && BetterSnow.shouldRender(worldIn, stateIn, posIn)) {
         BakedModel bakedmodel = BetterSnow.getModelSnowLayer();
         BlockState blockstate1 = BetterSnow.getStateSnowLayer();
         matrixStackIn.translate(-renderOffset.x, -renderOffset.y, -renderOffset.z);
         this.tesselateBlock(worldIn, bakedmodel, blockstate1, posIn, matrixStackIn, buffer, checkSides, random, rand, combinedOverlayIn);
      }

   }

   protected static enum AdjacencyInfo {
      DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.SOUTH}),
      UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.SOUTH}),
      NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_WEST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_EAST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST}),
      SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.WEST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.WEST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.EAST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.EAST}),
      WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.SOUTH}),
      EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.SOUTH});

      final Direction[] corners;
      final boolean doNonCubicWeight;
      final ModelBlockRenderer.SizeInfo[] vert0Weights;
      final ModelBlockRenderer.SizeInfo[] vert1Weights;
      final ModelBlockRenderer.SizeInfo[] vert2Weights;
      final ModelBlockRenderer.SizeInfo[] vert3Weights;
      private static final ModelBlockRenderer.AdjacencyInfo[] BY_FACING = Util.make(new ModelBlockRenderer.AdjacencyInfo[6], (infoIn) -> {
         infoIn[Direction.DOWN.get3DDataValue()] = DOWN;
         infoIn[Direction.UP.get3DDataValue()] = UP;
         infoIn[Direction.NORTH.get3DDataValue()] = NORTH;
         infoIn[Direction.SOUTH.get3DDataValue()] = SOUTH;
         infoIn[Direction.WEST.get3DDataValue()] = WEST;
         infoIn[Direction.EAST.get3DDataValue()] = EAST;
      });

      private AdjacencyInfo(Direction[] pCorners, float pShadeBrightness, boolean pDoNonCubicWeight, ModelBlockRenderer.SizeInfo[] pVert0Weights, ModelBlockRenderer.SizeInfo[] pVert1Weights, ModelBlockRenderer.SizeInfo[] pVert2Weights, ModelBlockRenderer.SizeInfo[] pVert3Weights) {
         this.corners = pCorners;
         this.doNonCubicWeight = pDoNonCubicWeight;
         this.vert0Weights = pVert0Weights;
         this.vert1Weights = pVert1Weights;
         this.vert2Weights = pVert2Weights;
         this.vert3Weights = pVert3Weights;
      }

      public static ModelBlockRenderer.AdjacencyInfo fromFacing(Direction pFacing) {
         return BY_FACING[pFacing.get3DDataValue()];
      }
   }

   public static class AmbientOcclusionFace {
      final float[] brightness = new float[4];
      final int[] lightmap = new int[4];
      private BlockPosM blockPos = new BlockPosM();

      public AmbientOcclusionFace() {
         this((ModelBlockRenderer)null);
      }

      public AmbientOcclusionFace(ModelBlockRenderer bmr) {
      }

      public void setMaxBlockLight() {
         int i = LightTexture.MAX_BRIGHTNESS;
         this.lightmap[0] = i;
         this.lightmap[1] = i;
         this.lightmap[2] = i;
         this.lightmap[3] = i;
         this.brightness[0] = 1.0F;
         this.brightness[1] = 1.0F;
         this.brightness[2] = 1.0F;
         this.brightness[3] = 1.0F;
      }

      public void calculate(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, Direction pDirection, float[] pShape, BitSet pShapeFlags, boolean pShade) {
         BlockPos blockpos = pShapeFlags.get(0) ? pPos.relative(pDirection) : pPos;
         ModelBlockRenderer.AdjacencyInfo modelblockrenderer$adjacencyinfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(pDirection);
         BlockPosM blockposm = this.blockPos;
         LightCacheOF lightcacheof = ModelBlockRenderer.LIGHT_CACHE_OF;
         blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0]);
         BlockState blockstate = pLevel.getBlockState(blockposm);
         int i = LightCacheOF.getPackedLight(blockstate, pLevel, blockposm);
         float f = LightCacheOF.getBrightness(blockstate, pLevel, blockposm);
         blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1]);
         BlockState blockstate1 = pLevel.getBlockState(blockposm);
         int j = LightCacheOF.getPackedLight(blockstate1, pLevel, blockposm);
         float f1 = LightCacheOF.getBrightness(blockstate1, pLevel, blockposm);
         blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[2]);
         BlockState blockstate2 = pLevel.getBlockState(blockposm);
         int k = LightCacheOF.getPackedLight(blockstate2, pLevel, blockposm);
         float f2 = LightCacheOF.getBrightness(blockstate2, pLevel, blockposm);
         blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[3]);
         BlockState blockstate3 = pLevel.getBlockState(blockposm);
         int l = LightCacheOF.getPackedLight(blockstate3, pLevel, blockposm);
         float f3 = LightCacheOF.getBrightness(blockstate3, pLevel, blockposm);
         boolean flag = !blockstate.isViewBlocking(pLevel, blockposm) || blockstate.getLightBlock(pLevel, blockposm) == 0;
         boolean flag1 = !blockstate1.isViewBlocking(pLevel, blockposm) || blockstate1.getLightBlock(pLevel, blockposm) == 0;
         boolean flag2 = !blockstate2.isViewBlocking(pLevel, blockposm) || blockstate2.getLightBlock(pLevel, blockposm) == 0;
         boolean flag3 = !blockstate3.isViewBlocking(pLevel, blockposm) || blockstate3.getLightBlock(pLevel, blockposm) == 0;
         float f4;
         int i1;
         if (!flag2 && !flag) {
            f4 = (f + f2) / 2.0F;
            i1 = blend(i, k, 0, 0);
         } else {
            blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0], modelblockrenderer$adjacencyinfo.corners[2]);
            BlockState blockstate4 = pLevel.getBlockState(blockposm);
            f4 = LightCacheOF.getBrightness(blockstate4, pLevel, blockposm);
            i1 = LightCacheOF.getPackedLight(blockstate4, pLevel, blockposm);
         }

         int j1;
         float f26;
         if (!flag3 && !flag) {
            f26 = (f + f3) / 2.0F;
            j1 = blend(i, l, 0, 0);
         } else {
            blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0], modelblockrenderer$adjacencyinfo.corners[3]);
            BlockState blockstate5 = pLevel.getBlockState(blockposm);
            f26 = LightCacheOF.getBrightness(blockstate5, pLevel, blockposm);
            j1 = LightCacheOF.getPackedLight(blockstate5, pLevel, blockposm);
         }

         int k1;
         float f27;
         if (!flag2 && !flag1) {
            f27 = (f1 + f2) / 2.0F;
            k1 = blend(j, k, 0, 0);
         } else {
            blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1], modelblockrenderer$adjacencyinfo.corners[2]);
            BlockState blockstate6 = pLevel.getBlockState(blockposm);
            f27 = LightCacheOF.getBrightness(blockstate6, pLevel, blockposm);
            k1 = LightCacheOF.getPackedLight(blockstate6, pLevel, blockposm);
         }

         int l1;
         float f28;
         if (!flag3 && !flag1) {
            f28 = (f1 + f3) / 2.0F;
            l1 = blend(j, l, 0, 0);
         } else {
            blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1], modelblockrenderer$adjacencyinfo.corners[3]);
            BlockState blockstate7 = pLevel.getBlockState(blockposm);
            f28 = LightCacheOF.getBrightness(blockstate7, pLevel, blockposm);
            l1 = LightCacheOF.getPackedLight(blockstate7, pLevel, blockposm);
         }

         int i3 = LightCacheOF.getPackedLight(pState, pLevel, pPos);
         blockposm.setPosOffset(pPos, pDirection);
         BlockState blockstate8 = pLevel.getBlockState(blockposm);
         if (pShapeFlags.get(0) || !blockstate8.isSolidRender(pLevel, blockposm)) {
            i3 = LightCacheOF.getPackedLight(blockstate8, pLevel, blockposm);
         }

         float f5 = pShapeFlags.get(0) ? LightCacheOF.getBrightness(pLevel.getBlockState(blockpos), pLevel, blockpos) : LightCacheOF.getBrightness(pLevel.getBlockState(pPos), pLevel, pPos);
         ModelBlockRenderer.AmbientVertexRemap modelblockrenderer$ambientvertexremap = ModelBlockRenderer.AmbientVertexRemap.fromFacing(pDirection);
         if (pShapeFlags.get(1) && modelblockrenderer$adjacencyinfo.doNonCubicWeight) {
            float f29 = (f3 + f + f26 + f5) * 0.25F;
            float f31 = (f2 + f + f4 + f5) * 0.25F;
            float f32 = (f2 + f1 + f27 + f5) * 0.25F;
            float f33 = (f3 + f1 + f28 + f5) * 0.25F;
            float f10 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[0].shape] * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[1].shape];
            float f11 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[2].shape] * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[3].shape];
            float f12 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[4].shape] * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[5].shape];
            float f13 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[6].shape] * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[7].shape];
            float f14 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[0].shape] * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[1].shape];
            float f15 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[2].shape] * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[3].shape];
            float f16 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[4].shape] * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[5].shape];
            float f17 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[6].shape] * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[7].shape];
            float f18 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[0].shape] * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[1].shape];
            float f19 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[2].shape] * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[3].shape];
            float f20 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[4].shape] * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[5].shape];
            float f21 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[6].shape] * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[7].shape];
            float f22 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[0].shape] * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[1].shape];
            float f23 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[2].shape] * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[3].shape];
            float f24 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[4].shape] * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[5].shape];
            float f25 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[6].shape] * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[7].shape];
            this.brightness[modelblockrenderer$ambientvertexremap.vert0] = f29 * f10 + f31 * f11 + f32 * f12 + f33 * f13;
            this.brightness[modelblockrenderer$ambientvertexremap.vert1] = f29 * f14 + f31 * f15 + f32 * f16 + f33 * f17;
            this.brightness[modelblockrenderer$ambientvertexremap.vert2] = f29 * f18 + f31 * f19 + f32 * f20 + f33 * f21;
            this.brightness[modelblockrenderer$ambientvertexremap.vert3] = f29 * f22 + f31 * f23 + f32 * f24 + f33 * f25;
            int i2 = blend(l, i, j1, i3);
            int j2 = blend(k, i, i1, i3);
            int k2 = blend(k, j, k1, i3);
            int l2 = blend(l, j, l1, i3);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert0] = this.blend(i2, j2, k2, l2, f10, f11, f12, f13);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert1] = this.blend(i2, j2, k2, l2, f14, f15, f16, f17);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert2] = this.blend(i2, j2, k2, l2, f18, f19, f20, f21);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert3] = this.blend(i2, j2, k2, l2, f22, f23, f24, f25);
         } else {
            float f6 = (f3 + f + f26 + f5) * 0.25F;
            float f7 = (f2 + f + f4 + f5) * 0.25F;
            float f8 = (f2 + f1 + f27 + f5) * 0.25F;
            float f9 = (f3 + f1 + f28 + f5) * 0.25F;
            this.lightmap[modelblockrenderer$ambientvertexremap.vert0] = blend(l, i, j1, i3);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert1] = blend(k, i, i1, i3);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert2] = blend(k, j, k1, i3);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert3] = blend(l, j, l1, i3);
            this.brightness[modelblockrenderer$ambientvertexremap.vert0] = f6;
            this.brightness[modelblockrenderer$ambientvertexremap.vert1] = f7;
            this.brightness[modelblockrenderer$ambientvertexremap.vert2] = f8;
            this.brightness[modelblockrenderer$ambientvertexremap.vert3] = f9;
         }

         float f30 = pLevel.getShade(pDirection, pShade);

         for(int j3 = 0; j3 < this.brightness.length; ++j3) {
            this.brightness[j3] *= f30;
         }

      }

      public static int blend(int pLightColor0, int pLightColor1, int pLightColor2, int pLightColor3) {
         if (pLightColor0 != 15794417 && pLightColor1 != 15794417 && pLightColor2 != 15794417 && pLightColor3 != 15794417) {
            int i = pLightColor0 + pLightColor1 + pLightColor2 + pLightColor3;
            int j = 4;
            if (pLightColor0 == 0) {
               --j;
            }

            if (pLightColor1 == 0) {
               --j;
            }

            if (pLightColor2 == 0) {
               --j;
            }

            if (pLightColor3 == 0) {
               --j;
            }

            switch (j) {
               case 0:
               case 1:
                  return i;
               case 2:
                  return i >> 1 & 16711935;
               case 3:
                  return i / 3 & 16711680 | (i & '\uffff') / 3;
               default:
                  return i >> 2 & 16711935;
            }
         } else {
            return pLightColor0 + pLightColor1 + pLightColor2 + pLightColor3 >> 2 & 16711935;
         }
      }

      private int blend(int pBrightness0, int pBrightness1, int pBrightness2, int pBrightness3, float pWeight0, float pWeight1, float pWeight2, float pWeight3) {
         int i = (int)((float)(pBrightness0 >> 16 & 255) * pWeight0 + (float)(pBrightness1 >> 16 & 255) * pWeight1 + (float)(pBrightness2 >> 16 & 255) * pWeight2 + (float)(pBrightness3 >> 16 & 255) * pWeight3) & 255;
         int j = (int)((float)(pBrightness0 & 255) * pWeight0 + (float)(pBrightness1 & 255) * pWeight1 + (float)(pBrightness2 & 255) * pWeight2 + (float)(pBrightness3 & 255) * pWeight3) & 255;
         return i << 16 | j;
      }
   }

   static enum AmbientVertexRemap {
      DOWN(0, 1, 2, 3),
      UP(2, 3, 0, 1),
      NORTH(3, 0, 1, 2),
      SOUTH(0, 1, 2, 3),
      WEST(3, 0, 1, 2),
      EAST(1, 2, 3, 0);

      final int vert0;
      final int vert1;
      final int vert2;
      final int vert3;
      private static final ModelBlockRenderer.AmbientVertexRemap[] BY_FACING = Util.make(new ModelBlockRenderer.AmbientVertexRemap[6], (remapIn) -> {
         remapIn[Direction.DOWN.get3DDataValue()] = DOWN;
         remapIn[Direction.UP.get3DDataValue()] = UP;
         remapIn[Direction.NORTH.get3DDataValue()] = NORTH;
         remapIn[Direction.SOUTH.get3DDataValue()] = SOUTH;
         remapIn[Direction.WEST.get3DDataValue()] = WEST;
         remapIn[Direction.EAST.get3DDataValue()] = EAST;
      });

      private AmbientVertexRemap(int pVert0, int pVert1, int pVert2, int pVert3) {
         this.vert0 = pVert0;
         this.vert1 = pVert1;
         this.vert2 = pVert2;
         this.vert3 = pVert3;
      }

      public static ModelBlockRenderer.AmbientVertexRemap fromFacing(Direction pFacing) {
         return BY_FACING[pFacing.get3DDataValue()];
      }
   }

   static class Cache {
      private boolean enabled;
      private final Long2IntLinkedOpenHashMap colorCache = Util.make(() -> {
         Long2IntLinkedOpenHashMap long2intlinkedopenhashmap = new Long2IntLinkedOpenHashMap(100, 0.25F) {
            protected void rehash(int p_111238_) {
            }
         };
         long2intlinkedopenhashmap.defaultReturnValue(Integer.MAX_VALUE);
         return long2intlinkedopenhashmap;
      });
      private final Long2FloatLinkedOpenHashMap brightnessCache = Util.make(() -> {
         Long2FloatLinkedOpenHashMap long2floatlinkedopenhashmap = new Long2FloatLinkedOpenHashMap(100, 0.25F) {
            protected void rehash(int p_111245_) {
            }
         };
         long2floatlinkedopenhashmap.defaultReturnValue(Float.NaN);
         return long2floatlinkedopenhashmap;
      });

      private Cache() {
      }

      public void enable() {
         this.enabled = true;
      }

      public void disable() {
         this.enabled = false;
         this.colorCache.clear();
         this.brightnessCache.clear();
      }

      public int getLightColor(BlockState pState, BlockAndTintGetter pLevel, BlockPos pPos) {
         long i = pPos.asLong();
         if (this.enabled) {
            int j = this.colorCache.get(i);
            if (j != Integer.MAX_VALUE) {
               return j;
            }
         }

         int k = LevelRenderer.getLightColor(pLevel, pState, pPos);
         if (this.enabled) {
            if (this.colorCache.size() == 100) {
               this.colorCache.removeFirstInt();
            }

            this.colorCache.put(i, k);
         }

         return k;
      }

      public float getShadeBrightness(BlockState pState, BlockAndTintGetter pLevel, BlockPos pPos) {
         long i = pPos.asLong();
         if (this.enabled) {
            float f = this.brightnessCache.get(i);
            if (!Float.isNaN(f)) {
               return f;
            }
         }

         float f1 = pState.getShadeBrightness(pLevel, pPos);
         if (this.enabled) {
            if (this.brightnessCache.size() == 100) {
               this.brightnessCache.removeFirstFloat();
            }

            this.brightnessCache.put(i, f1);
         }

         return f1;
      }
   }

   protected static enum SizeInfo {
      DOWN(Direction.DOWN, false),
      UP(Direction.UP, false),
      NORTH(Direction.NORTH, false),
      SOUTH(Direction.SOUTH, false),
      WEST(Direction.WEST, false),
      EAST(Direction.EAST, false),
      FLIP_DOWN(Direction.DOWN, true),
      FLIP_UP(Direction.UP, true),
      FLIP_NORTH(Direction.NORTH, true),
      FLIP_SOUTH(Direction.SOUTH, true),
      FLIP_WEST(Direction.WEST, true),
      FLIP_EAST(Direction.EAST, true);

      final int shape;

      private SizeInfo(Direction pDirection, boolean pFlip) {
         this.shape = pDirection.get3DDataValue() + (pFlip ? ModelBlockRenderer.DIRECTIONS.length : 0);
      }
   }
}