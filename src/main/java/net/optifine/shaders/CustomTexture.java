package net.optifine.shaders;

import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;

public class CustomTexture implements ICustomTexture {
   private int textureUnit = -1;
   private String path = null;
   private AbstractTexture texture = null;

   public CustomTexture(int textureUnit, String path, AbstractTexture texture) {
      this.textureUnit = textureUnit;
      this.path = path;
      this.texture = texture;
   }

   public int getTextureUnit() {
      return this.textureUnit;
   }

   public String getPath() {
      return this.path;
   }

   public AbstractTexture getTexture() {
      return this.texture;
   }

   public int getTextureId() {
      return this.texture.getId();
   }

   public void deleteTexture() {
      TextureUtil.releaseTextureId(this.texture.getId());
   }

   public String toString() {
      return "textureUnit: " + this.textureUnit + ", path: " + this.path + ", glTextureId: " + this.getTextureId();
   }
}