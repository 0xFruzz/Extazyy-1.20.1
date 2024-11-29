package net.optifine.util;

import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BufferUtil {
   public static String getBufferHex(BufferBuilder bb) {
      VertexFormat.Mode vertexformat$mode = bb.getDrawMode();
      String s = "";
      int i = -1;
      if (vertexformat$mode == VertexFormat.Mode.QUADS) {
         s = "quad";
         i = 4;
      } else {
         if (vertexformat$mode != VertexFormat.Mode.TRIANGLES) {
            return "Invalid draw mode: " + vertexformat$mode;
         }

         s = "triangle";
         i = 3;
      }

      StringBuffer stringbuffer = new StringBuffer();
      int j = bb.getVertexCount();

      for(int k = 0; k < j; ++k) {
         if (k % i == 0) {
            stringbuffer.append(s + " " + k / i + "\n");
         }

         String s1 = getVertexHex(k, bb);
         stringbuffer.append(s1);
         stringbuffer.append("\n");
      }

      return stringbuffer.toString();
   }

   private static String getVertexHex(int vertex, BufferBuilder bb) {
      StringBuffer stringbuffer = new StringBuffer();
      ByteBuffer bytebuffer = bb.getByteBuffer();
      VertexFormat vertexformat = bb.getVertexFormat();
      int i = bb.getStartPosition() + vertex * vertexformat.getVertexSize();

      for(VertexFormatElement vertexformatelement : vertexformat.getElements()) {
         if (vertexformatelement.getElementCount() > 0) {
            stringbuffer.append("(");
         }

         for(int j = 0; j < vertexformatelement.getElementCount(); ++j) {
            if (j > 0) {
               stringbuffer.append(" ");
            }

            switch (vertexformatelement.getType()) {
               case FLOAT:
                  stringbuffer.append(bytebuffer.getFloat(i));
                  break;
               case UBYTE:
               case BYTE:
                  stringbuffer.append((int)bytebuffer.get(i));
                  break;
               case USHORT:
               case SHORT:
                  stringbuffer.append((int)bytebuffer.getShort(i));
                  break;
               case UINT:
               case INT:
                  stringbuffer.append((int)bytebuffer.getShort(i));
                  break;
               default:
                  stringbuffer.append("??");
            }

            i += vertexformatelement.getType().getSize();
         }

         if (vertexformatelement.getElementCount() > 0) {
            stringbuffer.append(")");
         }
      }

      return stringbuffer.toString();
   }

   public static String getBufferString(IntBuffer buf) {
      if (buf == null) {
         return "null";
      } else {
         StringBuffer stringbuffer = new StringBuffer();
         stringbuffer.append("(pos=" + buf.position() + " lim=" + buf.limit() + " cap=" + buf.capacity() + ")");
         stringbuffer.append("[");
         int i = Math.min(buf.limit(), 1024);

         for(int j = 0; j < i; ++j) {
            if (j > 0) {
               stringbuffer.append(", ");
            }

            stringbuffer.append(buf.get(j));
         }

         stringbuffer.append("]");
         return stringbuffer.toString();
      }
   }

   public static int[] toArray(IntBuffer buf) {
      int[] aint = new int[buf.limit()];

      for(int i = 0; i < aint.length; ++i) {
         aint[i] = buf.get(i);
      }

      return aint;
   }

   public static FloatBuffer createDirectFloatBuffer(int capacity) {
      return MemoryTracker.create(capacity << 2).asFloatBuffer();
   }

   public static void fill(FloatBuffer buf, float val) {
      buf.clear();

      for(int i = 0; i < buf.capacity(); ++i) {
         buf.put(i, val);
      }

      buf.clear();
   }
}