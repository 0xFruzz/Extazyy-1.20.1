package net.optifine.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.RenderType;
import net.optifine.Config;
import net.optifine.shaders.ShadersRender;
import net.optifine.util.LinkedList;
import org.lwjgl.PointerBuffer;

public class VboRegion {
   private RenderType layer = null;
   private int glArrayObjectId = GlStateManager._glGenVertexArrays();
   private int glBufferId = GlStateManager._glGenBuffers();
   private int capacity = 4096;
   private int positionTop = 0;
   private int sizeUsed;
   private LinkedList<VboRange> rangeList = new LinkedList<>();
   private VboRange compactRangeLast = null;
   private PointerBuffer bufferIndexVertex = Config.createDirectPointerBuffer(this.capacity);
   private IntBuffer bufferCountVertex = Config.createDirectIntBuffer(this.capacity);
   private final int vertexBytes = DefaultVertexFormat.BLOCK.getVertexSize();
   private VertexFormat.Mode drawMode = VertexFormat.Mode.QUADS;
   private boolean isShaders = Config.isShaders();

   public VboRegion(RenderType layer) {
      this.layer = layer;
      this.bindBuffer();
      long i = this.toBytes(this.capacity);
      GlStateManager._glBufferData(GlStateManager.GL_ARRAY_BUFFER, i, GlStateManager.GL_STATIC_DRAW);
      this.unbindBuffer();
   }

   public void bufferData(ByteBuffer data, VboRange range) {
      if (this.glBufferId >= 0) {
         int i = range.getPosition();
         int j = range.getSize();
         int k = this.toVertex((long)data.limit());
         if (k <= 0) {
            if (i >= 0) {
               range.setPosition(-1);
               range.setSize(0);
               this.rangeList.remove(range.getNode());
               this.sizeUsed -= j;
            }

         } else {
            if (k > j) {
               range.setPosition(this.positionTop);
               range.setSize(k);
               this.positionTop += k;
               if (i >= 0) {
                  this.rangeList.remove(range.getNode());
               }

               this.rangeList.addLast(range.getNode());
            }

            range.setSize(k);
            this.sizeUsed += k - j;
            this.checkVboSize(range.getPositionNext());
            long l = this.toBytes(range.getPosition());
            this.bindVertexArray();
            this.bindBuffer();
            GlStateManager.bufferSubData(GlStateManager.GL_ARRAY_BUFFER, l, data);
            this.unbindBuffer();
            unbindVertexArray();
            if (this.positionTop > this.sizeUsed * 11 / 10) {
               this.compactRanges(1);
            }

         }
      }
   }

   private void compactRanges(int countMax) {
      if (!this.rangeList.isEmpty()) {
         VboRange vborange = this.compactRangeLast;
         if (vborange == null || !this.rangeList.contains(vborange.getNode())) {
            vborange = this.rangeList.getFirst().getItem();
         }

         int i = vborange.getPosition();
         VboRange vborange1 = vborange.getPrev();
         if (vborange1 == null) {
            i = 0;
         } else {
            i = vborange1.getPositionNext();
         }

         int j = 0;

         while(vborange != null && j < countMax) {
            ++j;
            if (vborange.getPosition() == i) {
               i += vborange.getSize();
               vborange = vborange.getNext();
            } else {
               int k = vborange.getPosition() - i;
               if (vborange.getSize() <= k) {
                  this.copyVboData(vborange.getPosition(), i, vborange.getSize());
                  vborange.setPosition(i);
                  i += vborange.getSize();
                  vborange = vborange.getNext();
               } else {
                  this.checkVboSize(this.positionTop + vborange.getSize());
                  this.copyVboData(vborange.getPosition(), this.positionTop, vborange.getSize());
                  vborange.setPosition(this.positionTop);
                  this.positionTop += vborange.getSize();
                  VboRange vborange2 = vborange.getNext();
                  this.rangeList.remove(vborange.getNode());
                  this.rangeList.addLast(vborange.getNode());
                  vborange = vborange2;
               }
            }
         }

         if (vborange == null) {
            this.positionTop = this.rangeList.getLast().getItem().getPositionNext();
         }

         this.compactRangeLast = vborange;
      }
   }

   private void checkRanges() {
      int i = 0;
      int j = 0;

      for(VboRange vborange = this.rangeList.getFirst().getItem(); vborange != null; vborange = vborange.getNext()) {
         ++i;
         j += vborange.getSize();
         if (vborange.getPosition() < 0 || vborange.getSize() <= 0 || vborange.getPositionNext() > this.positionTop) {
            throw new RuntimeException("Invalid range: " + vborange);
         }

         VboRange vborange1 = vborange.getPrev();
         if (vborange1 != null && vborange.getPosition() < vborange1.getPositionNext()) {
            throw new RuntimeException("Invalid range: " + vborange);
         }

         VboRange vborange2 = vborange.getNext();
         if (vborange2 != null && vborange.getPositionNext() > vborange2.getPosition()) {
            throw new RuntimeException("Invalid range: " + vborange);
         }
      }

      if (i != this.rangeList.getSize()) {
         throw new RuntimeException("Invalid count: " + i + " <> " + this.rangeList.getSize());
      } else if (j != this.sizeUsed) {
         throw new RuntimeException("Invalid size: " + j + " <> " + this.sizeUsed);
      }
   }

