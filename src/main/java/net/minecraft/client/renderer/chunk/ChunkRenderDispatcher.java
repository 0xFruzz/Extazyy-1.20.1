package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.optifine.BlockPosM;
import net.optifine.Config;
import net.optifine.CustomBlockLayers;
import net.optifine.override.ChunkCacheOF;
import net.optifine.reflect.Reflector;
import net.optifine.render.AabbFrame;
import net.optifine.render.ChunkLayerMap;
import net.optifine.render.ChunkLayerSet;
import net.optifine.render.ICamera;
import net.optifine.render.RenderEnv;
import net.optifine.render.RenderTypes;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.shaders.Shaders;
import net.optifine.util.ChunkUtils;
import net.optifine.util.SingleIterable;
import org.slf4j.Logger;

public class ChunkRenderDispatcher {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_WORKERS_32_BIT = 4;
   private static final VertexFormat VERTEX_FORMAT = DefaultVertexFormat.BLOCK;
   private static final int MAX_HIGH_PRIORITY_QUOTA = 2;
   private final PriorityBlockingQueue<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> toBatchHighPriority = Queues.newPriorityBlockingQueue();
   private final Queue<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> toBatchLowPriority = Queues.newLinkedBlockingDeque();
   private int highPriorityQuota = 2;
   private final Queue<ChunkBufferBuilderPack> freeBuffers;
   private final Queue<Runnable> toUpload = Queues.newConcurrentLinkedQueue();
   private volatile int toBatchCount;
   private volatile int freeBufferCount;
   final ChunkBufferBuilderPack fixedBuffers;
   private final ProcessorMailbox<Runnable> mailbox;
   private final Executor executor;
   ClientLevel level;
   final LevelRenderer renderer;
   private Vec3 camera = Vec3.ZERO;
   private int countRenderBuilders;
   private List<ChunkBufferBuilderPack> listPausedBuilders = new ArrayList<>();
   public static final RenderType[] BLOCK_RENDER_LAYERS = RenderType.chunkBufferLayers().toArray(new RenderType[0]);
   public static final RenderType[] BLOCK_RENDER_LAYERS_FORGE = RenderType.chunkBufferLayers().toArray(new RenderType[0]);
   private static final boolean FORGE = Reflector.ForgeHooksClient.exists();
   public static int renderChunksUpdated;

   public ChunkRenderDispatcher(ClientLevel pLevel, LevelRenderer pRenderer, Executor pExecutor, boolean pIs64Bit, ChunkBufferBuilderPack pFixedBuffers) {
      this(pLevel, pRenderer, pExecutor, pIs64Bit, pFixedBuffers, -1);
   }

