package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;

public class VertexFormatElement {
   private final VertexFormatElement.Type type;
   private final VertexFormatElement.Usage usage;
   private final int index;
   private final int count;
   private final int byteSize;
   private String name;
   private int attributeIndex = -1;

   public VertexFormatElement(int pIndex, VertexFormatElement.Type pType, VertexFormatElement.Usage pUsage, int pCount) {
      if (this.supportsUsage(pIndex, pUsage)) {
         this.usage = pUsage;
         this.type = pType;
         this.index = pIndex;
         this.count = pCount;
         this.byteSize = pType.getSize() * this.count;
      } else {
         throw new IllegalStateException("Multiple vertex elements of the same type other than UVs are not supported");
      }
   }

   private boolean supportsUsage(int pIndex, VertexFormatElement.Usage pUsage) {
      return pIndex == 0 || pUsage == VertexFormatElement.Usage.UV;
   }

   public final VertexFormatElement.Type getType() {
      return this.type;
   }

   public final VertexFormatElement.Usage getUsage() {
      return this.usage;
   }

   public final int getCount() {
      return this.count;
   }

   public final int getIndex() {
      return this.index;
   }

   public String toString() {
      return this.name != null ? this.name : this.count + "," + this.usage.getName() + "," + this.type.getName();
   }

   public final int getByteSize() {
      return this.byteSize;
   }

   public final boolean isPosition() {
      return this.usage == VertexFormatElement.Usage.POSITION;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther != null && this.getClass() == pOther.getClass()) {
         VertexFormatElement vertexformatelement = (VertexFormatElement)pOther;
         if (this.count != vertexformatelement.count) {
            return false;
         } else if (this.index != vertexformatelement.index) {
            return false;
         } else if (this.type != vertexformatelement.type) {
            return false;
         } else {
            return this.usage == vertexformatelement.usage;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int i = this.type.hashCode();
      i = 31 * i + this.usage.hashCode();
      i = 31 * i + this.index;
      return 31 * i + this.count;
   }

   public void setupBufferState(int pStateIndex, long pOffset, int pStride) {
      this.usage.setupBufferState(this.count, this.type.getGlType(), pStride, pOffset, this.index, pStateIndex);
   }

   public void clearBufferState(int pElementIndex) {
      this.usage.clearBufferState(this.index, pElementIndex);
   }

   public final int getElementCount() {
      return this.count;
   }

   public String getName() {
      return this.name;
   }

   public VertexFormatElement setName(String name) {
      this.name = name;
      return this;
   }

   public int getAttributeIndex() {
      return this.attributeIndex;
   }

   public void setAttributeIndex(int attributeIndex) {
      this.attributeIndex = attributeIndex;
   }

   public int getAttributeIndex(int elementIndex) {
      return this.attributeIndex;
   }

   public static enum Type {
      FLOAT(4, "Float", 5126),
      UBYTE(1, "Unsigned Byte", 5121),
      BYTE(1, "Byte", 5120),
      USHORT(2, "Unsigned Short", 5123),
      SHORT(2, "Short", 5122),
      UINT(4, "Unsigned Int", 5125),
      INT(4, "Int", 5124);

      private final int size;
      private final String name;
      private final int glType;

      private Type(int pSize, String pName, int pGlType) {
         this.size = pSize;
         this.name = pName;
         this.glType = pGlType;
      }

      public int getSize() {
         return this.size;
      }

      public String getName() {
         return this.name;
      }

      public int getGlType() {
         return this.glType;
      }
   }

   public static enum Usage {
      POSITION("Position", (p_167042_0_, p_167042_1_, p_167042_2_, p_167042_3_, p_167042_5_, p_167042_6_) -> {
         GlStateManager._enableVertexAttribArray(p_167042_6_);
         GlStateManager._vertexAttribPointer(p_167042_6_, p_167042_0_, p_167042_1_, false, p_167042_2_, p_167042_3_);
      }, (p_167039_0_, p_167039_1_) -> {
         GlStateManager._disableVertexAttribArray(p_167039_1_);
      }),
      NORMAL("Normal", (p_167032_0_, p_167032_1_, p_167032_2_, p_167032_3_, p_167032_5_, p_167032_6_) -> {
         GlStateManager._enableVertexAttribArray(p_167032_6_);
         GlStateManager._vertexAttribPointer(p_167032_6_, p_167032_0_, p_167032_1_, true, p_167032_2_, p_167032_3_);
      }, (p_167029_0_, p_167029_1_) -> {
         GlStateManager._disableVertexAttribArray(p_167029_1_);
      }),
      COLOR("Vertex Color", (p_167022_0_, p_167022_1_, p_167022_2_, p_167022_3_, p_167022_5_, p_167022_6_) -> {
         GlStateManager._enableVertexAttribArray(p_167022_6_);
         GlStateManager._vertexAttribPointer(p_167022_6_, p_167022_0_, p_167022_1_, true, p_167022_2_, p_167022_3_);
      }, (p_167019_0_, p_167019_1_) -> {
         GlStateManager._disableVertexAttribArray(p_167019_1_);
      }),
      UV("UV", (p_167012_0_, p_167012_1_, p_167012_2_, p_167012_3_, p_167012_5_, p_167012_6_) -> {
         GlStateManager._enableVertexAttribArray(p_167012_6_);
         if (p_167012_1_ == 5126) {
            GlStateManager._vertexAttribPointer(p_167012_6_, p_167012_0_, p_167012_1_, false, p_167012_2_, p_167012_3_);
         } else {
            GlStateManager._vertexAttribIPointer(p_167012_6_, p_167012_0_, p_167012_1_, p_167012_2_, p_167012_3_);
         }

      }, (p_167009_0_, p_167009_1_) -> {
         GlStateManager._disableVertexAttribArray(p_167009_1_);
      }),
      PADDING("Padding", (p_167002_0_, p_167002_1_, p_167002_2_, p_167002_3_, p_167002_5_, p_167002_6_) -> {
      }, (p_166999_0_, p_166999_1_) -> {
      }),
      GENERIC("Generic", (p_166992_0_, p_166992_1_, p_166992_2_, p_166992_3_, p_166992_5_, p_166992_6_) -> {
         GlStateManager._enableVertexAttribArray(p_166992_6_);
         GlStateManager._vertexAttribPointer(p_166992_6_, p_166992_0_, p_166992_1_, false, p_166992_2_, p_166992_3_);
      }, (p_166989_0_, p_166989_1_) -> {
         GlStateManager._disableVertexAttribArray(p_166989_1_);
      });

      private final String name;
      private final VertexFormatElement.Usage.SetupState setupState;
      private final VertexFormatElement.Usage.ClearState clearState;

      private Usage(String pName, VertexFormatElement.Usage.SetupState pSetupState, VertexFormatElement.Usage.ClearState pClearState) {
         this.name = pName;
         this.setupState = pSetupState;
         this.clearState = pClearState;
      }

      void setupBufferState(int pCount, int pGlType, int pStride, long pOffset, int pIndex, int pStateIndex) {
         this.setupState.setupBufferState(pCount, pGlType, pStride, pOffset, pIndex, pStateIndex);
      }

      public void clearBufferState(int pIndex, int pElementIndex) {
         this.clearState.clearBufferState(pIndex, pElementIndex);
      }

      public String getName() {
         return this.name;
      }

      @FunctionalInterface
      interface ClearState {
         void clearBufferState(int pIndex, int pElementIndex);
      }

      @FunctionalInterface
      interface SetupState {
         void setupBufferState(int pCount, int pGlType, int pStride, long pOffset, int pIndex, int pStateIndex);
      }
   }
}