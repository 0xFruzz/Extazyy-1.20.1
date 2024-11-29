package net.optifine.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.optifine.Config;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersTex;
import org.lwjgl.PointerBuffer;

public class MultiTextureRenderer {
   private static PointerBuffer bufferPositions = Config.createDirectPointerBuffer(1024);
   private static IntBuffer bufferCounts = Config.createDirectIntBuffer(1024);
   private static boolean shaders;

   public static void draw(VertexFormat.Mode drawMode, int indexTypeIn, MultiTextureData multiTextureData) {
      shaders = Config.isShaders();
      SpriteRenderData[] aspriterenderdata = multiTextureData.getSpriteRenderDatas();

      for(int i = 0; i < aspriterenderdata.length; ++i) {
         SpriteRenderData spriterenderdata = aspriterenderdata[i];
         draw(drawMode, indexTypeIn, spriterenderdata);
      }

   }

   private static void draw(VertexFormat.Mode drawMode, int indexTypeIn, SpriteRenderData srd) {
      TextureAtlasSprite textureatlassprite = srd.getSprite();
      int[] aint = srd.getPositions();
      int[] aint1 = srd.getCounts();
      textureatlassprite.bindSpriteTexture();
      if (shaders) {
         int i = textureatlassprite.spriteNormal != null ? textureatlassprite.spriteNormal.glSpriteTextureId : 0;
         int j = textureatlassprite.spriteSpecular != null ? textureatlassprite.spriteSpecular.glSpriteTextureId : 0;
         TextureAtlas textureatlas = textureatlassprite.getTextureAtlas();
         ShadersTex.bindNSTextures(i, j, textureatlas.isNormalBlend(), textureatlas.isSpecularBlend(), textureatlas.isMipmaps());
         if (Shaders.uniform_spriteBounds.isDefined()) {
            Shaders.uniform_spriteBounds.setValue(textureatlassprite.getU0(), textureatlassprite.getV0(), textureatlassprite.getU1(), textureatlassprite.getV1());
         }
      }

      if (bufferPositions.capacity() < aint.length) {
         int k = Mth.smallestEncompassingPowerOfTwo(aint.length);
         bufferPositions = Config.createDirectPointerBuffer(k);
         bufferCounts = Config.createDirectIntBuffer(k);
      }

      bufferPositions.clear();
      bufferCounts.clear();
      int l = getIndexSize(indexTypeIn);

      for(int i1 = 0; i1 < aint.length; ++i1) {
         bufferPositions.put((long)(drawMode.indexCount(aint[i1]) * l));
      }

      for(int j1 = 0; j1 < aint1.length; ++j1) {
         bufferCounts.put(drawMode.indexCount(aint1[j1]));
      }

      bufferPositions.flip();
      bufferCounts.flip();
      GlStateManager.glMultiDrawElements(drawMode.asGLMode, bufferCounts, indexTypeIn, bufferPositions);
   }

   private static int getIndexSize(int indexTypeIn) {
      if (indexTypeIn == 5125) {
         return 4;
      } else {
         return indexTypeIn == 5123 ? 2 : 1;
      }
   }
}