package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.IdentityHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.optifine.entity.model.IEntityRenderer;
import net.optifine.util.Either;

public interface BlockEntityRenderer<T extends BlockEntity> extends IEntityRenderer {
   IdentityHashMap<BlockEntityRenderer, BlockEntityType> CACHED_TYPES = new IdentityHashMap<>();

   void render(T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay);

   default boolean shouldRenderOffScreen(T pBlockEntity) {
      return false;
   }

   default int getViewDistance() {
      return 64;
   }

   default boolean shouldRender(T pBlockEntity, Vec3 pCameraPos) {
      return Vec3.atCenterOf(pBlockEntity.getBlockPos()).closerThan(pCameraPos, (double)this.getViewDistance());
   }

   default Either<EntityType, BlockEntityType> getType() {
      BlockEntityType blockentitytype = CACHED_TYPES.get(this);
      return blockentitytype == null ? null : Either.makeRight(blockentitytype);
   }

   default void setType(Either<EntityType, BlockEntityType> type) {
      CACHED_TYPES.put(this, type.getRight().get());
   }

   default ResourceLocation getLocationTextureCustom() {
      return null;
   }

   default void setLocationTextureCustom(ResourceLocation locationTextureCustom) {
   }
}