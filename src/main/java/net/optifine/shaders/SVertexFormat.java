package net.optifine.shaders;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class SVertexFormat {
   public static final int vertexSizeBlock = 18;
   public static final int offsetMidBlock = 8;
   public static final int offsetMidTexCoord = 9;
   public static final int offsetTangent = 11;
   public static final int offsetEntity = 13;
   public static final int offsetVelocity = 15;
   public static final VertexFormatElement SHADERS_MIDBLOCK_3B = makeElement("SHADERS_MIDOFFSET_3B", 0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.PADDING, 3);
   public static final VertexFormatElement PADDING_1B = makeElement("PADDING_1B", 0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.PADDING, 1);
   public static final VertexFormatElement SHADERS_MIDTEXCOORD_2F = makeElement("SHADERS_MIDTEXCOORD_2F", 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.PADDING, 2);
   public static final VertexFormatElement SHADERS_TANGENT_4S = makeElement("SHADERS_TANGENT_4S", 0, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.PADDING, 4);
   public static final VertexFormatElement SHADERS_MC_ENTITY_4S = makeElement("SHADERS_MC_ENTITY_4S", 0, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.PADDING, 4);
   public static final VertexFormatElement SHADERS_VELOCITY_3F = makeElement("SHADERS_VELOCITY_3F", 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.PADDING, 3);

   public static VertexFormat makeExtendedFormatBlock(VertexFormat blockVanilla) {
      ImmutableMap.Builder<String, VertexFormatElement> builder = ImmutableMap.builder();
      builder.putAll(blockVanilla.getElementMapping());
      builder.put("MidOffset", SHADERS_MIDBLOCK_3B);
      builder.put("PaddingMO", PADDING_1B);
      builder.put("MidTexCoord", SHADERS_MIDTEXCOORD_2F);
      builder.put("Tangent", SHADERS_TANGENT_4S);
      builder.put("McEntity", SHADERS_MC_ENTITY_4S);
      builder.put("Velocity", SHADERS_VELOCITY_3F);
      return new VertexFormat(builder.build());
   }

   public static VertexFormat makeExtendedFormatEntity(VertexFormat entityVanilla) {
      ImmutableMap.Builder<String, VertexFormatElement> builder = ImmutableMap.builder();
      builder.putAll(entityVanilla.getElementMapping());
      builder.put("MidTexCoord", SHADERS_MIDTEXCOORD_2F);
      builder.put("Tangent", SHADERS_TANGENT_4S);
      builder.put("McEntity", SHADERS_MC_ENTITY_4S);
      builder.put("Velocity", SHADERS_VELOCITY_3F);
      return new VertexFormat(builder.build());
   }

   private static VertexFormatElement makeElement(String name, int indexIn, VertexFormatElement.Type typeIn, VertexFormatElement.Usage usageIn, int count) {
      VertexFormatElement vertexformatelement = new VertexFormatElement(indexIn, typeIn, usageIn, count);
      vertexformatelement.setName(name);
      return vertexformatelement;
   }
}