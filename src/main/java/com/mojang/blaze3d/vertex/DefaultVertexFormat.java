package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableMap;
import net.optifine.Config;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.SVertexFormat;

public class DefaultVertexFormat {
   public static final VertexFormatElement ELEMENT_POSITION = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
   public static final VertexFormatElement ELEMENT_COLOR = new VertexFormatElement(0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.COLOR, 4);
   public static final VertexFormatElement ELEMENT_UV0 = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 2);
   public static final VertexFormatElement ELEMENT_UV1 = new VertexFormatElement(1, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.UV, 2);
   public static final VertexFormatElement ELEMENT_UV2 = new VertexFormatElement(2, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.UV, 2);
   public static final VertexFormatElement ELEMENT_NORMAL = new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.NORMAL, 3);
   public static final VertexFormatElement ELEMENT_PADDING = new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.PADDING, 1);
   public static final VertexFormatElement ELEMENT_UV = ELEMENT_UV0;
   public static final VertexFormat BLIT_SCREEN = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("UV", ELEMENT_UV).put("Color", ELEMENT_COLOR).build());
   public static final VertexFormat BLOCK = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("Color", ELEMENT_COLOR).put("UV0", ELEMENT_UV0).put("UV2", ELEMENT_UV2).put("Normal", ELEMENT_NORMAL).put("Padding", ELEMENT_PADDING).build());
   public static final VertexFormat NEW_ENTITY = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("Color", ELEMENT_COLOR).put("UV0", ELEMENT_UV0).put("UV1", ELEMENT_UV1).put("UV2", ELEMENT_UV2).put("Normal", ELEMENT_NORMAL).put("Padding", ELEMENT_PADDING).build());
   public static final VertexFormat BLOCK_VANILLA = BLOCK.duplicate();
   public static final VertexFormat BLOCK_SHADERS = SVertexFormat.makeExtendedFormatBlock(BLOCK_VANILLA);
   public static final int BLOCK_VANILLA_SIZE = BLOCK_VANILLA.getVertexSize();
   public static final int BLOCK_SHADERS_SIZE = BLOCK_SHADERS.getVertexSize();
   public static final VertexFormat ENTITY_VANILLA = NEW_ENTITY.duplicate();
   public static final VertexFormat ENTITY_SHADERS = SVertexFormat.makeExtendedFormatEntity(ENTITY_VANILLA);
   public static final int ENTITY_VANILLA_SIZE = ENTITY_VANILLA.getVertexSize();
   public static final int ENTITY_SHADERS_SIZE = ENTITY_SHADERS.getVertexSize();
   public static final VertexFormat PARTICLE = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("UV0", ELEMENT_UV0).put("Color", ELEMENT_COLOR).put("UV2", ELEMENT_UV2).build());
   public static final VertexFormat POSITION = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).build());
   public static final VertexFormat POSITION_COLOR = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("Color", ELEMENT_COLOR).build());
   public static final VertexFormat POSITION_COLOR_NORMAL = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("Color", ELEMENT_COLOR).put("Normal", ELEMENT_NORMAL).put("Padding", ELEMENT_PADDING).build());
   public static final VertexFormat POSITION_COLOR_LIGHTMAP = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("Color", ELEMENT_COLOR).put("UV2", ELEMENT_UV2).build());
   public static final VertexFormat POSITION_TEX = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("UV0", ELEMENT_UV0).build());
   public static final VertexFormat POSITION_COLOR_TEX = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("Color", ELEMENT_COLOR).put("UV0", ELEMENT_UV0).build());
   public static final VertexFormat POSITION_TEX_COLOR = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("UV0", ELEMENT_UV0).put("Color", ELEMENT_COLOR).build());
   public static final VertexFormat POSITION_COLOR_TEX_LIGHTMAP = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("Color", ELEMENT_COLOR).put("UV0", ELEMENT_UV0).put("UV2", ELEMENT_UV2).build());
   public static final VertexFormat POSITION_TEX_LIGHTMAP_COLOR = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("UV0", ELEMENT_UV0).put("UV2", ELEMENT_UV2).put("Color", ELEMENT_COLOR).build());
   public static final VertexFormat POSITION_TEX_COLOR_NORMAL = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("UV0", ELEMENT_UV0).put("Color", ELEMENT_COLOR).put("Normal", ELEMENT_NORMAL).put("Padding", ELEMENT_PADDING).build());

   public static void updateVertexFormats() {
      if (Config.isShaders()) {
         BLOCK.copyFrom(BLOCK_SHADERS);
         NEW_ENTITY.copyFrom(ENTITY_SHADERS);
      } else {
         BLOCK.copyFrom(BLOCK_VANILLA);
         NEW_ENTITY.copyFrom(ENTITY_VANILLA);
      }

      if (Reflector.IQuadTransformer.exists()) {
         int i = BLOCK.getIntegerSize();
         Reflector.IQuadTransformer_STRIDE.setStaticIntUnsafe(i);
         Reflector.QuadBakingVertexConsumer_QUAD_DATA_SIZE.setStaticIntUnsafe(i * 4);
      }

   }

   static {
      ELEMENT_POSITION.setName("POSITION_3F").setAttributeIndex(0);
      ELEMENT_COLOR.setName("COLOR_4UB").setAttributeIndex(1);
      ELEMENT_UV0.setName("TEX_2F").setAttributeIndex(2);
      ELEMENT_UV1.setName("TEX_2S").setAttributeIndex(3);
      ELEMENT_UV2.setName("TEX_2SB").setAttributeIndex(4);
      ELEMENT_NORMAL.setName("NORMAL_3B").setAttributeIndex(5);
      ELEMENT_PADDING.setName("PADDING_1B");
      BLIT_SCREEN.setName("BLIT_SCREEN");
      BLOCK.setName("BLOCK");
      NEW_ENTITY.setName("ENTITY");
      BLOCK_SHADERS.setName("BLOCK_SHADERS");
      ENTITY_SHADERS.setName("ENTITY_SHADERS");
      PARTICLE.setName("PARTICLE_POSITION_TEX_COLOR_LMAP");
      POSITION.setName("POSITION");
      POSITION_COLOR.setName("POSITION_COLOR");
      POSITION_COLOR_NORMAL.setName("POSITION_COLOR_NORMAL");
      POSITION_COLOR_LIGHTMAP.setName("POSITION_COLOR_LIGHTMAP");
      POSITION_TEX.setName("POSITION_TEX");
      POSITION_COLOR_TEX.setName("POSITION_COLOR_TEX");
      POSITION_TEX_COLOR.setName("POSITION_TEX_COLOR");
      POSITION_COLOR_TEX_LIGHTMAP.setName("POSITION_COLOR_TEX_LIGHTMAP");
      POSITION_TEX_LIGHTMAP_COLOR.setName("POSITION_TEX_LIGHTMAP_COLOR");
      POSITION_TEX_COLOR_NORMAL.setName("POSITION_TEX_COLOR_NORMAL");
   }
}