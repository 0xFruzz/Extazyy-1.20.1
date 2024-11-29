package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.optifine.Config;
import net.optifine.render.MultiTextureData;
import net.optifine.render.MultiTextureRenderer;
import net.optifine.render.VboRange;
import net.optifine.render.VboRegion;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.util.GpuMemory;
import org.joml.Matrix4f;

public class VertexBuffer implements AutoCloseable {
   private final VertexBuffer.Usage usage;
   private int vertexBufferId;
   private int indexBufferId;
   private int arrayObjectId;
   @Nullable
   private VertexFormat format;
   @Nullable
   private RenderSystem.AutoStorageIndexBuffer sequentialIndices;
   private VertexFormat.IndexType indexType;
   private int indexCount;
   private VertexFormat.Mode mode;
   private VboRegion vboRegion;
   private VboRange vboRange;
   private MultiTextureData multiTextureData;
   private static ByteBuffer emptyBuffer = MemoryTracker.create(0);

   public VertexBuffer(VertexBuffer.Usage pUsage) {
      this.usage = pUsage;
      RenderSystem.assertOnRenderThread();
      this.vertexBufferId = GlStateManager._glGenBuffers();
      this.indexBufferId = GlStateManager._glGenBuffers();
      this.arrayObjectId = GlStateManager._glGenVertexArrays();
   }

   public void upload(BufferBuilder.RenderedBuffer pBuffer) {
      if (!this.isInvalid()) {
         RenderSystem.assertOnRenderThread();

         try {
            GpuMemory.bufferFreed((long)this.getVertexBufferSize());
            GpuMemory.bufferFreed((long)this.getIndexBufferSize());
            BufferBuilder.DrawState bufferbuilder$drawstate = pBuffer.drawState();
            this.format = this.uploadVertexBuffer(bufferbuilder$drawstate, pBuffer.vertexBuffer());
            this.sequentialIndices = this.uploadIndexBuffer(bufferbuilder$drawstate, pBuffer.indexBuffer());
            this.indexCount = bufferbuilder$drawstate.indexCount();
            this.indexType = bufferbuilder$drawstate.indexType();
            this.mode = bufferbuilder$drawstate.mode();
            GpuMemory.bufferAllocated((long)this.getVertexBufferSize());
            GpuMemory.bufferAllocated((long)this.getIndexBufferSize());
            if (this.vboRegion == null) {
               this.multiTextureData = bufferbuilder$drawstate.getMultiTextureData();
               return;
            }

            ByteBuffer bytebuffer = pBuffer.vertexBuffer();
            bytebuffer.position(0);
            bytebuffer.limit(bufferbuilder$drawstate.vertexBufferSize());
            this.vboRegion.bufferData(bytebuffer, this.vboRange);
            bytebuffer.position(0);
            bytebuffer.limit(bufferbuilder$drawstate.bufferSize());
         } finally {
            pBuffer.release();
         }

      }
   }

   private VertexFormat uploadVertexBuffer(BufferBuilder.DrawState pDrawState, ByteBuffer pBuffer) {
      if (this.vboRegion != null) {
         return pDrawState.format();
      } else {
         boolean flag = false;
         if (!pDrawState.format().equals(this.format)) {
            if (this.format != null) {
               this.format.clearBufferState();
            }

            GlStateManager._glBindBuffer(34962, this.vertexBufferId);
            pDrawState.format().setupBufferState();
            if (Config.isShaders()) {
               ShadersRender.setupArrayPointersVbo();
            }

            flag = true;
         }

         if (!pDrawState.indexOnly()) {
            if (!flag) {
               GlStateManager._glBindBuffer(34962, this.vertexBufferId);
            }

            RenderSystem.glBufferData(34962, pBuffer, this.usage.id);
         }

         return pDrawState.format();
      }
   }

   @Nullable
   private RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(BufferBuilder.DrawState pDrawState, ByteBuffer pBuffer) {
      if (!pDrawState.sequentialIndex()) {
         if (this.vboRegion != null) {
            return null;
         } else {
            GlStateManager._glBindBuffer(34963, this.indexBufferId);
            RenderSystem.glBufferData(34963, pBuffer, this.usage.id);
            return null;
         }
      } else {
         RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(pDrawState.mode());
         int i = pDrawState.indexCount();
         if (this.vboRegion != null && pDrawState.mode() == VertexFormat.Mode.QUADS) {
            i = 65536;
         }

         if (rendersystem$autostorageindexbuffer != this.sequentialIndices || !rendersystem$autostorageindexbuffer.hasStorage(i)) {
            rendersystem$autostorageindexbuffer.bind(i);
         }

         return rendersystem$autostorageindexbuffer;
      }
   }

   public void bind() {
      BufferUploader.invalidate();
      if (this.arrayObjectId >= 0) {
         GlStateManager._glBindVertexArray(this.arrayObjectId);
      }

   }

   public static void unbind() {
      BufferUploader.invalidate();
      GlStateManager._glBindVertexArray(0);
   }