   public ChunkRenderDispatcher(ClientLevel worldIn, LevelRenderer worldRendererIn, Executor executorIn, boolean java64bit, ChunkBufferBuilderPack fixedBuilderIn, int countRenderBuildersIn) {
      this.level = worldIn;
      this.renderer = worldRendererIn;
      int i = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3D) / (RenderType.chunkBufferLayers().stream().mapToInt(RenderType::bufferSize).sum() * 4) - 1);
      int j = Runtime.getRuntime().availableProcessors();
      int k = java64bit ? j : Math.min(j, 4);
      int l = Math.max(1, Math.min(k, i));
      if (countRenderBuildersIn > 0) {
         l = countRenderBuildersIn;
      }

      this.fixedBuffers = fixedBuilderIn;
      List<ChunkBufferBuilderPack> list = Lists.newArrayListWithExpectedSize(l);

      try {
         for(int i1 = 0; i1 < l; ++i1) {
            list.add(new ChunkBufferBuilderPack());
         }
      } catch (OutOfMemoryError outofmemoryerror1) {
         LOGGER.warn("Allocated only {}/{} buffers", list.size(), l);
         int j1 = Math.min(list.size() * 2 / 3, list.size() - 1);

         for(int k1 = 0; k1 < j1; ++k1) {
            list.remove(list.size() - 1);
         }

         System.gc();
      }

      this.freeBuffers = Queues.newConcurrentLinkedQueue(list);
      this.freeBufferCount = this.freeBuffers.size();
      this.countRenderBuilders = this.freeBufferCount;
      this.executor = executorIn;
      this.mailbox = ProcessorMailbox.create(executorIn, "Chunk Renderer");
      this.mailbox.tell(this::runTask);
   }

   public void setLevel(ClientLevel pLevel) {
      this.level = pLevel;
   }

   private void runTask() {
      if (!this.freeBuffers.isEmpty()) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.pollTask();
         if (chunkrenderdispatcher$renderchunk$chunkcompiletask != null) {
            ChunkBufferBuilderPack chunkbufferbuilderpack = this.freeBuffers.poll();
            if (chunkbufferbuilderpack == null) {
               this.toBatchHighPriority.add(chunkrenderdispatcher$renderchunk$chunkcompiletask);
               return;
            }

            this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
            this.freeBufferCount = this.freeBuffers.size();
            CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName(chunkrenderdispatcher$renderchunk$chunkcompiletask.name(), () -> {
               return chunkrenderdispatcher$renderchunk$chunkcompiletask.doTask(chunkbufferbuilderpack);
            }), this.executor).thenCompose((p_194415_0_) -> {
               return p_194415_0_;
            }).whenComplete((taskResultIn, throwableIn) -> {
               if (throwableIn != null) {
                  Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwableIn, "Batching chunks"));
               } else {
                  this.mailbox.tell(() -> {
                     if (taskResultIn == ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL) {
                        chunkbufferbuilderpack.clearAll();
                     } else {
                        chunkbufferbuilderpack.discardAll();
                     }

                     this.freeBuffers.add(chunkbufferbuilderpack);
                     this.freeBufferCount = this.freeBuffers.size();
                     this.runTask();
                  });
               }

            });
         }
      }

   }

   @Nullable
   private ChunkRenderDispatcher.RenderChunk.ChunkCompileTask pollTask() {
      if (this.highPriorityQuota <= 0) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.toBatchLowPriority.poll();
         if (chunkrenderdispatcher$renderchunk$chunkcompiletask != null) {
            this.highPriorityQuota = 2;
            return chunkrenderdispatcher$renderchunk$chunkcompiletask;
         }
      }

      ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask1 = this.toBatchHighPriority.poll();
      if (chunkrenderdispatcher$renderchunk$chunkcompiletask1 != null) {
         --this.highPriorityQuota;
         return chunkrenderdispatcher$renderchunk$chunkcompiletask1;
      } else {
         this.highPriorityQuota = 2;
         return this.toBatchLowPriority.poll();
      }
   }

   public String getStats() {
      return String.format(Locale.ROOT, "pC: %03d, pU: %02d, aB: %02d", this.toBatchCount, this.toUpload.size(), this.freeBufferCount);
   }

   public int getToBatchCount() {
      return this.toBatchCount;
   }

   public int getToUpload() {
      return this.toUpload.size();
   }

   public int getFreeBufferCount() {
      return this.freeBufferCount;
   }

   public void setCamera(Vec3 pCamera) {
      this.camera = pCamera;
   }

   public Vec3 getCameraPosition() {
      return this.camera;
   }

   public void uploadAllPendingUploads() {
      Runnable runnable;
      while((runnable = this.toUpload.poll()) != null) {
         runnable.run();
      }

   }

   public void rebuildChunkSync(ChunkRenderDispatcher.RenderChunk pChunk, RenderRegionCache pRegionCache) {
      pChunk.compileSync(pRegionCache);
   }

   public void blockUntilClear() {
      this.clearBatchQueue();
   }

   public void schedule(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask pTask) {
      this.mailbox.tell(() -> {
         if (pTask.isHighPriority) {
            this.toBatchHighPriority.offer(pTask);
         } else {
            this.toBatchLowPriority.offer(pTask);
         }

         this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
         this.runTask();
      });
   }

   public CompletableFuture<Void> uploadChunkLayer(BufferBuilder.RenderedBuffer pBuilder, VertexBuffer pBuffer) {
      return CompletableFuture.runAsync(() -> {
         if (!pBuffer.isInvalid()) {
            pBuffer.bind();
            pBuffer.upload(pBuilder);
            VertexBuffer.unbind();
         }

      }, this.toUpload::add);
   }

   private void clearBatchQueue() {
      while(!this.toBatchHighPriority.isEmpty()) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.toBatchHighPriority.poll();
         if (chunkrenderdispatcher$renderchunk$chunkcompiletask != null) {
            chunkrenderdispatcher$renderchunk$chunkcompiletask.cancel();
         }
      }

      while(!this.toBatchLowPriority.isEmpty()) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask1 = this.toBatchLowPriority.poll();
         if (chunkrenderdispatcher$renderchunk$chunkcompiletask1 != null) {
            chunkrenderdispatcher$renderchunk$chunkcompiletask1.cancel();
         }
      }

      this.toBatchCount = 0;
   }

   public boolean isQueueEmpty() {
      return this.toBatchCount == 0 && this.toUpload.isEmpty();
   }

   public void dispose() {
      this.clearBatchQueue();
      this.mailbox.close();
      this.freeBuffers.clear();
   }

   public void pauseChunkUpdates() {
      long i = System.currentTimeMillis();
      if (this.listPausedBuilders.size() <= 0) {
         while(this.listPausedBuilders.size() != this.countRenderBuilders) {
            this.uploadAllPendingUploads();
            ChunkBufferBuilderPack chunkbufferbuilderpack = this.freeBuffers.poll();
            if (chunkbufferbuilderpack != null) {
               this.listPausedBuilders.add(chunkbufferbuilderpack);
            }

            if (System.currentTimeMillis() > i + 1000L) {
               break;
            }
         }

      }
   }

   public void resumeChunkUpdates() {
      this.freeBuffers.addAll(this.listPausedBuilders);
      this.listPausedBuilders.clear();
   }

   public boolean updateChunkNow(ChunkRenderDispatcher.RenderChunk renderChunk, RenderRegionCache regionCacheIn) {
      this.rebuildChunkSync(renderChunk, regionCacheIn);
      return true;
   }

   public boolean updateChunkLater(ChunkRenderDispatcher.RenderChunk renderChunk, RenderRegionCache regionCacheIn) {
      if (this.freeBuffers.isEmpty()) {
         return false;
      } else {
         renderChunk.rebuildChunkAsync(this, regionCacheIn);
         return true;
      }
   }

   public boolean updateTransparencyLater(ChunkRenderDispatcher.RenderChunk renderChunk) {
      return this.freeBuffers.isEmpty() ? false : renderChunk.resortTransparency(RenderTypes.TRANSLUCENT, this);
   }

   public void addUploadTask(Runnable r) {
      if (r != null) {
         this.toUpload.add(r);
      }
   }

   static enum ChunkTaskResult {
      SUCCESSFUL,
      CANCELLED;
   }

   public static class CompiledChunk {
      public static final ChunkRenderDispatcher.CompiledChunk UNCOMPILED = new ChunkRenderDispatcher.CompiledChunk() {
         public boolean facesCanSeeEachother(Direction p_112782_, Direction p_112783_) {
            return false;
         }

         public void setAnimatedSprites(RenderType layer, BitSet animatedSprites) {
            throw new UnsupportedOperationException();
         }
      };
      final Set<RenderType> hasBlocks = new ChunkLayerSet();
      final List<BlockEntity> renderableBlockEntities = Lists.newArrayList();
      VisibilitySet visibilitySet = new VisibilitySet();
      @Nullable
      BufferBuilder.SortState transparencyState;
      private BitSet[] animatedSprites = new BitSet[RenderType.CHUNK_RENDER_TYPES.length];

      public boolean hasNoRenderableLayers() {
         return this.hasBlocks.isEmpty();
      }

      public boolean isEmpty(RenderType pRenderType) {
         return !this.hasBlocks.contains(pRenderType);
      }

      public List<BlockEntity> getRenderableBlockEntities() {
         return this.renderableBlockEntities;
      }

      public boolean facesCanSeeEachother(Direction pFace, Direction pOtherFace) {
         return this.visibilitySet.visibilityBetween(pFace, pOtherFace);
      }

      public BitSet getAnimatedSprites(RenderType layer) {
         return this.animatedSprites[layer.ordinal()];
      }

      public void setAnimatedSprites(BitSet[] animatedSprites) {
         this.animatedSprites = animatedSprites;
      }

      public boolean isLayerUsed(RenderType renderTypeIn) {
         return this.hasBlocks.contains(renderTypeIn);
      }

      public void setLayerUsed(RenderType renderTypeIn) {
         this.hasBlocks.add(renderTypeIn);
      }

      public boolean hasTerrainBlockEntities() {
         return !this.hasNoRenderableLayers() || !this.getRenderableBlockEntities().isEmpty();
      }

      public Set<RenderType> getLayersUsed() {
         return this.hasBlocks;
      }
   }

   public class RenderChunk {
      public static final int SIZE = 16;
      public final int index;
      public final AtomicReference<ChunkRenderDispatcher.CompiledChunk> compiled = new AtomicReference<>(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
      final AtomicInteger initialCompilationCancelCount = new AtomicInteger(0);
      @Nullable
      private ChunkRenderDispatcher.RenderChunk.RebuildTask lastRebuildTask;
      @Nullable
      private ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask lastResortTransparencyTask;
      private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
      private final ChunkLayerMap<VertexBuffer> buffers = new ChunkLayerMap<>((renderType) -> {
         return new VertexBuffer(VertexBuffer.Usage.STATIC);
      });
      private AABB bb;
      private boolean dirty = true;
      final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
      private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], (posArrIn) -> {
         for(int i = 0; i < posArrIn.length; ++i) {
            posArrIn[i] = new BlockPos.MutableBlockPos();
         }

      });
      private boolean playerChanged;
      private final boolean isMipmaps = Config.isMipmaps();
      private boolean playerUpdate = false;
      private boolean needsBackgroundPriorityUpdate;
      private boolean renderRegions = Config.isRenderRegions();
      public int regionX;
      public int regionZ;
      public int regionDX;
      public int regionDY;
      public int regionDZ;
      private final ChunkRenderDispatcher.RenderChunk[] renderChunksOfset16 = new ChunkRenderDispatcher.RenderChunk[6];
      private boolean renderChunksOffset16Updated = false;
      private LevelChunk chunk;
      private ChunkRenderDispatcher.RenderChunk[] renderChunkNeighbours = new ChunkRenderDispatcher.RenderChunk[Direction.VALUES.length];
      private ChunkRenderDispatcher.RenderChunk[] renderChunkNeighboursValid = new ChunkRenderDispatcher.RenderChunk[Direction.VALUES.length];
      private boolean renderChunkNeighboursUpated = false;
      private LevelRenderer.RenderChunkInfo renderInfo = new LevelRenderer.RenderChunkInfo(this, (Direction)null, 0);
      public AabbFrame boundingBoxParent;
      private SectionPos sectionPosition;

      public RenderChunk(int pIndex, int pX, int pY, int pZ) {
         this.index = pIndex;
         this.setOrigin(pX, pY, pZ);
      }

      private boolean doesChunkExistAt(BlockPos pPos) {
         return ChunkRenderDispatcher.this.level.getChunk(SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()), ChunkStatus.FULL, false) != null;
      }

      public boolean hasAllNeighbors() {
         int i = 24;
         return !(this.getDistToPlayerSqr() > 576.0D) ? true : this.doesChunkExistAt(this.origin);
      }

      public AABB getBoundingBox() {
         return this.bb;
      }

      public VertexBuffer getBuffer(RenderType pRenderType) {
         return this.buffers.get(pRenderType);
      }

      public void setOrigin(int pX, int pY, int pZ) {
         this.reset();
         this.origin.set(pX, pY, pZ);
         this.sectionPosition = SectionPos.of(this.origin);
         if (this.renderRegions) {
            int i = 8;
            this.regionX = pX >> i << i;
            this.regionZ = pZ >> i << i;
            this.regionDX = pX - this.regionX;
            this.regionDY = pY;
            this.regionDZ = pZ - this.regionZ;
         }

         this.bb = new AABB((double)pX, (double)pY, (double)pZ, (double)(pX + 16), (double)(pY + 16), (double)(pZ + 16));

         for(Direction direction : Direction.VALUES) {
            this.relativeOrigins[direction.ordinal()].set(this.origin).move(direction, 16);
         }

         this.renderChunksOffset16Updated = false;
         this.renderChunkNeighboursUpated = false;

         for(int j = 0; j < this.renderChunkNeighbours.length; ++j) {
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.renderChunkNeighbours[j];
            if (chunkrenderdispatcher$renderchunk != null) {
               chunkrenderdispatcher$renderchunk.renderChunkNeighboursUpated = false;
            }
         }

         this.chunk = null;
         this.boundingBoxParent = null;
      }

      protected double getDistToPlayerSqr() {
         Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
         double d0 = this.bb.minX + 8.0D - camera.getPosition().x;
         double d1 = this.bb.minY + 8.0D - camera.getPosition().y;
         double d2 = this.bb.minZ + 8.0D - camera.getPosition().z;
         return d0 * d0 + d1 * d1 + d2 * d2;
      }

      void beginLayer(BufferBuilder pBuilder) {
         pBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
      }

      public ChunkRenderDispatcher.CompiledChunk getCompiledChunk() {
         return this.compiled.get();
      }

      private void reset() {
         this.cancelTasks();
         this.compiled.set(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
         this.dirty = true;
      }

      public void releaseBuffers() {
         this.reset();
         this.buffers.values().forEach(VertexBuffer::close);
      }

      public BlockPos getOrigin() {
         return this.origin;
      }

      public void setDirty(boolean pReRenderOnMainThread) {
         boolean flag = this.dirty;
         this.dirty = true;
         this.playerChanged = pReRenderOnMainThread | (flag && this.playerChanged);
         if (this.isWorldPlayerUpdate()) {
            this.playerUpdate = true;
         }

         if (!flag) {
            ChunkRenderDispatcher.this.renderer.onChunkRenderNeedsUpdate(this);
         }

      }

      public void setNotDirty() {
         this.dirty = false;
         this.playerChanged = false;
         this.playerUpdate = false;
         this.needsBackgroundPriorityUpdate = false;
      }

      public boolean isDirty() {
         return this.dirty;
      }

      public boolean isDirtyFromPlayer() {
         return this.dirty && this.playerChanged;
      }

      public BlockPos getRelativeOrigin(Direction pDirection) {
         return this.relativeOrigins[pDirection.ordinal()];
      }

      public boolean resortTransparency(RenderType pType, ChunkRenderDispatcher pDispatcher) {
         ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher$compiledchunk = this.getCompiledChunk();
         if (this.lastResortTransparencyTask != null) {
            this.lastResortTransparencyTask.cancel();
         }

         if (!chunkrenderdispatcher$compiledchunk.hasBlocks.contains(pType)) {
            return false;
         } else {
            if (ChunkRenderDispatcher.FORGE) {
               this.lastResortTransparencyTask = new ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask(new ChunkPos(this.getOrigin()), this.getDistToPlayerSqr(), chunkrenderdispatcher$compiledchunk);
            } else {
               this.lastResortTransparencyTask = new ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask(this.getDistToPlayerSqr(), chunkrenderdispatcher$compiledchunk);
            }

            pDispatcher.schedule(this.lastResortTransparencyTask);
            return true;
         }
      }

      protected boolean cancelTasks() {
         boolean flag = false;
         if (this.lastRebuildTask != null) {
            this.lastRebuildTask.cancel();
            this.lastRebuildTask = null;
            flag = true;
         }

         if (this.lastResortTransparencyTask != null) {
            this.lastResortTransparencyTask.cancel();
            this.lastResortTransparencyTask = null;
         }

         return flag;
      }

      public ChunkRenderDispatcher.RenderChunk.ChunkCompileTask createCompileTask(RenderRegionCache pRegionCache) {
         boolean flag = this.cancelTasks();
         BlockPos blockpos = this.origin.immutable();
         int i = 1;
         RenderChunkRegion renderchunkregion = null;
         boolean flag1 = this.compiled.get() == ChunkRenderDispatcher.CompiledChunk.UNCOMPILED;
         if (flag1 && flag) {
            this.initialCompilationCancelCount.incrementAndGet();
         }

         ChunkPos chunkpos = ChunkRenderDispatcher.FORGE ? new ChunkPos(this.getOrigin()) : null;
         this.lastRebuildTask = new ChunkRenderDispatcher.RenderChunk.RebuildTask(chunkpos, this.getDistToPlayerSqr(), renderchunkregion, !flag1 || this.initialCompilationCancelCount.get() > 2);
         return this.lastRebuildTask;
      }

      public void rebuildChunkAsync(ChunkRenderDispatcher pDispatcher, RenderRegionCache pRegionCache) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.createCompileTask(pRegionCache);
         pDispatcher.schedule(chunkrenderdispatcher$renderchunk$chunkcompiletask);
      }

      void updateGlobalBlockEntities(Collection<BlockEntity> pBlockEntities) {
         Set<BlockEntity> set = Sets.newHashSet(pBlockEntities);
         Set<BlockEntity> set1;
         synchronized(this.globalBlockEntities) {
            set1 = Sets.newHashSet(this.globalBlockEntities);
            set.removeAll(this.globalBlockEntities);
            set1.removeAll(pBlockEntities);
            this.globalBlockEntities.clear();
            this.globalBlockEntities.addAll(pBlockEntities);
         }

         ChunkRenderDispatcher.this.renderer.updateGlobalBlockEntities(set1, set);
      }

      public void compileSync(RenderRegionCache pRegionCache) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.createCompileTask(pRegionCache);
         chunkrenderdispatcher$renderchunk$chunkcompiletask.doTask(ChunkRenderDispatcher.this.fixedBuffers);
      }

      private boolean isWorldPlayerUpdate() {
         if (ChunkRenderDispatcher.this.level instanceof ClientLevel) {
            ClientLevel clientlevel = ChunkRenderDispatcher.this.level;
            return clientlevel.isPlayerUpdate();
         } else {
            return false;
         }
      }

      public boolean isPlayerUpdate() {
         return this.playerUpdate;
      }

      public void setNeedsBackgroundPriorityUpdate(boolean needsBackgroundPriorityUpdate) {
         this.needsBackgroundPriorityUpdate = needsBackgroundPriorityUpdate;
      }

      public boolean needsBackgroundPriorityUpdate() {
         return this.needsBackgroundPriorityUpdate;
      }

      private Iterable<RenderType> getBlockRenderLayers(BakedModel model, BlockState blockState, BlockPos blockPos, RandomSource randomsource, ModelData modelData, SingleIterable<RenderType> singleLayer) {
         if (ChunkRenderDispatcher.FORGE) {
            randomsource.setSeed(blockState.getSeed(blockPos));
            return model.getRenderTypes(blockState, randomsource, modelData);
         } else {
            singleLayer.setValue(ItemBlockRenderTypes.getChunkRenderType(blockState));
            return singleLayer;
         }
      }

      private RenderType fixBlockLayer(BlockGetter worldReader, BlockState blockState, BlockPos blockPos, RenderType layer) {
         if (CustomBlockLayers.isActive()) {
            RenderType rendertype = CustomBlockLayers.getRenderLayer(worldReader, blockState, blockPos);
            if (rendertype != null) {
               return rendertype;
            }
         }

         if (this.isMipmaps) {
            if (layer == RenderTypes.CUTOUT) {
               Block block = blockState.getBlock();
               if (block instanceof RedStoneWireBlock) {
                  return layer;
               }

               if (block instanceof CactusBlock) {
                  return layer;
               }

               return RenderTypes.CUTOUT_MIPPED;
            }
         } else if (layer == RenderTypes.CUTOUT_MIPPED) {
            return RenderTypes.CUTOUT;
         }

         return layer;
      }

      private void postRenderOverlays(ChunkBufferBuilderPack regionRenderCacheBuilder, Set<RenderType> renderTypes) {
         this.postRenderOverlay(RenderTypes.CUTOUT, regionRenderCacheBuilder, renderTypes);
         this.postRenderOverlay(RenderTypes.CUTOUT_MIPPED, regionRenderCacheBuilder, renderTypes);
         this.postRenderOverlay(RenderTypes.TRANSLUCENT, regionRenderCacheBuilder, renderTypes);
      }

      private void postRenderOverlay(RenderType layer, ChunkBufferBuilderPack regionRenderCacheBuilder, Set<RenderType> renderTypes) {
         BufferBuilder bufferbuilder = regionRenderCacheBuilder.builder(layer);
         if (bufferbuilder.building()) {
            renderTypes.add(layer);
         }

      }

      private ChunkCacheOF makeChunkCacheOF(BlockPos posIn) {
         BlockPos blockpos = posIn.offset(-1, -1, -1);
         BlockPos blockpos1 = posIn.offset(16, 16, 16);
         RenderRegionCache renderregioncache = new RenderRegionCache();
         RenderChunkRegion renderchunkregion = renderregioncache.createRegion(ChunkRenderDispatcher.this.level, blockpos, blockpos1, 1, false);
         return new ChunkCacheOF(renderchunkregion, blockpos, blockpos1, 1);
      }

      public ChunkRenderDispatcher.RenderChunk getRenderChunkOffset16(ViewArea viewFrustum, Direction facing) {
         if (!this.renderChunksOffset16Updated) {
            for(int i = 0; i < Direction.VALUES.length; ++i) {
               Direction direction = Direction.VALUES[i];
               BlockPos blockpos = this.getRelativeOrigin(direction);
               this.renderChunksOfset16[i] = viewFrustum.getRenderChunkAt(blockpos);
            }

            this.renderChunksOffset16Updated = true;
         }

         return this.renderChunksOfset16[facing.ordinal()];
      }

      public LevelChunk getChunk() {
         return this.getChunk(this.origin);
      }

      private LevelChunk getChunk(BlockPos posIn) {
         LevelChunk levelchunk = this.chunk;
         if (levelchunk != null && ChunkUtils.isLoaded(levelchunk)) {
            return levelchunk;
         } else {
            levelchunk = ChunkRenderDispatcher.this.level.getChunkAt(posIn);
            this.chunk = levelchunk;
            return levelchunk;
         }
      }

      public boolean isChunkRegionEmpty() {
         return this.isChunkRegionEmpty(this.origin);
      }

      private boolean isChunkRegionEmpty(BlockPos posIn) {
         int i = posIn.getY();
         int j = i + 15;
         return this.getChunk(posIn).isYSpaceEmpty(i, j);
      }

      public void setRenderChunkNeighbour(Direction facing, ChunkRenderDispatcher.RenderChunk neighbour) {
         this.renderChunkNeighbours[facing.ordinal()] = neighbour;
         this.renderChunkNeighboursValid[facing.ordinal()] = neighbour;
      }

      public ChunkRenderDispatcher.RenderChunk getRenderChunkNeighbour(Direction facing) {
         if (!this.renderChunkNeighboursUpated) {
            this.updateRenderChunkNeighboursValid();
         }

         return this.renderChunkNeighboursValid[facing.ordinal()];
      }

      public LevelRenderer.RenderChunkInfo getRenderInfo() {
         return this.renderInfo;
      }

      public LevelRenderer.RenderChunkInfo getRenderInfo(Direction dirIn, int counterIn) {
         this.renderInfo.initialize(dirIn, counterIn);
         return this.renderInfo;
      }

      private void updateRenderChunkNeighboursValid() {
         int i = this.getOrigin().getX();
         int j = this.getOrigin().getZ();
         int k = Direction.NORTH.ordinal();
         int l = Direction.SOUTH.ordinal();
         int i1 = Direction.WEST.ordinal();
         int j1 = Direction.EAST.ordinal();
         this.renderChunkNeighboursValid[k] = this.renderChunkNeighbours[k].getOrigin().getZ() == j - 16 ? this.renderChunkNeighbours[k] : null;
         this.renderChunkNeighboursValid[l] = this.renderChunkNeighbours[l].getOrigin().getZ() == j + 16 ? this.renderChunkNeighbours[l] : null;
         this.renderChunkNeighboursValid[i1] = this.renderChunkNeighbours[i1].getOrigin().getX() == i - 16 ? this.renderChunkNeighbours[i1] : null;
         this.renderChunkNeighboursValid[j1] = this.renderChunkNeighbours[j1].getOrigin().getX() == i + 16 ? this.renderChunkNeighbours[j1] : null;
         this.renderChunkNeighboursUpated = true;
      }

      public boolean isBoundingBoxInFrustum(ICamera camera, int frameCount) {
         return this.getBoundingBoxParent().isBoundingBoxInFrustumFully(camera, frameCount) ? true : camera.isBoundingBoxInFrustum(this.bb);
      }

      public AabbFrame getBoundingBoxParent() {
         if (this.boundingBoxParent == null) {
            BlockPos blockpos = this.getOrigin();
            int i = blockpos.getX();
            int j = blockpos.getY();
            int k = blockpos.getZ();
            int l = 5;
            int i1 = i >> l << l;
            int j1 = j >> l << l;
            int k1 = k >> l << l;
            if (i1 != i || j1 != j || k1 != k) {
               AabbFrame aabbframe = ChunkRenderDispatcher.this.renderer.getRenderChunk(new BlockPos(i1, j1, k1)).getBoundingBoxParent();
               if (aabbframe != null && aabbframe.minX == (double)i1 && aabbframe.minY == (double)j1 && aabbframe.minZ == (double)k1) {
                  this.boundingBoxParent = aabbframe;
               }
            }

            if (this.boundingBoxParent == null) {
               int l1 = 1 << l;
               this.boundingBoxParent = new AabbFrame((double)i1, (double)j1, (double)k1, (double)(i1 + l1), (double)(j1 + l1), (double)(k1 + l1));
            }
         }

         return this.boundingBoxParent;
      }

      public ClientLevel getWorld() {
         return ChunkRenderDispatcher.this.level;
      }

      public SectionPos getSectionPosition() {
         return this.sectionPosition;
      }

      public String toString() {
         return "pos: " + this.getOrigin();
      }

      abstract class ChunkCompileTask implements Comparable<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> {
         protected final double distAtCreation;
         protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
         protected final boolean isHighPriority;
         protected Map<BlockPos, ModelData> modelData;

         public ChunkCompileTask(double pDistAtCreation, boolean pIsHighPriority) {
            this((ChunkPos)null, pDistAtCreation, pIsHighPriority);
         }

         public ChunkCompileTask(ChunkPos pos, double distanceSqIn, boolean highPriorityIn) {
            this.distAtCreation = distanceSqIn;
            this.isHighPriority = highPriorityIn;
            if (pos == null) {
               this.modelData = Collections.emptyMap();
            } else {
               this.modelData = Minecraft.getInstance().level.getModelDataManager().getAt(pos);
            }

         }

         public abstract CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> doTask(ChunkBufferBuilderPack pBuffers);

         public abstract void cancel();

         protected abstract String name();

         public int compareTo(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask pOther) {
            return Doubles.compare(this.distAtCreation, pOther.distAtCreation);
         }

         public ModelData getModelData(BlockPos pos) {
            return this.modelData.getOrDefault(pos, ModelData.EMPTY);
         }
      }

      class RebuildTask extends ChunkRenderDispatcher.RenderChunk.ChunkCompileTask {
         @Nullable
         protected RenderChunkRegion region;

         public RebuildTask(@Nullable double pDistAtCreation, RenderChunkRegion pRegion, boolean pIsHighPriority) {
            this((ChunkPos)null, pDistAtCreation, pRegion, pIsHighPriority);
         }

         public RebuildTask(ChunkPos pos, double distanceSqIn, @Nullable RenderChunkRegion renderCacheIn, boolean highPriorityIn) {
            super(pos, distanceSqIn, highPriorityIn);
            this.region = renderCacheIn;
         }

         protected String name() {
            return "rend_chk_rebuild";
         }

         public CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> doTask(ChunkBufferBuilderPack pBuffers) {
            if (this.isCancelled.get()) {
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else if (!RenderChunk.this.hasAllNeighbors()) {
               this.region = null;
               RenderChunk.this.setDirty(false);
               this.isCancelled.set(true);
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else if (this.isCancelled.get()) {
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else {
               Vec3 vec3 = ChunkRenderDispatcher.this.getCameraPosition();
               float f = (float)vec3.x;
               float f1 = (float)vec3.y;
               float f2 = (float)vec3.z;
               ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults chunkrenderdispatcher$renderchunk$rebuildtask$compileresults = this.compile(f, f1, f2, pBuffers);
               RenderChunk.this.updateGlobalBlockEntities(chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.globalBlockEntities);
               if (this.isCancelled.get()) {
                  chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.renderedLayers.values().forEach(BufferBuilder.RenderedBuffer::release);
                  return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
               } else {
                  ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher$compiledchunk = new ChunkRenderDispatcher.CompiledChunk();
                  chunkrenderdispatcher$compiledchunk.visibilitySet = chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.visibilitySet;
                  chunkrenderdispatcher$compiledchunk.renderableBlockEntities.addAll(chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.blockEntities);
                  chunkrenderdispatcher$compiledchunk.transparencyState = chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.transparencyState;
                  chunkrenderdispatcher$compiledchunk.setAnimatedSprites(chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.animatedSprites);
                  List<CompletableFuture<Void>> list = Lists.newArrayList();
                  chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.renderedLayers.forEach((renderTypeIn, bufferIn) -> {
                     list.add(ChunkRenderDispatcher.this.uploadChunkLayer(bufferIn, RenderChunk.this.getBuffer(renderTypeIn)));
                     chunkrenderdispatcher$compiledchunk.hasBlocks.add(renderTypeIn);
                  });
                  return Util.sequenceFailFast(list).handle((listIn, throwableIn) -> {
                     if (throwableIn != null && !(throwableIn instanceof CancellationException) && !(throwableIn instanceof InterruptedException)) {
                        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwableIn, "Rendering chunk"));
                     }

                     if (this.isCancelled.get()) {
                        return ChunkRenderDispatcher.ChunkTaskResult.CANCELLED;
                     } else {
                        RenderChunk.this.compiled.set(chunkrenderdispatcher$compiledchunk);
                        RenderChunk.this.initialCompilationCancelCount.set(0);
                        ChunkRenderDispatcher.this.renderer.addRecentlyCompiledChunk(RenderChunk.this);
                        return ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL;
                     }
                  });
               }
            }
         }

         private ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults compile(float pX, float pY, float pZ, ChunkBufferBuilderPack pChunkBufferBuilderPack) {
            ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults chunkrenderdispatcher$renderchunk$rebuildtask$compileresults = new ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults();
            int i = 1;
            BlockPos blockpos = RenderChunk.this.origin.immutable();
            BlockPos blockpos1 = blockpos.offset(15, 15, 15);
            VisGraph visgraph = new VisGraph();
            this.region = null;
            PoseStack posestack = new PoseStack();
            if (!RenderChunk.this.isChunkRegionEmpty(blockpos)) {
               ++ChunkRenderDispatcher.renderChunksUpdated;
               ChunkCacheOF chunkcacheof = RenderChunk.this.makeChunkCacheOF(blockpos);
               chunkcacheof.renderStart();
               SingleIterable<RenderType> singleiterable = new SingleIterable<>();
               boolean flag = Config.isShaders();
               boolean flag1 = flag && Shaders.useMidBlockAttrib;
               ModelBlockRenderer.enableCaching();
               Set<RenderType> set = new ReferenceArraySet<>(RenderType.chunkBufferLayers().size());
               RandomSource randomsource = RandomSource.create();
               BlockRenderDispatcher blockrenderdispatcher = Minecraft.getInstance().getBlockRenderer();

               for(BlockPosM blockposm : (Iterable<BlockPosM>)BlockPosM.getAllInBoxMutable(blockpos, blockpos1)) {
                  BlockState blockstate = chunkcacheof.getBlockState(blockposm);
                  if (!blockstate.isAir()) {
                     if (blockstate.isSolidRender(chunkcacheof, blockposm)) {
                        visgraph.setOpaque(blockposm);
                     }

                     if (blockstate.hasBlockEntity()) {
                        BlockEntity blockentity = chunkcacheof.getBlockEntity(blockposm);
                        if (blockentity != null) {
                           this.handleBlockEntity(chunkrenderdispatcher$renderchunk$rebuildtask$compileresults, blockentity);
                        }
                     }

                     FluidState fluidstate = blockstate.getFluidState();
                     if (!fluidstate.isEmpty()) {
                        RenderType rendertype = ItemBlockRenderTypes.getRenderLayer(fluidstate);
                        BufferBuilder bufferbuilder = pChunkBufferBuilderPack.builder(rendertype);
                        bufferbuilder.setBlockLayer(rendertype);
                        RenderEnv renderenv = bufferbuilder.getRenderEnv(blockstate, blockposm);
                        renderenv.setRegionRenderCacheBuilder(pChunkBufferBuilderPack);
                        chunkcacheof.setRenderEnv(renderenv);
                        if (set.add(rendertype)) {
                           RenderChunk.this.beginLayer(bufferbuilder);
                        }

                        blockrenderdispatcher.renderLiquid(blockposm, chunkcacheof, bufferbuilder, blockstate, fluidstate);
                     }

                     if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                        BakedModel bakedmodel = blockrenderdispatcher.getBlockModel(blockstate);
                        ModelData modeldata = ChunkRenderDispatcher.FORGE ? bakedmodel.getModelData(chunkcacheof, blockposm, blockstate, this.getModelData(blockposm)) : null;

                        for(RenderType rendertype1 : RenderChunk.this.getBlockRenderLayers(bakedmodel, blockstate, blockposm, randomsource, modeldata, singleiterable)) {
                           RenderType rendertype2 = RenderChunk.this.fixBlockLayer(chunkcacheof, blockstate, blockposm, rendertype1);
                           BufferBuilder bufferbuilder1 = pChunkBufferBuilderPack.builder(rendertype2);
                           bufferbuilder1.setBlockLayer(rendertype2);
                           RenderEnv renderenv1 = bufferbuilder1.getRenderEnv(blockstate, blockposm);
                           renderenv1.setRegionRenderCacheBuilder(pChunkBufferBuilderPack);
                           chunkcacheof.setRenderEnv(renderenv1);
                           if (set.add(rendertype2)) {
                              RenderChunk.this.beginLayer(bufferbuilder1);
                           }

                           posestack.pushPose();
                           posestack.translate((float)RenderChunk.this.regionDX + (float)(blockposm.getX() & 15), (float)RenderChunk.this.regionDY + (float)(blockposm.getY() & 15), (float)RenderChunk.this.regionDZ + (float)(blockposm.getZ() & 15));
                           if (flag1) {
                              bufferbuilder1.setMidBlock(0.5F + (float)RenderChunk.this.regionDX + (float)(blockposm.getX() & 15), 0.5F + (float)RenderChunk.this.regionDY + (float)(blockposm.getY() & 15), 0.5F + (float)RenderChunk.this.regionDZ + (float)(blockposm.getZ() & 15));
                           }

                           blockrenderdispatcher.renderBatched(blockstate, blockposm, chunkcacheof, posestack, bufferbuilder1, true, randomsource, modeldata, rendertype1);
                           if (renderenv1.isOverlaysRendered()) {
                              RenderChunk.this.postRenderOverlays(pChunkBufferBuilderPack, set);
                              renderenv1.setOverlaysRendered(false);
                           }

                           posestack.popPose();
                        }
                     }
                  }
               }

               if (set.contains(RenderType.translucent())) {
                  BufferBuilder bufferbuilder2 = pChunkBufferBuilderPack.builder(RenderType.translucent());
                  if (!bufferbuilder2.isCurrentBatchEmpty()) {
                     bufferbuilder2.setQuadSorting(VertexSorting.byDistance((float)RenderChunk.this.regionDX + pX - (float)blockpos.getX(), (float)RenderChunk.this.regionDY + pY - (float)blockpos.getY(), (float)RenderChunk.this.regionDZ + pZ - (float)blockpos.getZ()));
                     chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.transparencyState = bufferbuilder2.getSortState();
                  }
               }

               for(RenderType rendertype3 : set) {
                  BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = pChunkBufferBuilderPack.builder(rendertype3).endOrDiscardIfEmpty();
                  if (bufferbuilder$renderedbuffer != null) {
                     chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.renderedLayers.put(rendertype3, bufferbuilder$renderedbuffer);
                  }
               }

               for(RenderType rendertype5 : ChunkRenderDispatcher.BLOCK_RENDER_LAYERS) {
                  chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.setAnimatedSprites(rendertype5, (BitSet)null);
               }

               for(RenderType rendertype4 : set) {
                  if (Config.isShaders()) {
                     SVertexBuilder.calcNormalChunkLayer(pChunkBufferBuilderPack.builder(rendertype4));
                  }

                  BufferBuilder bufferbuilder3 = pChunkBufferBuilderPack.builder(rendertype4);
                  if (bufferbuilder3.animatedSprites != null && !bufferbuilder3.animatedSprites.isEmpty()) {
                     chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.setAnimatedSprites(rendertype4, (BitSet)bufferbuilder3.animatedSprites.clone());
                  }
               }

               chunkcacheof.renderFinish();
               ModelBlockRenderer.clearCache();
            }

            chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.visibilitySet = visgraph.resolve();
            return chunkrenderdispatcher$renderchunk$rebuildtask$compileresults;
         }

         private <E extends BlockEntity> void handleBlockEntity(ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults pCompileResults, E pBlockEntity) {
            BlockEntityRenderer<E> blockentityrenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(pBlockEntity);
            if (blockentityrenderer != null) {
               if (blockentityrenderer.shouldRenderOffScreen(pBlockEntity)) {
                  pCompileResults.globalBlockEntities.add(pBlockEntity);
               } else {
                  pCompileResults.blockEntities.add(pBlockEntity);
               }
            }

         }

         public void cancel() {
            this.region = null;
            if (this.isCancelled.compareAndSet(false, true)) {
               RenderChunk.this.setDirty(false);
            }

         }

         static final class CompileResults {
            public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
            public final List<BlockEntity> blockEntities = new ArrayList<>();
            public final Map<RenderType, BufferBuilder.RenderedBuffer> renderedLayers = new Reference2ObjectArrayMap<>();
            public VisibilitySet visibilitySet = new VisibilitySet();
            @Nullable
            public BufferBuilder.SortState transparencyState;
            public BitSet[] animatedSprites = new BitSet[RenderType.CHUNK_RENDER_TYPES.length];

            public void setAnimatedSprites(RenderType layer, BitSet animatedSprites) {
               this.animatedSprites[layer.ordinal()] = animatedSprites;
            }
         }
      }

      class ResortTransparencyTask extends ChunkRenderDispatcher.RenderChunk.ChunkCompileTask {
         private final ChunkRenderDispatcher.CompiledChunk compiledChunk;

         public ResortTransparencyTask(double pDistAtCreation, ChunkRenderDispatcher.CompiledChunk pCompiledChunk) {
            this((ChunkPos)null, pDistAtCreation, pCompiledChunk);
         }

         public ResortTransparencyTask(ChunkPos pos, double distanceSqIn, ChunkRenderDispatcher.CompiledChunk compiledChunkIn) {
            super(pos, distanceSqIn, true);
            this.compiledChunk = compiledChunkIn;
         }

         protected String name() {
            return "rend_chk_sort";
         }

         public CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> doTask(ChunkBufferBuilderPack pBuffers) {
            if (this.isCancelled.get()) {
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else if (!RenderChunk.this.hasAllNeighbors()) {
               this.isCancelled.set(true);
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else if (this.isCancelled.get()) {
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else {
               Vec3 vec3 = ChunkRenderDispatcher.this.getCameraPosition();
               float f = (float)vec3.x;
               float f1 = (float)vec3.y;
               float f2 = (float)vec3.z;
               BufferBuilder.SortState bufferbuilder$sortstate = this.compiledChunk.transparencyState;
               if (bufferbuilder$sortstate != null && !this.compiledChunk.isEmpty(RenderType.translucent())) {
                  BufferBuilder bufferbuilder = pBuffers.builder(RenderType.translucent());
                  bufferbuilder.setBlockLayer(RenderType.translucent());
                  RenderChunk.this.beginLayer(bufferbuilder);
                  bufferbuilder.restoreSortState(bufferbuilder$sortstate);
                  bufferbuilder.setQuadSorting(VertexSorting.byDistance((float)RenderChunk.this.regionDX + f - (float)RenderChunk.this.origin.getX(), (float)RenderChunk.this.regionDY + f1 - (float)RenderChunk.this.origin.getY(), (float)RenderChunk.this.regionDZ + f2 - (float)RenderChunk.this.origin.getZ()));
                  this.compiledChunk.transparencyState = bufferbuilder.getSortState();
                  BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = bufferbuilder.end();
                  if (this.isCancelled.get()) {
                     bufferbuilder$renderedbuffer.release();
                     return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                  } else {
                     CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> completablefuture = ChunkRenderDispatcher.this.uploadChunkLayer(bufferbuilder$renderedbuffer, RenderChunk.this.getBuffer(RenderType.translucent())).thenApply((voidIn) -> {
                        return ChunkRenderDispatcher.ChunkTaskResult.CANCELLED;
                     });
                     return completablefuture.handle((taskResultIn, throwableIn) -> {
                        if (throwableIn != null && !(throwableIn instanceof CancellationException) && !(throwableIn instanceof InterruptedException)) {
                           Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwableIn, "Rendering chunk"));
                        }

                        return this.isCancelled.get() ? ChunkRenderDispatcher.ChunkTaskResult.CANCELLED : ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL;
                     });
                  }
               } else {
                  return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
               }
            }
         }

         public void cancel() {
            this.isCancelled.set(true);
         }
      }
   }
}
