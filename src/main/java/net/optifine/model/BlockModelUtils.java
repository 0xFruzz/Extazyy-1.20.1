package net.optifine.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.optifine.Config;
import net.optifine.util.RandomUtils;
import org.joml.Vector3f;

public class BlockModelUtils {
   private static final float VERTEX_COORD_ACCURACY = 1.0E-6F;
   private static final RandomSource RANDOM = RandomUtils.makeThreadSafeRandomSource(0);

   public static BakedModel makeModelCube(String spriteName, int tintIndex) {
      TextureAtlasSprite textureatlassprite = Config.getTextureMap().getUploadedSprite(spriteName);
      return makeModelCube(textureatlassprite, tintIndex);
   }

   public static BakedModel makeModelCube(TextureAtlasSprite sprite, int tintIndex) {
      List list = new ArrayList();
      Direction[] adirection = Direction.VALUES;
      Map<Direction, List<BakedQuad>> map = new HashMap<>();

      for(int i = 0; i < adirection.length; ++i) {
         Direction direction = adirection[i];
         List list1 = new ArrayList();
         list1.add(makeBakedQuad(direction, sprite, tintIndex));
         map.put(direction, list1);
      }

      ItemOverrides itemoverrides = ItemOverrides.EMPTY;
      BakedModel bakedmodel = new SimpleBakedModel(list, map, true, true, true, sprite, ItemTransforms.NO_TRANSFORMS, itemoverrides);
      return bakedmodel;
   }

   public static BakedModel joinModelsCube(BakedModel modelBase, BakedModel modelAdd) {
      List<BakedQuad> list = new ArrayList<>();
      list.addAll(modelBase.getQuads((BlockState)null, (Direction)null, RANDOM));
      list.addAll(modelAdd.getQuads((BlockState)null, (Direction)null, RANDOM));
      Direction[] adirection = Direction.VALUES;
      Map<Direction, List<BakedQuad>> map = new HashMap<>();

      for(int i = 0; i < adirection.length; ++i) {
         Direction direction = adirection[i];
         List list1 = new ArrayList();
         list1.addAll(modelBase.getQuads((BlockState)null, direction, RANDOM));
         list1.addAll(modelAdd.getQuads((BlockState)null, direction, RANDOM));
         map.put(direction, list1);
      }

      boolean flag = modelBase.useAmbientOcclusion();
      boolean flag1 = modelBase.isCustomRenderer();
      TextureAtlasSprite textureatlassprite = modelBase.getParticleIcon();
      ItemTransforms itemtransforms = modelBase.getTransforms();
      ItemOverrides itemoverrides = modelBase.getOverrides();
      BakedModel bakedmodel = new SimpleBakedModel(list, map, flag, flag1, true, textureatlassprite, itemtransforms, itemoverrides);
      return bakedmodel;
   }

   public static BakedQuad makeBakedQuad(Direction facing, TextureAtlasSprite sprite, int tintIndex) {
      Vector3f vector3f = new Vector3f(0.0F, 0.0F, 0.0F);
      Vector3f vector3f1 = new Vector3f(16.0F, 16.0F, 16.0F);
      BlockFaceUV blockfaceuv = new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0);
      BlockElementFace blockelementface = new BlockElementFace(facing, tintIndex, "#" + facing.getSerializedName(), blockfaceuv);
      BlockModelRotation blockmodelrotation = BlockModelRotation.X0_Y0;
      BlockElementRotation blockelementrotation = null;
      boolean flag = true;
      ResourceLocation resourcelocation = sprite.getName();
      FaceBakery facebakery = new FaceBakery();
      return facebakery.bakeQuad(vector3f, vector3f1, blockelementface, sprite, facing, blockmodelrotation, blockelementrotation, flag, resourcelocation);
   }

   public static BakedModel makeModel(String modelName, String spriteOldName, String spriteNewName) {
      TextureAtlas textureatlas = Config.getTextureMap();
      TextureAtlasSprite textureatlassprite = textureatlas.getUploadedSprite(spriteOldName);
      TextureAtlasSprite textureatlassprite1 = textureatlas.getUploadedSprite(spriteNewName);
      return makeModel(modelName, textureatlassprite, textureatlassprite1);
   }

   public static BakedModel makeModel(String modelName, TextureAtlasSprite spriteOld, TextureAtlasSprite spriteNew) {
      if (spriteOld != null && spriteNew != null) {
         ModelManager modelmanager = Config.getModelManager();
         if (modelmanager == null) {
            return null;
         } else {
            ModelResourceLocation modelresourcelocation = ModelResourceLocation.vanilla(modelName, "");
            BakedModel bakedmodel = modelmanager.getModel(modelresourcelocation);
            if (bakedmodel != null && bakedmodel != modelmanager.getMissingModel()) {
               BakedModel bakedmodel1 = ModelUtils.duplicateModel(bakedmodel);
               Direction[] adirection = Direction.VALUES;

               for(int i = 0; i < adirection.length; ++i) {
                  Direction direction = adirection[i];
                  List<BakedQuad> list = bakedmodel1.getQuads((BlockState)null, direction, RANDOM);
                  replaceTexture(list, spriteOld, spriteNew);
               }

               List<BakedQuad> list1 = bakedmodel1.getQuads((BlockState)null, (Direction)null, RANDOM);
               replaceTexture(list1, spriteOld, spriteNew);
               return bakedmodel1;
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static void replaceTexture(List<BakedQuad> quads, TextureAtlasSprite spriteOld, TextureAtlasSprite spriteNew) {
      List<BakedQuad> list = new ArrayList<>();

      for(BakedQuad bakedquad : quads) {
         if (bakedquad.getSprite() == spriteOld) {
            bakedquad = new BakedQuadRetextured(bakedquad, spriteNew);
         }

         list.add(bakedquad);
      }

      quads.clear();
      quads.addAll(list);
   }

   public static void snapVertexPosition(Vector3f pos) {
      pos.set(snapVertexCoord(pos.x()), snapVertexCoord(pos.y()), snapVertexCoord(pos.z()));
   }

   private static float snapVertexCoord(float x) {
      if (x > -1.0E-6F && x < 1.0E-6F) {
         return 0.0F;
      } else {
         return x > 0.999999F && x < 1.000001F ? 1.0F : x;
      }
   }

   public static AABB getOffsetBoundingBox(AABB aabb, BlockBehaviour.OffsetType offsetType, BlockPos pos) {
      int i = pos.getX();
      int j = pos.getZ();
      long k = (long)(i * 3129871) ^ (long)j * 116129781L;
      k = k * k * 42317861L + k * 11L;
      double d0 = ((double)((float)(k >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D;
      double d1 = ((double)((float)(k >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D;
      double d2 = 0.0D;
      if (offsetType == BlockBehaviour.OffsetType.XYZ) {
         d2 = ((double)((float)(k >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D;
      }

      return aabb.move(d0, d2, d1);
   }
}