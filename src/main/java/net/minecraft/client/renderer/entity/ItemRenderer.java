package net.minecraft.client.renderer.entity;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.MatrixUtil;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.CustomItems;
import net.optifine.EmissiveTextures;
import net.optifine.reflect.Reflector;
import net.optifine.render.VertexBuilderWrapper;
import net.optifine.shaders.Shaders;
import net.optifine.util.SingleIterable;

public class ItemRenderer implements ResourceManagerReloadListener {
   public static final ResourceLocation ENCHANTED_GLINT_ENTITY = new ResourceLocation("textures/misc/enchanted_glint_entity.png");
   public static final ResourceLocation ENCHANTED_GLINT_ITEM = new ResourceLocation("textures/misc/enchanted_glint_item.png");
   private static final Set<Item> IGNORED = Sets.newHashSet(Items.AIR);
   public static final int GUI_SLOT_CENTER_X = 8;
   public static final int GUI_SLOT_CENTER_Y = 8;
   public static final int ITEM_COUNT_BLIT_OFFSET = 200;
   public static final float COMPASS_FOIL_UI_SCALE = 0.5F;
   public static final float COMPASS_FOIL_FIRST_PERSON_SCALE = 0.75F;
   public static final float COMPASS_FOIL_TEXTURE_SCALE = 0.0078125F;
   private static final ModelResourceLocation TRIDENT_MODEL = ModelResourceLocation.vanilla("trident", "inventory");
   public static final ModelResourceLocation TRIDENT_IN_HAND_MODEL = ModelResourceLocation.vanilla("trident_in_hand", "inventory");
   private static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.vanilla("spyglass", "inventory");
   public static final ModelResourceLocation SPYGLASS_IN_HAND_MODEL = ModelResourceLocation.vanilla("spyglass_in_hand", "inventory");
   private final Minecraft minecraft;
   private final ItemModelShaper itemModelShaper;
   private final TextureManager textureManager;
   private final ItemColors itemColors;
   private final BlockEntityWithoutLevelRenderer blockEntityRenderer;
   public ModelManager modelManager = null;
   private static boolean renderItemGui = false;

   public ItemRenderer(Minecraft pMinecraft, TextureManager pTextureManager, ModelManager pModelManager, ItemColors pItemColors, BlockEntityWithoutLevelRenderer pBlockEntityRenderer) {
      this.minecraft = pMinecraft;
      this.textureManager = pTextureManager;
      this.modelManager = pModelManager;
      if (Reflector.ForgeItemModelShaper_Constructor.exists()) {
         this.itemModelShaper = (ItemModelShaper)Reflector.newInstance(Reflector.ForgeItemModelShaper_Constructor, this.modelManager);
      } else {
         this.itemModelShaper = new ItemModelShaper(pModelManager);
      }

      this.blockEntityRenderer = pBlockEntityRenderer;

      for(Item item : BuiltInRegistries.ITEM) {
         if (!IGNORED.contains(item)) {
            this.itemModelShaper.register(item, new ModelResourceLocation(BuiltInRegistries.ITEM.getKey(item), "inventory"));
         }
      }

      this.itemColors = pItemColors;
   }

   public ItemModelShaper getItemModelShaper() {
      return this.itemModelShaper;
   }

   public void renderModelLists(BakedModel pModel, ItemStack pStack, int pCombinedLight, int pCombinedOverlay, PoseStack pPoseStack, VertexConsumer pBuffer) {
      if (Config.isMultiTexture()) {
         pBuffer.setRenderBlocks(true);
      }

      RandomSource randomsource = RandomSource.create();
      long i = 42L;

      for(Direction direction : Direction.VALUES) {
         randomsource.setSeed(42L);
         this.renderQuadList(pPoseStack, pBuffer, pModel.getQuads((BlockState)null, direction, randomsource), pStack, pCombinedLight, pCombinedOverlay);
      }

      randomsource.setSeed(42L);
      this.renderQuadList(pPoseStack, pBuffer, pModel.getQuads((BlockState)null, (Direction)null, randomsource), pStack, pCombinedLight, pCombinedOverlay);
   }

