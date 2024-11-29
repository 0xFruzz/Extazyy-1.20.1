package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import net.optifine.SmartAnimations;

public class Tesselator {
   private static final int MAX_MEMORY_USE = 8388608;
   private static final int MAX_FLOATS = 2097152;
   private final BufferBuilder builder;
   private static final Tesselator INSTANCE = new Tesselator();

   public static Tesselator getInstance() {
      RenderSystem.assertOnGameThreadOrInit();
      return INSTANCE;
   }

   public Tesselator(int pCapacity) {
      this.builder = new BufferBuilder(pCapacity);
   }

   public Tesselator() {
      this(2097152);
   }

   public void end() {
      if (this.builder.animatedSprites != null) {
         SmartAnimations.spritesRendered(this.builder.animatedSprites);
      }

      BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = this.builder.endOrDiscardIfEmpty();
      if (bufferbuilder$renderedbuffer != null) {
         BufferUploader.drawWithShader(bufferbuilder$renderedbuffer);
      }

   }

   public BufferBuilder getBuilder() {
      return this.builder;
   }
}