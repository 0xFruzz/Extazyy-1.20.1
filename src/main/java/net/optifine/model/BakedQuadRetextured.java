package net.optifine.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Arrays;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class BakedQuadRetextured extends BakedQuad {
   public BakedQuadRetextured(BakedQuad quad, TextureAtlasSprite spriteIn) {
      super(remapVertexData(quad.getVertices(), quad.getSprite(), spriteIn), quad.getTintIndex(), FaceBakery.calculateFacing(quad.getVertices()), spriteIn, quad.isShade());
   }

   private static int[] remapVertexData(int[] vertexData, TextureAtlasSprite sprite, TextureAtlasSprite spriteNew) {
      int[] aint = Arrays.copyOf(vertexData, vertexData.length);

      for(int i = 0; i < 4; ++i) {
         VertexFormat vertexformat = DefaultVertexFormat.BLOCK;
         int j = vertexformat.getIntegerSize() * i;
         int k = vertexformat.getOffset(2) / 4;
         aint[j + k] = Float.floatToRawIntBits(spriteNew.getU((double)sprite.getUnInterpolatedU(Float.intBitsToFloat(vertexData[j + k]))));
         aint[j + k + 1] = Float.floatToRawIntBits(spriteNew.getV((double)sprite.getUnInterpolatedV(Float.intBitsToFloat(vertexData[j + k + 1]))));
      }

      return aint;
   }
}