   public void render(ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel) {
      if (!pItemStack.isEmpty()) {
         pPoseStack.pushPose();
         boolean flag = pDisplayContext == ItemDisplayContext.GUI || pDisplayContext == ItemDisplayContext.GROUND || pDisplayContext == ItemDisplayContext.FIXED;
         if (flag) {
            if (pItemStack.is(Items.TRIDENT)) {
               pModel = this.itemModelShaper.getModelManager().getModel(TRIDENT_MODEL);
            } else if (pItemStack.is(Items.SPYGLASS)) {
               pModel = this.itemModelShaper.getModelManager().getModel(SPYGLASS_MODEL);
            }
         }

         if (Reflector.ForgeHooksClient_handleCameraTransforms.exists()) {
            pModel = (BakedModel)Reflector.ForgeHooksClient_handleCameraTransforms.call(pPoseStack, pModel, pDisplayContext, pLeftHand);
         } else {
            pModel.getTransforms().getTransform(pDisplayContext).apply(pLeftHand, pPoseStack);
         }

         pPoseStack.translate(-0.5F, -0.5F, -0.5F);
         if (!pModel.isCustomRenderer() && (!pItemStack.is(Items.TRIDENT) || flag)) {
            boolean flag1;
            if (pDisplayContext != ItemDisplayContext.GUI && !pDisplayContext.firstPerson() && pItemStack.getItem() instanceof BlockItem) {
               Block block = ((BlockItem)pItemStack.getItem()).getBlock();
               flag1 = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
            } else {
               flag1 = true;
            }

            boolean flag2 = Reflector.ForgeHooksClient.exists();
            Iterable<BakedModel> iterable = (Iterable<BakedModel>)(flag2 ? pModel.getRenderPasses(pItemStack, flag1) : new SingleIterable<>(pModel));
            Iterable<RenderType> iterable1 = (Iterable<RenderType>)(flag2 ? pModel.getRenderTypes(pItemStack, flag1) : new SingleIterable<>(ItemBlockRenderTypes.getRenderType(pItemStack, flag1)));

            for(BakedModel bakedmodel : iterable) {
               pModel = bakedmodel;

               for(RenderType rendertype : iterable1) {
                  VertexConsumer vertexconsumer;
                  if (hasAnimatedTexture(pItemStack) && pItemStack.hasFoil()) {
                     pPoseStack.pushPose();
                     PoseStack.Pose posestack$pose = pPoseStack.last();
                     if (pDisplayContext == ItemDisplayContext.GUI) {
                        MatrixUtil.mulComponentWise(posestack$pose.pose(), 0.5F);
                     } else if (pDisplayContext.firstPerson()) {
                        MatrixUtil.mulComponentWise(posestack$pose.pose(), 0.75F);
                     }

                     if (flag1) {
                        vertexconsumer = getCompassFoilBufferDirect(pBuffer, rendertype, posestack$pose);
                     } else {
                        vertexconsumer = getCompassFoilBuffer(pBuffer, rendertype, posestack$pose);
                     }

                     pPoseStack.popPose();
                  } else if (flag1) {
                     vertexconsumer = getFoilBufferDirect(pBuffer, rendertype, true, pItemStack.hasFoil());
                  } else {
                     vertexconsumer = getFoilBuffer(pBuffer, rendertype, true, pItemStack.hasFoil());
                  }

                  if (Config.isCustomItems()) {
                     pModel = CustomItems.getCustomItemModel(pItemStack, pModel, ItemOverrides.lastModelLocation, false);
                     ItemOverrides.lastModelLocation = null;
                  }

                  if (EmissiveTextures.isActive()) {
                     EmissiveTextures.beginRender();
                  }

                  this.renderModelLists(pModel, pItemStack, pCombinedLight, pCombinedOverlay, pPoseStack, vertexconsumer);
                  if (EmissiveTextures.isActive()) {
                     if (EmissiveTextures.hasEmissive()) {
                        EmissiveTextures.beginRenderEmissive();
                        VertexConsumer vertexconsumer1 = vertexconsumer instanceof VertexBuilderWrapper ? ((VertexBuilderWrapper)vertexconsumer).getVertexBuilder() : vertexconsumer;
                        this.renderModelLists(pModel, pItemStack, LightTexture.MAX_BRIGHTNESS, pCombinedOverlay, pPoseStack, vertexconsumer1);
                        EmissiveTextures.endRenderEmissive();
                     }

                     EmissiveTextures.endRender();
                  }
               }
            }
         } else if (Reflector.MinecraftForge.exists()) {
            IClientItemExtensions.of(pItemStack).getCustomRenderer().renderByItem(pItemStack, pDisplayContext, pPoseStack, pBuffer, pCombinedLight, pCombinedOverlay);
         } else {
            this.blockEntityRenderer.renderByItem(pItemStack, pDisplayContext, pPoseStack, pBuffer, pCombinedLight, pCombinedOverlay);
         }

         pPoseStack.popPose();
      }

   }

