package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.reflect.Reflector;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.shaders.Shaders;

public class LiquidBlockRenderer {
   private static final float MAX_FLUID_HEIGHT = 0.8888889F;
   private final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
   private final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
   private TextureAtlasSprite waterOverlay;

   protected void setupSprites() {
      this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
      this.lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
      this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
      this.waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
      this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
   }

   private static boolean isNeighborSameFluid(FluidState pFirstState, FluidState pSecondState) {
      return pSecondState.getType().isSame(pFirstState.getType());
   }

   private static boolean isFaceOccludedByState(BlockGetter pLevel, Direction pFace, float pHeight, BlockPos pPos, BlockState pState) {
      if (pState.canOcclude()) {
         VoxelShape voxelshape = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double)pHeight, 1.0D);
         VoxelShape voxelshape1 = pState.getOcclusionShape(pLevel, pPos);
         return Shapes.blockOccudes(voxelshape, voxelshape1, pFace);
      } else {
         return false;
      }
   }

   private static boolean isFaceOccludedByNeighbor(BlockGetter pLevel, BlockPos pPos, Direction pSide, float pHeight, BlockState pBlockState) {
      return isFaceOccludedByState(pLevel, pSide, pHeight, pPos.relative(pSide), pBlockState);
   }

   private static boolean isFaceOccludedBySelf(BlockGetter pLevel, BlockPos pPos, BlockState pState, Direction pFace) {
      return isFaceOccludedByState(pLevel, pFace.getOpposite(), 1.0F, pPos, pState);
   }

   public static boolean shouldRenderFace(BlockAndTintGetter pLevel, BlockPos pPos, FluidState pFluidState, BlockState pBlockState, Direction pSide, FluidState pNeighborFluid) {
      return !isFaceOccludedBySelf(pLevel, pPos, pBlockState, pSide) && !isNeighborSameFluid(pFluidState, pNeighborFluid);
   }

   public void tesselate(BlockAndTintGetter pLevel, BlockPos pPos, VertexConsumer pVertexConsumer, BlockState pBlockState, FluidState pFluidState) {
      BlockState blockstate = pFluidState.createLegacyBlock();

      try {
         if (Config.isShaders()) {
            SVertexBuilder.pushEntity(blockstate, pVertexConsumer);
         }

         boolean flag = pFluidState.is(FluidTags.LAVA);
         TextureAtlasSprite[] atextureatlassprite = flag ? this.lavaIcons : this.waterIcons;
         if (Reflector.ForgeHooksClient_getFluidSprites.exists()) {
            TextureAtlasSprite[] atextureatlassprite1 = (TextureAtlasSprite[])Reflector.call(Reflector.ForgeHooksClient_getFluidSprites, pLevel, pPos, pFluidState);
            if (atextureatlassprite1 != null) {
               atextureatlassprite = atextureatlassprite1;
            }
         }

         RenderEnv renderenv = pVertexConsumer.getRenderEnv(blockstate, pPos);
         boolean flag1 = !flag && Minecraft.useAmbientOcclusion();
         int i = -1;
         float f = 1.0F;
         if (Reflector.ForgeHooksClient.exists()) {
            i = IClientFluidTypeExtensions.of(pFluidState).getTintColor(pFluidState, pLevel, pPos);
            f = (float)(i >> 24 & 255) / 255.0F;
         }

         BlockState blockstate1 = pLevel.getBlockState(pPos.relative(Direction.DOWN));
         FluidState fluidstate = blockstate1.getFluidState();
         BlockState blockstate2 = pLevel.getBlockState(pPos.relative(Direction.UP));
         FluidState fluidstate1 = blockstate2.getFluidState();
         BlockState blockstate3 = pLevel.getBlockState(pPos.relative(Direction.NORTH));
         FluidState fluidstate2 = blockstate3.getFluidState();
         BlockState blockstate4 = pLevel.getBlockState(pPos.relative(Direction.SOUTH));
         FluidState fluidstate3 = blockstate4.getFluidState();
         BlockState blockstate5 = pLevel.getBlockState(pPos.relative(Direction.WEST));
         FluidState fluidstate4 = blockstate5.getFluidState();
         BlockState blockstate6 = pLevel.getBlockState(pPos.relative(Direction.EAST));
         FluidState fluidstate5 = blockstate6.getFluidState();
         boolean flag2 = !isNeighborSameFluid(pFluidState, fluidstate1);
         boolean flag3 = shouldRenderFace(pLevel, pPos, pFluidState, pBlockState, Direction.DOWN, fluidstate) && !isFaceOccludedByNeighbor(pLevel, pPos, Direction.DOWN, 0.8888889F, blockstate1);
         boolean flag4 = shouldRenderFace(pLevel, pPos, pFluidState, pBlockState, Direction.NORTH, fluidstate2);
         boolean flag5 = shouldRenderFace(pLevel, pPos, pFluidState, pBlockState, Direction.SOUTH, fluidstate3);
         boolean flag6 = shouldRenderFace(pLevel, pPos, pFluidState, pBlockState, Direction.WEST, fluidstate4);
         boolean flag7 = shouldRenderFace(pLevel, pPos, pFluidState, pBlockState, Direction.EAST, fluidstate5);
         if (flag2 || flag3 || flag7 || flag6 || flag4 || flag5) {
            if (i < 0) {
               i = CustomColors.getFluidColor(pLevel, blockstate, pPos, renderenv);
            }

            float f1 = (float)(i >> 16 & 255) / 255.0F;
            float f2 = (float)(i >> 8 & 255) / 255.0F;
            float f3 = (float)(i & 255) / 255.0F;
            float f4 = pLevel.getShade(Direction.DOWN, true);
            float f5 = pLevel.getShade(Direction.UP, true);
            float f6 = pLevel.getShade(Direction.NORTH, true);
            float f7 = pLevel.getShade(Direction.WEST, true);
            Fluid fluid = pFluidState.getType();
            float f8 = this.getHeight(pLevel, fluid, pPos, pBlockState, pFluidState);
            float f9;
            float f10;
            float f11;
            float f12;
            if (f8 >= 1.0F) {
               f9 = 1.0F;
               f10 = 1.0F;
               f11 = 1.0F;
               f12 = 1.0F;
            } else {
               float f13 = this.getHeight(pLevel, fluid, pPos.north(), blockstate3, fluidstate2);
               float f14 = this.getHeight(pLevel, fluid, pPos.south(), blockstate4, fluidstate3);
               float f15 = this.getHeight(pLevel, fluid, pPos.east(), blockstate6, fluidstate5);
               float f16 = this.getHeight(pLevel, fluid, pPos.west(), blockstate5, fluidstate4);
               f9 = this.calculateAverageHeight(pLevel, fluid, f8, f13, f15, pPos.relative(Direction.NORTH).relative(Direction.EAST));
               f10 = this.calculateAverageHeight(pLevel, fluid, f8, f13, f16, pPos.relative(Direction.NORTH).relative(Direction.WEST));
               f11 = this.calculateAverageHeight(pLevel, fluid, f8, f14, f15, pPos.relative(Direction.SOUTH).relative(Direction.EAST));
               f12 = this.calculateAverageHeight(pLevel, fluid, f8, f14, f16, pPos.relative(Direction.SOUTH).relative(Direction.WEST));
            }

            double d1 = (double)(pPos.getX() & 15);
            double d2 = (double)(pPos.getY() & 15);
            double d0 = (double)(pPos.getZ() & 15);
            if (Config.isRenderRegions()) {
               int j = pPos.getX() >> 4 << 4;
               int k = pPos.getY() >> 4 << 4;
               int l = pPos.getZ() >> 4 << 4;
               int i1 = 8;
               int j1 = j >> i1 << i1;
               int k1 = l >> i1 << i1;
               int l1 = j - j1;
               int i2 = l - k1;
               d1 += (double)l1;
               d2 += (double)k;
               d0 += (double)i2;
            }

            if (Config.isShaders() && Shaders.useMidBlockAttrib) {
               pVertexConsumer.setMidBlock((float)(d1 + 0.5D), (float)(d2 + 0.5D), (float)(d0 + 0.5D));
            }

            float f24 = 0.001F;
            float f25 = flag3 ? 0.001F : 0.0F;
            if (flag2 && !isFaceOccludedByNeighbor(pLevel, pPos, Direction.UP, Math.min(Math.min(f10, f12), Math.min(f11, f9)), blockstate2)) {
               f10 -= 0.001F;
               f12 -= 0.001F;
               f11 -= 0.001F;
               f9 -= 0.001F;
               Vec3 vec3 = pFluidState.getFlow(pLevel, pPos);
               float f17;
               float f18;
               float f19;
               float f27;
               float f29;
               float f31;
               float f34;
               float f37;
               if (vec3.x == 0.0D && vec3.z == 0.0D) {
                  TextureAtlasSprite textureatlassprite1 = atextureatlassprite[0];
                  pVertexConsumer.setSprite(textureatlassprite1);
                  f27 = textureatlassprite1.getU(0.0D);
                  f17 = textureatlassprite1.getV(0.0D);
                  f29 = f27;
                  f37 = textureatlassprite1.getV(16.0D);
                  f31 = textureatlassprite1.getU(16.0D);
                  f18 = f37;
                  f34 = f31;
                  f19 = f17;
               } else {
                  TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
                  pVertexConsumer.setSprite(textureatlassprite);
                  float f20 = (float)Mth.atan2(vec3.z, vec3.x) - ((float)Math.PI / 2F);
                  float f21 = Mth.sin(f20) * 0.25F;
                  float f22 = Mth.cos(f20) * 0.25F;
                  float f23 = 8.0F;
                  f27 = textureatlassprite.getU((double)(8.0F + (-f22 - f21) * 16.0F));
                  f17 = textureatlassprite.getV((double)(8.0F + (-f22 + f21) * 16.0F));
                  f29 = textureatlassprite.getU((double)(8.0F + (-f22 + f21) * 16.0F));
                  f37 = textureatlassprite.getV((double)(8.0F + (f22 + f21) * 16.0F));
                  f31 = textureatlassprite.getU((double)(8.0F + (f22 + f21) * 16.0F));
                  f18 = textureatlassprite.getV((double)(8.0F + (f22 - f21) * 16.0F));
                  f34 = textureatlassprite.getU((double)(8.0F + (f22 - f21) * 16.0F));
                  f19 = textureatlassprite.getV((double)(8.0F + (-f22 - f21) * 16.0F));
               }

               float f41 = (f27 + f29 + f31 + f34) / 4.0F;
               float f42 = (f17 + f37 + f18 + f19) / 4.0F;
               float f43 = atextureatlassprite[0].uvShrinkRatio();
               f27 = Mth.lerp(f43, f27, f41);
               f29 = Mth.lerp(f43, f29, f41);
               f31 = Mth.lerp(f43, f31, f41);
               f34 = Mth.lerp(f43, f34, f41);
               f17 = Mth.lerp(f43, f17, f42);
               f37 = Mth.lerp(f43, f37, f42);
               f18 = Mth.lerp(f43, f18, f42);
               f19 = Mth.lerp(f43, f19, f42);
               int l5 = this.getLightColor(pLevel, pPos);
               int j2 = l5;
               int k2 = l5;
               int l2 = l5;
               int i3 = l5;
               if (flag1) {
                  BlockPos blockpos = pPos.north();
                  BlockPos blockpos1 = pPos.south();
                  BlockPos blockpos2 = pPos.east();
                  BlockPos blockpos3 = pPos.west();
                  int j3 = this.getLightColor(pLevel, blockpos);
                  int k3 = this.getLightColor(pLevel, blockpos1);
                  int l3 = this.getLightColor(pLevel, blockpos2);
                  int i4 = this.getLightColor(pLevel, blockpos3);
                  int j4 = this.getLightColor(pLevel, blockpos.west());
                  int k4 = this.getLightColor(pLevel, blockpos1.west());
                  int l4 = this.getLightColor(pLevel, blockpos1.east());
                  int i5 = this.getLightColor(pLevel, blockpos.east());
                  j2 = ModelBlockRenderer.AmbientOcclusionFace.blend(j3, j4, i4, l5);
                  k2 = ModelBlockRenderer.AmbientOcclusionFace.blend(k3, k4, i4, l5);
                  l2 = ModelBlockRenderer.AmbientOcclusionFace.blend(k3, l4, l3, l5);
                  i3 = ModelBlockRenderer.AmbientOcclusionFace.blend(j3, i5, l3, l5);
               }

               float f46 = f5 * f1;
               float f47 = f5 * f2;
               float f49 = f5 * f3;
               this.vertexVanilla(pVertexConsumer, d1 + 0.0D, d2 + (double)f10, d0 + 0.0D, f46, f47, f49, f, f27, f17, j2);
               this.vertexVanilla(pVertexConsumer, d1 + 0.0D, d2 + (double)f12, d0 + 1.0D, f46, f47, f49, f, f29, f37, k2);
               this.vertexVanilla(pVertexConsumer, d1 + 1.0D, d2 + (double)f11, d0 + 1.0D, f46, f47, f49, f, f31, f18, l2);
               this.vertexVanilla(pVertexConsumer, d1 + 1.0D, d2 + (double)f9, d0 + 0.0D, f46, f47, f49, f, f34, f19, i3);
               if (pFluidState.shouldRenderBackwardUpFace(pLevel, pPos.above())) {
                  this.vertexVanilla(pVertexConsumer, d1 + 0.0D, d2 + (double)f10, d0 + 0.0D, f46, f47, f49, f, f27, f17, j2);
                  this.vertexVanilla(pVertexConsumer, d1 + 1.0D, d2 + (double)f9, d0 + 0.0D, f46, f47, f49, f, f34, f19, i3);
                  this.vertexVanilla(pVertexConsumer, d1 + 1.0D, d2 + (double)f11, d0 + 1.0D, f46, f47, f49, f, f31, f18, l2);
                  this.vertexVanilla(pVertexConsumer, d1 + 0.0D, d2 + (double)f12, d0 + 1.0D, f46, f47, f49, f, f29, f37, k2);
               }
            }

            if (flag3) {
               pVertexConsumer.setSprite(atextureatlassprite[0]);
               float f26 = atextureatlassprite[0].getU0();
               float f28 = atextureatlassprite[0].getU1();
               float f30 = atextureatlassprite[0].getV0();
               float f32 = atextureatlassprite[0].getV1();
               int k5 = this.getLightColor(pLevel, pPos.below());
               float f36 = pLevel.getShade(Direction.DOWN, true);
               float f38 = f36 * f1;
               float f39 = f36 * f2;
               float f40 = f36 * f3;
               this.vertexVanilla(pVertexConsumer, d1, d2 + (double)f25, d0 + 1.0D, f38, f39, f40, f, f26, f32, k5);
               this.vertexVanilla(pVertexConsumer, d1, d2 + (double)f25, d0, f38, f39, f40, f, f26, f30, k5);
               this.vertexVanilla(pVertexConsumer, d1 + 1.0D, d2 + (double)f25, d0, f38, f39, f40, f, f28, f30, k5);
               this.vertexVanilla(pVertexConsumer, d1 + 1.0D, d2 + (double)f25, d0 + 1.0D, f38, f39, f40, f, f28, f32, k5);
            }

            int j5 = this.getLightColor(pLevel, pPos);

            for(Direction direction : Direction.Plane.HORIZONTAL) {
               float f33;
               float f35;
               double d3;
               double d4;
               double d5;
               double d6;
               boolean flag8;
               switch (direction) {
                  case NORTH:
                     f33 = f10;
                     f35 = f9;
                     d3 = d1;
                     d5 = d1 + 1.0D;
                     d4 = d0 + (double)0.001F;
                     d6 = d0 + (double)0.001F;
                     flag8 = flag4;
                     break;
                  case SOUTH:
                     f33 = f11;
                     f35 = f12;
                     d3 = d1 + 1.0D;
                     d5 = d1;
                     d4 = d0 + 1.0D - (double)0.001F;
                     d6 = d0 + 1.0D - (double)0.001F;
                     flag8 = flag5;
                     break;
                  case WEST:
                     f33 = f12;
                     f35 = f10;
                     d3 = d1 + (double)0.001F;
                     d5 = d1 + (double)0.001F;
                     d4 = d0 + 1.0D;
                     d6 = d0;
                     flag8 = flag6;
                     break;
                  default:
                     f33 = f9;
                     f35 = f11;
                     d3 = d1 + 1.0D - (double)0.001F;
                     d5 = d1 + 1.0D - (double)0.001F;
                     d4 = d0;
                     d6 = d0 + 1.0D;
                     flag8 = flag7;
               }

               if (flag8 && !isFaceOccludedByNeighbor(pLevel, pPos, direction, Math.max(f33, f35), pLevel.getBlockState(pPos.relative(direction)))) {
                  BlockPos blockpos4 = pPos.relative(direction);
                  TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
                  float f44 = 0.0F;
                  float f45 = 0.0F;
                  boolean flag9 = !flag;
                  if (Reflector.IForgeBlockState_shouldDisplayFluidOverlay.exists()) {
                     flag9 = atextureatlassprite[2] != null;
                  }

                  if (flag9) {
                     BlockState blockstate7 = pLevel.getBlockState(blockpos4);
                     Block block = blockstate7.getBlock();
                     boolean flag10 = false;
                     if (Reflector.IForgeBlockState_shouldDisplayFluidOverlay.exists()) {
                        flag10 = Reflector.callBoolean(blockstate7, Reflector.IForgeBlockState_shouldDisplayFluidOverlay, pLevel, blockpos4, pFluidState);
                     }

                     if (flag10 || block instanceof HalfTransparentBlock || block instanceof LeavesBlock || block == Blocks.BEACON) {
                        textureatlassprite2 = this.waterOverlay;
                     }

                     if (block == Blocks.FARMLAND || block == Blocks.DIRT_PATH) {
                        f44 = 0.9375F;
                        f45 = 0.9375F;
                     }

                     if (block instanceof SlabBlock) {
                        SlabBlock slabblock = (SlabBlock)block;
                        if (blockstate7.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) {
                           f44 = 0.5F;
                           f45 = 0.5F;
                        }
                     }
                  }

                  pVertexConsumer.setSprite(textureatlassprite2);
                  if (!(f33 <= f44) || !(f35 <= f45)) {
                     f44 = Math.min(f44, f33);
                     f45 = Math.min(f45, f35);
                     if (f44 > f24) {
                        f44 -= f24;
                     }

                     if (f45 > f24) {
                        f45 -= f24;
                     }

                     float f48 = textureatlassprite2.getV((double)((1.0F - f44) * 16.0F * 0.5F));
                     float f50 = textureatlassprite2.getV((double)((1.0F - f45) * 16.0F * 0.5F));
                     float f51 = textureatlassprite2.getU(0.0D);
                     float f52 = textureatlassprite2.getU(8.0D);
                     float f53 = textureatlassprite2.getV((double)((1.0F - f33) * 16.0F * 0.5F));
                     float f54 = textureatlassprite2.getV((double)((1.0F - f35) * 16.0F * 0.5F));
                     float f55 = textureatlassprite2.getV(8.0D);
                     float f56 = direction != Direction.NORTH && direction != Direction.SOUTH ? pLevel.getShade(Direction.WEST, true) : pLevel.getShade(Direction.NORTH, true);
                     float f57 = f5 * f56 * f1;
                     float f58 = f5 * f56 * f2;
                     float f59 = f5 * f56 * f3;
                     this.vertexVanilla(pVertexConsumer, d3, d2 + (double)f33, d4, f57, f58, f59, f, f51, f53, j5);
                     this.vertexVanilla(pVertexConsumer, d5, d2 + (double)f35, d6, f57, f58, f59, f, f52, f54, j5);
                     this.vertexVanilla(pVertexConsumer, d5, d2 + (double)f25, d6, f57, f58, f59, f, f52, f50, j5);
                     this.vertexVanilla(pVertexConsumer, d3, d2 + (double)f25, d4, f57, f58, f59, f, f51, f48, j5);
                     if (textureatlassprite2 != this.waterOverlay) {
                        this.vertexVanilla(pVertexConsumer, d3, d2 + (double)f25, d4, f57, f58, f59, f, f51, f48, j5);
                        this.vertexVanilla(pVertexConsumer, d5, d2 + (double)f25, d6, f57, f58, f59, f, f52, f50, j5);
                        this.vertexVanilla(pVertexConsumer, d5, d2 + (double)f35, d6, f57, f58, f59, f, f52, f54, j5);
                        this.vertexVanilla(pVertexConsumer, d3, d2 + (double)f33, d4, f57, f58, f59, f, f51, f53, j5);
                     }
                  }
               }
            }

            pVertexConsumer.setSprite((TextureAtlasSprite)null);
         }
      } finally {
         if (Config.isShaders()) {
            SVertexBuilder.popEntity(pVertexConsumer);
         }

      }

   }

   private float calculateAverageHeight(BlockAndTintGetter pLevel, Fluid pFluid, float pCurrentHeight, float pHeight1, float pHeight2, BlockPos pPos) {
      if (!(pHeight2 >= 1.0F) && !(pHeight1 >= 1.0F)) {
         float[] afloat = new float[2];
         if (pHeight2 > 0.0F || pHeight1 > 0.0F) {
            float f = this.getHeight(pLevel, pFluid, pPos);
            if (f >= 1.0F) {
               return 1.0F;
            }

            this.addWeightedHeight(afloat, f);
         }

         this.addWeightedHeight(afloat, pCurrentHeight);
         this.addWeightedHeight(afloat, pHeight2);
         this.addWeightedHeight(afloat, pHeight1);
         return afloat[0] / afloat[1];
      } else {
         return 1.0F;
      }
   }

   private void addWeightedHeight(float[] pOutput, float pHeight) {
      if (pHeight >= 0.8F) {
         pOutput[0] += pHeight * 10.0F;
         pOutput[1] += 10.0F;
      } else if (pHeight >= 0.0F) {
         pOutput[0] += pHeight;
         pOutput[1] += 1.0F;
      }

   }

   private float getHeight(BlockAndTintGetter pLevel, Fluid pFluid, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return this.getHeight(pLevel, pFluid, pPos, blockstate, blockstate.getFluidState());
   }

   private float getHeight(BlockAndTintGetter pLevel, Fluid pFluid, BlockPos pPos, BlockState pBlockState, FluidState pFluidState) {
      if (pFluid.isSame(pFluidState.getType())) {
         BlockState blockstate = pLevel.getBlockState(pPos.above());
         return pFluid.isSame(blockstate.getFluidState().getType()) ? 1.0F : pFluidState.getOwnHeight();
      } else {
         return !pBlockState.isSolid() ? 0.0F : -1.0F;
      }
   }

   private void vertex(VertexConsumer pConsumer, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pU, float pV, int pPackedLight) {
      pConsumer.vertex(pX, pY, pZ).color(pRed, pGreen, pBlue, 1.0F).uv(pU, pV).uv2(pPackedLight).normal(0.0F, 1.0F, 0.0F).endVertex();
   }

   private void vertexVanilla(VertexConsumer buffer, double x, double y, double z, float red, float green, float blue, float alpha, float u, float v, int combinedLight) {
      buffer.vertex(x, y, z).color(red, green, blue, alpha).uv(u, v).uv2(combinedLight).normal(0.0F, 1.0F, 0.0F).endVertex();
   }

   private int getLightColor(BlockAndTintGetter pLevel, BlockPos pPos) {
      int i = LevelRenderer.getLightColor(pLevel, pPos);
      int j = LevelRenderer.getLightColor(pLevel, pPos.above());
      int k = i & 255;
      int l = j & 255;
      int i1 = i >> 16 & 255;
      int j1 = j >> 16 & 255;
      return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
   }
}
