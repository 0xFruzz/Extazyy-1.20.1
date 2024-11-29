package net.optifine.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.optifine.Config;
import net.optifine.shaders.SimpleShaderTexture;

public class GpuMemory {
   private static long bufferAllocated = 0L;
   private static long textureAllocated = 0L;
   private static long textureAllocatedUpdateTime = 0L;

   public static synchronized void bufferAllocated(long size) {
      bufferAllocated += size;
   }

   public static synchronized void bufferFreed(long size) {
      bufferAllocated -= size;
   }

   public static long getBufferAllocated() {
      return bufferAllocated;
   }

   public static long getTextureAllocated() {
      if (System.currentTimeMillis() > textureAllocatedUpdateTime) {
         textureAllocated = calculateTextureAllocated();
         textureAllocatedUpdateTime = System.currentTimeMillis() + 1000L;
      }

      return textureAllocated;
   }

   private static long calculateTextureAllocated() {
      TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
      long i = 0L;

      for(AbstractTexture abstracttexture : texturemanager.getTextures()) {
         long j = getTextureSize(abstracttexture);
         if (Config.isShaders()) {
            j *= 3L;
         }

         i += j;
      }

      return i;
   }

   public static long getTextureSize(AbstractTexture texture) {
      if (texture instanceof DynamicTexture dynamictexture) {
         NativeImage nativeimage = dynamictexture.getPixels();
         if (nativeimage != null) {
            return nativeimage.getSize();
         }
      }

      if (texture instanceof FontTexture fonttexture) {
         return 262144L;
      } else if (texture instanceof SimpleShaderTexture simpleshadertexture) {
         return simpleshadertexture.getSize();
      } else if (texture instanceof SimpleTexture simpletexture) {
         return simpletexture.size;
      } else if (texture instanceof TextureAtlas textureatlas) {
         return (long)(textureatlas.getWidth() * textureatlas.getHeight() * 4);
      } else {
         return 0L;
      }
   }
}