   private void checkVboSize(int sizeMin) {
      if (this.capacity < sizeMin) {
         this.expandVbo(sizeMin);
      }

   }

   private void copyVboData(int posFrom, int posTo, int size) {
      long i = this.toBytes(posFrom);
      long j = this.toBytes(posTo);
      long k = this.toBytes(size);
      GlStateManager._glBindBuffer(GlStateManager.GL_COPY_READ_BUFFER, this.glBufferId);
      GlStateManager._glBindBuffer(GlStateManager.GL_COPY_WRITE_BUFFER, this.glBufferId);
      GlStateManager.copyBufferSubData(GlStateManager.GL_COPY_READ_BUFFER, GlStateManager.GL_COPY_WRITE_BUFFER, i, j, k);
      Config.checkGlError("Copy VBO range");
      GlStateManager._glBindBuffer(GlStateManager.GL_COPY_READ_BUFFER, 0);
      GlStateManager._glBindBuffer(GlStateManager.GL_COPY_WRITE_BUFFER, 0);
   }

   private void expandVbo(int sizeMin) {
      int i;
      for(i = this.capacity * 6 / 4; i < sizeMin; i = i * 6 / 4) {
      }

      long j = this.toBytes(this.capacity);
      long k = this.toBytes(i);
      int l = GlStateManager._glGenBuffers();
      GlStateManager._glBindBuffer(GlStateManager.GL_ARRAY_BUFFER, l);
      GlStateManager._glBufferData(GlStateManager.GL_ARRAY_BUFFER, k, GlStateManager.GL_STATIC_DRAW);
      Config.checkGlError("Expand VBO");
      GlStateManager._glBindBuffer(GlStateManager.GL_ARRAY_BUFFER, 0);
      GlStateManager._glBindBuffer(GlStateManager.GL_COPY_READ_BUFFER, this.glBufferId);
      GlStateManager._glBindBuffer(GlStateManager.GL_COPY_WRITE_BUFFER, l);
      GlStateManager.copyBufferSubData(GlStateManager.GL_COPY_READ_BUFFER, GlStateManager.GL_COPY_WRITE_BUFFER, 0L, 0L, j);
      Config.checkGlError("Copy VBO: " + k);
      GlStateManager._glBindBuffer(GlStateManager.GL_COPY_READ_BUFFER, 0);
      GlStateManager._glBindBuffer(GlStateManager.GL_COPY_WRITE_BUFFER, 0);
      GlStateManager._glDeleteBuffers(this.glBufferId);
      this.bufferIndexVertex = Config.createDirectPointerBuffer(i);
      this.bufferCountVertex = Config.createDirectIntBuffer(i);
      this.glBufferId = l;
      this.capacity = i;
   }

   public void bindVertexArray() {
      GlStateManager._glBindVertexArray(this.glArrayObjectId);
   }

   public void bindBuffer() {
      GlStateManager._glBindBuffer(GlStateManager.GL_ARRAY_BUFFER, this.glBufferId);
   }

   public void drawArrays(VertexFormat.Mode drawMode, VboRange range) {
      if (this.drawMode != drawMode) {
         if (this.bufferIndexVertex.position() > 0) {
            throw new IllegalArgumentException("Mixed region draw modes: " + this.drawMode + " != " + drawMode);
         }

         this.drawMode = drawMode;
      }

      int i = 4;
      int j = drawMode.indexCount(range.getPosition()) * i;
      this.bufferIndexVertex.put((long)j);
      int k = drawMode.indexCount(range.getSize());
      this.bufferCountVertex.put(k);
   }

   public void finishDraw() {
      this.bindVertexArray();
      this.bindBuffer();
      this.layer.format().setupBufferState();
      if (this.isShaders) {
         ShadersRender.setupArrayPointersVbo();
      }

      int i = this.drawMode.indexCount(this.positionTop);
      RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(this.drawMode);
      VertexFormat.IndexType vertexformat$indextype = rendersystem$autostorageindexbuffer.type();
      rendersystem$autostorageindexbuffer.bind(i);
      this.bufferIndexVertex.flip();
      this.bufferCountVertex.flip();
      GlStateManager.glMultiDrawElements(this.drawMode.asGLMode, this.bufferCountVertex, vertexformat$indextype.asGLType, this.bufferIndexVertex);
      this.bufferIndexVertex.limit(this.bufferIndexVertex.capacity());
      this.bufferCountVertex.limit(this.bufferCountVertex.capacity());
      if (this.positionTop > this.sizeUsed * 11 / 10) {
         this.compactRanges(1);
      }

   }

   public void unbindBuffer() {
      GlStateManager._glBindBuffer(GlStateManager.GL_ARRAY_BUFFER, 0);
   }

   public static void unbindVertexArray() {
      GlStateManager._glBindVertexArray(0);
   }

   public void deleteGlBuffers() {
      if (this.glArrayObjectId >= 0) {
         GlStateManager._glDeleteVertexArrays(this.glArrayObjectId);
         this.glArrayObjectId = -1;
      }

      if (this.glBufferId >= 0) {
         GlStateManager._glDeleteBuffers(this.glBufferId);
         this.glBufferId = -1;
      }

   }

   private long toBytes(int vertex) {
      return (long)vertex * (long)this.vertexBytes;
   }

   private int toVertex(long bytes) {
      return (int)(bytes / (long)this.vertexBytes);
   }

   public int getPositionTop() {
      return this.positionTop;
   }
}