   private static boolean hasAnimatedTexture(ItemStack pStack) {
      return pStack.is(ItemTags.COMPASSES) || pStack.is(Items.CLOCK);
   }

   public static VertexConsumer getArmorFoilBuffer(MultiBufferSource pBuffer, RenderType pRenderType, boolean pNoEntity, boolean pWithGlint) {
      if (Shaders.isShadowPass) {
         pWithGlint = false;
      }

      if (EmissiveTextures.isRenderEmissive()) {
         pWithGlint = false;
      }

      return pWithGlint ? VertexMultiConsumer.create(pBuffer.getBuffer(pNoEntity ? RenderType.armorGlint() : RenderType.armorEntityGlint()), pBuffer.getBuffer(pRenderType)) : pBuffer.getBuffer(pRenderType);
   }

   public static VertexConsumer getCompassFoilBuffer(MultiBufferSource pBuffer, RenderType pRenderType, PoseStack.Pose pMatrixEntry) {
      return VertexMultiConsumer.create(new SheetedDecalTextureGenerator(pBuffer.getBuffer(RenderType.glint()), pMatrixEntry.pose(), pMatrixEntry.normal(), 0.0078125F), pBuffer.getBuffer(pRenderType));
   }

   public static VertexConsumer getCompassFoilBufferDirect(MultiBufferSource pBuffer, RenderType pRenderType, PoseStack.Pose pMatrixEntry) {
      return VertexMultiConsumer.create(new SheetedDecalTextureGenerator(pBuffer.getBuffer(RenderType.glintDirect()), pMatrixEntry.pose(), pMatrixEntry.normal(), 0.0078125F), pBuffer.getBuffer(pRenderType));
   }

   public static VertexConsumer getFoilBuffer(MultiBufferSource pBuffer, RenderType pRenderType, boolean pIsItem, boolean pGlint) {
      if (Shaders.isShadowPass) {
         pGlint = false;
      }

      if (EmissiveTextures.isRenderEmissive()) {
         pGlint = false;
      }

      if (!pGlint) {
         return pBuffer.getBuffer(pRenderType);
      } else {
         return Minecraft.useShaderTransparency() && pRenderType == Sheets.translucentItemSheet() ? VertexMultiConsumer.create(pBuffer.getBuffer(RenderType.glintTranslucent()), pBuffer.getBuffer(pRenderType)) : VertexMultiConsumer.create(pBuffer.getBuffer(pIsItem ? RenderType.glint() : RenderType.entityGlint()), pBuffer.getBuffer(pRenderType));
      }
   }

   public static VertexConsumer getFoilBufferDirect(MultiBufferSource pBuffer, RenderType pRenderType, boolean pNoEntity, boolean pWithGlint) {
      if (Shaders.isShadowPass) {
         pWithGlint = false;
      }

      if (EmissiveTextures.isRenderEmissive()) {
         pWithGlint = false;
      }

      return pWithGlint ? VertexMultiConsumer.create(pBuffer.getBuffer(pNoEntity ? RenderType.glintDirect() : RenderType.entityGlintDirect()), pBuffer.getBuffer(pRenderType)) : pBuffer.getBuffer(pRenderType);
   }

