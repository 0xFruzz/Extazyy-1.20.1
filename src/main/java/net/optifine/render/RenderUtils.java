package net.optifine.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBuffers;

public class RenderUtils {
   private static boolean flushRenderBuffers = true;
   private static Minecraft mc = Minecraft.getInstance();

   public static boolean setFlushRenderBuffers(boolean flushRenderBuffers) {
      boolean flag = RenderUtils.flushRenderBuffers;
      RenderUtils.flushRenderBuffers = flushRenderBuffers;
      return flag;
   }

   public static boolean isFlushRenderBuffers() {
      return flushRenderBuffers;
   }

   public static void flushRenderBuffers() {
      if (flushRenderBuffers) {
         RenderBuffers renderbuffers = mc.renderBuffers();
         renderbuffers.bufferSource().flushRenderBuffers();
         renderbuffers.crumblingBufferSource().flushRenderBuffers();
      }
   }

   public static void finishRenderBuffers() {
      RenderBuffers renderbuffers = mc.renderBuffers();
      renderbuffers.bufferSource().endBatch();
      renderbuffers.crumblingBufferSource().endBatch();
   }
}