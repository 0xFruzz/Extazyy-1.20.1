package net.optifine.player;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

public class CapeImageBuffer implements Runnable {
   private AbstractClientPlayer player;
   private ResourceLocation resourceLocation;
   private boolean elytraOfCape;

   public CapeImageBuffer(AbstractClientPlayer player, ResourceLocation resourceLocation) {
      this.player = player;
      this.resourceLocation = resourceLocation;
   }

   public void run() {
   }

   public NativeImage parseUserSkin(NativeImage imageRaw) {
      NativeImage nativeimage = CapeUtils.parseCape(imageRaw);
      this.elytraOfCape = CapeUtils.isElytraCape(imageRaw, nativeimage);
      return nativeimage;
   }

   public void skinAvailable() {
      if (this.player != null) {
         this.player.setLocationOfCape(this.resourceLocation);
         this.player.setElytraOfCape(this.elytraOfCape);
      }

      this.cleanup();
   }

   public void cleanup() {
      this.player = null;
   }

   public boolean isElytraOfCape() {
      return this.elytraOfCape;
   }
}