   private void renderQuadList(PoseStack pPoseStack, VertexConsumer pBuffer, List<BakedQuad> pQuads, ItemStack pItemStack, int pCombinedLight, int pCombinedOverlay) {
      boolean flag = !pItemStack.isEmpty();
      PoseStack.Pose posestack$pose = pPoseStack.last();
      boolean flag1 = EmissiveTextures.isActive();
      int i = pQuads.size();
      int j = i > 0 && Config.isCustomColors() ? CustomColors.getColorFromItemStack(pItemStack, -1, -1) : -1;

      for(int k = 0; k < i; ++k) {
         BakedQuad bakedquad = pQuads.get(k);
         if (flag1) {
            bakedquad = EmissiveTextures.getEmissiveQuad(bakedquad);
            if (bakedquad == null) {
               continue;
            }
         }

         int l = j;
         if (flag && bakedquad.isTinted()) {
            l = this.itemColors.getColor(pItemStack, bakedquad.getTintIndex());
            if (Config.isCustomColors()) {
               l = CustomColors.getColorFromItemStack(pItemStack, bakedquad.getTintIndex(), l);
            }
         }

         float f = (float)(l >> 16 & 255) / 255.0F;
         float f1 = (float)(l >> 8 & 255) / 255.0F;
         float f2 = (float)(l & 255) / 255.0F;
         if (Reflector.ForgeHooksClient.exists()) {
            pBuffer.putBulkData(posestack$pose, bakedquad, f, f1, f2, 1.0F, pCombinedLight, pCombinedOverlay, true);
         } else {
            pBuffer.putBulkData(posestack$pose, bakedquad, f, f1, f2, pCombinedLight, pCombinedOverlay);
         }
      }

   }

   public BakedModel getModel(ItemStack pStack, @Nullable Level pLevel, @Nullable LivingEntity pEntity, int pSeed) {
      BakedModel bakedmodel;
      if (pStack.is(Items.TRIDENT)) {
         bakedmodel = this.itemModelShaper.getModelManager().getModel(TRIDENT_IN_HAND_MODEL);
      } else if (pStack.is(Items.SPYGLASS)) {
         bakedmodel = this.itemModelShaper.getModelManager().getModel(SPYGLASS_IN_HAND_MODEL);
      } else {
         bakedmodel = this.itemModelShaper.getItemModel(pStack);
      }

      ClientLevel clientlevel = pLevel instanceof ClientLevel ? (ClientLevel)pLevel : null;
      ItemOverrides.lastModelLocation = null;
      BakedModel bakedmodel1 = bakedmodel.getOverrides().resolve(bakedmodel, pStack, clientlevel, pEntity, pSeed);
      if (Config.isCustomItems()) {
         bakedmodel1 = CustomItems.getCustomItemModel(pStack, bakedmodel1, ItemOverrides.lastModelLocation, true);
      }

      return bakedmodel1 == null ? this.itemModelShaper.getModelManager().getMissingModel() : bakedmodel1;
   }

   public void renderStatic(ItemStack pStack, ItemDisplayContext pDisplayContext, int pCombinedLight, int pCombinedOverlay, PoseStack pPoseStack, MultiBufferSource pBuffer, @Nullable Level pLevel, int pSeed) {
      this.renderStatic((LivingEntity)null, pStack, pDisplayContext, false, pPoseStack, pBuffer, pLevel, pCombinedLight, pCombinedOverlay, pSeed);
   }

   public void renderStatic(@Nullable LivingEntity pEntity, ItemStack pItemStack, ItemDisplayContext pDiplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, @Nullable Level pLevel, int pCombinedLight, int pCombinedOverlay, int pSeed) {
      if (!pItemStack.isEmpty()) {
         BakedModel bakedmodel = this.getModel(pItemStack, pLevel, pEntity, pSeed);
         this.render(pItemStack, pDiplayContext, pLeftHand, pPoseStack, pBuffer, pCombinedLight, pCombinedOverlay, bakedmodel);
      }

   }

   public void onResourceManagerReload(ResourceManager pResourceManager) {
      this.itemModelShaper.rebuildCache();
   }

   public static boolean isRenderItemGui() {
      return renderItemGui;
   }

   public static void setRenderItemGui(boolean renderItemGui) {
      ItemRenderer.renderItemGui = renderItemGui;
   }

   public BlockEntityWithoutLevelRenderer getBlockEntityRenderer() {
      return this.blockEntityRenderer;
   }
}