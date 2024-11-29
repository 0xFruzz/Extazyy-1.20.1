package net.optifine.shaders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class CustomTextureLocation implements ICustomTexture {
   private int textureUnit = -1;
   private ResourceLocation location;
   private int variant = 0;
   private AbstractTexture texture;
   public static final int VARIANT_BASE = 0;
   public static final int VARIANT_NORMAL = 1;
   public static final int VARIANT_SPECULAR = 2;

   public CustomTextureLocation(int textureUnit, ResourceLocation location, int variant) {
      this.textureUnit = textureUnit;
      this.location = location;
      this.variant = variant;
   }

   public AbstractTexture getTexture() {
      if (this.texture == null) {
         TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
         this.texture = texturemanager.getTexture(this.location);
         if (this.texture == null) {
            this.texture = new SimpleTexture(this.location);
            texturemanager.register(this.location, this.texture);
            this.texture = texturemanager.getTexture(this.location);
         }
      }

      return this.texture;
   }

   public void reloadTexture() {
      this.texture = null;
   }

   public int getTextureId() {
      AbstractTexture abstracttexture = this.getTexture();
      if (this.variant != 0 && abstracttexture instanceof AbstractTexture) {
         MultiTexID multitexid = abstracttexture.multiTex;
         if (multitexid != null) {
            if (this.variant == 1) {
               return multitexid.norm;
            }

            if (this.variant == 2) {
               return multitexid.spec;
            }
         }
      }

      return abstracttexture.getId();
   }

   public int getTextureUnit() {
      return this.textureUnit;
   }

   public void deleteTexture() {
   }

   public String toString() {
      return "textureUnit: " + this.textureUnit + ", location: " + this.location + ", glTextureId: " + (this.texture != null ? this.texture.getId() : "");
   }
}