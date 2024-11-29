package net.optifine.player;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.optifine.Config;
import net.optifine.util.TextureUtils;

public class CapeUtils {
   private static final Pattern PATTERN_USERNAME = Pattern.compile("[a-zA-Z0-9_]+");

   public static void downloadCape(AbstractClientPlayer player) {
      String s = player.getNameClear();
      if (s != null && !s.isEmpty() && !s.contains("\u0000") && PATTERN_USERNAME.matcher(s).matches()) {
         String s1 = "http://s.optifine.net/capes/" + s + ".png";
         ResourceLocation resourcelocation = new ResourceLocation("capeof/" + s);
         TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
         AbstractTexture abstracttexture = texturemanager.getTexture(resourcelocation, (AbstractTexture)null);
         if (abstracttexture != null && abstracttexture instanceof HttpTexture) {
            HttpTexture httptexture = (HttpTexture)abstracttexture;
            if (httptexture.imageFound != null) {
               if (httptexture.imageFound) {
                  player.setLocationOfCape(resourcelocation, player);
                  if (httptexture.getProcessTask() instanceof CapeImageBuffer) {
                     CapeImageBuffer capeimagebuffer1 = (CapeImageBuffer)httptexture.getProcessTask();
                     player.setElytraOfCape(capeimagebuffer1.isElytraOfCape());
                  }
               }

               return;
            }
         }

         CapeImageBuffer capeimagebuffer = new CapeImageBuffer(player, resourcelocation);
         ResourceLocation resourcelocation1 = TextureUtils.LOCATION_TEXTURE_EMPTY;
         HttpTexture httptexture1 = new HttpTexture((File)null, s1, resourcelocation1, false, capeimagebuffer);
         httptexture1.pipeline = true;
         texturemanager.register(resourcelocation, httptexture1);
      }

   }

   public static NativeImage parseCape(NativeImage img) {
      int i = 64;
      int j = 32;
      int k = img.getWidth();

      for(int l = img.getHeight(); i < k || j < l; j *= 2) {
         i *= 2;
      }

      NativeImage nativeimage = new NativeImage(i, j, true);
      nativeimage.copyFrom(img);
      img.close();
      return nativeimage;
   }

   public static boolean isElytraCape(NativeImage imageRaw, NativeImage imageFixed) {
      return imageRaw.getWidth() > imageFixed.getHeight();
   }

   public static void reloadCape(AbstractClientPlayer player) {
      String s = player.getNameClear();
      ResourceLocation resourcelocation = new ResourceLocation("capeof/" + s);
      TextureManager texturemanager = Config.getTextureManager();
      AbstractTexture abstracttexture = texturemanager.getTexture(resourcelocation);
      if (abstracttexture instanceof SimpleTexture simpletexture) {
         simpletexture.releaseId();
         texturemanager.release(resourcelocation);
      }

      player.setLocationOfCape((ResourceLocation)null);
      player.setElytraOfCape(false);
      downloadCape(player);
   }
}