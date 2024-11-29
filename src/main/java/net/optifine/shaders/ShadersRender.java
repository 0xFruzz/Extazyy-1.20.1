package net.optifine.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.optifine.Config;
import net.optifine.Lagometer;
import net.optifine.reflect.Reflector;
import net.optifine.render.GlBlendState;
import net.optifine.render.GlCullState;
import net.optifine.render.ICamera;
import net.optifine.render.RenderTypes;
import net.optifine.util.MathUtils;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class ShadersRender {
   private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
   public static boolean frustumTerrainShadowChanged = false;
   public static boolean frustumEntitiesShadowChanged = false;
   public static int countEntitiesRenderedShadow;
   public static int countTileEntitiesRenderedShadow;
   private static Map<String, List<Entity>> mapEntityLists = new HashMap<>();
   private static final List<LevelRenderer.RenderChunkInfo> nullRenderChunkInfo = new ArrayList<>();
   private static final List<Entity> nullEntity = new ArrayList<>();

   public static void setFrustrumPosition(ICamera frustum, double x, double y, double z) {
      frustum.setCameraPosition(x, y, z);
   }

   public static void beginTerrainSolid() {
      if (Shaders.isRenderingWorld) {
         Shaders.fogEnabled = true;
         Shaders.useProgram(Shaders.ProgramTerrain);
         Shaders.setRenderStage(RenderStage.TERRAIN_SOLID);
      }

   }

   public static void beginTerrainCutoutMipped() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramTerrain);
         Shaders.setRenderStage(RenderStage.TERRAIN_CUTOUT_MIPPED);
      }

   }

   public static void beginTerrainCutout() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramTerrain);
         Shaders.setRenderStage(RenderStage.TERRAIN_CUTOUT);
      }

   }

   public static void endTerrain() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramTexturedLit);
         Shaders.setRenderStage(RenderStage.NONE);
      }

   }

   public static void beginTranslucent() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramWater);
         Shaders.setRenderStage(RenderStage.TERRAIN_TRANSLUCENT);
      }

   }

   public static void endTranslucent() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramTexturedLit);
         Shaders.setRenderStage(RenderStage.NONE);
      }

   }

   public static void beginTripwire() {
      if (Shaders.isRenderingWorld) {
         Shaders.setRenderStage(RenderStage.TRIPWIRE);
      }

   }

   public static void endTripwire() {
      if (Shaders.isRenderingWorld) {
         Shaders.setRenderStage(RenderStage.NONE);
      }

   }

   public static void renderHand0(GameRenderer er, PoseStack matrixStackIn, Camera activeRenderInfo, float partialTicks) {
      if (!Shaders.isShadowPass) {
         boolean flag = Shaders.isItemToRenderMainTranslucent();
         boolean flag1 = Shaders.isItemToRenderOffTranslucent();
         if (!flag || !flag1) {
            Shaders.readCenterDepth();
            Shaders.beginHand(matrixStackIn, false);
            Shaders.setSkipRenderHands(flag, flag1);
           // RenderSystem.setShaderColor(ColorUtil.getRed(ColorUtil.getColorStyle(0)), ColorUtil.getGreen(ColorUtil.getColorStyle(0)), ColorUtil.getBlue(ColorUtil.getColorStyle(0)), 255f);
            er.renderHand(matrixStackIn, activeRenderInfo, partialTicks, true, false, false);
            Shaders.endHand(matrixStackIn);
            Shaders.setHandsRendered(!flag, !flag1);
            Shaders.setSkipRenderHands(false, false);
         }
      }

   }

   public static void renderHand1(GameRenderer er, PoseStack matrixStackIn, Camera activeRenderInfo, float partialTicks) {
      if (!Shaders.isShadowPass && !Shaders.isBothHandsRendered()) {
         Shaders.readCenterDepth();
         GlStateManager._enableBlend();
         Shaders.beginHand(matrixStackIn, true);
         Shaders.setSkipRenderHands(Shaders.isHandRenderedMain(), Shaders.isHandRenderedOff());
         er.renderHand(matrixStackIn, activeRenderInfo, partialTicks, true, false, true);
         Shaders.endHand(matrixStackIn);
         Shaders.setHandsRendered(true, true);
         Shaders.setSkipRenderHands(false, false);
      }

   }

   public static void renderItemFP(ItemInHandRenderer itemRenderer, float partialTicks, PoseStack matrixStackIn, MultiBufferSource.BufferSource bufferIn, LocalPlayer playerEntityIn, int combinedLightIn, boolean renderTranslucent) {
      Config.getEntityRenderDispatcher().setRenderedEntity(playerEntityIn);
      GlStateManager._depthMask(true);
      if (renderTranslucent) {
         GlStateManager._depthFunc(519);
         matrixStackIn.pushPose();
         DrawBuffers drawbuffers = GlState.getDrawBuffers();
         GlState.setDrawBuffers(Shaders.drawBuffersNone);
         Shaders.renderItemKeepDepthMask = true;
         itemRenderer.renderHandsWithItems(partialTicks, matrixStackIn, bufferIn, playerEntityIn, combinedLightIn);
         Shaders.renderItemKeepDepthMask = false;
         GlState.setDrawBuffers(drawbuffers);
         matrixStackIn.popPose();
      }

      GlStateManager._depthFunc(515);
      itemRenderer.renderHandsWithItems(partialTicks, matrixStackIn, bufferIn, playerEntityIn, combinedLightIn);
      Config.getEntityRenderDispatcher().setRenderedEntity((Entity)null);
   }

   public static void renderFPOverlay(GameRenderer er, PoseStack matrixStackIn, Camera activeRenderInfo, float partialTicks) {
      if (!Shaders.isShadowPass) {
         Shaders.beginFPOverlay();
         er.renderHand(matrixStackIn, activeRenderInfo, partialTicks, false, true, false);
         Shaders.endFPOverlay();
      }

   }

   public static void beginBlockDamage() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramDamagedBlock);
         Shaders.setRenderStage(RenderStage.DESTROY);
         if (Shaders.ProgramDamagedBlock.getId() == Shaders.ProgramTerrain.getId()) {
            GlState.setDrawBuffers(Shaders.drawBuffersColorAtt[0]);
            GlStateManager._depthMask(false);
         }
      }

   }

   public static void endBlockDamage() {
      if (Shaders.isRenderingWorld) {
         GlStateManager._depthMask(true);
         Shaders.useProgram(Shaders.ProgramTexturedLit);
         Shaders.setRenderStage(RenderStage.NONE);
      }

   }

   public static void beginOutline() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramBasic);
         Shaders.setRenderStage(RenderStage.OUTLINE);
      }

   }

   public static void endOutline() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramTexturedLit);
         Shaders.setRenderStage(RenderStage.NONE);
      }

   }

   public static void beginDebug() {
      if (Shaders.isRenderingWorld) {
         Shaders.setRenderStage(RenderStage.DEBUG);
      }

   }

   public static void endDebug() {
      if (Shaders.isRenderingWorld) {
         Shaders.setRenderStage(RenderStage.NONE);
      }

   }

   public static void renderShadowMap(GameRenderer entityRenderer, Camera activeRenderInfo, int pass, float partialTicks, long finishTimeNano) {
      if (Shaders.hasShadowMap) {
         Minecraft minecraft = Minecraft.getInstance();
         minecraft.getProfiler().popPush("shadow pass");
         LevelRenderer levelrenderer = minecraft.levelRenderer;
         Shaders.isShadowPass = true;
         Shaders.updateProjectionMatrix();
         Shaders.checkGLError("pre shadow");
         Matrix4f matrix4f = RenderSystem.getProjectionMatrix();
         VertexSorting vertexsorting = RenderSystem.getVertexSorting();
         RenderSystem.getModelViewStack().pushPose();
         minecraft.getProfiler().popPush("shadow clear");
         Shaders.sfb.bindFramebuffer();
         Shaders.checkGLError("shadow bind sfb");
         minecraft.getProfiler().popPush("shadow camera");
         updateActiveRenderInfo(activeRenderInfo, minecraft, partialTicks);
         PoseStack posestack = new PoseStack();
         Shaders.setCameraShadow(posestack, activeRenderInfo, partialTicks);
         Matrix4f matrix4f1 = RenderSystem.getProjectionMatrix();
         Shaders.checkGLError("shadow camera");
         Shaders.dispatchComputes(Shaders.dfb, Shaders.ProgramShadow.getComputePrograms());
         Shaders.useProgram(Shaders.ProgramShadow);
         Shaders.sfb.setDrawBuffers();
         Shaders.checkGLError("shadow drawbuffers");
         GL30.glReadBuffer(0);
         Shaders.checkGLError("shadow readbuffer");
         Shaders.sfb.setDepthTexture();
         Shaders.sfb.setColorTextures(true);
         Shaders.checkFramebufferStatus("shadow fb");
         GlStateManager._clearColor(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.clear(256);

         for(int i = 0; i < Shaders.usedShadowColorBuffers; ++i) {
            if (Shaders.shadowBuffersClear[i]) {
               Vector4f vector4f = Shaders.shadowBuffersClearColor[i];
               if (vector4f != null) {
                  GlStateManager._clearColor(vector4f.x(), vector4f.y(), vector4f.z(), vector4f.w());
               } else {
                  GlStateManager._clearColor(1.0F, 1.0F, 1.0F, 1.0F);
               }

               GlState.setDrawBuffers(Shaders.drawBuffersColorAtt[i]);
               GlStateManager.clear(16384);
            }
         }

         Shaders.sfb.setDrawBuffers();
         Shaders.checkGLError("shadow clear");
         minecraft.getProfiler().popPush("shadow frustum");
         Frustum frustum = makeShadowFrustum(activeRenderInfo, partialTicks);
         minecraft.getProfiler().popPush("shadow culling");
         Vec3 vec3 = activeRenderInfo.getPosition();
         frustum.prepare(vec3.x, vec3.y, vec3.z);
         GlStateManager._enableDepthTest();
         GlStateManager._depthFunc(515);
         GlStateManager._depthMask(true);
         GlStateManager._colorMask(true, true, true, true);
         GlStateManager.lockCull(new GlCullState(false));
         GlStateManager.lockBlend(new GlBlendState(false));
         minecraft.getProfiler().popPush("shadow prepareterrain");
         minecraft.getTextureManager().bindForSetup(TextureAtlas.LOCATION_BLOCKS);
         minecraft.getProfiler().popPush("shadow setupterrain");
         levelrenderer.setShadowRenderInfos(true);
         Lagometer.timerVisibility.start();
         if (!levelrenderer.isDebugFrustum()) {
            applyFrustumShadow(levelrenderer, frustum);
         }

         Lagometer.timerVisibility.end();
         minecraft.getProfiler().popPush("shadow updatechunks");
         minecraft.getProfiler().popPush("shadow terrain");
         double d0 = vec3.x();
         double d1 = vec3.y();
         double d2 = vec3.z();
         Lagometer.timerTerrain.start();
         if (Shaders.isRenderShadowTerrain()) {
            GlStateManager.disableAlphaTest();
            levelrenderer.renderChunkLayer(RenderTypes.SOLID, posestack, d0, d1, d2, matrix4f1);
            Shaders.checkGLError("shadow terrain solid");
            GlStateManager.enableAlphaTest();
            levelrenderer.renderChunkLayer(RenderTypes.CUTOUT_MIPPED, posestack, d0, d1, d2, matrix4f1);
            Shaders.checkGLError("shadow terrain cutoutmipped");
            minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
            levelrenderer.renderChunkLayer(RenderTypes.CUTOUT, posestack, d0, d1, d2, matrix4f1);
            minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
            Shaders.checkGLError("shadow terrain cutout");
         }

         minecraft.getProfiler().popPush("shadow entities");
         countEntitiesRenderedShadow = 0;
         countTileEntitiesRenderedShadow = 0;
         LevelRenderer levelrenderer1 = minecraft.levelRenderer;
         EntityRenderDispatcher entityrenderdispatcher = minecraft.getEntityRenderDispatcher();
         MultiBufferSource.BufferSource multibuffersource$buffersource = levelrenderer1.getRenderTypeTextures().bufferSource();
         boolean flag = Shaders.isShadowPass && !minecraft.player.isSpectator();
         int j = minecraft.level.getMinBuildHeight();
         int k = minecraft.level.getMaxBuildHeight();

         for(Entity entity : Shaders.isRenderShadowEntities() ? Shaders.getCurrentWorld().entitiesForRendering() : nullEntity) {
            if (levelrenderer1.shouldRenderEntity(entity, j, k) && (entityrenderdispatcher.shouldRender(entity, frustum, d0, d1, d2) || entity.hasIndirectPassenger(minecraft.player)) && (entity != activeRenderInfo.getEntity() || flag || activeRenderInfo.isDetached() || activeRenderInfo.getEntity() instanceof LivingEntity && ((LivingEntity)activeRenderInfo.getEntity()).isSleeping()) && (!(entity instanceof LocalPlayer) || activeRenderInfo.getEntity() == entity)) {
               String s = entity.getClass().getName();
               List<Entity> list = mapEntityLists.get(s);
               if (list == null) {
                  list = new ArrayList<>();
                  mapEntityLists.put(s, list);
               }

               list.add(entity);
            }
         }

         for(List<Entity> list2 : mapEntityLists.values()) {
            for(Entity entity1 : list2) {
               ++countEntitiesRenderedShadow;
               Shaders.nextEntity(entity1);
               levelrenderer1.renderEntity(entity1, d0, d1, d2, partialTicks, posestack, multibuffersource$buffersource);
            }

            list2.clear();
         }

         multibuffersource$buffersource.endLastBatch();
         levelrenderer1.checkPoseStack(posestack);
         multibuffersource$buffersource.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
         multibuffersource$buffersource.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
         multibuffersource$buffersource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
         multibuffersource$buffersource.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));
         Shaders.endEntities();
         Shaders.beginBlockEntities();
         SignRenderer.updateTextRenderDistance();
         boolean flag1 = Reflector.IForgeBlockEntity_getRenderBoundingBox.exists();
         Frustum frustum1 = frustum;

         for(LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo : Shaders.isRenderShadowBlockEntities() ? levelrenderer1.getRenderInfosTileEntities() : nullRenderChunkInfo) {
            List<BlockEntity> list1 = levelrenderer$renderchunkinfo.chunk.getCompiledChunk().getRenderableBlockEntities();
            if (!list1.isEmpty()) {
               for(BlockEntity blockentity : list1) {
                  if (flag1) {
                     AABB aabb = (AABB)Reflector.call(blockentity, Reflector.IForgeBlockEntity_getRenderBoundingBox);
                     if (aabb != null && !frustum1.isVisible(aabb)) {
                        continue;
                     }
                  }

                  ++countTileEntitiesRenderedShadow;
                  Shaders.nextBlockEntity(blockentity);
                  BlockPos blockpos = blockentity.getBlockPos();
                  posestack.pushPose();
                  posestack.translate((double)blockpos.getX() - d0, (double)blockpos.getY() - d1, (double)blockpos.getZ() - d2);
                  minecraft.getBlockEntityRenderDispatcher().render(blockentity, partialTicks, posestack, multibuffersource$buffersource);
                  posestack.popPose();
               }
            }
         }

         levelrenderer1.checkPoseStack(posestack);
         multibuffersource$buffersource.endBatch(RenderType.solid());
         multibuffersource$buffersource.endBatch(Sheets.solidBlockSheet());
         multibuffersource$buffersource.endBatch(Sheets.cutoutBlockSheet());
         multibuffersource$buffersource.endBatch(Sheets.bedSheet());
         multibuffersource$buffersource.endBatch(Sheets.shulkerBoxSheet());
         multibuffersource$buffersource.endBatch(Sheets.signSheet());
         multibuffersource$buffersource.endBatch(Sheets.chestSheet());
         multibuffersource$buffersource.endBatch();
         Shaders.endBlockEntities();
         Lagometer.timerTerrain.end();
         Shaders.checkGLError("shadow entities");
         GlStateManager._depthMask(true);
         GlStateManager._disableBlend();
         GlStateManager.unlockCull();
         GlStateManager._enableCull();
         GlStateManager._blendFuncSeparate(770, 771, 1, 0);
         GlStateManager.alphaFunc(516, 0.1F);
         if (Shaders.usedShadowDepthBuffers >= 2) {
            GlStateManager._activeTexture(33989);
            Shaders.checkGLError("pre copy shadow depth");
            GL11.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, Shaders.shadowMapWidth, Shaders.shadowMapHeight);
            Shaders.checkGLError("copy shadow depth");
            GlStateManager._activeTexture(33984);
         }

         GlStateManager._disableBlend();
         GlStateManager._depthMask(true);
         minecraft.getTextureManager().bindForSetup(TextureAtlas.LOCATION_BLOCKS);
         Shaders.checkGLError("shadow pre-translucent");
         Shaders.sfb.setDrawBuffers();
         Shaders.checkGLError("shadow drawbuffers pre-translucent");
         Shaders.checkFramebufferStatus("shadow pre-translucent");
         if (Shaders.isRenderShadowTranslucent()) {
            Lagometer.timerTerrain.start();
            minecraft.getProfiler().popPush("shadow translucent");
            levelrenderer.renderChunkLayer(RenderTypes.TRANSLUCENT, posestack, d0, d1, d2, matrix4f1);
            Shaders.checkGLError("shadow translucent");
            Lagometer.timerTerrain.end();
         }

         GlStateManager.unlockBlend();
         GlStateManager._depthMask(true);
         GlStateManager._enableCull();
         GlStateManager._disableBlend();
         GL30.glFlush();
         Shaders.checkGLError("shadow flush");
         Shaders.isShadowPass = false;
         levelrenderer.setShadowRenderInfos(false);
         minecraft.getProfiler().popPush("shadow postprocess");
         if (Shaders.hasGlGenMipmap) {
            Shaders.sfb.generateDepthMipmaps(Shaders.shadowMipmapEnabled);
            Shaders.sfb.generateColorMipmaps(true, Shaders.shadowColorMipmapEnabled);
         }

         Shaders.checkGLError("shadow postprocess");
         if (Shaders.hasShadowcompPrograms) {
            Shaders.renderShadowComposites();
         }

         Shaders.dfb.bindFramebuffer();
         GlStateManager._viewport(0, 0, Shaders.renderWidth, Shaders.renderHeight);
         GlState.setDrawBuffers((DrawBuffers)null);
         minecraft.getTextureManager().bindForSetup(TextureAtlas.LOCATION_BLOCKS);
         Shaders.useProgram(Shaders.ProgramTerrain);
         RenderSystem.getModelViewStack().popPose();
         RenderSystem.applyModelViewMatrix();
         RenderSystem.setProjectionMatrix(matrix4f, vertexsorting);
         Shaders.checkGLError("shadow end");
      }

   }

   public static void applyFrustumShadow(LevelRenderer renderGlobal, Frustum frustum) {
      Minecraft minecraft = Config.getMinecraft();
      minecraft.getProfiler().push("apply_shadow_frustum");
      int i = (int)Shaders.getShadowRenderDistance();
      int j = (int)Config.getGameRenderer().getRenderDistance();
      boolean flag = i > 0 && i < j;
      int k = flag ? i : -1;
      if (frustumTerrainShadowChanged || renderGlobal.needsFrustumUpdate()) {
         renderGlobal.applyFrustum(frustum, false, k);
         frustumTerrainShadowChanged = false;
      }

      if (frustumEntitiesShadowChanged || minecraft.level.getSectionStorage().isUpdated()) {
         renderGlobal.applyFrustumEntities(frustum, k);
         frustumEntitiesShadowChanged = false;
      }

      minecraft.getProfiler().pop();
   }

   public static Frustum makeShadowFrustum(Camera camera, float partialTicks) {
      if (!Shaders.isShadowCulling()) {
         return new ClippingHelperDummy();
      } else {
         Minecraft minecraft = Config.getMinecraft();
         GameRenderer gamerenderer = Config.getGameRenderer();
         PoseStack posestack = new PoseStack();
         if (Reflector.ForgeHooksClient_onCameraSetup.exists()) {
            Object object = Reflector.ForgeHooksClient_onCameraSetup.call(gamerenderer, camera, partialTicks);
            float f = Reflector.callFloat(object, Reflector.ViewportEvent_ComputeCameraAngles_getYaw);
            float f1 = Reflector.callFloat(object, Reflector.ViewportEvent_ComputeCameraAngles_getPitch);
            float f2 = Reflector.callFloat(object, Reflector.ViewportEvent_ComputeCameraAngles_getRoll);
            camera.setAnglesInternal(f, f1);
            posestack.mulPose(Axis.ZP.rotationDegrees(f2));
         }

         posestack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
         posestack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
         double d3 = gamerenderer.getFov(camera, partialTicks, true);
         double d4 = Math.max(d3, (double)minecraft.options.fov().get().intValue());
         Matrix4f matrix4f = gamerenderer.getProjectionMatrix(d4);
         Matrix4f matrix4f1 = posestack.last().pose();
         Vec3 vec3 = camera.getPosition();
         double d0 = vec3.x();
         double d1 = vec3.y();
         double d2 = vec3.z();
         Frustum frustum = new ShadowFrustum(matrix4f1, matrix4f);
         frustum.prepare(d0, d1, d2);
         return frustum;
      }
   }

   public static void updateActiveRenderInfo(Camera activeRenderInfo, Minecraft mc, float partialTicks) {
      activeRenderInfo.setup(mc.level, (Entity)(mc.getCameraEntity() == null ? mc.player : mc.getCameraEntity()), !mc.options.getCameraType().isFirstPerson(), mc.options.getCameraType().isMirrored(), partialTicks);
   }

   public static void preRenderChunkLayer(RenderType blockLayerIn) {
      if (blockLayerIn == RenderTypes.SOLID) {
         beginTerrainSolid();
      }

      if (blockLayerIn == RenderTypes.CUTOUT_MIPPED) {
         beginTerrainCutoutMipped();
      }

      if (blockLayerIn == RenderTypes.CUTOUT) {
         beginTerrainCutout();
      }

      if (blockLayerIn == RenderTypes.TRANSLUCENT) {
         beginTranslucent();
      }

      if (blockLayerIn == RenderType.tripwire()) {
         beginTripwire();
      }

      if (Shaders.isRenderBackFace(blockLayerIn)) {
         GlStateManager._disableCull();
      }

   }

   public static void postRenderChunkLayer(RenderType blockLayerIn) {
      if (Shaders.isRenderBackFace(blockLayerIn)) {
         GlStateManager._enableCull();
      }

   }

   public static void preRender(RenderType renderType, BufferBuilder buffer) {
      if (Shaders.isRenderingWorld) {
         if (!Shaders.isShadowPass) {
            if (renderType.isGlint()) {
               renderEnchantedGlintBegin();
            } else if (renderType.getName().equals("eyes")) {
               Shaders.beginSpiderEyes();
            } else if (renderType.getName().equals("crumbling")) {
               beginBlockDamage();
            } else if (renderType != RenderType.LINES && renderType != RenderType.LINE_STRIP) {
               if (renderType == RenderType.waterMask()) {
                  Shaders.beginWaterMask();
               } else if (renderType.getName().equals("beacon_beam")) {
                  Shaders.beginBeacon();
               }
            } else {
               Shaders.beginLines();
            }

         }
      }
   }

   public static void postRender(RenderType renderType, BufferBuilder buffer) {
      if (Shaders.isRenderingWorld) {
         if (!Shaders.isShadowPass) {
            if (renderType.isGlint()) {
               renderEnchantedGlintEnd();
            } else if (renderType.getName().equals("eyes")) {
               Shaders.endSpiderEyes();
            } else if (renderType.getName().equals("crumbling")) {
               endBlockDamage();
            } else if (renderType != RenderType.LINES && renderType != RenderType.LINE_STRIP) {
               if (renderType == RenderType.waterMask()) {
                  Shaders.endWaterMask();
               } else if (renderType.getName().equals("beacon_beam")) {
                  Shaders.endBeacon();
               }
            } else {
               Shaders.endLines();
            }

         }
      }
   }

   public static void enableArrayPointerVbo() {
      GL20.glEnableVertexAttribArray(Shaders.midBlockAttrib);
      GL20.glEnableVertexAttribArray(Shaders.midTexCoordAttrib);
      GL20.glEnableVertexAttribArray(Shaders.tangentAttrib);
      GL20.glEnableVertexAttribArray(Shaders.entityAttrib);
   }

   public static void setupArrayPointersVbo() {
      int i = 18;
      enableArrayPointerVbo();
      GL20.glVertexAttribPointer(Shaders.midBlockAttrib, 3, 5120, false, 72, 32L);
      GL20.glVertexAttribPointer(Shaders.midTexCoordAttrib, 2, 5126, false, 72, 36L);
      GL20.glVertexAttribPointer(Shaders.tangentAttrib, 4, 5122, false, 72, 44L);
      GL20.glVertexAttribPointer(Shaders.entityAttrib, 3, 5122, false, 72, 52L);
   }

   public static void beaconBeamBegin() {
      Shaders.useProgram(Shaders.ProgramBeaconBeam);
   }

   public static void beaconBeamStartQuad1() {
   }

   public static void beaconBeamStartQuad2() {
   }

   public static void beaconBeamDraw1() {
   }

   public static void beaconBeamDraw2() {
      GlStateManager._disableBlend();
   }

   public static void renderEnchantedGlintBegin() {
      Shaders.useProgram(Shaders.ProgramArmorGlint);
   }

   public static void renderEnchantedGlintEnd() {
      if (Shaders.isRenderingWorld) {
         if (Shaders.isRenderingFirstPersonHand() && Shaders.isRenderBothHands()) {
            Shaders.useProgram(Shaders.ProgramHand);
         } else {
            Shaders.useProgram(Shaders.ProgramEntities);
         }
      } else {
         Shaders.useProgram(Shaders.ProgramNone);
      }

   }

   public static boolean renderEndPortal(TheEndPortalBlockEntity te, float partialTicks, float offset, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
      if (!Shaders.isShadowPass && Shaders.activeProgram.getId() == 0) {
         return false;
      } else {
         PoseStack.Pose posestack$pose = matrixStackIn.last();
         Matrix4f matrix4f = posestack$pose.pose();
         Matrix3f matrix3f = posestack$pose.normal();
         VertexConsumer vertexconsumer = bufferIn.getBuffer(RenderType.entitySolid(END_PORTAL_TEXTURE));
         float f = 0.5F;
         float f1 = f * 0.15F;
         float f2 = f * 0.3F;
         float f3 = f * 0.4F;
         float f4 = 0.0F;
         float f5 = 0.2F;
         float f6 = (float)(System.currentTimeMillis() % 100000L) / 100000.0F;
         float f7 = 0.0F;
         float f8 = 0.0F;
         float f9 = 0.0F;
         if (te.shouldRenderFace(Direction.SOUTH)) {
            Vec3i vec3i = Direction.SOUTH.getNormal();
            float f10 = (float)vec3i.getX();
            float f11 = (float)vec3i.getY();
            float f12 = (float)vec3i.getZ();
            float f13 = MathUtils.getTransformX(matrix3f, f10, f11, f12);
            float f14 = MathUtils.getTransformY(matrix3f, f10, f11, f12);
            float f15 = MathUtils.getTransformZ(matrix3f, f10, f11, f12);
            vertexconsumer.vertex(matrix4f, f7, f8, f9 + 1.0F).color(f1, f2, f3, 1.0F).uv(f4 + f6, f4 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f13, f14, f15).endVertex();
            vertexconsumer.vertex(matrix4f, f7 + 1.0F, f8, f9 + 1.0F).color(f1, f2, f3, 1.0F).uv(f4 + f6, f5 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f13, f14, f15).endVertex();
            vertexconsumer.vertex(matrix4f, f7 + 1.0F, f8 + 1.0F, f9 + 1.0F).color(f1, f2, f3, 1.0F).uv(f5 + f6, f5 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f13, f14, f15).endVertex();
            vertexconsumer.vertex(matrix4f, f7, f8 + 1.0F, f9 + 1.0F).color(f1, f2, f3, 1.0F).uv(f5 + f6, f4 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f13, f14, f15).endVertex();
         }

         if (te.shouldRenderFace(Direction.NORTH)) {
            Vec3i vec3i1 = Direction.NORTH.getNormal();
            float f16 = (float)vec3i1.getX();
            float f21 = (float)vec3i1.getY();
            float f26 = (float)vec3i1.getZ();
            float f31 = MathUtils.getTransformX(matrix3f, f16, f21, f26);
            float f36 = MathUtils.getTransformY(matrix3f, f16, f21, f26);
            float f41 = MathUtils.getTransformZ(matrix3f, f16, f21, f26);
            vertexconsumer.vertex(matrix4f, f7, f8 + 1.0F, f9).color(f1, f2, f3, 1.0F).uv(f5 + f6, f5 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f31, f36, f41).endVertex();
            vertexconsumer.vertex(matrix4f, f7 + 1.0F, f8 + 1.0F, f9).color(f1, f2, f3, 1.0F).uv(f5 + f6, f4 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f31, f36, f41).endVertex();
            vertexconsumer.vertex(matrix4f, f7 + 1.0F, f8, f9).color(f1, f2, f3, 1.0F).uv(f4 + f6, f4 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f31, f36, f41).endVertex();
            vertexconsumer.vertex(matrix4f, f7, f8, f9).color(f1, f2, f3, 1.0F).uv(f4 + f6, f5 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f31, f36, f41).endVertex();
         }

         if (te.shouldRenderFace(Direction.EAST)) {
            Vec3i vec3i2 = Direction.EAST.getNormal();
            float f17 = (float)vec3i2.getX();
            float f22 = (float)vec3i2.getY();
            float f27 = (float)vec3i2.getZ();
            float f32 = MathUtils.getTransformX(matrix3f, f17, f22, f27);
            float f37 = MathUtils.getTransformY(matrix3f, f17, f22, f27);
            float f42 = MathUtils.getTransformZ(matrix3f, f17, f22, f27);
            vertexconsumer.vertex(matrix4f, f7 + 1.0F, f8 + 1.0F, f9).color(f1, f2, f3, 1.0F).uv(f5 + f6, f5 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f32, f37, f42).endVertex();
            vertexconsumer.vertex(matrix4f, f7 + 1.0F, f8 + 1.0F, f9 + 1.0F).color(f1, f2, f3, 1.0F).uv(f5 + f6, f4 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f32, f37, f42).endVertex();
            vertexconsumer.vertex(matrix4f, f7 + 1.0F, f8, f9 + 1.0F).color(f1, f2, f3, 1.0F).uv(f4 + f6, f4 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f32, f37, f42).endVertex();
            vertexconsumer.vertex(matrix4f, f7 + 1.0F, f8, f9).color(f1, f2, f3, 1.0F).uv(f4 + f6, f5 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f32, f37, f42).endVertex();
         }

         if (te.shouldRenderFace(Direction.WEST)) {
            Vec3i vec3i3 = Direction.WEST.getNormal();
            float f18 = (float)vec3i3.getX();
            float f23 = (float)vec3i3.getY();
            float f28 = (float)vec3i3.getZ();
            float f33 = MathUtils.getTransformX(matrix3f, f18, f23, f28);
            float f38 = MathUtils.getTransformY(matrix3f, f18, f23, f28);
            float f43 = MathUtils.getTransformZ(matrix3f, f18, f23, f28);
            vertexconsumer.vertex(matrix4f, f7, f8, f9).color(f1, f2, f3, 1.0F).uv(f4 + f6, f4 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f33, f38, f43).endVertex();
            vertexconsumer.vertex(matrix4f, f7, f8, f9 + 1.0F).color(f1, f2, f3, 1.0F).uv(f4 + f6, f5 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f33, f38, f43).endVertex();
            vertexconsumer.vertex(matrix4f, f7, f8 + 1.0F, f9 + 1.0F).color(f1, f2, f3, 1.0F).uv(f5 + f6, f5 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f33, f38, f43).endVertex();
            vertexconsumer.vertex(matrix4f, f7, f8 + 1.0F, f9).color(f1, f2, f3, 1.0F).uv(f5 + f6, f4 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f33, f38, f43).endVertex();
         }

         if (te.shouldRenderFace(Direction.DOWN)) {
            Vec3i vec3i4 = Direction.DOWN.getNormal();
            float f19 = (float)vec3i4.getX();
            float f24 = (float)vec3i4.getY();
            float f29 = (float)vec3i4.getZ();
            float f34 = MathUtils.getTransformX(matrix3f, f19, f24, f29);
            float f39 = MathUtils.getTransformY(matrix3f, f19, f24, f29);
            float f44 = MathUtils.getTransformZ(matrix3f, f19, f24, f29);
            vertexconsumer.vertex(matrix4f, f7, f8, f9).color(f1, f2, f3, 1.0F).uv(f4 + f6, f4 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f34, f39, f44).endVertex();
            vertexconsumer.vertex(matrix4f, f7 + 1.0F, f8, f9).color(f1, f2, f3, 1.0F).uv(f4 + f6, f5 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f34, f39, f44).endVertex();
            vertexconsumer.vertex(matrix4f, f7 + 1.0F, f8, f9 + 1.0F).color(f1, f2, f3, 1.0F).uv(f5 + f6, f5 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f34, f39, f44).endVertex();
            vertexconsumer.vertex(matrix4f, f7, f8, f9 + 1.0F).color(f1, f2, f3, 1.0F).uv(f5 + f6, f4 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f34, f39, f44).endVertex();
         }

         if (te.shouldRenderFace(Direction.UP)) {
            Vec3i vec3i5 = Direction.UP.getNormal();
            float f20 = (float)vec3i5.getX();
            float f25 = (float)vec3i5.getY();
            float f30 = (float)vec3i5.getZ();
            float f35 = MathUtils.getTransformX(matrix3f, f20, f25, f30);
            float f40 = MathUtils.getTransformY(matrix3f, f20, f25, f30);
            float f45 = MathUtils.getTransformZ(matrix3f, f20, f25, f30);
            vertexconsumer.vertex(matrix4f, f7, f8 + offset, f9 + 1.0F).color(f1, f2, f3, 1.0F).uv(f4 + f6, f4 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f35, f40, f45).endVertex();
            vertexconsumer.vertex(matrix4f, f7 + 1.0F, f8 + offset, f9 + 1.0F).color(f1, f2, f3, 1.0F).uv(f4 + f6, f5 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f35, f40, f45).endVertex();
            vertexconsumer.vertex(matrix4f, f7 + 1.0F, f8 + offset, f9).color(f1, f2, f3, 1.0F).uv(f5 + f6, f5 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f35, f40, f45).endVertex();
            vertexconsumer.vertex(matrix4f, f7, f8 + offset, f9).color(f1, f2, f3, 1.0F).uv(f5 + f6, f4 + f6).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(f35, f40, f45).endVertex();
         }

         return true;
      }
   }
}