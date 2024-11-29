package net.minecraft.world.level.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class Block extends BlockBehaviour implements ItemLike {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Holder.Reference<Block> builtInRegistryHolder = BuiltInRegistries.BLOCK.createIntrusiveHolder(this);
   public static final IdMapper<BlockState> BLOCK_STATE_REGISTRY = new IdMapper<>();
   private static final LoadingCache<VoxelShape, Boolean> SHAPE_FULL_BLOCK_CACHE = CacheBuilder.newBuilder().maximumSize(512L).weakKeys().build(new CacheLoader<VoxelShape, Boolean>() {
      public Boolean load(VoxelShape p_49972_) {
         return !Shapes.joinIsNotEmpty(Shapes.block(), p_49972_, BooleanOp.NOT_SAME);
      }
   });
   public static final int UPDATE_NEIGHBORS = 1;
   public static final int UPDATE_CLIENTS = 2;
   public static final int UPDATE_INVISIBLE = 4;
   public static final int UPDATE_IMMEDIATE = 8;
   public static final int UPDATE_KNOWN_SHAPE = 16;
   public static final int UPDATE_SUPPRESS_DROPS = 32;
   public static final int UPDATE_MOVE_BY_PISTON = 64;
   public static final int UPDATE_NONE = 4;
   public static final int UPDATE_ALL = 3;
   public static final int UPDATE_ALL_IMMEDIATE = 11;
   public static final float INDESTRUCTIBLE = -1.0F;
   public static final float INSTANT = 0.0F;
   public static final int UPDATE_LIMIT = 512;
   protected final StateDefinition<Block, BlockState> stateDefinition;
   private BlockState defaultBlockState;
   @Nullable
   private String descriptionId;
   @Nullable
   private Item item;
   private static final int CACHE_SIZE = 2048;
   private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
      Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(2048, 0.25F) {
         protected void rehash(int p_49979_) {
         }
      };
      object2bytelinkedopenhashmap.defaultReturnValue((byte)127);
      return object2bytelinkedopenhashmap;
   });

   public static int getId(@Nullable BlockState pState) {
      if (pState == null) {
         return 0;
      } else {
         int i = BLOCK_STATE_REGISTRY.getId(pState);
         return i == -1 ? 0 : i;
      }
   }

   public static BlockState stateById(int pId) {
      BlockState blockstate = BLOCK_STATE_REGISTRY.byId(pId);
      return blockstate == null ? Blocks.AIR.defaultBlockState() : blockstate;
   }

   public static Block byItem(@Nullable Item pItem) {
      return pItem instanceof BlockItem ? ((BlockItem)pItem).getBlock() : Blocks.AIR;
   }

   public static BlockState pushEntitiesUp(BlockState pOldState, BlockState pNewState, LevelAccessor pLevel, BlockPos pPos) {
      VoxelShape voxelshape = Shapes.joinUnoptimized(pOldState.getCollisionShape(pLevel, pPos), pNewState.getCollisionShape(pLevel, pPos), BooleanOp.ONLY_SECOND).move((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ());
      if (voxelshape.isEmpty()) {
         return pNewState;
      } else {
         for(Entity entity : pLevel.getEntities((Entity)null, voxelshape.bounds())) {
            double d0 = Shapes.collide(Direction.Axis.Y, entity.getBoundingBox().move(0.0D, 1.0D, 0.0D), List.of(voxelshape), -1.0D);
            entity.teleportRelative(0.0D, 1.0D + d0, 0.0D);
         }

         return pNewState;
      }
   }

   public static VoxelShape box(double pX1, double pY1, double pZ1, double pX2, double pY2, double pZ2) {
      return Shapes.box(pX1 / 16.0D, pY1 / 16.0D, pZ1 / 16.0D, pX2 / 16.0D, pY2 / 16.0D, pZ2 / 16.0D);
   }

   public static BlockState updateFromNeighbourShapes(BlockState pCurrentState, LevelAccessor pLevel, BlockPos pPos) {
      BlockState blockstate = pCurrentState;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(Direction direction : UPDATE_SHAPE_ORDER) {
         blockpos$mutableblockpos.setWithOffset(pPos, direction);
         blockstate = blockstate.updateShape(direction, pLevel.getBlockState(blockpos$mutableblockpos), pLevel, pPos, blockpos$mutableblockpos);
      }

      return blockstate;
   }

   public static void updateOrDestroy(BlockState pOldState, BlockState pNewState, LevelAccessor pLevel, BlockPos pPos, int pFlags) {
      updateOrDestroy(pOldState, pNewState, pLevel, pPos, pFlags, 512);
   }

   public static void updateOrDestroy(BlockState pOldState, BlockState pNewState, LevelAccessor pLevel, BlockPos pPos, int pFlags, int pRecursionLeft) {
      if (pNewState != pOldState) {
         if (pNewState.isAir()) {
            if (!pLevel.isClientSide()) {
               pLevel.destroyBlock(pPos, (pFlags & 32) == 0, (Entity)null, pRecursionLeft);
            }
         } else {
            pLevel.setBlock(pPos, pNewState, pFlags & -33, pRecursionLeft);
         }
      }

   }

   public Block(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(this);
      this.createBlockStateDefinition(builder);
      this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
      this.registerDefaultState(this.stateDefinition.any());
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         String s = this.getClass().getSimpleName();
         if (!s.endsWith("Block")) {
            LOGGER.error("Block classes should end with Block and {} doesn't.", (Object)s);
         }
      }

   }

   public static boolean isExceptionForConnection(BlockState pState) {
      return pState.getBlock() instanceof LeavesBlock || pState.is(Blocks.BARRIER) || pState.is(Blocks.CARVED_PUMPKIN) || pState.is(Blocks.JACK_O_LANTERN) || pState.is(Blocks.MELON) || pState.is(Blocks.PUMPKIN) || pState.is(BlockTags.SHULKER_BOXES);
   }

   public boolean isRandomlyTicking(BlockState pState) {
      return this.isRandomlyTicking;
   }

   public static boolean shouldRenderFace(BlockState pState, BlockGetter pLevel, BlockPos pOffset, Direction pFace, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      if (pState.skipRendering(blockstate, pFace)) {
         return false;
      } else if (blockstate.canOcclude()) {
         Block.BlockStatePairKey block$blockstatepairkey = new Block.BlockStatePairKey(pState, blockstate, pFace);
         Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = OCCLUSION_CACHE.get();
         byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$blockstatepairkey);
         if (b0 != 127) {
            return b0 != 0;
         } else {
            VoxelShape voxelshape = pState.getFaceOcclusionShape(pLevel, pOffset, pFace);
            if (voxelshape.isEmpty()) {
               return true;
            } else {
               VoxelShape voxelshape1 = blockstate.getFaceOcclusionShape(pLevel, pPos, pFace.getOpposite());
               boolean flag = Shapes.joinIsNotEmpty(voxelshape, voxelshape1, BooleanOp.ONLY_FIRST);
               if (object2bytelinkedopenhashmap.size() == 2048) {
                  object2bytelinkedopenhashmap.removeLastByte();
               }

               object2bytelinkedopenhashmap.putAndMoveToFirst(block$blockstatepairkey, (byte)(flag ? 1 : 0));
               return flag;
            }
         }
      } else {
         return true;
      }
   }

   public static boolean canSupportRigidBlock(BlockGetter pLevel, BlockPos pPos) {
      return pLevel.getBlockState(pPos).isFaceSturdy(pLevel, pPos, Direction.UP, SupportType.RIGID);
   }

   public static boolean canSupportCenter(LevelReader pLevel, BlockPos pPos, Direction pDirection) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return pDirection == Direction.DOWN && blockstate.is(BlockTags.UNSTABLE_BOTTOM_CENTER) ? false : blockstate.isFaceSturdy(pLevel, pPos, pDirection, SupportType.CENTER);
   }

   public static boolean isFaceFull(VoxelShape pShape, Direction pFace) {
      VoxelShape voxelshape = pShape.getFaceShape(pFace);
      return isShapeFullBlock(voxelshape);
   }

   public static boolean isShapeFullBlock(VoxelShape pShape) {
      return SHAPE_FULL_BLOCK_CACHE.getUnchecked(pShape);
   }

   public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return !isShapeFullBlock(pState.getShape(pLevel, pPos)) && pState.getFluidState().isEmpty();
   }

   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
   }

   public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
   }

   public static List<ItemStack> getDrops(BlockState pState, ServerLevel pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity) {
      LootParams.Builder lootparams$builder = (new LootParams.Builder(pLevel)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, pBlockEntity);
      return pState.getDrops(lootparams$builder);
   }

   public static List<ItemStack> getDrops(BlockState pState, ServerLevel pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity, @Nullable Entity pEntity, ItemStack pTool) {
      LootParams.Builder lootparams$builder = (new LootParams.Builder(pLevel)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pPos)).withParameter(LootContextParams.TOOL, pTool).withOptionalParameter(LootContextParams.THIS_ENTITY, pEntity).withOptionalParameter(LootContextParams.BLOCK_ENTITY, pBlockEntity);
      return pState.getDrops(lootparams$builder);
   }

   public static void dropResources(BlockState pState, Level pLevel, BlockPos pPos) {
      if (pLevel instanceof ServerLevel) {
         getDrops(pState, (ServerLevel)pLevel, pPos, (BlockEntity)null).forEach((p_152406_) -> {
            popResource(pLevel, pPos, p_152406_);
         });
         pState.spawnAfterBreak((ServerLevel)pLevel, pPos, ItemStack.EMPTY, true);
      }

   }

   public static void dropResources(BlockState pState, LevelAccessor pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity) {
      if (pLevel instanceof ServerLevel) {
         getDrops(pState, (ServerLevel)pLevel, pPos, pBlockEntity).forEach((p_49859_) -> {
            popResource((ServerLevel)pLevel, pPos, p_49859_);
         });
         pState.spawnAfterBreak((ServerLevel)pLevel, pPos, ItemStack.EMPTY, true);
      }

   }

   public static void dropResources(BlockState pState, Level pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity, @Nullable Entity pEntity, ItemStack pTool) {
      if (pLevel instanceof ServerLevel) {
         getDrops(pState, (ServerLevel)pLevel, pPos, pBlockEntity, pEntity, pTool).forEach((p_49944_) -> {
            popResource(pLevel, pPos, p_49944_);
         });
         pState.spawnAfterBreak((ServerLevel)pLevel, pPos, pTool, true);
      }

   }

   public static void popResource(Level pLevel, BlockPos pPos, ItemStack pStack) {
      double d0 = (double)EntityType.ITEM.getHeight() / 2.0D;
      double d1 = (double)pPos.getX() + 0.5D + Mth.nextDouble(pLevel.random, -0.25D, 0.25D);
      double d2 = (double)pPos.getY() + 0.5D + Mth.nextDouble(pLevel.random, -0.25D, 0.25D) - d0;
      double d3 = (double)pPos.getZ() + 0.5D + Mth.nextDouble(pLevel.random, -0.25D, 0.25D);
      popResource(pLevel, () -> {
         return new ItemEntity(pLevel, d1, d2, d3, pStack);
      }, pStack);
   }

   public static void popResourceFromFace(Level pLevel, BlockPos pPos, Direction pDirection, ItemStack pStack) {
      int i = pDirection.getStepX();
      int j = pDirection.getStepY();
      int k = pDirection.getStepZ();
      double d0 = (double)EntityType.ITEM.getWidth() / 2.0D;
      double d1 = (double)EntityType.ITEM.getHeight() / 2.0D;
      double d2 = (double)pPos.getX() + 0.5D + (i == 0 ? Mth.nextDouble(pLevel.random, -0.25D, 0.25D) : (double)i * (0.5D + d0));
      double d3 = (double)pPos.getY() + 0.5D + (j == 0 ? Mth.nextDouble(pLevel.random, -0.25D, 0.25D) : (double)j * (0.5D + d1)) - d1;
      double d4 = (double)pPos.getZ() + 0.5D + (k == 0 ? Mth.nextDouble(pLevel.random, -0.25D, 0.25D) : (double)k * (0.5D + d0));
      double d5 = i == 0 ? Mth.nextDouble(pLevel.random, -0.1D, 0.1D) : (double)i * 0.1D;
      double d6 = j == 0 ? Mth.nextDouble(pLevel.random, 0.0D, 0.1D) : (double)j * 0.1D + 0.1D;
      double d7 = k == 0 ? Mth.nextDouble(pLevel.random, -0.1D, 0.1D) : (double)k * 0.1D;
      popResource(pLevel, () -> {
         return new ItemEntity(pLevel, d2, d3, d4, pStack, d5, d6, d7);
      }, pStack);
   }

   private static void popResource(Level pLevel, Supplier<ItemEntity> pItemEntitySupplier, ItemStack pStack) {
      if (!pLevel.isClientSide && !pStack.isEmpty() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
         ItemEntity itementity = pItemEntitySupplier.get();
         itementity.setDefaultPickUpDelay();
         pLevel.addFreshEntity(itementity);
      }
   }

   protected void popExperience(ServerLevel pLevel, BlockPos pPos, int pAmount) {
      if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
         ExperienceOrb.award(pLevel, Vec3.atCenterOf(pPos), pAmount);
      }

   }

   public float getExplosionResistance() {
      return this.explosionResistance;
   }

   public void wasExploded(Level pLevel, BlockPos pPos, Explosion pExplosion) {
   }

   public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return this.defaultBlockState();
   }

   public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @Nullable BlockEntity pBlockEntity, ItemStack pTool) {
      pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
      pPlayer.causeFoodExhaustion(0.005F);
      dropResources(pState, pLevel, pPos, pBlockEntity, pPlayer, pTool);
   }

   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
   }

   public boolean isPossibleToRespawnInThis(BlockState pState) {
      return !pState.isSolid() && !pState.liquid();
   }

   public MutableComponent getName() {
      return Component.translatable(this.getDescriptionId());
   }

   public String getDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("block", BuiltInRegistries.BLOCK.getKey(this));
      }

      return this.descriptionId;
   }

   public void fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance) {
      pEntity.causeFallDamage(pFallDistance, 1.0F, pEntity.damageSources().fall());
   }

   public void updateEntityAfterFallOn(BlockGetter pLevel, Entity pEntity) {
      pEntity.setDeltaMovement(pEntity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return new ItemStack(this);
   }

   public float getFriction() {
      return this.friction;
   }

   public float getSpeedFactor() {
      return this.speedFactor;
   }

   public float getJumpFactor() {
      return this.jumpFactor;
   }

   protected void spawnDestroyParticles(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState) {
      pLevel.levelEvent(pPlayer, 2001, pPos, getId(pState));
   }

   public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      this.spawnDestroyParticles(pLevel, pPlayer, pPos, pState);
      if (pState.is(BlockTags.GUARDED_BY_PIGLINS)) {
         PiglinAi.angerNearbyPiglins(pPlayer, false);
      }

      pLevel.gameEvent(GameEvent.BLOCK_DESTROY, pPos, GameEvent.Context.of(pPlayer, pState));
   }

   public void handlePrecipitation(BlockState pState, Level pLevel, BlockPos pPos, Biome.Precipitation pPrecipitation) {
   }

   public boolean dropFromExplosion(Explosion pExplosion) {
      return true;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
   }

   public StateDefinition<Block, BlockState> getStateDefinition() {
      return this.stateDefinition;
   }

   protected final void registerDefaultState(BlockState pState) {
      this.defaultBlockState = pState;
   }

   public final BlockState defaultBlockState() {
      return this.defaultBlockState;
   }

   public final BlockState withPropertiesOf(BlockState pState) {
      BlockState blockstate = this.defaultBlockState();

      for(Property<?> property : pState.getBlock().getStateDefinition().getProperties()) {
         if (blockstate.hasProperty(property)) {
            blockstate = copyProperty(pState, blockstate, property);
         }
      }

      return blockstate;
   }

   private static <T extends Comparable<T>> BlockState copyProperty(BlockState pSourceState, BlockState pTargetState, Property<T> pProperty) {
      return pTargetState.setValue(pProperty, pSourceState.getValue(pProperty));
   }

   public SoundType getSoundType(BlockState pState) {
      return this.soundType;
   }

   public Item asItem() {
      if (this.item == null) {
         this.item = Item.byBlock(this);
      }

      return this.item;
   }

   public boolean hasDynamicShape() {
      return this.dynamicShape;
   }

   public String toString() {
      return "Block{" + BuiltInRegistries.BLOCK.getKey(this) + "}";
   }

   public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
   }

   protected Block asBlock() {
      return this;
   }

   protected ImmutableMap<BlockState, VoxelShape> getShapeForEachState(Function<BlockState, VoxelShape> pShapeGetter) {
      return this.stateDefinition.getPossibleStates().stream().collect(ImmutableMap.toImmutableMap(Function.identity(), pShapeGetter));
   }

   /** @deprecated */
   @Deprecated
   public Holder.Reference<Block> builtInRegistryHolder() {
      return this.builtInRegistryHolder;
   }

   protected void tryDropExperience(ServerLevel pLevel, BlockPos pPos, ItemStack pHeldItem, IntProvider pAmount) {
      if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, pHeldItem) == 0) {
         int i = pAmount.sample(pLevel.random);
         if (i > 0) {
            this.popExperience(pLevel, pPos, i);
         }
      }

   }

   public static final class BlockStatePairKey {
      private final BlockState first;
      private final BlockState second;
      private final Direction direction;

      public BlockStatePairKey(BlockState pFirst, BlockState pSecond, Direction pDirection) {
         this.first = pFirst;
         this.second = pSecond;
         this.direction = pDirection;
      }

      public boolean equals(Object pOther) {
         if (this == pOther) {
            return true;
         } else if (!(pOther instanceof Block.BlockStatePairKey)) {
            return false;
         } else {
            Block.BlockStatePairKey block$blockstatepairkey = (Block.BlockStatePairKey)pOther;
            return this.first == block$blockstatepairkey.first && this.second == block$blockstatepairkey.second && this.direction == block$blockstatepairkey.direction;
         }
      }

      public int hashCode() {
         int i = this.first.hashCode();
         i = 31 * i + this.second.hashCode();
         return 31 * i + this.direction.hashCode();
      }
   }
}