package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.Config;

public class MushroomCowMushroomLayer<T extends MushroomCow> extends RenderLayer<T, CowModel<T>> {
   private ModelPart modelRendererMushroom;
   private static final ResourceLocation LOCATION_MUSHROOM_RED = new ResourceLocation("textures/entity/cow/red_mushroom.png");
   private static final ResourceLocation LOCATION_MUSHROOM_BROWN = new ResourceLocation("textures/entity/cow/brown_mushroom.png");
   private static boolean hasTextureMushroomRed = false;
   private static boolean hasTextureMushroomBrown = false;
   private final BlockRenderDispatcher blockRenderer;

   public MushroomCowMushroomLayer(RenderLayerParent<T, CowModel<T>> pRenderer, BlockRenderDispatcher pBlockRenderer) {
      super(pRenderer);
      this.blockRenderer = pBlockRenderer;
      this.modelRendererMushroom = new ModelPart(new ArrayList<>(), new HashMap<>());
      this.modelRendererMushroom.setTextureSize(16, 16);
      this.modelRendererMushroom.x = 8.0F;
      this.modelRendererMushroom.z = 8.0F;
      this.modelRendererMushroom.yRot = ((float)Math.PI / 4F);
      int[][] aint = new int[][]{null, null, {16, 16, 0, 0}, {16, 16, 0, 0}, null, null};
      this.modelRendererMushroom.addBox(aint, -10.0F, 0.0F, 0.0F, 20.0F, 16.0F, 0.0F, 0.0F);
      int[][] aint1 = new int[][]{null, null, null, null, {16, 16, 0, 0}, {16, 16, 0, 0}};
      this.modelRendererMushroom.addBox(aint1, 0.0F, 0.0F, -10.0F, 0.0F, 16.0F, 20.0F, 0.0F);
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (!pLivingEntity.isBaby()) {
         Minecraft minecraft = Minecraft.getInstance();
         boolean flag = minecraft.shouldEntityAppearGlowing(pLivingEntity) && pLivingEntity.isInvisible();
         if (!pLivingEntity.isInvisible() || flag) {
            BlockState blockstate = pLivingEntity.getVariant().getBlockState();
            ResourceLocation resourcelocation = this.getCustomMushroom(blockstate);
            VertexConsumer vertexconsumer = null;
            if (resourcelocation != null) {
               vertexconsumer = pBuffer.getBuffer(RenderType.entityCutout(resourcelocation));
            }

            int i = LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F);
            BakedModel bakedmodel = this.blockRenderer.getBlockModel(blockstate);
            pPoseStack.pushPose();
            pPoseStack.translate(0.2F, -0.35F, 0.5F);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(-48.0F));
            pPoseStack.scale(-1.0F, -1.0F, 1.0F);
            pPoseStack.translate(-0.5F, -0.5F, -0.5F);
            if (resourcelocation != null) {
               this.modelRendererMushroom.render(pPoseStack, vertexconsumer, pPackedLight, i);
            } else {
               this.renderMushroomBlock(pPoseStack, pBuffer, pPackedLight, flag, blockstate, i, bakedmodel);
            }

            pPoseStack.popPose();
            pPoseStack.pushPose();
            pPoseStack.translate(0.2F, -0.35F, 0.5F);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(42.0F));
            pPoseStack.translate(0.1F, 0.0F, -0.6F);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(-48.0F));
            pPoseStack.scale(-1.0F, -1.0F, 1.0F);
            pPoseStack.translate(-0.5F, -0.5F, -0.5F);
            if (resourcelocation != null) {
               this.modelRendererMushroom.render(pPoseStack, vertexconsumer, pPackedLight, i);
            } else {
               this.renderMushroomBlock(pPoseStack, pBuffer, pPackedLight, flag, blockstate, i, bakedmodel);
            }

            pPoseStack.popPose();
            pPoseStack.pushPose();
            this.getParentModel().getHead().translateAndRotate(pPoseStack);
            pPoseStack.translate(0.0F, -0.7F, -0.2F);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(-78.0F));
            pPoseStack.scale(-1.0F, -1.0F, 1.0F);
            pPoseStack.translate(-0.5F, -0.5F, -0.5F);
            if (resourcelocation != null) {
               this.modelRendererMushroom.render(pPoseStack, vertexconsumer, pPackedLight, i);
            } else {
               this.renderMushroomBlock(pPoseStack, pBuffer, pPackedLight, flag, blockstate, i, bakedmodel);
            }

            pPoseStack.popPose();
         }
      }

   }

   private void renderMushroomBlock(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, boolean pOutlineOnly, BlockState pState, int pPackedOverlay, BakedModel pModel) {
      if (pOutlineOnly) {
         this.blockRenderer.getModelRenderer().renderModel(pPoseStack.last(), pBuffer.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), pState, pModel, 0.0F, 0.0F, 0.0F, pPackedLight, pPackedOverlay);
      } else {
         this.blockRenderer.renderSingleBlock(pState, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
      }

   }

   private ResourceLocation getCustomMushroom(BlockState iblockstate) {
      Block block = iblockstate.getBlock();
      if (block == Blocks.RED_MUSHROOM && hasTextureMushroomRed) {
         return LOCATION_MUSHROOM_RED;
      } else {
         return block == Blocks.BROWN_MUSHROOM && hasTextureMushroomBrown ? LOCATION_MUSHROOM_BROWN : null;
      }
   }

   public static void update() {
      hasTextureMushroomRed = Config.hasResource(LOCATION_MUSHROOM_RED);
      hasTextureMushroomBrown = Config.hasResource(LOCATION_MUSHROOM_BROWN);
   }
}