package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class VertexFormat {
   private ImmutableList<VertexFormatElement> elements;
   private ImmutableMap<String, VertexFormatElement> elementMapping;
   private IntList offsets = new IntArrayList();
   private int vertexSize;
   @Nullable
   private VertexBuffer immediateDrawVertexBuffer;
   private String name;
   private int positionElementOffset = -1;
   private int normalElementOffset = -1;
   private int colorElementOffset = -1;
   private Int2IntMap uvOffsetsById = new Int2IntArrayMap();

   public VertexFormat(ImmutableMap<String, VertexFormatElement> pElementMapping) {
      this.elementMapping = pElementMapping;
      this.elements = pElementMapping.values().asList();
      int i = 0;

      for(VertexFormatElement vertexformatelement : pElementMapping.values()) {
         this.offsets.add(i);
         VertexFormatElement.Usage vertexformatelement$usage = vertexformatelement.getUsage();
         if (vertexformatelement$usage == VertexFormatElement.Usage.POSITION) {
            this.positionElementOffset = i;
         } else if (vertexformatelement$usage == VertexFormatElement.Usage.NORMAL) {
            this.normalElementOffset = i;
         } else if (vertexformatelement$usage == VertexFormatElement.Usage.COLOR) {
            this.colorElementOffset = i;
         } else if (vertexformatelement$usage == VertexFormatElement.Usage.UV) {
            this.uvOffsetsById.put(vertexformatelement.getIndex(), i);
         }

         i += vertexformatelement.getByteSize();
      }

      this.vertexSize = i;
   }

   public String toString() {
      return "format: " + this.name + " " + this.elementMapping.size() + " elements: " + (String)this.elementMapping.entrySet().stream().map(Object::toString).collect(Collectors.joining(" "));
   }

   public int getIntegerSize() {
      return this.getVertexSize() / 4;
   }

   public int getVertexSize() {
      return this.vertexSize;
   }

   public ImmutableList<VertexFormatElement> getElements() {
      return this.elements;
   }

   public ImmutableList<String> getElementAttributeNames() {
      return this.elementMapping.keySet().asList();
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther != null && this.getClass() == pOther.getClass()) {
         VertexFormat vertexformat = (VertexFormat)pOther;
         return this.vertexSize != vertexformat.vertexSize ? false : this.elementMapping.equals(vertexformat.elementMapping);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.elementMapping.hashCode();
   }

   public void setupBufferState() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::_setupBufferState);
      } else {
         this._setupBufferState();
      }

   }

   private void _setupBufferState() {
      int i = this.getVertexSize();
      List<VertexFormatElement> list = this.getElements();

      for(int j = 0; j < list.size(); ++j) {
         VertexFormatElement vertexformatelement = list.get(j);
         int k = vertexformatelement.getAttributeIndex(j);
         if (k >= 0) {
            vertexformatelement.setupBufferState(k, (long)this.offsets.getInt(j), i);
         }
      }

   }

   public void clearBufferState() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::_clearBufferState);
      } else {
         this._clearBufferState();
      }

   }

   private void _clearBufferState() {
      ImmutableList<VertexFormatElement> immutablelist = this.getElements();

      for(int i = 0; i < immutablelist.size(); ++i) {
         VertexFormatElement vertexformatelement = immutablelist.get(i);
         int j = vertexformatelement.getAttributeIndex(i);
         if (j >= 0) {
            vertexformatelement.clearBufferState(j);
         }
      }

   }

   public VertexBuffer getImmediateDrawVertexBuffer() {
      VertexBuffer vertexbuffer = this.immediateDrawVertexBuffer;
      if (vertexbuffer == null) {
         this.immediateDrawVertexBuffer = vertexbuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
      }

      return vertexbuffer;
   }

   public int getOffset(int index) {
      return this.offsets.getInt(index);
   }

   public boolean hasPosition() {
      return this.positionElementOffset >= 0;
   }

   public int getPositionOffset() {
      return this.positionElementOffset;
   }

   public boolean hasNormal() {
      return this.normalElementOffset >= 0;
   }

   public int getNormalOffset() {
      return this.normalElementOffset;
   }

   public boolean hasColor() {
      return this.colorElementOffset >= 0;
   }

   public int getColorOffset() {
      return this.colorElementOffset;
   }

   public boolean hasUV(int id) {
      return this.uvOffsetsById.containsKey(id);
   }

   public int getUvOffsetById(int id) {
      return this.uvOffsetsById.get(id);
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void copyFrom(VertexFormat vf) {
      this.elements = vf.elements;
      this.elementMapping = vf.elementMapping;
      this.offsets = vf.offsets;
      this.vertexSize = vf.vertexSize;
      this.immediateDrawVertexBuffer = vf.immediateDrawVertexBuffer;
      this.name = vf.name;
      this.positionElementOffset = vf.positionElementOffset;
      this.normalElementOffset = vf.normalElementOffset;
      this.colorElementOffset = vf.colorElementOffset;
      this.uvOffsetsById = vf.uvOffsetsById;
   }

   public VertexFormat duplicate() {
      VertexFormat vertexformat = new VertexFormat(ImmutableMap.of());
      vertexformat.copyFrom(this);
      return vertexformat;
   }

   public ImmutableMap<String, VertexFormatElement> getElementMapping() {
      return this.elementMapping;
   }

   public static enum IndexType {
      SHORT(5123, 2),
      INT(5125, 4);

      public final int asGLType;
      public final int bytes;

      private IndexType(int pAsGLType, int pBytes) {
         this.asGLType = pAsGLType;
         this.bytes = pBytes;
      }

      public static VertexFormat.IndexType least(int pIndexCount) {
         return (pIndexCount & -65536) != 0 ? INT : SHORT;
      }
   }

   public static enum Mode {
      LINES(4, 2, 2, false),
      LINE_STRIP(5, 2, 1, true),
      DEBUG_LINES(1, 2, 2, false),
      DEBUG_LINE_STRIP(3, 2, 1, true),
      TRIANGLES(4, 3, 3, false),
      TRIANGLE_STRIP(5, 3, 1, true),
      TRIANGLE_FAN(6, 3, 1, true),
      QUADS(4, 4, 4, false);

      public final int asGLMode;
      public final int primitiveLength;
      public final int primitiveStride;
      public final boolean connectedPrimitives;

      private Mode(int pAsGLMode, int pPrimitiveLength, int pPrimitiveStride, boolean pConnectedPrimitives) {
         this.asGLMode = pAsGLMode;
         this.primitiveLength = pPrimitiveLength;
         this.primitiveStride = pPrimitiveStride;
         this.connectedPrimitives = pConnectedPrimitives;
      }

      public int indexCount(int pVertices) {
         int i;
         switch (this) {
            case LINE_STRIP:
            case DEBUG_LINES:
            case DEBUG_LINE_STRIP:
            case TRIANGLES:
            case TRIANGLE_STRIP:
            case TRIANGLE_FAN:
               i = pVertices;
               break;
            case LINES:
            case QUADS:
               i = pVertices / 4 * 6;
               break;
            default:
               i = 0;
         }

         return i;
      }
   }
}