package net.optifine.entity.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class RendererCache {
   private Map<String, EntityRenderer> mapEntityRenderers = new HashMap<>();
   private Map<String, BlockEntityRenderer> mapBlockEntityRenderers = new HashMap<>();

   public EntityRenderer get(EntityType type, int index, Supplier<EntityRenderer> supplier) {
      String s = BuiltInRegistries.ENTITY_TYPE.getKey(type) + ":" + index;
      EntityRenderer entityrenderer = this.mapEntityRenderers.get(s);
      if (entityrenderer == null) {
         entityrenderer = supplier.get();
         this.mapEntityRenderers.put(s, entityrenderer);
      }

      return entityrenderer;
   }

   public BlockEntityRenderer get(BlockEntityType type, int index, Supplier<BlockEntityRenderer> supplier) {
      String s = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type) + ":" + index;
      BlockEntityRenderer blockentityrenderer = this.mapBlockEntityRenderers.get(s);
      if (blockentityrenderer == null) {
         blockentityrenderer = supplier.get();
         this.mapBlockEntityRenderers.put(s, blockentityrenderer);
      }

      return blockentityrenderer;
   }

   public void put(EntityType type, int index, EntityRenderer renderer) {
      String s = BuiltInRegistries.ENTITY_TYPE.getKey(type) + ":" + index;
      this.mapEntityRenderers.put(s, renderer);
   }

   public void put(BlockEntityType type, int index, BlockEntityRenderer renderer) {
      String s = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type) + ":" + index;
      this.mapBlockEntityRenderers.put(s, renderer);
   }

   public void clear() {
      this.mapEntityRenderers.clear();
      this.mapBlockEntityRenderers.clear();
   }
}