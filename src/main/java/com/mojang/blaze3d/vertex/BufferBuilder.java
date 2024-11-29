package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.Config;
import net.optifine.SmartAnimations;
import net.optifine.render.MultiTextureBuilder;
import net.optifine.render.MultiTextureData;
import net.optifine.render.RenderEnv;
import net.optifine.render.VertexPosition;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.util.BufferUtil;
import net.optifine.util.MathUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public class BufferBuilder extends DefaultedVertexConsumer implements BufferVertexConsumer {
   private static final int GROWTH_SIZE = 2097152;
   private static final Logger LOGGER = LogUtils.getLogger();
   private ByteBuffer buffer;
   private int renderedBufferCount;
   private int renderedBufferPointer;
   private int nextElementByte;
   private int vertices;
   @Nullable
   private VertexFormatElement currentElement;
   private int elementIndex;
   private VertexFormat format;
   private VertexFormat.Mode mode;
   private boolean fastFormat;
   private boolean fullFormat;
   private boolean building;
   @Nullable
   private Vector3f[] sortingPoints;
   @Nullable
   private VertexSorting sorting;
   private boolean indexOnly;
   private RenderType renderType;
   private boolean renderBlocks;
   private TextureAtlasSprite[] quadSprites = null;
   private TextureAtlasSprite[] quadSpritesPrev = null;
   private int[] quadOrdering = null;
   private TextureAtlasSprite quadSprite = null;
   private MultiTextureBuilder multiTextureBuilder = new MultiTextureBuilder();
   public SVertexBuilder sVertexBuilder;
   public RenderEnv renderEnv = null;
   public BitSet animatedSprites = null;
   public BitSet animatedSpritesCached = new BitSet();
   private ByteBuffer byteBufferTriangles;
   private Vector3f tempVec3f = new Vector3f();
   private float[] tempFloat4 = new float[4];
   private int[] tempInt4 = new int[4];
   private IntBuffer intBuffer;
   private FloatBuffer floatBuffer;
   private MultiBufferSource.BufferSource renderTypeBuffer;
   private FloatBuffer floatBufferSort;
   private VertexPosition[] quadVertexPositions;
   private Vector3f midBlock = new Vector3f();

   public BufferBuilder(int pCapacity) {
      this.buffer = MemoryTracker.create(pCapacity * 6);
      this.intBuffer = this.buffer.asIntBuffer();
      this.floatBuffer = this.buffer.asFloatBuffer();
      SVertexBuilder.initVertexBuilder(this);
   }

   private void ensureVertexCapacity() {
      this.ensureCapacity(this.format.getVertexSize());
   }

   private void ensureCapacity(int pIncreaseAmount) {
      if (this.nextElementByte + pIncreaseAmount > this.buffer.capacity()) {
         int i = this.buffer.capacity();
         int j = i + roundUp(pIncreaseAmount);
         LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", i, j);
         ByteBuffer bytebuffer = MemoryTracker.resize(this.buffer, j);
         bytebuffer.rewind();
         this.buffer = bytebuffer;
         this.intBuffer = this.buffer.asIntBuffer();
         this.floatBuffer = this.buffer.asFloatBuffer();
         if (this.quadSprites != null) {
            TextureAtlasSprite[] atextureatlassprite = this.quadSprites;
            int k = this.getBufferQuadSize();
            this.quadSprites = new TextureAtlasSprite[k];
            System.arraycopy(atextureatlassprite, 0, this.quadSprites, 0, Math.min(atextureatlassprite.length, this.quadSprites.length));
            this.quadSpritesPrev = null;
         }
      }

   }

   private static int roundUp(int pX) {
      int i = 2097152;
      if (pX == 0) {
         return i;
      } else {
         if (pX < 0) {
            i *= -1;
         }

         int j = pX % i;
         return j == 0 ? pX : pX + i - j;
      }
   }

   public void setQuadSorting(VertexSorting pQuadSorting) {
      if (this.mode == VertexFormat.Mode.QUADS) {
         this.sorting = pQuadSorting;
         if (this.sortingPoints == null) {
            this.sortingPoints = this.makeQuadSortingPoints();
         }
      }

   }

   public BufferBuilder.SortState getSortState() {
      TextureAtlasSprite[] atextureatlassprite = null;
      if (this.quadSprites != null) {
         int i = this.vertices / 4;
         atextureatlassprite = Arrays.copyOfRange(this.quadSprites, 0, i);
      }

      return new BufferBuilder.SortState(this.mode, this.vertices, this.sortingPoints, this.sorting, atextureatlassprite);
   }

   public void restoreSortState(BufferBuilder.SortState pSortState) {
      this.buffer.rewind();
      this.mode = pSortState.mode;
      this.vertices = pSortState.vertices;
      this.nextElementByte = this.renderedBufferPointer;
      this.sortingPoints = pSortState.sortingPoints;
      this.sorting = pSortState.sorting;
      this.indexOnly = true;
      if (this.quadSprites != null && pSortState.quadSprites != null) {
         System.arraycopy(pSortState.quadSprites, 0, this.quadSprites, 0, Math.min(pSortState.quadSprites.length, this.quadSprites.length));
      }

   }

   public void begin(VertexFormat.Mode pMode, VertexFormat pFormat) {
      if (this.building) {
         throw new IllegalStateException("Already building!");
      } else {
         this.building = true;
         this.mode = pMode;
         this.switchFormat(pFormat);
         this.currentElement = pFormat.getElements().get(0);
         this.elementIndex = 0;
         this.buffer.rewind();
         if (Config.isShaders()) {
            SVertexBuilder.endSetVertexFormat(this);
         }

         if (Config.isMultiTexture()) {
            this.initQuadSprites();
         }

         if (SmartAnimations.isActive()) {
            if (this.animatedSprites == null) {
               this.animatedSprites = this.animatedSpritesCached;
            }

            this.animatedSprites.clear();
         } else if (this.animatedSprites != null) {
            this.animatedSprites = null;
         }

      }
   }

   public VertexConsumer uv(float u, float v) {
      if (this.quadSprite != null && this.quadSprites != null) {
         u = this.quadSprite.toSingleU(u);
         v = this.quadSprite.toSingleV(v);
         this.quadSprites[this.vertices / 4] = this.quadSprite;
      }

      return BufferVertexConsumer.super.uv(u, v);
   }

   private void switchFormat(VertexFormat pFormat) {
      if (this.format != pFormat) {
         this.format = pFormat;
         boolean flag = pFormat == DefaultVertexFormat.NEW_ENTITY;
         boolean flag1 = pFormat == DefaultVertexFormat.BLOCK;
         this.fastFormat = flag || flag1;
         this.fullFormat = flag;
      }

   }

   private IntConsumer intConsumer(int pNextElementByte, VertexFormat.IndexType pIndexType) {
      MutableInt mutableint = new MutableInt(pNextElementByte);
      IntConsumer intconsumer;
      switch (pIndexType) {
         case SHORT:
            intconsumer = (valueIn) -> {
               this.buffer.putShort(mutableint.getAndAdd(2), (short)valueIn);
            };
            break;
         case INT:
            intconsumer = (valueIn) -> {
               this.buffer.putInt(mutableint.getAndAdd(4), valueIn);
            };
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return intconsumer;
   }

   private Vector3f[] makeQuadSortingPoints() {
      FloatBuffer floatbuffer = this.buffer.asFloatBuffer();
      int i = this.renderedBufferPointer / 4;
      int j = this.format.getIntegerSize();
      int k = j * this.mode.primitiveStride;
      int l = this.vertices / this.mode.primitiveStride;
      Vector3f[] avector3f = new Vector3f[l];

      for(int i1 = 0; i1 < l; ++i1) {
         float f = floatbuffer.get(i + i1 * k + 0);
         float f1 = floatbuffer.get(i + i1 * k + 1);
         float f2 = floatbuffer.get(i + i1 * k + 2);
         float f3 = floatbuffer.get(i + i1 * k + j * 2 + 0);
         float f4 = floatbuffer.get(i + i1 * k + j * 2 + 1);
         float f5 = floatbuffer.get(i + i1 * k + j * 2 + 2);
         float f6 = (f + f3) / 2.0F;
         float f7 = (f1 + f4) / 2.0F;
         float f8 = (f2 + f5) / 2.0F;
         avector3f[i1] = new Vector3f(f6, f7, f8);
      }

      return avector3f;
   }

   private void putSortedQuadIndices(VertexFormat.IndexType pIndexType) {
      if (this.sortingPoints != null && this.sorting != null) {
         int[] aint = this.sorting.sort(this.sortingPoints);
         IntConsumer intconsumer = this.intConsumer(this.nextElementByte, pIndexType);

         for(int i : aint) {
            intconsumer.accept(i * this.mode.primitiveStride + 0);
            intconsumer.accept(i * this.mode.primitiveStride + 1);
            intconsumer.accept(i * this.mode.primitiveStride + 2);
            intconsumer.accept(i * this.mode.primitiveStride + 2);
            intconsumer.accept(i * this.mode.primitiveStride + 3);
            intconsumer.accept(i * this.mode.primitiveStride + 0);
         }

         if (this.quadSprites != null) {
            this.quadOrdering = aint;
         }

      } else {
         throw new IllegalStateException("Sorting state uninitialized");
      }
   }

   public boolean isCurrentBatchEmpty() {
      return this.vertices == 0;
   }

   @Nullable
   public BufferBuilder.RenderedBuffer endOrDiscardIfEmpty() {
      this.ensureDrawing();
      if (this.isCurrentBatchEmpty()) {
         this.reset();
         return null;
      } else {
         BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = this.storeRenderedBuffer();
         this.reset();
         return bufferbuilder$renderedbuffer;
      }
   }

   public BufferBuilder.RenderedBuffer end() {
      this.ensureDrawing();
      BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = this.storeRenderedBuffer();
      this.reset();
      return bufferbuilder$renderedbuffer;
   }

   private void ensureDrawing() {
      if (!this.building) {
         throw new IllegalStateException("Not building!");
      }
   }

   private BufferBuilder.RenderedBuffer storeRenderedBuffer() {
      int i = this.mode.indexCount(this.vertices);
      int j = !this.indexOnly ? this.vertices * this.format.getVertexSize() : 0;
      VertexFormat.IndexType vertexformat$indextype = VertexFormat.IndexType.least(i);
      boolean flag;
      int k;
      if (this.sortingPoints != null) {
         int l = Mth.roundToward(i * vertexformat$indextype.bytes, 4);
         this.ensureCapacity(l);
         this.putSortedQuadIndices(vertexformat$indextype);
         flag = false;
         this.nextElementByte += l;
         k = j + l;
      } else {
         flag = true;
         k = j;
      }

      int i1 = this.renderedBufferPointer;
      this.renderedBufferPointer += k;
      ++this.renderedBufferCount;
      MultiTextureData multitexturedata = this.multiTextureBuilder.build(this.vertices, this.renderType, this.quadSprites, this.quadOrdering);
      BufferBuilder.DrawState bufferbuilder$drawstate = new BufferBuilder.DrawState(this.format, this.vertices, i, this.mode, vertexformat$indextype, this.indexOnly, flag, multitexturedata);
      return new BufferBuilder.RenderedBuffer(i1, bufferbuilder$drawstate);
   }

   private void reset() {
      this.building = false;
      this.vertices = 0;
      this.currentElement = null;
      this.elementIndex = 0;
      this.sortingPoints = null;
      this.sorting = null;
      this.indexOnly = false;
      this.renderType = null;
      this.renderBlocks = false;
      if (this.quadSprites != null) {
         this.quadSpritesPrev = this.quadSprites;
      }

      this.quadSprites = null;
      this.quadSprite = null;
      this.quadOrdering = null;
   }

   public void putByte(int pIndex, byte pByteValue) {
      this.buffer.put(this.nextElementByte + pIndex, pByteValue);
   }

   public void putShort(int pIndex, short pShortValue) {
      this.buffer.putShort(this.nextElementByte + pIndex, pShortValue);
   }

   public void putFloat(int pIndex, float pFloatValue) {
      this.buffer.putFloat(this.nextElementByte + pIndex, pFloatValue);
   }

   public void endVertex() {
      if (this.elementIndex != 0) {
         throw new IllegalStateException("Not filled all elements of the vertex");
      } else {
         ++this.vertices;
         this.ensureVertexCapacity();
         if (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP) {
            int i = this.format.getVertexSize();
            this.buffer.put(this.nextElementByte, this.buffer, this.nextElementByte - i, i);
            this.nextElementByte += i;
            ++this.vertices;
            this.ensureVertexCapacity();
         }

         if (Config.isShaders()) {
            SVertexBuilder.endAddVertex(this);
         }

      }
   }

   public void nextElement() {
      ImmutableList<VertexFormatElement> immutablelist = this.format.getElements();
      this.elementIndex = (this.elementIndex + 1) % immutablelist.size();
      this.nextElementByte += this.currentElement.getByteSize();
      VertexFormatElement vertexformatelement = immutablelist.get(this.elementIndex);
      this.currentElement = vertexformatelement;
      if (vertexformatelement.getUsage() == VertexFormatElement.Usage.PADDING) {
         this.nextElement();
      }

      if (this.defaultColorSet && this.currentElement.getUsage() == VertexFormatElement.Usage.COLOR) {
         BufferVertexConsumer.super.color(this.defaultR, this.defaultG, this.defaultB, this.defaultA);
      }

   }

   public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
      if (this.defaultColorSet) {
         throw new IllegalStateException();
      } else {
         return BufferVertexConsumer.super.color(pRed, pGreen, pBlue, pAlpha);
      }
   }

   public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
      if (this.defaultColorSet) {
         throw new IllegalStateException();
      } else {
         if (this.fastFormat) {
            this.putFloat(0, pX);
            this.putFloat(4, pY);
            this.putFloat(8, pZ);
            this.putByte(12, (byte)((int)(pRed * 255.0F)));
            this.putByte(13, (byte)((int)(pGreen * 255.0F)));
            this.putByte(14, (byte)((int)(pBlue * 255.0F)));
            this.putByte(15, (byte)((int)(pAlpha * 255.0F)));
            this.putFloat(16, pTexU);
            this.putFloat(20, pTexV);
            int i;
            if (this.fullFormat) {
               this.putShort(24, (short)(pOverlayUV & '\uffff'));
               this.putShort(26, (short)(pOverlayUV >> 16 & '\uffff'));
               i = 28;
            } else {
               i = 24;
            }

            this.putShort(i + 0, (short)(pLightmapUV & '\uffff'));
            this.putShort(i + 2, (short)(pLightmapUV >> 16 & '\uffff'));
            this.putByte(i + 4, BufferVertexConsumer.normalIntValue(pNormalX));
            this.putByte(i + 5, BufferVertexConsumer.normalIntValue(pNormalY));
            this.putByte(i + 6, BufferVertexConsumer.normalIntValue(pNormalZ));
            this.nextElementByte += this.format.getVertexSize();
            this.endVertex();
         } else {
            super.vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
         }

      }
   }

   void releaseRenderedBuffer() {
      if (this.renderedBufferCount > 0 && --this.renderedBufferCount == 0) {
         this.clear();
      }

   }

   public void clear() {
      if (this.renderedBufferCount > 0) {
         LOGGER.warn("Clearing BufferBuilder with unused batches");
      }

      this.discard();
   }

   public void discard() {
      this.renderedBufferCount = 0;
      this.renderedBufferPointer = 0;
      this.nextElementByte = 0;
      this.quadSprite = null;
   }

   public VertexFormatElement currentElement() {
      if (this.currentElement == null) {
         throw new IllegalStateException("BufferBuilder not started");
      } else {
         return this.currentElement;
      }
   }

   public boolean building() {
      return this.building;
   }

   public void putSprite(TextureAtlasSprite sprite) {
      if (this.animatedSprites != null && sprite != null && sprite.isTerrain() && sprite.getAnimationIndex() >= 0) {
         this.animatedSprites.set(sprite.getAnimationIndex());
      }

      if (this.quadSprites != null) {
         int i = this.vertices / 4;
         this.quadSprites[i] = sprite;
      }

   }

   public void setSprite(TextureAtlasSprite sprite) {
      if (this.animatedSprites != null && sprite != null && sprite.isTerrain() && sprite.getAnimationIndex() >= 0) {
         this.animatedSprites.set(sprite.getAnimationIndex());
      }

      if (this.quadSprites != null) {
         this.quadSprite = sprite;
      }

   }

   public boolean isMultiTexture() {
      return this.quadSprites != null;
   }

   public void setRenderType(RenderType renderType) {
      this.renderType = renderType;
   }

   public RenderType getRenderType() {
      return this.renderType;
   }

   public void setRenderBlocks(boolean renderBlocks) {
      this.renderBlocks = renderBlocks;
      if (Config.isMultiTexture()) {
         this.initQuadSprites();
      }

   }

   public void setBlockLayer(RenderType layer) {
      this.renderType = layer;
      this.renderBlocks = true;
   }

   private void initQuadSprites() {
      if (this.renderBlocks) {
         if (this.renderType != null) {
            if (this.quadSprites == null) {
               if (this.building) {
                  if (this.vertices > 0) {
                     VertexFormat.Mode vertexformat$mode = this.mode;
                     VertexFormat vertexformat = this.format;
                     RenderType rendertype = this.renderType;
                     boolean flag = this.renderBlocks;
                     this.renderType.end(this, RenderSystem.getVertexSorting());
                     this.begin(vertexformat$mode, vertexformat);
                     this.renderType = rendertype;
                     this.renderBlocks = flag;
                  }

                  this.quadSprites = this.quadSpritesPrev;
                  if (this.quadSprites == null || this.quadSprites.length < this.getBufferQuadSize()) {
                     this.quadSprites = new TextureAtlasSprite[this.getBufferQuadSize()];
                  }

               }
            }
         }
      }
   }

   private int getBufferQuadSize() {
      return this.buffer.capacity() / this.format.getVertexSize();
   }

   public RenderEnv getRenderEnv(BlockState blockStateIn, BlockPos blockPosIn) {
      if (this.renderEnv == null) {
         this.renderEnv = new RenderEnv(blockStateIn, blockPosIn);
         return this.renderEnv;
      } else {
         this.renderEnv.reset(blockStateIn, blockPosIn);
         return this.renderEnv;
      }
   }

   private static void quadsToTriangles(ByteBuffer byteBuffer, VertexFormat vertexFormat, int vertexCount, ByteBuffer byteBufferTriangles) {
      int i = vertexFormat.getVertexSize();
      int j = byteBuffer.limit();
      byteBuffer.rewind();
      byteBufferTriangles.clear();

      for(int k = 0; k < vertexCount; k += 4) {
         byteBuffer.limit((k + 3) * i);
         byteBuffer.position(k * i);
         byteBufferTriangles.put(byteBuffer);
         byteBuffer.limit((k + 1) * i);
         byteBuffer.position(k * i);
         byteBufferTriangles.put(byteBuffer);
         byteBuffer.limit((k + 2 + 2) * i);
         byteBuffer.position((k + 2) * i);
         byteBufferTriangles.put(byteBuffer);
      }

      byteBuffer.limit(j);
      byteBuffer.rewind();
      byteBufferTriangles.flip();
   }

   public VertexFormat.Mode getDrawMode() {
      return this.mode;
   }

   public int getVertexCount() {
      return this.vertices;
   }

   public Vector3f getTempVec3f(Vector3f vec) {
      this.tempVec3f.set(vec.x(), vec.y(), vec.z());
      return this.tempVec3f;
   }

   public Vector3f getTempVec3f(float x, float y, float z) {
      this.tempVec3f.set(x, y, z);
      return this.tempVec3f;
   }

   public float[] getTempFloat4(float f1, float f2, float f3, float f4) {
      this.tempFloat4[0] = f1;
      this.tempFloat4[1] = f2;
      this.tempFloat4[2] = f3;
      this.tempFloat4[3] = f4;
      return this.tempFloat4;
   }

   public int[] getTempInt4(int i1, int i2, int i3, int i4) {
      this.tempInt4[0] = i1;
      this.tempInt4[1] = i2;
      this.tempInt4[2] = i3;
      this.tempInt4[3] = i4;
      return this.tempInt4;
   }

   public ByteBuffer getByteBuffer() {
      return this.buffer;
   }

   public FloatBuffer getFloatBuffer() {
      return this.floatBuffer;
   }

   public IntBuffer getIntBuffer() {
      return this.intBuffer;
   }

   public int getBufferIntSize() {
      return this.vertices * this.format.getIntegerSize();
   }

   private FloatBuffer getFloatBufferSort(int size) {
      if (this.floatBufferSort == null || this.floatBufferSort.capacity() < size) {
         this.floatBufferSort = BufferUtil.createDirectFloatBuffer(size);
      }

      return this.floatBufferSort;
   }

   public MultiBufferSource.BufferSource getRenderTypeBuffer() {
      return this.renderTypeBuffer;
   }

   public void setRenderTypeBuffer(MultiBufferSource.BufferSource renderTypeBuffer) {
      this.renderTypeBuffer = renderTypeBuffer;
   }

   public boolean canAddVertexText() {
      if (this.format.getVertexSize() != DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP.getVertexSize()) {
         return false;
      } else {
         return this.nextElementByte % 4 == 0;
      }
   }

   public void addVertexText(Matrix4f mat4, float x, float y, float z, int col, float texU, float texV, int lightmapUV) {
      if (mat4 != null) {
         float f = MathUtils.getTransformX(mat4, x, y, z);
         float f1 = MathUtils.getTransformY(mat4, x, y, z);
         float f2 = MathUtils.getTransformZ(mat4, x, y, z);
         x = f;
         y = f1;
         z = f2;
      }

      int i = this.nextElementByte / 4;
      this.floatBuffer.put(i++, x);
      this.floatBuffer.put(i++, y);
      this.floatBuffer.put(i++, z);
      this.intBuffer.put(i++, col);
      this.floatBuffer.put(i++, texU);
      this.floatBuffer.put(i++, texV);
      this.intBuffer.put(i++, lightmapUV);
      this.nextElementByte += this.format.getVertexSize();
      ++this.vertices;
      this.ensureCapacity(this.format.getVertexSize());
      if (Config.isShaders()) {
         SVertexBuilder.endAddVertex(this);
      }

   }

   public boolean canAddVertexFast() {
      return !this.defaultColorSet && this.fastFormat && this.nextElementByte % 4 == 0 && this.fullFormat;
   }

   public void addVertexFast(float x, float y, float z, int color, float texU, float texV, int overlayUV, int lightmapUV, int normals) {
      int i = this.nextElementByte / 4;
      this.floatBuffer.put(i++, x);
      this.floatBuffer.put(i++, y);
      this.floatBuffer.put(i++, z);
      this.intBuffer.put(i++, color);
      this.floatBuffer.put(i++, texU);
      this.floatBuffer.put(i++, texV);
      this.intBuffer.put(i++, overlayUV);
      this.intBuffer.put(i++, lightmapUV);
      this.intBuffer.put(i++, normals);
      this.nextElementByte += this.format.getVertexSize();
      ++this.vertices;
      this.ensureCapacity(this.format.getVertexSize());
      if (Config.isShaders()) {
         SVertexBuilder.endAddVertex(this);
      }

   }

   public void setQuadVertexPositions(VertexPosition[] vps) {
      this.quadVertexPositions = vps;
   }

   public VertexPosition[] getQuadVertexPositions() {
      return this.quadVertexPositions;
   }

   public void setMidBlock(float mx, float my, float mz) {
      this.midBlock.set(mx, my, mz);
   }

   public Vector3f getMidBlock() {
      return this.midBlock;
   }

   public void putBulkData(ByteBuffer buffer) {
      if (Config.isShaders()) {
         SVertexBuilder.beginAddVertexData(this, buffer);
      }

      this.ensureCapacity(buffer.limit() + this.format.getVertexSize());
      this.buffer.position(this.nextElementByte);
      this.buffer.put(buffer);
      this.buffer.position(0);
      this.vertices += buffer.limit() / this.format.getVertexSize();
      this.nextElementByte += buffer.limit();
      if (Config.isShaders()) {
         SVertexBuilder.endAddVertexData(this);
      }

   }

   public void getBulkData(ByteBuffer buffer) {
      this.buffer.position(0);
      this.buffer.limit(this.nextElementByte);
      buffer.put(this.buffer);
      this.buffer.clear();
   }

   public VertexFormat getVertexFormat() {
      return this.format;
   }

   public int getStartPosition() {
      return this.renderedBufferPointer;
   }

   public int getIntStartPosition() {
      return this.renderedBufferPointer / 4;
   }

   ByteBuffer bufferSlice(int pStartPointer, int pEndPointer) {
      return MemoryUtil.memSlice(this.buffer, pStartPointer, pEndPointer - pStartPointer);
   }

   public static record DrawState(VertexFormat format, int vertexCount, int indexCount, VertexFormat.Mode drawMode, VertexFormat.IndexType indexType, boolean indexOnly, boolean sequentialIndex, MultiTextureData multiTextureData) {

      public DrawState(VertexFormat format, int vertexCount, int indexCount, VertexFormat.Mode mode, VertexFormat.IndexType indexType, boolean indexOnly, boolean sequentialIndex) {
         this(format, vertexCount, indexCount, mode, indexType, indexOnly, sequentialIndex, (MultiTextureData)null);
      }

      public MultiTextureData getMultiTextureData() {
         return this.multiTextureData;
      }

      public int vertexBufferSize() {
         return this.vertexCount * this.format.getVertexSize();
      }

      public int vertexBufferStart() {
         return 0;
      }

      public int vertexBufferEnd() {
         return this.vertexBufferSize();
      }

      public int indexBufferStart() {
         return this.indexOnly ? 0 : this.vertexBufferEnd();
      }

      public int indexBufferEnd() {
         return this.indexBufferStart() + this.indexBufferSize();
      }

      private int indexBufferSize() {
         return this.sequentialIndex ? 0 : this.indexCount * this.indexType.bytes;
      }

      public int bufferSize() {
         return this.indexBufferEnd();
      }

      public VertexFormat format() {
         return this.format;
      }

      public int vertexCount() {
         return this.vertexCount;
      }

      public int indexCount() {
         return this.indexCount;
      }

      public VertexFormat.Mode mode() {
         return this.drawMode;
      }

      public VertexFormat.IndexType indexType() {
         return this.indexType;
      }

      public boolean indexOnly() {
         return this.indexOnly;
      }

      public boolean sequentialIndex() {
         return this.sequentialIndex;
      }
   }

   public class RenderedBuffer {
      private final int pointer;
      private final BufferBuilder.DrawState drawState;
      private boolean released;

      RenderedBuffer(int pPointer, BufferBuilder.DrawState pDrawState) {
         this.pointer = pPointer;
         this.drawState = pDrawState;
      }

      public ByteBuffer vertexBuffer() {
         int i = this.pointer + this.drawState.vertexBufferStart();
         int j = this.pointer + this.drawState.vertexBufferEnd();
         return BufferBuilder.this.bufferSlice(i, j);
      }

      public ByteBuffer indexBuffer() {
         int i = this.pointer + this.drawState.indexBufferStart();
         int j = this.pointer + this.drawState.indexBufferEnd();
         return BufferBuilder.this.bufferSlice(i, j);
      }

      public BufferBuilder.DrawState drawState() {
         return this.drawState;
      }

      public boolean isEmpty() {
         return this.drawState.vertexCount == 0;
      }

      public void release() {
         if (this.released) {
            throw new IllegalStateException("Buffer has already been released!");
         } else {
            BufferBuilder.this.releaseRenderedBuffer();
            this.released = true;
         }
      }
   }

   public static class SortState {
      final VertexFormat.Mode mode;
      final int vertices;
      @Nullable
      final Vector3f[] sortingPoints;
      @Nullable
      final VertexSorting sorting;
      private TextureAtlasSprite[] quadSprites;

      private SortState(VertexFormat.Mode modeIn, int verticesIn, @Nullable Vector3f[] sortingPointsIn, VertexSorting sortingIn, TextureAtlasSprite[] quadSpritesIn) {
         this(modeIn, verticesIn, sortingPointsIn, sortingIn);
         this.quadSprites = quadSpritesIn;
      }

      SortState(VertexFormat.Mode pMode, int pVertices, @Nullable Vector3f[] pSortingPoints, @Nullable VertexSorting pSorting) {
         this.mode = pMode;
         this.vertices = pVertices;
         this.sortingPoints = pSortingPoints;
         this.sorting = pSorting;
      }
   }
}
