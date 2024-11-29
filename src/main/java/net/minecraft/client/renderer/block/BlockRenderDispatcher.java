package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.optifine.reflect.Reflector;

public class BlockRenderDispatcher implements ResourceManagerReloadListener {
   private final BlockModelShaper blockModelShaper;
   private final ModelBlockRenderer modelRenderer;
   private final BlockEntityWithoutLevelRenderer blockEntityRenderer;
   private final LiquidBlockRenderer liquidBlockRenderer;
   private final RandomSource random = RandomSource.create();
   private final BlockColors blockColors;

   public BlockRenderDispatcher(BlockModelShaper pBlockModelShaper, BlockEntityWithoutLevelRenderer pBlockEntityRenderer, BlockColors pBlockColors) {
      this.blockModelShaper = pBlockModelShaper;
      this.blockEntityRenderer = pBlockEntityRenderer;
      this.blockColors = pBlockColors;
      if (Reflector.ForgeModelBlockRenderer_Constructor.exists()) {
         this.modelRenderer = (ModelBlockRenderer)Reflector.newInstance(Reflector.ForgeModelBlockRenderer_Constructor, this.blockColors);
      } else {
         this.modelRenderer = new ModelBlockRenderer(this.blockColors);
      }

      this.liquidBlockRenderer = new LiquidBlockRenderer();
   }

   public BlockModelShaper getBlockModelShaper() {
      return this.blockModelShaper;
   }

   public void renderBreakingTexture(BlockState pState, BlockPos pPos, BlockAndTintGetter pLevel, PoseStack pPoseStack, VertexConsumer pConsumer) {
      this.renderBreakingTexture(pState, pPos, pLevel, pPoseStack, pConsumer, ModelData.EMPTY);
   }

   public void renderBreakingTexture(BlockState blockStateIn, BlockPos posIn, BlockAndTintGetter lightReaderIn, PoseStack matrixStackIn, VertexConsumer vertexBuilderIn, ModelData modelData) {
      if (blockStateIn.getRenderShape() == RenderShape.MODEL) {
         BakedModel bakedmodel = this.blockModelShaper.getBlockModel(blockStateIn);
         long i = blockStateIn.getSeed(posIn);
         this.modelRenderer.tesselateBlock(lightReaderIn, bakedmodel, blockStateIn, posIn, matrixStackIn, vertexBuilderIn, true, this.random, i, OverlayTexture.NO_OVERLAY, modelData, (RenderType)null);
      }

   }

   public void renderBatched(BlockState pState, BlockPos pPos, BlockAndTintGetter pLevel, PoseStack pPoseStack, VertexConsumer pConsumer, boolean pCheckSides, RandomSource pRandom) {
      this.renderBatched(pState, pPos, pLevel, pPoseStack, pConsumer, pCheckSides, pRandom, ModelData.EMPTY, (RenderType)null);
   }

   public void renderBatched(BlockState blockStateIn, BlockPos posIn, BlockAndTintGetter lightReaderIn, PoseStack matrixStackIn, VertexConsumer vertexBuilderIn, boolean checkSides, RandomSource rand, ModelData modelData, RenderType renderType) {
      try {
         RenderShape rendershape = blockStateIn.getRenderShape();
         if (rendershape == RenderShape.MODEL) {
            this.modelRenderer.tesselateBlock(lightReaderIn, this.getBlockModel(blockStateIn), blockStateIn, posIn, matrixStackIn, vertexBuilderIn, checkSides, rand, blockStateIn.getSeed(posIn), OverlayTexture.NO_OVERLAY, modelData, renderType);
         }

      } catch (Throwable throwable1) {
         CrashReport crashreport = CrashReport.forThrowable(throwable1, "Tesselating block in world");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
         CrashReportCategory.populateBlockDetails(crashreportcategory, lightReaderIn, posIn, blockStateIn);
         throw new ReportedException(crashreport);
      }
   }

   public void renderLiquid(BlockPos pPos, BlockAndTintGetter pLevel, VertexConsumer pConsumer, BlockState pBlockState, FluidState pFluidState) {
      try {
         this.liquidBlockRenderer.tesselate(pLevel, pPos, pConsumer, pBlockState, pFluidState);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating liquid in world");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
         CrashReportCategory.populateBlockDetails(crashreportcategory, pLevel, pPos, (BlockState)null);
         throw new ReportedException(crashreport);
      }
   }

   public ModelBlockRenderer getModelRenderer() {
      return this.modelRenderer;
   }

   public BakedModel getBlockModel(BlockState pState) {
      return this.blockModelShaper.getBlockModel(pState);
   }

   public void renderSingleBlock(BlockState pState, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
      this.renderSingleBlock(pState, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay, ModelData.EMPTY, (RenderType)null);
   }

   public void renderSingleBlock(BlockState blockStateIn, PoseStack matrixStackIn, MultiBufferSource bufferTypeIn, int combinedLightIn, int combinedOverlayIn, ModelData modelData, RenderType renderType) {
      RenderShape rendershape = blockStateIn.getRenderShape();
      if (rendershape != RenderShape.INVISIBLE) {
         switch (rendershape) {
            case MODEL:
               BakedModel bakedmodel = this.getBlockModel(blockStateIn);
               int i = this.blockColors.getColor(blockStateIn, (BlockAndTintGetter)null, (BlockPos)null, 0);
               float f = (float)(i >> 16 & 255) / 255.0F;
               float f1 = (float)(i >> 8 & 255) / 255.0F;
               float f2 = (float)(i & 255) / 255.0F;
               if (Reflector.ForgeHooksClient.exists()) {
                  for(RenderType rendertype : bakedmodel.getRenderTypes(blockStateIn, RandomSource.create(42L), modelData)) {
                     this.modelRenderer.renderModel(matrixStackIn.last(), bufferTypeIn.getBuffer(renderType != null ? renderType : RenderTypeHelper.getEntityRenderType(rendertype, false)), blockStateIn, bakedmodel, f, f1, f2, combinedLightIn, combinedOverlayIn, modelData, rendertype);
                  }
               } else {
                  this.modelRenderer.renderModel(matrixStackIn.last(), bufferTypeIn.getBuffer(ItemBlockRenderTypes.getRenderType(blockStateIn, false)), blockStateIn, bakedmodel, f, f1, f2, combinedLightIn, combinedOverlayIn);
               }
               break;
            case ENTITYBLOCK_ANIMATED:
               if (Reflector.MinecraftForge.exists()) {
                  ItemStack itemstack = new ItemStack(blockStateIn.getBlock());
                  IClientItemExtensions iclientitemextensions = IClientItemExtensions.of(itemstack);
                  BlockEntityWithoutLevelRenderer blockentitywithoutlevelrenderer = iclientitemextensions.getCustomRenderer();
                  blockentitywithoutlevelrenderer.renderByItem(itemstack, ItemDisplayContext.NONE, matrixStackIn, bufferTypeIn, combinedLightIn, combinedOverlayIn);
               } else {
                  this.blockEntityRenderer.renderByItem(new ItemStack(blockStateIn.getBlock()), ItemDisplayContext.NONE, matrixStackIn, bufferTypeIn, combinedLightIn, combinedOverlayIn);
               }
         }
      }

   }

   public void onResourceManagerReload(ResourceManager pResourceManager) {
      this.liquidBlockRenderer.setupSprites();
   }
}