   public void draw() {
      if (this.vboRegion != null) {
         this.vboRegion.drawArrays(VertexFormat.Mode.QUADS, this.vboRange);
      } else if (this.multiTextureData != null) {
         MultiTextureRenderer.draw(this.mode, this.getIndexType().asGLType, this.multiTextureData);
      } else {
         RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.getIndexType().asGLType);
      }

   }

   private VertexFormat.IndexType getIndexType() {
      RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = this.sequentialIndices;
      return rendersystem$autostorageindexbuffer != null ? rendersystem$autostorageindexbuffer.type() : this.indexType;
   }

   public void drawWithShader(Matrix4f pModelViewMatrix, Matrix4f pProjectionMatrix, ShaderInstance pShader) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this._drawWithShader(new Matrix4f(pModelViewMatrix), new Matrix4f(pProjectionMatrix), pShader);
         });
      } else {
         this._drawWithShader(pModelViewMatrix, pProjectionMatrix, pShader);
      }

   }

   private void _drawWithShader(Matrix4f pModelViewMatrix, Matrix4f pProjectionMatrix, ShaderInstance pShader) {
      for(int i = 0; i < 12; ++i) {
         int j = RenderSystem.getShaderTexture(i);
         pShader.setSampler(i, j);
      }

      if (pShader.MODEL_VIEW_MATRIX != null) {
         pShader.MODEL_VIEW_MATRIX.set(pModelViewMatrix);
      }

      if (pShader.PROJECTION_MATRIX != null) {
         pShader.PROJECTION_MATRIX.set(pProjectionMatrix);
      }

      if (pShader.INVERSE_VIEW_ROTATION_MATRIX != null) {
         pShader.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
      }

      if (pShader.COLOR_MODULATOR != null) {
         pShader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
      }

      if (pShader.GLINT_ALPHA != null) {
         pShader.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
      }

      if (pShader.FOG_START != null) {
         pShader.FOG_START.set(RenderSystem.getShaderFogStart());
      }

      if (pShader.FOG_END != null) {
         pShader.FOG_END.set(RenderSystem.getShaderFogEnd());
      }

      if (pShader.FOG_COLOR != null) {
         pShader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
      }

      if (pShader.FOG_SHAPE != null) {
         pShader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
      }

      if (pShader.TEXTURE_MATRIX != null) {
         pShader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
      }

      if (pShader.GAME_TIME != null) {
         pShader.GAME_TIME.set(RenderSystem.getShaderGameTime());
      }

      if (pShader.SCREEN_SIZE != null) {
         Window window = Minecraft.getInstance().getWindow();
         pShader.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
      }

      if (pShader.LINE_WIDTH != null && (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP)) {
         pShader.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
      }

      RenderSystem.setupShaderLights(pShader);
      pShader.apply();
      boolean flag = Config.isShaders() && Shaders.isRenderingWorld;
      boolean flag1 = flag && SVertexBuilder.preDrawArrays(this.format);
      if (flag) {
         Shaders.setModelViewMatrix(pModelViewMatrix);
         Shaders.setProjectionMatrix(pProjectionMatrix);
         Shaders.setTextureMatrix(RenderSystem.getTextureMatrix());
         Shaders.setColorModulator(RenderSystem.getShaderColor());
      }

      this.draw();
      if (flag1) {
         SVertexBuilder.postDrawArrays();
      }

      pShader.clear();
   }

   public void close() {
      if (this.vertexBufferId >= 0) {
         RenderSystem.glDeleteBuffers(this.vertexBufferId);
         this.vertexBufferId = -1;
         GpuMemory.bufferFreed((long)this.getVertexBufferSize());
      }

      if (this.indexBufferId >= 0) {
         RenderSystem.glDeleteBuffers(this.indexBufferId);
         this.indexBufferId = -1;
         GpuMemory.bufferFreed((long)this.getIndexBufferSize());
      }

      if (this.arrayObjectId >= 0) {
         RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
         this.arrayObjectId = -1;
      }

      this.indexCount = 0;
   }

   public VertexFormat getFormat() {
      return this.format;
   }

   public boolean isInvalid() {
      if (this.vboRegion != null) {
         return false;
      } else {
         return this.arrayObjectId == -1;
      }
   }

   public void setVboRegion(VboRegion vboRegion) {
      if (vboRegion != null) {
         this.close();
         this.vboRegion = vboRegion;
         this.vboRange = new VboRange();
      }
   }

   public VboRegion getVboRegion() {
      return this.vboRegion;
   }

   public boolean isEmpty() {
      return this.indexCount <= 0;
   }

   public void clearData() {
      if (this.indexCount > 0) {
         if (this.vboRegion != null) {
            this.vboRegion.bufferData(emptyBuffer, this.vboRange);
            this.indexCount = 0;
         } else {
            this.bind();
            if (this.vertexBufferId >= 0) {
               GlStateManager._glBindBuffer(34962, this.vertexBufferId);
               GlStateManager._glBufferData(34962, 0L, this.usage.id);
               GpuMemory.bufferFreed((long)this.getVertexBufferSize());
            }

            if (this.indexBufferId >= 0 && this.sequentialIndices == null) {
               GlStateManager._glBindBuffer(34963, this.indexBufferId);
               GlStateManager._glBufferData(34963, 0L, this.usage.id);
               GpuMemory.bufferFreed((long)this.getIndexBufferSize());
            }

            unbind();
            this.indexCount = 0;
         }
      }
   }

   public int getIndexCount() {
      return this.indexCount;
   }

   private int getVertexBufferSize() {
      return this.format == null ? 0 : this.indexCount * this.format.getVertexSize();
   }

   private int getIndexBufferSize() {
      if (this.sequentialIndices != null) {
         return 0;
      } else {
         return this.indexType == null ? 0 : this.indexCount * this.indexType.bytes;
      }
   }

   public static enum Usage {
      STATIC(35044),
      DYNAMIC(35048);

      final int id;

      private Usage(int pId) {
         this.id = pId;
      }
   }
}