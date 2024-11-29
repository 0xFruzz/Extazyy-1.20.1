package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.optifine.Config;
import net.optifine.util.BlockUtils;

public class BlockState extends BlockBehaviour.BlockStateBase implements IForgeBlockState {
   public static final Codec<BlockState> CODEC = codec(BuiltInRegistries.BLOCK.byNameCodec(), Block::defaultBlockState).stable();
   private int blockId = -1;
   private int metadata = -1;
   private ResourceLocation blockLocation;
   private int blockStateId = -1;
   private static final AtomicInteger blockStateIdCounter = new AtomicInteger(0);

   public int getBlockId() {
      if (this.blockId < 0) {
         this.blockId = BuiltInRegistries.BLOCK.getId(this.getBlock());
      }

      return this.blockId;
   }

   public int getMetadata() {
      if (this.metadata < 0) {
         this.metadata = BlockUtils.getMetadata(this);
         if (this.metadata < 0) {
            Config.warn("Metadata not found, block: " + this.getBlockLocation());
            this.metadata = 0;
         }
      }

      return this.metadata;
   }

   public ResourceLocation getBlockLocation() {
      if (this.blockLocation == null) {
         this.blockLocation = BuiltInRegistries.BLOCK.getKey(this.getBlock());
      }

      return this.blockLocation;
   }

   public int getBlockStateId() {
      if (this.blockStateId < 0) {
         this.blockStateId = blockStateIdCounter.incrementAndGet();
      }

      return this.blockStateId;
   }

   public int getLightValue(BlockGetter world, BlockPos pos) {
      return this.getLightEmission();
   }

   public boolean isCacheOpaqueCube() {
      return this.cache != null && this.cache.solidRender;
   }

   public boolean isCacheOpaqueCollisionShape() {
      return this.cache != null && this.cache.isCollisionShapeFullBlock;
   }

   public BlockState(Block p_61042_, ImmutableMap<Property<?>, Comparable<?>> p_61043_, MapCodec<BlockState> p_61044_) {
      super(p_61042_, p_61043_, p_61044_);
   }

   protected BlockState asState() {
      return this;
   }
}