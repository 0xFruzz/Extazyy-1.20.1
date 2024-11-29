package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.ModelData;
import net.optifine.BlockPosM;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.CustomSky;
import net.optifine.DynamicLights;
import net.optifine.EmissiveTextures;
import net.optifine.Lagometer;
import net.optifine.SmartAnimations;
import net.optifine.Vec3M;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;
import net.optifine.render.ChunkVisibility;
import net.optifine.render.RenderEnv;
import net.optifine.render.RenderStateManager;
import net.optifine.render.RenderUtils;
import net.optifine.render.VboRegion;
import net.optifine.shaders.RenderStage;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.util.BiomeUtils;
import net.optifine.util.GpuMemory;
import net.optifine.util.MathUtils;
import net.optifine.util.PairInt;
import net.optifine.util.RandomUtils;
import net.optifine.util.RenderChunkUtils;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import ru.fruzz.extazyy.Extazyy;

public class LevelRenderer implements ResourceManagerReloadListener, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int CHUNK_SIZE = 16;
   private static final int HALF_CHUNK_SIZE = 8;
   private static final float SKY_DISC_RADIUS = 512.0F;
   private static final int MINIMUM_ADVANCED_CULLING_DISTANCE = 60;
   private static final double CEILED_SECTION_DIAGONAL = Math.ceil(Math.sqrt(3.0D) * 16.0D);
   private static final int MIN_FOG_DISTANCE = 32;
   private static final int RAIN_RADIUS = 10;
   private static final int RAIN_DIAMETER = 21;
   private static final int TRANSPARENT_SORT_COUNT = 15;
   private static final int HALF_A_SECOND_IN_MILLIS = 500;
   private static final ResourceLocation MOON_LOCATION = new ResourceLocation("textures/environment/moon_phases.png");
   private static final ResourceLocation SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
   private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
   private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
   private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation("textures/misc/forcefield.png");
   private static final ResourceLocation RAIN_LOCATION = new ResourceLocation("textures/environment/rain.png");
   private static final ResourceLocation SNOW_LOCATION = new ResourceLocation("textures/environment/snow.png");
   public static final Direction[] DIRECTIONS = Direction.values();
   private final Minecraft minecraft;
   private final EntityRenderDispatcher entityRenderDispatcher;
   private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
   private final RenderBuffers renderBuffers;
   @Nullable
   private ClientLevel level;
   private final BlockingQueue<ChunkRenderDispatcher.RenderChunk> recentlyCompiledChunks = new LinkedBlockingQueue<>();
   private final AtomicReference<LevelRenderer.RenderChunkStorage> renderChunkStorage = new AtomicReference<>();
   private ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum = new ObjectArrayList<>(10000);
   private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
   @Nullable
   private Future<?> lastFullRenderChunkUpdate;
   @Nullable
   private ViewArea viewArea;
   @Nullable
   private VertexBuffer starBuffer;
   @Nullable
   private VertexBuffer skyBuffer;
   @Nullable
   private VertexBuffer darkBuffer;
   private boolean generateClouds = true;
   @Nullable
   private VertexBuffer cloudBuffer;
   private final RunningTrimmedMean frameTimes = new RunningTrimmedMean(100);
   private int ticks;
   private final Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = new Int2ObjectOpenHashMap<>();
   private final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress = new Long2ObjectOpenHashMap<>();
   private final Map<BlockPos, SoundInstance> playingRecords = Maps.newHashMap();
   @Nullable
   private RenderTarget entityTarget;
   @Nullable
   private PostChain entityEffect;
   @Nullable
   private RenderTarget translucentTarget;
   @Nullable
   private RenderTarget itemEntityTarget;
   @Nullable
   private RenderTarget particlesTarget;
   @Nullable
   private RenderTarget weatherTarget;
   @Nullable
   private RenderTarget cloudsTarget;
   @Nullable
   private PostChain transparencyChain;
   private double lastCameraX = Double.MIN_VALUE;
   private double lastCameraY = Double.MIN_VALUE;
   private double lastCameraZ = Double.MIN_VALUE;
   private int lastCameraChunkX = Integer.MIN_VALUE;
   private int lastCameraChunkY = Integer.MIN_VALUE;
   private int lastCameraChunkZ = Integer.MIN_VALUE;
   private double prevCamX = Double.MIN_VALUE;
   private double prevCamY = Double.MIN_VALUE;
   private double prevCamZ = Double.MIN_VALUE;
   private double prevCamRotX = Double.MIN_VALUE;
   private double prevCamRotY = Double.MIN_VALUE;
   private int prevCloudX = Integer.MIN_VALUE;
   private int prevCloudY = Integer.MIN_VALUE;
   private int prevCloudZ = Integer.MIN_VALUE;
   private Vec3 prevCloudColor = Vec3.ZERO;
   @Nullable
   private CloudStatus prevCloudsType;
   @Nullable
   private ChunkRenderDispatcher chunkRenderDispatcher;
   private int lastViewDistance = -1;
   private int renderedEntities;
   private int culledEntities;
   private Frustum cullingFrustum;
   private boolean captureFrustum;
   @Nullable
   private Frustum capturedFrustum;
   private final Vector4f[] frustumPoints = new Vector4f[8];
   private final Vector3d frustumPos = new Vector3d(0.0D, 0.0D, 0.0D);
   private double xTransparentOld;
   private double yTransparentOld;
   private double zTransparentOld;
   private boolean needsFullRenderChunkUpdate = true;
   private final AtomicLong nextFullUpdateMillis = new AtomicLong(0L);
   private final AtomicBoolean needsFrustumUpdate = new AtomicBoolean(false);
   private int rainSoundTime;
   private final float[] rainSizeX = new float[1024];
   private final float[] rainSizeZ = new float[1024];
   public Set chunksToResortTransparency = new LinkedHashSet();
   private int countChunksToUpdate = 0;
   private ObjectArrayList<LevelRenderer.RenderChunkInfo> renderInfosTerrain = new ObjectArrayList<>(1024);
   private LongOpenHashSet renderInfosEntities = new LongOpenHashSet(1024);
   private List<LevelRenderer.RenderChunkInfo> renderInfosTileEntities = new ArrayList<>(1024);
   private ObjectArrayList renderInfosTerrainNormal = new ObjectArrayList(1024);
   private LongOpenHashSet renderInfosEntitiesNormal = new LongOpenHashSet(1024);
   private List renderInfosTileEntitiesNormal = new ArrayList(1024);
   private ObjectArrayList renderInfosTerrainShadow = new ObjectArrayList(1024);
   private LongOpenHashSet renderInfosEntitiesShadow = new LongOpenHashSet(1024);
   private List renderInfosTileEntitiesShadow = new ArrayList(1024);
   private int renderDistance = 0;
   private int renderDistanceSq = 0;
   private int renderDistanceXZSq = 0;
   private int countTileEntitiesRendered;
   private RenderEnv renderEnv = new RenderEnv(Blocks.AIR.defaultBlockState(), new BlockPos(0, 0, 0));
   public boolean renderOverlayDamaged = false;
   public boolean renderOverlayEyes = false;
   private boolean firstWorldLoad = false;
   private static int renderEntitiesCounter = 0;
   public int loadVisibleChunksCounter = -1;
   public static MessageSignature loadVisibleChunksMessageId = new MessageSignature(RandomUtils.getRandomBytes(256));
   private static boolean ambientOcclusion = false;
   private Map<String, List<Entity>> mapEntityLists = new HashMap<>();
   private Map<RenderType, Map> mapRegionLayers = new LinkedHashMap<>();
   private int frameId;
   private boolean debugFixTerrainFrustumShadow;

   public LevelRenderer(Minecraft pMinecraft, EntityRenderDispatcher pEntityRenderDispatcher, BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, RenderBuffers pRenderBuffers) {
      this.minecraft = pMinecraft;
      this.entityRenderDispatcher = pEntityRenderDispatcher;
      this.blockEntityRenderDispatcher = pBlockEntityRenderDispatcher;
      this.renderBuffers = pRenderBuffers;

      for(int i = 0; i < 32; ++i) {
         for(int j = 0; j < 32; ++j) {
            float f = (float)(j - 16);
            float f1 = (float)(i - 16);
            float f2 = Mth.sqrt(f * f + f1 * f1);
            this.rainSizeX[i << 5 | j] = -f1 / f2;
            this.rainSizeZ[i << 5 | j] = f / f2;
         }
      }

      this.createStars();
      this.createLightSky();
      this.createDarkSky();
   }

   private void renderSnowAndRain(LightTexture pLightTexture, float pPartialTick, double pCamX, double pCamY, double pCamZ) {
      if (!Reflector.IForgeDimensionSpecialEffects_renderSnowAndRain.exists() || !Reflector.callBoolean(this.level.effects(), Reflector.IForgeDimensionSpecialEffects_renderSnowAndRain, this.level, this.ticks, pPartialTick, pLightTexture, pCamX, pCamY, pCamZ)) {
         float f = this.minecraft.level.getRainLevel(pPartialTick);
         if (!(f <= 0.0F)) {
            if (Config.isRainOff()) {
               return;
            }

            pLightTexture.turnOnLightLayer();
            Level level = this.minecraft.level;
            int i = Mth.floor(pCamX);
            int j = Mth.floor(pCamY);
            int k = Mth.floor(pCamZ);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            int l = 5;
            if (Config.isRainFancy()) {
               l = 10;
            }

            RenderSystem.depthMask(Minecraft.useShaderTransparency());
            if (Config.isShaders()) {
               GlStateManager._depthMask(Shaders.isRainDepth());
            }

            int i1 = -1;
            float f1 = (float)this.ticks + pPartialTick;
            RenderSystem.setShader(GameRenderer::getParticleShader);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for(int j1 = k - l; j1 <= k + l; ++j1) {
               for(int k1 = i - l; k1 <= i + l; ++k1) {
                  int l1 = (j1 - k + 16) * 32 + k1 - i + 16;
                  double d0 = (double)this.rainSizeX[l1] * 0.5D;
                  double d1 = (double)this.rainSizeZ[l1] * 0.5D;
                  blockpos$mutableblockpos.set((double)k1, pCamY, (double)j1);
                  Biome biome = level.getBiome(blockpos$mutableblockpos).value();
                  if (biome.hasPrecipitation()) {
                     int i2 = level.getHeight(Heightmap.Types.MOTION_BLOCKING, k1, j1);
                     int j2 = j - l;
                     int k2 = j + l;
                     if (j2 < i2) {
                        j2 = i2;
                     }

                     if (k2 < i2) {
                        k2 = i2;
                     }

                     int l2 = i2;
                     if (i2 < j) {
                        l2 = j;
                     }

                     if (j2 != k2) {
                        RandomSource randomsource = RandomSource.create((long)(k1 * k1 * 3121 + k1 * 45238971 ^ j1 * j1 * 418711 + j1 * 13761));
                        blockpos$mutableblockpos.set(k1, j2, j1);
                        Biome.Precipitation biome$precipitation = biome.getPrecipitationAt(blockpos$mutableblockpos);
                        if (biome$precipitation == Biome.Precipitation.RAIN) {
                           if (i1 != 0) {
                              if (i1 >= 0) {
                                 tesselator.end();
                              }

                              i1 = 0;
                              RenderSystem.setShaderTexture(0, RAIN_LOCATION);
                              bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                           }

                           int i3 = this.ticks + k1 * k1 * 3121 + k1 * 45238971 + j1 * j1 * 418711 + j1 * 13761 & 31;
                           float f2 = -((float)i3 + pPartialTick) / 32.0F * (3.0F + randomsource.nextFloat());
                           double d2 = (double)k1 + 0.5D - pCamX;
                           double d4 = (double)j1 + 0.5D - pCamZ;
                           float f3 = (float)Math.sqrt(d2 * d2 + d4 * d4) / (float)l;
                           float f4 = ((1.0F - f3 * f3) * 0.5F + 0.5F) * f;
                           blockpos$mutableblockpos.set(k1, l2, j1);
                           int j3 = getLightColor(level, blockpos$mutableblockpos);
                           bufferbuilder.vertex((double)k1 - pCamX - d0 + 0.5D, (double)k2 - pCamY, (double)j1 - pCamZ - d1 + 0.5D).uv(0.0F, (float)j2 * 0.25F + f2).color(1.0F, 1.0F, 1.0F, f4).uv2(j3).endVertex();
                           bufferbuilder.vertex((double)k1 - pCamX + d0 + 0.5D, (double)k2 - pCamY, (double)j1 - pCamZ + d1 + 0.5D).uv(1.0F, (float)j2 * 0.25F + f2).color(1.0F, 1.0F, 1.0F, f4).uv2(j3).endVertex();
                           bufferbuilder.vertex((double)k1 - pCamX + d0 + 0.5D, (double)j2 - pCamY, (double)j1 - pCamZ + d1 + 0.5D).uv(1.0F, (float)k2 * 0.25F + f2).color(1.0F, 1.0F, 1.0F, f4).uv2(j3).endVertex();
                           bufferbuilder.vertex((double)k1 - pCamX - d0 + 0.5D, (double)j2 - pCamY, (double)j1 - pCamZ - d1 + 0.5D).uv(0.0F, (float)k2 * 0.25F + f2).color(1.0F, 1.0F, 1.0F, f4).uv2(j3).endVertex();
                        } else if (biome$precipitation == Biome.Precipitation.SNOW) {
                           if (i1 != 1) {
                              if (i1 >= 0) {
                                 tesselator.end();
                              }

                              i1 = 1;
                              RenderSystem.setShaderTexture(0, SNOW_LOCATION);
                              bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                           }

                           float f5 = -((float)(this.ticks & 511) + pPartialTick) / 512.0F;
                           float f6 = (float)(randomsource.nextDouble() + (double)f1 * 0.01D * (double)((float)randomsource.nextGaussian()));
                           float f7 = (float)(randomsource.nextDouble() + (double)(f1 * (float)randomsource.nextGaussian()) * 0.001D);
                           double d3 = (double)k1 + 0.5D - pCamX;
                           double d5 = (double)j1 + 0.5D - pCamZ;
                           float f8 = (float)Math.sqrt(d3 * d3 + d5 * d5) / (float)l;
                           float f9 = ((1.0F - f8 * f8) * 0.3F + 0.5F) * f;
                           blockpos$mutableblockpos.set(k1, l2, j1);
                           int k3 = getLightColor(level, blockpos$mutableblockpos);
                           int l3 = k3 >> 16 & '\uffff';
                           int i4 = k3 & '\uffff';
                           int j4 = (l3 * 3 + 240) / 4;
                           int k4 = (i4 * 3 + 240) / 4;
                           bufferbuilder.vertex((double)k1 - pCamX - d0 + 0.5D, (double)k2 - pCamY, (double)j1 - pCamZ - d1 + 0.5D).uv(0.0F + f6, (float)j2 * 0.25F + f5 + f7).color(1.0F, 1.0F, 1.0F, f9).uv2(k4, j4).endVertex();
                           bufferbuilder.vertex((double)k1 - pCamX + d0 + 0.5D, (double)k2 - pCamY, (double)j1 - pCamZ + d1 + 0.5D).uv(1.0F + f6, (float)j2 * 0.25F + f5 + f7).color(1.0F, 1.0F, 1.0F, f9).uv2(k4, j4).endVertex();
                           bufferbuilder.vertex((double)k1 - pCamX + d0 + 0.5D, (double)j2 - pCamY, (double)j1 - pCamZ + d1 + 0.5D).uv(1.0F + f6, (float)k2 * 0.25F + f5 + f7).color(1.0F, 1.0F, 1.0F, f9).uv2(k4, j4).endVertex();
                           bufferbuilder.vertex((double)k1 - pCamX - d0 + 0.5D, (double)j2 - pCamY, (double)j1 - pCamZ - d1 + 0.5D).uv(0.0F + f6, (float)k2 * 0.25F + f5 + f7).color(1.0F, 1.0F, 1.0F, f9).uv2(k4, j4).endVertex();
                        }
                     }
                  }
               }
            }

            if (i1 >= 0) {
               tesselator.end();
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            pLightTexture.turnOffLightLayer();
         }

      }
   }

   public void tickRain(Camera pCamera) {
      if (!Reflector.IForgeDimensionSpecialEffects_tickRain.exists() || !Reflector.callBoolean(this.level.effects(), Reflector.IForgeDimensionSpecialEffects_tickRain, this.level, this.ticks, pCamera)) {
         float f = this.minecraft.level.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F);
         if (!Config.isRainFancy()) {
            f /= 2.0F;
         }

         if (!(f <= 0.0F) && Config.isRainSplash()) {
            RandomSource randomsource = RandomSource.create((long)this.ticks * 312987231L);
            LevelReader levelreader = this.minecraft.level;
            BlockPos blockpos = BlockPos.containing(pCamera.getPosition());
            BlockPos blockpos1 = null;
            int i = (int)(100.0F * f * f) / (this.minecraft.options.particles().get() == ParticleStatus.DECREASED ? 2 : 1);

            for(int j = 0; j < i; ++j) {
               int k = randomsource.nextInt(21) - 10;
               int l = randomsource.nextInt(21) - 10;
               BlockPos blockpos2 = levelreader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos.offset(k, 0, l));
               if (blockpos2.getY() > levelreader.getMinBuildHeight() && blockpos2.getY() <= blockpos.getY() + 10 && blockpos2.getY() >= blockpos.getY() - 10) {
                  Biome biome = levelreader.getBiome(blockpos2).value();
                  if (biome.getPrecipitationAt(blockpos2) == Biome.Precipitation.RAIN) {
                     blockpos1 = blockpos2.below();
                     if (this.minecraft.options.particles().get() == ParticleStatus.MINIMAL) {
                        break;
                     }

                     double d0 = randomsource.nextDouble();
                     double d1 = randomsource.nextDouble();
                     BlockState blockstate = levelreader.getBlockState(blockpos1);
                     FluidState fluidstate = levelreader.getFluidState(blockpos1);
                     VoxelShape voxelshape = blockstate.getCollisionShape(levelreader, blockpos1);
                     double d2 = voxelshape.max(Direction.Axis.Y, d0, d1);
                     double d3 = (double)fluidstate.getHeight(levelreader, blockpos1);
                     double d4 = Math.max(d2, d3);
                     ParticleOptions particleoptions = !fluidstate.is(FluidTags.LAVA) && !blockstate.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(blockstate) ? ParticleTypes.RAIN : ParticleTypes.SMOKE;
                     this.minecraft.level.addParticle(particleoptions, (double)blockpos1.getX() + d0, (double)blockpos1.getY() + d4, (double)blockpos1.getZ() + d1, 0.0D, 0.0D, 0.0D);
                  }
               }
            }

            if (blockpos1 != null && randomsource.nextInt(3) < this.rainSoundTime++) {
               this.rainSoundTime = 0;
               if (blockpos1.getY() > blockpos.getY() + 1 && levelreader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos).getY() > Mth.floor((float)blockpos.getY())) {
                  this.minecraft.level.playLocalSound(blockpos1, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
               } else {
                  this.minecraft.level.playLocalSound(blockpos1, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
               }
            }
         }

      }
   }

   public void close() {
      if (this.entityEffect != null) {
         this.entityEffect.close();
      }

      if (this.transparencyChain != null) {
         this.transparencyChain.close();
      }

   }

   public void onResourceManagerReload(ResourceManager pResourceManager) {
      this.initOutline();
      if (Minecraft.useShaderTransparency()) {
         this.initTransparency();
      }

   }

   public void initOutline() {
      if (this.entityEffect != null) {
         this.entityEffect.close();
      }

      ResourceLocation resourcelocation = new ResourceLocation("shaders/post/entity_outline.json");

      try {
         this.entityEffect = new PostChain(this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), resourcelocation);
         this.entityEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
         this.entityTarget = this.entityEffect.getTempTarget("final");
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to load shader: {}", resourcelocation, ioexception);
         this.entityEffect = null;
         this.entityTarget = null;
      } catch (JsonSyntaxException jsonsyntaxexception) {
         LOGGER.warn("Failed to parse shader: {}", resourcelocation, jsonsyntaxexception);
         this.entityEffect = null;
         this.entityTarget = null;
      }

   }

   private void initTransparency() {
      this.deinitTransparency();
      ResourceLocation resourcelocation = new ResourceLocation("shaders/post/transparency.json");

      try {
         PostChain postchain = new PostChain(this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), resourcelocation);
         postchain.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
         RenderTarget rendertarget1 = postchain.getTempTarget("translucent");
         RenderTarget rendertarget2 = postchain.getTempTarget("itemEntity");
         RenderTarget rendertarget3 = postchain.getTempTarget("particles");
         RenderTarget rendertarget4 = postchain.getTempTarget("weather");
         RenderTarget rendertarget = postchain.getTempTarget("clouds");
         this.transparencyChain = postchain;
         this.translucentTarget = rendertarget1;
         this.itemEntityTarget = rendertarget2;
         this.particlesTarget = rendertarget3;
         this.weatherTarget = rendertarget4;
         this.cloudsTarget = rendertarget;
      } catch (Exception exception1) {
         String s = exception1 instanceof JsonSyntaxException ? "parse" : "load";
         String s1 = "Failed to " + s + " shader: " + resourcelocation;
         LevelRenderer.TransparencyShaderException levelrenderer$transparencyshaderexception = new LevelRenderer.TransparencyShaderException(s1, exception1);
         if (this.minecraft.getResourcePackRepository().getSelectedIds().size() > 1) {
            Component component = this.minecraft.getResourceManager().listPacks().findFirst().map((p_244728_0_) -> {
               return Component.literal(p_244728_0_.packId());
            }).orElse((MutableComponent)null);
            this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
            this.minecraft.clearResourcePacksOnError(levelrenderer$transparencyshaderexception, component);
         } else {
            CrashReport crashreport = this.minecraft.fillReport(new CrashReport(s1, levelrenderer$transparencyshaderexception));
            this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
            this.minecraft.options.save();
            LOGGER.error(LogUtils.FATAL_MARKER, s1, (Throwable)levelrenderer$transparencyshaderexception);
            this.minecraft.emergencySave();
            Minecraft.crash(crashreport);
         }
      }

   }

   private void deinitTransparency() {
      if (this.transparencyChain != null) {
         this.transparencyChain.close();
         this.translucentTarget.destroyBuffers();
         this.itemEntityTarget.destroyBuffers();
         this.particlesTarget.destroyBuffers();
         this.weatherTarget.destroyBuffers();
         this.cloudsTarget.destroyBuffers();
         this.transparencyChain = null;
         this.translucentTarget = null;
         this.itemEntityTarget = null;
         this.particlesTarget = null;
         this.weatherTarget = null;
         this.cloudsTarget = null;
      }

   }

   public void doEntityOutline() {
      if (this.shouldShowEntityOutlines()) {
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
         this.entityTarget.blitToScreen(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), false);
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
      }

   }

   public boolean shouldShowEntityOutlines() {
      if (!Config.isShaders() && !Config.isAntialiasing()) {
         return !this.minecraft.gameRenderer.isPanoramicMode() && this.entityTarget != null && this.entityEffect != null && this.minecraft.player != null;
      } else {
         return false;
      }
   }

   private void createDarkSky() {
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      if (this.darkBuffer != null) {
         this.darkBuffer.close();
      }

      this.darkBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
      BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = buildSkyDisc(bufferbuilder, -16.0F);
      this.darkBuffer.bind();
      this.darkBuffer.upload(bufferbuilder$renderedbuffer);
      VertexBuffer.unbind();
   }

   private void createLightSky() {
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      if (this.skyBuffer != null) {
         this.skyBuffer.close();
      }

      this.skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
      BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = buildSkyDisc(bufferbuilder, 16.0F);
      this.skyBuffer.bind();
      this.skyBuffer.upload(bufferbuilder$renderedbuffer);
      VertexBuffer.unbind();
   }

   private static BufferBuilder.RenderedBuffer buildSkyDisc(BufferBuilder pBuilder, float pY) {
      float f = Math.signum(pY) * 512.0F;
      float f1 = 512.0F;
      RenderSystem.setShader(GameRenderer::getPositionShader);
      pBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
      pBuilder.vertex(0.0D, (double)pY, 0.0D).endVertex();

      for(int i = -180; i <= 180; i += 45) {
         pBuilder.vertex((double)(f * Mth.cos((float)i * ((float)Math.PI / 180F))), (double)pY, (double)(512.0F * Mth.sin((float)i * ((float)Math.PI / 180F)))).endVertex();
      }

      return pBuilder.end();
   }

   private void createStars() {
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionShader);
      if (this.starBuffer != null) {
         this.starBuffer.close();
      }

      this.starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
      BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = this.drawStars(bufferbuilder);
      this.starBuffer.bind();
      this.starBuffer.upload(bufferbuilder$renderedbuffer);
      VertexBuffer.unbind();
   }

   private BufferBuilder.RenderedBuffer drawStars(BufferBuilder pBuilder) {
      RandomSource randomsource = RandomSource.create(10842L);
      pBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

      for(int i = 0; i < 1500; ++i) {
         double d0 = (double)(randomsource.nextFloat() * 2.0F - 1.0F);
         double d1 = (double)(randomsource.nextFloat() * 2.0F - 1.0F);
         double d2 = (double)(randomsource.nextFloat() * 2.0F - 1.0F);
         double d3 = (double)(0.15F + randomsource.nextFloat() * 0.1F);
         double d4 = d0 * d0 + d1 * d1 + d2 * d2;
         if (d4 < 1.0D && d4 > 0.01D) {
            d4 = 1.0D / Math.sqrt(d4);
            d0 *= d4;
            d1 *= d4;
            d2 *= d4;
            double d5 = d0 * 100.0D;
            double d6 = d1 * 100.0D;
            double d7 = d2 * 100.0D;
            double d8 = Math.atan2(d0, d2);
            double d9 = Math.sin(d8);
            double d10 = Math.cos(d8);
            double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
            double d12 = Math.sin(d11);
            double d13 = Math.cos(d11);
            double d14 = randomsource.nextDouble() * Math.PI * 2.0D;
            double d15 = Math.sin(d14);
            double d16 = Math.cos(d14);

            for(int j = 0; j < 4; ++j) {
               double d17 = 0.0D;
               double d18 = (double)((j & 2) - 1) * d3;
               double d19 = (double)((j + 1 & 2) - 1) * d3;
               double d20 = 0.0D;
               double d21 = d18 * d16 - d19 * d15;
               double d22 = d19 * d16 + d18 * d15;
               double d23 = d21 * d12 + 0.0D * d13;
               double d24 = 0.0D * d12 - d21 * d13;
               double d25 = d24 * d9 - d22 * d10;
               double d26 = d22 * d9 + d24 * d10;
               pBuilder.vertex(d5 + d25, d6 + d23, d7 + d26).endVertex();
            }
         }
      }

      return pBuilder.end();
   }

   public void setLevel(@Nullable ClientLevel pLevel) {
      this.lastCameraX = Double.MIN_VALUE;
      this.lastCameraY = Double.MIN_VALUE;
      this.lastCameraZ = Double.MIN_VALUE;
      this.lastCameraChunkX = Integer.MIN_VALUE;
      this.lastCameraChunkY = Integer.MIN_VALUE;
      this.lastCameraChunkZ = Integer.MIN_VALUE;
      this.entityRenderDispatcher.setLevel(pLevel);
      this.level = pLevel;
      if (Config.isDynamicLights()) {
         DynamicLights.clear();
      }

      ChunkVisibility.reset();
      this.renderEnv.reset((BlockState)null, (BlockPos)null);
      BiomeUtils.onWorldChanged(this.level);
      Shaders.checkWorldChanged(this.level);
      if (pLevel != null) {
         this.allChanged();
      } else {
         if (this.viewArea != null) {
            this.viewArea.releaseAllBuffers();
            this.viewArea = null;
         }

         if (this.chunkRenderDispatcher != null) {
            this.chunkRenderDispatcher.dispose();
         }

         this.chunkRenderDispatcher = null;
         this.globalBlockEntities.clear();
         this.renderChunkStorage.set((LevelRenderer.RenderChunkStorage)null);
         this.renderChunksInFrustum.clear();
         this.clearRenderInfos();
      }

   }

   public void graphicsChanged() {
      if (Minecraft.useShaderTransparency()) {
         this.initTransparency();
      } else {
         this.deinitTransparency();
      }

   }

   public void allChanged() {
      if (this.level != null) {
         this.graphicsChanged();
         this.level.clearTintCaches();
         if (this.chunkRenderDispatcher == null) {
            this.chunkRenderDispatcher = new ChunkRenderDispatcher(this.level, this, Util.backgroundExecutor(), this.minecraft.is64Bit(), this.renderBuffers.fixedBufferPack());
         } else {
            this.chunkRenderDispatcher.setLevel(this.level);
         }

         this.needsFullRenderChunkUpdate = true;
         this.generateClouds = true;
         this.recentlyCompiledChunks.clear();
         ItemBlockRenderTypes.setFancy(Config.isTreesFancy());
         ModelBlockRenderer.updateAoLightValue();
         if (Config.isDynamicLights()) {
            DynamicLights.clear();
         }

         SmartAnimations.update();
         ambientOcclusion = Minecraft.useAmbientOcclusion();
         this.lastViewDistance = this.minecraft.options.getEffectiveRenderDistance();
         this.renderDistance = this.lastViewDistance * 16;
         this.renderDistanceSq = this.renderDistance * this.renderDistance;
         double d0 = (double)((this.lastViewDistance + 1) * 16);
         this.renderDistanceXZSq = (int)(d0 * d0);
         if (this.viewArea != null) {
            this.viewArea.releaseAllBuffers();
         }

         GpuMemory.bufferFreed(GpuMemory.getBufferAllocated());
         this.chunkRenderDispatcher.blockUntilClear();
         synchronized(this.globalBlockEntities) {
            this.globalBlockEntities.clear();
         }

         this.viewArea = new ViewArea(this.chunkRenderDispatcher, this.level, this.minecraft.options.getEffectiveRenderDistance(), this);
         if (this.lastFullRenderChunkUpdate != null) {
            try {
               this.lastFullRenderChunkUpdate.get();
               this.lastFullRenderChunkUpdate = null;
            } catch (Exception exception) {
               LOGGER.warn("Full update failed", (Throwable)exception);
            }
         }

         this.renderChunkStorage.set(new LevelRenderer.RenderChunkStorage(this.viewArea.chunks.length));
         this.renderChunksInFrustum.clear();
         this.clearRenderInfos();
         this.killFrustum();
         Entity entity = this.minecraft.getCameraEntity();
         if (entity != null) {
            this.viewArea.repositionCamera(entity.getX(), entity.getZ());
         }
      }

   }

   public void resize(int pWidth, int pHeight) {
      this.needsUpdate();
      if (this.entityEffect != null) {
         this.entityEffect.resize(pWidth, pHeight);
      }

      if (this.transparencyChain != null) {
         this.transparencyChain.resize(pWidth, pHeight);
      }

   }

   public String getChunkStatistics() {
      int i = this.viewArea.chunks.length;
      int j = this.countRenderedChunks();
      return String.format(Locale.ROOT, "C: %d/%d %sD: %d, %s", j, i, this.minecraft.smartCull ? "(s) " : "", this.lastViewDistance, this.chunkRenderDispatcher == null ? "null" : this.chunkRenderDispatcher.getStats());
   }

   public ChunkRenderDispatcher getChunkRenderDispatcher() {
      return this.chunkRenderDispatcher;
   }

   public double getTotalChunks() {
      return (double)this.viewArea.chunks.length;
   }

   public double getLastViewDistance() {
      return (double)this.lastViewDistance;
   }

   public int countRenderedChunks() {
      return this.renderInfosTerrain.size();
   }

   public String getEntityStatistics() {
      return "E: " + this.renderedEntities + "/" + this.level.getEntityCount() + ", B: " + this.culledEntities + ", SD: " + this.level.getServerSimulationDistance() + ", " + Config.getVersionDebug();
   }

   private void setupRender(Camera pCamera, Frustum pFrustum, boolean pHasCapturedFrustum, boolean pIsSpectator) {
      Vec3 vec3 = pCamera.getPosition();
      if (this.minecraft.options.getEffectiveRenderDistance() != this.lastViewDistance) {
         this.allChanged();
      }

      this.level.getProfiler().push("camera");
      double d0 = this.minecraft.player.getX();
      double d1 = this.minecraft.player.getY();
      double d2 = this.minecraft.player.getZ();
      int i = SectionPos.posToSectionCoord(d0);
      int j = SectionPos.posToSectionCoord(d1);
      int k = SectionPos.posToSectionCoord(d2);
      if (this.lastCameraChunkX != i || this.lastCameraChunkY != j || this.lastCameraChunkZ != k) {
         this.lastCameraX = d0;
         this.lastCameraY = d1;
         this.lastCameraZ = d2;
         this.lastCameraChunkX = i;
         this.lastCameraChunkY = j;
         this.lastCameraChunkZ = k;
         this.viewArea.repositionCamera(d0, d2);
      }

      if (Config.isDynamicLights()) {
         DynamicLights.update(this);
      }

      this.chunkRenderDispatcher.setCamera(vec3);
      this.level.getProfiler().popPush("cull");
      this.minecraft.getProfiler().popPush("culling");
      BlockPos blockpos = pCamera.getBlockPosition();
      double d3 = Math.floor(vec3.x / 8.0D);
      double d4 = Math.floor(vec3.y / 8.0D);
      double d5 = Math.floor(vec3.z / 8.0D);
      this.needsFullRenderChunkUpdate = this.needsFullRenderChunkUpdate || d3 != this.prevCamX || d4 != this.prevCamY || d5 != this.prevCamZ;
      this.nextFullUpdateMillis.updateAndGet((p_234308_1_) -> {
         if (p_234308_1_ > 0L && System.currentTimeMillis() > p_234308_1_) {
            this.needsFullRenderChunkUpdate = true;
            return 0L;
         } else {
            return p_234308_1_;
         }
      });
      this.prevCamX = d3;
      this.prevCamY = d4;
      this.prevCamZ = d5;
      this.minecraft.getProfiler().popPush("update");
      boolean flag = this.minecraft.smartCull;
      if (pIsSpectator && this.level.getBlockState(blockpos).isSolidRender(this.level, blockpos)) {
         flag = false;
      }

      Lagometer.timerVisibility.start();
      if (!pHasCapturedFrustum) {
         if (this.needsFullRenderChunkUpdate && (this.lastFullRenderChunkUpdate == null || this.lastFullRenderChunkUpdate.isDone())) {
            this.minecraft.getProfiler().push("full_update_schedule");
            this.needsFullRenderChunkUpdate = false;
            boolean flag1 = flag;
            this.lastFullRenderChunkUpdate = Util.backgroundExecutor().submit(() -> {
               Queue<LevelRenderer.RenderChunkInfo> queue1 = Queues.newArrayDeque();
               this.initializeQueueForFullUpdate(pCamera, queue1);
               LevelRenderer.RenderChunkStorage levelrenderer$renderchunkstorage1 = new LevelRenderer.RenderChunkStorage(this.viewArea.chunks.length);
               queue1.forEach((info) -> {
                  levelrenderer$renderchunkstorage1.renderInfoMap.put(info.chunk, info);
               });
               this.updateRenderChunks(levelrenderer$renderchunkstorage1, levelrenderer$renderchunkstorage1.renderInfoMap, vec3, queue1, flag1);
               this.renderChunkStorage.set(levelrenderer$renderchunkstorage1);
               this.needsFrustumUpdate.set(true);
            });
            this.minecraft.getProfiler().pop();
         }

         LevelRenderer.RenderChunkStorage levelrenderer$renderchunkstorage = this.renderChunkStorage.get();
         if (!this.recentlyCompiledChunks.isEmpty()) {
            this.minecraft.getProfiler().push("partial_update");
            Queue<LevelRenderer.RenderChunkInfo> queue = Queues.newArrayDeque();

            while(!this.recentlyCompiledChunks.isEmpty()) {
               ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.recentlyCompiledChunks.poll();
               LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo = levelrenderer$renderchunkstorage.renderInfoMap.get(chunkrenderdispatcher$renderchunk);
               if (levelrenderer$renderchunkinfo != null && levelrenderer$renderchunkinfo.chunk == chunkrenderdispatcher$renderchunk) {
                  queue.add(levelrenderer$renderchunkinfo);
               }
            }

            this.updateRenderChunks(levelrenderer$renderchunkstorage, levelrenderer$renderchunkstorage.renderInfoMap, vec3, queue, flag);
            this.needsFrustumUpdate.set(true);
            this.minecraft.getProfiler().pop();
         }

         double d6 = Math.floor((double)(pCamera.getXRot() / 2.0F));
         double d7 = Math.floor((double)(pCamera.getYRot() / 2.0F));
         boolean flag2 = false;
         if (this.needsFrustumUpdate.compareAndSet(true, false) || d6 != this.prevCamRotX || d7 != this.prevCamRotY) {
            this.applyFrustum((new Frustum(pFrustum)).offsetToFullyIncludeCameraCube(8));
            this.prevCamRotX = d6;
            this.prevCamRotY = d7;
            flag2 = true;
            ShadersRender.frustumTerrainShadowChanged = true;
         }

         if (this.level.getSectionStorage().resetUpdated() || flag2) {
            this.applyFrustumEntities(pFrustum, -1);
            ShadersRender.frustumEntitiesShadowChanged = true;
         }
      }

      Lagometer.timerVisibility.end();
      this.minecraft.getProfiler().pop();
   }

   private void applyFrustum(Frustum pFrustum) {
      this.applyFrustum(pFrustum, true, -1);
   }

   public void applyFrustum(Frustum frustumIn, boolean updateRenderInfos, int maxChunkDistance) {
      if (!Minecraft.getInstance().isSameThread()) {
         throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
      } else {
         this.minecraft.getProfiler().push("apply_frustum");
         if (updateRenderInfos) {
            this.renderChunksInFrustum.clear();
         }

         this.clearRenderInfosTerrain();
         int i = (int)frustumIn.getCameraX() >> 4 << 4;
         int j = (int)frustumIn.getCameraY() >> 4 << 4;
         int k = (int)frustumIn.getCameraZ() >> 4 << 4;
         int l = maxChunkDistance * maxChunkDistance;

         for(LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo : (this.renderChunkStorage.get()).renderChunks) {
            if (frustumIn.isVisible(levelrenderer$renderchunkinfo.chunk.getBoundingBox())) {
               if (maxChunkDistance > 0) {
                  BlockPos blockpos = levelrenderer$renderchunkinfo.chunk.getOrigin();
                  int i1 = i - blockpos.getX();
                  int j1 = j - blockpos.getY();
                  int k1 = k - blockpos.getZ();
                  int l1 = i1 * i1 + j1 * j1 + k1 * k1;
                  if (l1 > l) {
                     continue;
                  }
               }

               if (updateRenderInfos) {
                  this.renderChunksInFrustum.add(levelrenderer$renderchunkinfo);
               }

               ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher$compiledchunk = levelrenderer$renderchunkinfo.chunk.getCompiledChunk();
               if (!chunkrenderdispatcher$compiledchunk.hasNoRenderableLayers()) {
                  this.renderInfosTerrain.add(levelrenderer$renderchunkinfo);
               }

               if (!chunkrenderdispatcher$compiledchunk.getRenderableBlockEntities().isEmpty()) {
                  this.renderInfosTileEntities.add(levelrenderer$renderchunkinfo);
               }
            }
         }

         this.minecraft.getProfiler().pop();
      }
   }

   private void initializeQueueForFullUpdate(Camera pCamera, Queue<LevelRenderer.RenderChunkInfo> pInfoQueue) {
      int i = 16;
      Vec3 vec3 = pCamera.getPosition();
      BlockPos blockpos = pCamera.getBlockPosition();
      ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.viewArea.getRenderChunkAt(blockpos);
      if (chunkrenderdispatcher$renderchunk == null) {
         boolean flag = blockpos.getY() > this.level.getMinBuildHeight();
         int j = flag ? this.level.getMaxBuildHeight() - 8 : this.level.getMinBuildHeight() + 8;
         int k = Mth.floor(vec3.x / 16.0D) * 16;
         int l = Mth.floor(vec3.z / 16.0D) * 16;
         List<LevelRenderer.RenderChunkInfo> list = Lists.newArrayList();

         for(int i1 = -this.lastViewDistance; i1 <= this.lastViewDistance; ++i1) {
            for(int j1 = -this.lastViewDistance; j1 <= this.lastViewDistance; ++j1) {
               ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk1 = this.viewArea.getRenderChunkAt(new BlockPos(k + SectionPos.sectionToBlockCoord(i1, 8), j, l + SectionPos.sectionToBlockCoord(j1, 8)));
               if (chunkrenderdispatcher$renderchunk1 != null) {
                  list.add(chunkrenderdispatcher$renderchunk1.getRenderInfo((Direction)null, 0));
               }
            }
         }

         list.sort(Comparator.comparingDouble((p_234301_1_) -> {
            return blockpos.distSqr(p_234301_1_.chunk.getOrigin().offset(8, 8, 8));
         }));
         pInfoQueue.addAll(list);
      } else {
         pInfoQueue.add(chunkrenderdispatcher$renderchunk.getRenderInfo((Direction)null, 0));
      }

   }

   public void addRecentlyCompiledChunk(ChunkRenderDispatcher.RenderChunk pRenderChunk) {
      this.recentlyCompiledChunks.add(pRenderChunk);
   }

   private void updateRenderChunks(LevelRenderer.RenderChunkStorage renderChunkStorage, LevelRenderer.RenderInfoMap renderInfoMap, Vec3 viewPos, Queue<LevelRenderer.RenderChunkInfo> renderQueue, boolean smartCull) {
      Set<LevelRenderer.RenderChunkInfo> set = renderChunkStorage.renderChunks;
      EntitySectionStorage entitysectionstorage = this.level.getSectionStorage();
      int i = 16;
      BlockPos blockpos = new BlockPos(Mth.floor(viewPos.x / 16.0D) * 16, Mth.floor(viewPos.y / 16.0D) * 16, Mth.floor(viewPos.z / 16.0D) * 16);
      BlockPos blockpos1 = blockpos.offset(8, 8, 8);
      Entity.setViewScale(Mth.clamp((double)this.minecraft.options.getEffectiveRenderDistance() / 8.0D, 1.0D, 2.5D) * this.minecraft.options.entityDistanceScaling().get());

      while(!renderQueue.isEmpty()) {
         LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo = renderQueue.poll();
         ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = levelrenderer$renderchunkinfo.chunk;
         ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk1 = levelrenderer$renderchunkinfo.chunk;
         ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher$compiledchunk = chunkrenderdispatcher$renderchunk1.compiled.get();
         boolean flag = chunkrenderdispatcher$compiledchunk.hasTerrainBlockEntities();
         if (flag || chunkrenderdispatcher$renderchunk1.isDirty()) {
            set.add(levelrenderer$renderchunkinfo);
         }

         boolean flag1 = Math.abs(chunkrenderdispatcher$renderchunk.getOrigin().getX() - blockpos.getX()) > 60 || Math.abs(chunkrenderdispatcher$renderchunk.getOrigin().getY() - blockpos.getY()) > 60 || Math.abs(chunkrenderdispatcher$renderchunk.getOrigin().getZ() - blockpos.getZ()) > 60;

         for(Direction direction : DIRECTIONS) {
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk2 = this.getRelativeFrom(blockpos, chunkrenderdispatcher$renderchunk, direction);
            if (chunkrenderdispatcher$renderchunk2 != null && (!smartCull || !levelrenderer$renderchunkinfo.hasDirection(direction.getOpposite()))) {
               if (smartCull && levelrenderer$renderchunkinfo.hasSourceDirections()) {
                  ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher$compiledchunk1 = chunkrenderdispatcher$renderchunk.getCompiledChunk();
                  boolean flag2 = false;

                  for(int j = 0; j < DIRECTIONS.length; ++j) {
                     if (levelrenderer$renderchunkinfo.hasSourceDirection(j) && chunkrenderdispatcher$compiledchunk1.facesCanSeeEachother(DIRECTIONS[j].getOpposite(), direction)) {
                        flag2 = true;
                        break;
                     }
                  }

                  if (!flag2) {
                     continue;
                  }
               }

               if (smartCull && flag1) {
                  BlockPos blockpos2;
                  byte b1;
                  label135: {
                     label134: {
                        blockpos2 = chunkrenderdispatcher$renderchunk2.getOrigin();
                        if (direction.getAxis() == Direction.Axis.X) {
                           if (blockpos1.getX() <= blockpos2.getX()) {
                              break label134;
                           }
                        } else if (blockpos1.getX() >= blockpos2.getX()) {
                           break label134;
                        }

                        b1 = 16;
                        break label135;
                     }

                     b1 = 0;
                  }

                  byte b2;
                  label127: {
                     label126: {
                        if (direction.getAxis() == Direction.Axis.Y) {
                           if (blockpos1.getY() <= blockpos2.getY()) {
                              break label126;
                           }
                        } else if (blockpos1.getY() >= blockpos2.getY()) {
                           break label126;
                        }

                        b2 = 16;
                        break label127;
                     }

                     b2 = 0;
                  }

                  byte b0;
                  label119: {
                     label118: {
                        if (direction.getAxis() == Direction.Axis.Z) {
                           if (blockpos1.getZ() <= blockpos2.getZ()) {
                              break label118;
                           }
                        } else if (blockpos1.getZ() >= blockpos2.getZ()) {
                           break label118;
                        }

                        b0 = 16;
                        break label119;
                     }

                     b0 = 0;
                  }

                  Vec3M vec3m = renderChunkStorage.vec3M1.set((double)blockpos2.getX() + (double)b1, (double)blockpos2.getY() + (double)b2, (double)blockpos2.getZ() + (double)b0);
                  Vec3M vec3m1 = renderChunkStorage.vec3M2.set(viewPos).subtract(vec3m).normalize().scale(CEILED_SECTION_DIAGONAL);
                  boolean flag3 = true;

                  while(renderChunkStorage.vec3M3.set(viewPos).subtract(vec3m).lengthSquared() > 3600.0D) {
                     vec3m = vec3m.add(vec3m1);
                     if (vec3m.y > (double)this.level.getMaxBuildHeight() || vec3m.y < (double)this.level.getMinBuildHeight()) {
                        break;
                     }

                     ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk3 = this.viewArea.getRenderChunkAt(renderChunkStorage.blockPosM1.setXyz(vec3m.x, vec3m.y, vec3m.z));
                     if (chunkrenderdispatcher$renderchunk3 == null || renderInfoMap.get(chunkrenderdispatcher$renderchunk3) == null) {
                        flag3 = false;
                        break;
                     }
                  }

                  if (!flag3) {
                     continue;
                  }
               }

               LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo1 = renderInfoMap.get(chunkrenderdispatcher$renderchunk2);
               if (levelrenderer$renderchunkinfo1 != null) {
                  levelrenderer$renderchunkinfo1.addSourceDirection(direction);
               } else if (!chunkrenderdispatcher$renderchunk2.hasAllNeighbors()) {
                  if (!this.closeToBorder(blockpos, chunkrenderdispatcher$renderchunk)) {
                     this.nextFullUpdateMillis.set(System.currentTimeMillis() + 500L);
                  }
               } else {
                  LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo2 = chunkrenderdispatcher$renderchunk2.getRenderInfo(direction, levelrenderer$renderchunkinfo.step + 1);
                  levelrenderer$renderchunkinfo2.setDirection(levelrenderer$renderchunkinfo.directions, direction);
                  renderQueue.add(levelrenderer$renderchunkinfo2);
                  renderInfoMap.put(chunkrenderdispatcher$renderchunk2, levelrenderer$renderchunkinfo2);
               }
            }
         }
      }

   }

   @Nullable
   private ChunkRenderDispatcher.RenderChunk getRelativeFrom(BlockPos pCameraChunkPos, ChunkRenderDispatcher.RenderChunk pRenderChunk, Direction pFacing) {
      BlockPos blockpos = pRenderChunk.getRelativeOrigin(pFacing);
      if (blockpos.getY() >= this.level.getMinBuildHeight() && blockpos.getY() < this.level.getMaxBuildHeight()) {
         if (Mth.abs(pCameraChunkPos.getY() - blockpos.getY()) > this.renderDistance) {
            return null;
         } else {
            int i = pCameraChunkPos.getX() - blockpos.getX();
            int j = pCameraChunkPos.getZ() - blockpos.getZ();
            int k = i * i + j * j;
            return k > this.renderDistanceXZSq ? null : this.viewArea.getRenderChunkAt(blockpos);
         }
      } else {
         return null;
      }
   }

   private boolean closeToBorder(BlockPos pPos, ChunkRenderDispatcher.RenderChunk pChunk) {
      int i = SectionPos.blockToSectionCoord(pPos.getX());
      int j = SectionPos.blockToSectionCoord(pPos.getZ());
      BlockPos blockpos = pChunk.getOrigin();
      int k = SectionPos.blockToSectionCoord(blockpos.getX());
      int l = SectionPos.blockToSectionCoord(blockpos.getZ());
      return !ChunkMap.isChunkInRange(k, l, i, j, this.lastViewDistance - 3);
   }

   private void captureFrustum(Matrix4f pViewMatrix, Matrix4f pProjectionMatrix, double pCamX, double pCamY, double pCamZ, Frustum pCapturedFrustrum) {
      this.capturedFrustum = pCapturedFrustrum;
      Matrix4f matrix4f = new Matrix4f(pProjectionMatrix);
      matrix4f.mul(pViewMatrix);
      matrix4f.invert();
      this.frustumPos.x = pCamX;
      this.frustumPos.y = pCamY;
      this.frustumPos.z = pCamZ;
      this.frustumPoints[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
      this.frustumPoints[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
      this.frustumPoints[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
      this.frustumPoints[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
      this.frustumPoints[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
      this.frustumPoints[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
      this.frustumPoints[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.frustumPoints[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

      for(int i = 0; i < 8; ++i) {
         matrix4f.transform(this.frustumPoints[i]);
         this.frustumPoints[i].div(this.frustumPoints[i].w());
      }

   }

   public void prepareCullFrustum(PoseStack pPoseStack, Vec3 pCameraPos, Matrix4f pProjectionMatrix) {
      Matrix4f matrix4f = pPoseStack.last().pose();
      double d0 = pCameraPos.x();
      double d1 = pCameraPos.y();
      double d2 = pCameraPos.z();
      this.cullingFrustum = new Frustum(matrix4f, pProjectionMatrix);
      this.cullingFrustum.prepare(d0, d1, d2);
      if (Config.isShaders() && !Shaders.isFrustumCulling()) {
         this.cullingFrustum.disabled = true;
      }

   }

   public void renderLevel(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix) {
      RenderSystem.setShaderGameTime(this.level.getGameTime(), pPartialTick);
      this.blockEntityRenderDispatcher.prepare(this.level, pCamera, this.minecraft.hitResult);
      this.entityRenderDispatcher.prepare(this.level, pCamera, this.minecraft.crosshairPickEntity);
      ProfilerFiller profilerfiller = this.level.getProfiler();
      profilerfiller.popPush("light_update_queue");
      this.level.pollLightUpdates();
      profilerfiller.popPush("light_updates");
      this.level.getChunkSource().getLightEngine().runLightUpdates();
      Vec3 vec3 = pCamera.getPosition();
      double d0 = vec3.x();
      double d1 = vec3.y();
      double d2 = vec3.z();
      Matrix4f matrix4f = pPoseStack.last().pose();
      profilerfiller.popPush("culling");
      boolean flag = this.capturedFrustum != null;
      Frustum frustum;
      if (flag) {
         frustum = this.capturedFrustum;
         frustum.prepare(this.frustumPos.x, this.frustumPos.y, this.frustumPos.z);
      } else {
         frustum = this.cullingFrustum;
      }

      this.minecraft.getProfiler().popPush("captureFrustum");
      if (this.captureFrustum) {
         this.captureFrustum(matrix4f, pProjectionMatrix, vec3.x, vec3.y, vec3.z, flag ? new Frustum(matrix4f, pProjectionMatrix) : frustum);
         this.captureFrustum = false;
         frustum = this.capturedFrustum;
         frustum.disabled = Config.isShaders() && !Shaders.isFrustumCulling();
         frustum.prepare(vec3.x, vec3.y, vec3.z);
         this.applyFrustum(frustum, false, -1);
         this.applyFrustumEntities(frustum, -1);
      }

      if (this.debugFixTerrainFrustumShadow) {
         this.captureFrustum(matrix4f, pProjectionMatrix, vec3.x, vec3.y, vec3.z, ShadersRender.makeShadowFrustum(pCamera, pPartialTick));
         this.debugFixTerrainFrustumShadow = false;
         frustum = this.capturedFrustum;
         frustum.prepare(vec3.x, vec3.y, vec3.z);
         ShadersRender.frustumTerrainShadowChanged = true;
         ShadersRender.frustumEntitiesShadowChanged = true;
         ShadersRender.applyFrustumShadow(this, frustum);
      }

      profilerfiller.popPush("clear");
      if (Config.isShaders()) {
         Shaders.setViewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
      } else {
         RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
      }

      FogRenderer.setupColor(pCamera, pPartialTick, this.minecraft.level, this.minecraft.options.getEffectiveRenderDistance(), pGameRenderer.getDarkenWorldAmount(pPartialTick));
      FogRenderer.levelFogColor();
      RenderSystem.clear(16640, Minecraft.ON_OSX);
      boolean flag1 = Config.isShaders();
      if (flag1) {
         Shaders.clearRenderBuffer();
         Shaders.setCamera(pPoseStack, pCamera, pPartialTick);
         Shaders.renderPrepare();
      }

      float f = pGameRenderer.getRenderDistance();
      boolean flag2 = this.minecraft.level.effects().isFoggyAt(Mth.floor(d0), Mth.floor(d1)) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
      boolean flag3 = this.minecraft.level.effects().isFoggyAt(Mth.floor(d0), Mth.floor(d1));
      if ((Config.isSkyEnabled() || Config.isSunMoonEnabled() || Config.isStarsEnabled()) && !Shaders.isShadowPass) {
         profilerfiller.popPush("sky");
         if (flag1) {
            Shaders.beginSky();
         }

         RenderSystem.setShader(GameRenderer::getPositionShader);

         this.renderSky(pPoseStack, pProjectionMatrix, pPartialTick, pCamera, flag3, () -> {
            FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_SKY, f, flag2, pPartialTick);
         });
         if (flag1) {
            Shaders.endSky();
         }
      } else {
         GlStateManager._disableBlend();
      }

      if (Reflector.ForgeHooksClient_dispatchRenderStageS.exists()) {
         ReflectorForge.dispatchRenderStageS(Reflector.RenderLevelStageEvent_Stage_AFTER_SKY, this, pPoseStack, pProjectionMatrix, this.ticks, pCamera, frustum);
      }

      profilerfiller.popPush("fog");
      FogRenderer.setupFog(pCamera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(f, 32.0F), flag2, pPartialTick);
      profilerfiller.popPush("terrain_setup");
      this.checkLoadVisibleChunks(pCamera, frustum, this.minecraft.player.isSpectator());
      ++this.frameId;
      this.setupRender(pCamera, frustum, flag, this.minecraft.player.isSpectator());
      profilerfiller.popPush("compilechunks");
      this.compileChunks(pCamera);
      profilerfiller.popPush("terrain");
      Lagometer.timerTerrain.start();
      if (this.minecraft.options.ofSmoothFps) {
         this.minecraft.getProfiler().popPush("finish");
         GL11.glFinish();
         this.minecraft.getProfiler().popPush("terrain");
      }

      if (Config.isFogOff() && FogRenderer.fogStandard) {
         RenderSystem.setFogAllowed(false);
      }

      this.renderChunkLayer(RenderType.solid(), pPoseStack, d0, d1, d2, pProjectionMatrix);
      this.minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, this.minecraft.options.mipmapLevels().get() > 0);
      this.renderChunkLayer(RenderType.cutoutMipped(), pPoseStack, d0, d1, d2, pProjectionMatrix);
      this.minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
      this.renderChunkLayer(RenderType.cutout(), pPoseStack, d0, d1, d2, pProjectionMatrix);
      if (flag1) {
         ShadersRender.endTerrain();
      }

      if (this.level.effects().constantAmbientLight()) {
         Lighting.setupNetherLevel(pPoseStack.last().pose());
      } else {
         Lighting.setupLevel(pPoseStack.last().pose());
      }

      if (flag1) {
         Shaders.beginEntities();
      }

      ItemFrameRenderer.updateItemRenderDistance();
      profilerfiller.popPush("entities");
      ++renderEntitiesCounter;
      this.renderedEntities = 0;
      this.culledEntities = 0;
      this.countTileEntitiesRendered = 0;
      if (this.itemEntityTarget != null) {
         this.itemEntityTarget.clear(Minecraft.ON_OSX);
         this.itemEntityTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
         this.minecraft.getMainRenderTarget().bindWrite(false);
      }

      if (this.weatherTarget != null) {
         this.weatherTarget.clear(Minecraft.ON_OSX);
      }

      if (this.shouldShowEntityOutlines()) {
         this.entityTarget.clear(Minecraft.ON_OSX);
         this.minecraft.getMainRenderTarget().bindWrite(false);
      }

      boolean flag4 = false;
      MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
      if (Config.isFastRender()) {
         RenderStateManager.enableCache();
         multibuffersource$buffersource.enableCache();
      }

      int i = this.level.getMinBuildHeight();
      int j = this.level.getMaxBuildHeight();
      if (Config.isRenderRegions() || Config.isMultiTexture()) {
         GameRenderer.getPositionShader().apply();
      }

      for(Entity entity : this.level.entitiesForRendering()) {
         if (this.shouldRenderEntity(entity, i, j)) {
            boolean flag5 = entity == this.minecraft.player && !this.minecraft.player.isSpectator();
            if (this.entityRenderDispatcher.shouldRender(entity, frustum, d0, d1, d2) || entity.hasIndirectPassenger(this.minecraft.player)) {
               BlockPos blockpos = entity.blockPosition();
               if ((this.level.isOutsideBuildHeight(blockpos.getY()) || this.isChunkCompiled(blockpos)) && (entity != pCamera.getEntity() || pCamera.isDetached() || pCamera.getEntity() instanceof LivingEntity && ((LivingEntity)pCamera.getEntity()).isSleeping()) && (!(entity instanceof LocalPlayer) || pCamera.getEntity() == entity || flag5)) {
                  String s = entity.getClass().getName();
                  List<Entity> list = this.mapEntityLists.get(s);
                  if (list == null) {
                     list = new ArrayList<>();
                     this.mapEntityLists.put(s, list);
                  }

                  list.add(entity);
               }
            }
         }
      }

      for(List<Entity> list1 : this.mapEntityLists.values()) {
         for(Entity entity1 : list1) {
            ++this.renderedEntities;
            if (entity1.tickCount == 0) {
               entity1.xOld = entity1.getX();
               entity1.yOld = entity1.getY();
               entity1.zOld = entity1.getZ();
            }

            MultiBufferSource multibuffersource1;
            if (this.shouldShowEntityOutlines() && this.minecraft.shouldEntityAppearGlowing(entity1)) {
               flag4 = true;
               OutlineBufferSource outlinebuffersource = this.renderBuffers.outlineBufferSource();
               multibuffersource1 = outlinebuffersource;
               int k = entity1.getTeamColor();
               outlinebuffersource.setColor(100, 0, 0, 255);
            } else {
               if (Reflector.IForgeEntity_hasCustomOutlineRendering.exists() && this.shouldShowEntityOutlines() && Reflector.callBoolean(entity1, Reflector.IForgeEntity_hasCustomOutlineRendering, this.minecraft.player)) {
                  flag4 = true;
               }

               multibuffersource1 = multibuffersource$buffersource;
            }

            if (flag1) {
               Shaders.nextEntity(entity1);
            }

            this.renderEntity(entity1, d0, d1, d2, pPartialTick, pPoseStack, multibuffersource1);
         }

         list1.clear();
      }

      multibuffersource$buffersource.endLastBatch();
      this.checkPoseStack(pPoseStack);
      multibuffersource$buffersource.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
      multibuffersource$buffersource.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
      multibuffersource$buffersource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
      multibuffersource$buffersource.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));
      if (Config.isFastRender()) {
         multibuffersource$buffersource.flushCache();
         RenderStateManager.flushCache();
      }

      if (flag1) {
         Shaders.endEntities();
      }

      if (Reflector.ForgeHooksClient_dispatchRenderStageS.exists()) {
         ReflectorForge.dispatchRenderStageS(Reflector.RenderLevelStageEvent_Stage_AFTER_ENTITIES, this, pPoseStack, pProjectionMatrix, this.ticks, pCamera, frustum);
      }

      if (flag1) {
         Shaders.beginBlockEntities();
      }

      profilerfiller.popPush("blockentities");
      SignRenderer.updateTextRenderDistance();
      boolean flag6 = Reflector.IForgeBlockEntity_getRenderBoundingBox.exists();
      Frustum frustum1 = frustum;

      for(LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo : this.renderInfosTileEntities) {
         List<BlockEntity> list2 = levelrenderer$renderchunkinfo.chunk.getCompiledChunk().getRenderableBlockEntities();
         if (!list2.isEmpty()) {
            for(BlockEntity blockentity1 : list2) {
               if (flag6) {
                  AABB aabb = (AABB)Reflector.call(blockentity1, Reflector.IForgeBlockEntity_getRenderBoundingBox);
                  if (aabb != null && !frustum1.isVisible(aabb)) {
                     continue;
                  }
               }

               if (flag1) {
                  Shaders.nextBlockEntity(blockentity1);
               }

               BlockPos blockpos4 = blockentity1.getBlockPos();
               MultiBufferSource multibuffersource = multibuffersource$buffersource;
               pPoseStack.pushPose();
               pPoseStack.translate((double)blockpos4.getX() - d0, (double)blockpos4.getY() - d1, (double)blockpos4.getZ() - d2);
               SortedSet<BlockDestructionProgress> sortedset = this.destructionProgress.get(blockpos4.asLong());
               if (sortedset != null && !sortedset.isEmpty()) {
                  int l = sortedset.last().getProgress();
                  if (l >= 0) {
                     PoseStack.Pose posestack$pose = pPoseStack.last();
                     VertexConsumer vertexconsumer = new SheetedDecalTextureGenerator(this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(l)), posestack$pose.pose(), posestack$pose.normal(), 1.0F);
                     multibuffersource = (p_234295_2_) -> {
                        VertexConsumer vertexconsumer3 = multibuffersource$buffersource.getBuffer(p_234295_2_);
                        return p_234295_2_.affectsCrumbling() ? VertexMultiConsumer.create(vertexconsumer, vertexconsumer3) : vertexconsumer3;
                     };
                  }
               }

               if (Reflector.IForgeBlockEntity_hasCustomOutlineRendering.exists() && this.shouldShowEntityOutlines() && Reflector.callBoolean(blockentity1, Reflector.IForgeBlockEntity_hasCustomOutlineRendering, this.minecraft.player)) {
                  flag4 = true;
               }

               this.blockEntityRenderDispatcher.render(blockentity1, pPartialTick, pPoseStack, multibuffersource);
               pPoseStack.popPose();
               ++this.countTileEntitiesRendered;
            }
         }
      }

      synchronized(this.globalBlockEntities) {
         for(BlockEntity blockentity : this.globalBlockEntities) {
            if (flag6) {
               AABB aabb1 = (AABB)Reflector.call(blockentity, Reflector.IForgeBlockEntity_getRenderBoundingBox);
               if (aabb1 != null && !frustum1.isVisible(aabb1)) {
                  continue;
               }
            }

            if (flag1) {
               Shaders.nextBlockEntity(blockentity);
            }

            BlockPos blockpos3 = blockentity.getBlockPos();
            pPoseStack.pushPose();
            pPoseStack.translate((double)blockpos3.getX() - d0, (double)blockpos3.getY() - d1, (double)blockpos3.getZ() - d2);
            if (Reflector.IForgeBlockEntity_hasCustomOutlineRendering.exists() && this.shouldShowEntityOutlines() && Reflector.callBoolean(blockentity, Reflector.IForgeBlockEntity_hasCustomOutlineRendering, this.minecraft.player)) {
               flag4 = true;
            }

            this.blockEntityRenderDispatcher.render(blockentity, pPartialTick, pPoseStack, multibuffersource$buffersource);
            pPoseStack.popPose();
            ++this.countTileEntitiesRendered;
         }
      }

      this.checkPoseStack(pPoseStack);
      multibuffersource$buffersource.endBatch(RenderType.solid());
      multibuffersource$buffersource.endBatch(RenderType.endPortal());
      multibuffersource$buffersource.endBatch(RenderType.endGateway());
      multibuffersource$buffersource.endBatch(Sheets.solidBlockSheet());
      multibuffersource$buffersource.endBatch(Sheets.cutoutBlockSheet());
      multibuffersource$buffersource.endBatch(Sheets.bedSheet());
      multibuffersource$buffersource.endBatch(Sheets.shulkerBoxSheet());
      multibuffersource$buffersource.endBatch(Sheets.signSheet());
      multibuffersource$buffersource.endBatch(Sheets.hangingSignSheet());
      multibuffersource$buffersource.endBatch(Sheets.chestSheet());
      this.renderBuffers.outlineBufferSource().endOutlineBatch();
      if (Config.isFastRender()) {
         multibuffersource$buffersource.disableCache();
         RenderStateManager.disableCache();
      }

      Lagometer.timerTerrain.end();
      if (flag4) {
         this.entityEffect.process(pPartialTick);
         this.minecraft.getMainRenderTarget().bindWrite(false);
      }

      if (flag1) {
         Shaders.endBlockEntities();
      }

      if (Reflector.ForgeHooksClient_dispatchRenderStageS.exists()) {
         ReflectorForge.dispatchRenderStageS(Reflector.RenderLevelStageEvent_Stage_AFTER_BLOCK_ENTITIES, this, pPoseStack, pProjectionMatrix, this.ticks, pCamera, frustum);
      }

      this.renderOverlayDamaged = true;
      profilerfiller.popPush("destroyProgress");

      for(Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>> entry : this.destructionProgress.long2ObjectEntrySet()) {
         BlockPos blockpos2 = BlockPos.of(entry.getLongKey());
         double d3 = (double)blockpos2.getX() - d0;
         double d4 = (double)blockpos2.getY() - d1;
         double d5 = (double)blockpos2.getZ() - d2;
         if (!(d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D)) {
            SortedSet<BlockDestructionProgress> sortedset1 = entry.getValue();
            if (sortedset1 != null && !sortedset1.isEmpty()) {
               int i1 = sortedset1.last().getProgress();
               pPoseStack.pushPose();
               pPoseStack.translate((double)blockpos2.getX() - d0, (double)blockpos2.getY() - d1, (double)blockpos2.getZ() - d2);
               PoseStack.Pose posestack$pose1 = pPoseStack.last();
               VertexConsumer vertexconsumer1 = new SheetedDecalTextureGenerator(this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(i1)), posestack$pose1.pose(), posestack$pose1.normal(), 1.0F);
               ModelData modeldata = this.level.getModelDataManager().getAt(blockpos2);
               if (modeldata == null) {
                  modeldata = ModelData.EMPTY;
               }

               this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(blockpos2), blockpos2, this.level, pPoseStack, vertexconsumer1, modeldata);
               pPoseStack.popPose();
            }
         }
      }

      this.renderOverlayDamaged = false;
      RenderUtils.flushRenderBuffers();
      --renderEntitiesCounter;
      this.checkPoseStack(pPoseStack);
      HitResult hitresult = this.minecraft.hitResult;
      if (pRenderBlockOutline && hitresult != null && hitresult.getType() == HitResult.Type.BLOCK) {
         profilerfiller.popPush("outline");
         BlockPos blockpos1 = ((BlockHitResult)hitresult).getBlockPos();
         BlockState blockstate = this.level.getBlockState(blockpos1);
         if (flag1) {
            ShadersRender.beginOutline();
         }

         if (!Reflector.callBoolean(Reflector.ForgeHooksClient_onDrawHighlight, this, pCamera, hitresult, pPartialTick, pPoseStack, multibuffersource$buffersource) && !blockstate.isAir() && this.level.getWorldBorder().isWithinBounds(blockpos1)) {
            VertexConsumer vertexconsumer2 = multibuffersource$buffersource.getBuffer(RenderType.lines());
            this.renderHitOutline(pPoseStack, vertexconsumer2, pCamera.getEntity(), d0, d1, d2, blockpos1, blockstate);
         }

         if (flag1) {
            multibuffersource$buffersource.endBatch(RenderType.lines());
            ShadersRender.endOutline();
         }
      } else if (hitresult != null && hitresult.getType() == HitResult.Type.ENTITY) {
         Reflector.ForgeHooksClient_onDrawHighlight.call(this, pCamera, hitresult, pPartialTick, pPoseStack, multibuffersource$buffersource);
      }

      this.minecraft.debugRenderer.render(pPoseStack, multibuffersource$buffersource, d0, d1, d2);
      multibuffersource$buffersource.endLastBatch();
      PoseStack posestack = RenderSystem.getModelViewStack();
      RenderSystem.applyModelViewMatrix();
      if (flag1) {
         RenderUtils.finishRenderBuffers();
         ShadersRender.beginDebug();
      }

      multibuffersource$buffersource.endBatch(Sheets.translucentCullBlockSheet());
      multibuffersource$buffersource.endBatch(Sheets.bannerSheet());
      multibuffersource$buffersource.endBatch(Sheets.shieldSheet());
      multibuffersource$buffersource.endBatch(RenderType.armorGlint());
      multibuffersource$buffersource.endBatch(RenderType.armorEntityGlint());
      multibuffersource$buffersource.endBatch(RenderType.glint());
      multibuffersource$buffersource.endBatch(RenderType.glintDirect());
      multibuffersource$buffersource.endBatch(RenderType.glintTranslucent());
      multibuffersource$buffersource.endBatch(RenderType.entityGlint());
      multibuffersource$buffersource.endBatch(RenderType.entityGlintDirect());
      multibuffersource$buffersource.endBatch(RenderType.waterMask());
      this.renderBuffers.crumblingBufferSource().endBatch();
      if (flag1) {
         multibuffersource$buffersource.endBatch();
         ShadersRender.endDebug();
         Shaders.preRenderHand();
         Matrix4f matrix4f1 = MathUtils.copy(RenderSystem.getProjectionMatrix());
         ShadersRender.renderHand0(pGameRenderer, pPoseStack, pCamera, pPartialTick);
         RenderSystem.setProjectionMatrix(matrix4f1, RenderSystem.getVertexSorting());
         Shaders.preWater();
      }

      if (this.transparencyChain != null) {
         multibuffersource$buffersource.endBatch(RenderType.lines());
         multibuffersource$buffersource.endBatch();
         this.translucentTarget.clear(Minecraft.ON_OSX);
         this.translucentTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
         profilerfiller.popPush("translucent");
         this.renderChunkLayer(RenderType.translucent(), pPoseStack, d0, d1, d2, pProjectionMatrix);
         profilerfiller.popPush("string");
         this.renderChunkLayer(RenderType.tripwire(), pPoseStack, d0, d1, d2, pProjectionMatrix);
         this.particlesTarget.clear(Minecraft.ON_OSX);
         this.particlesTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
         RenderStateShard.PARTICLES_TARGET.setupRenderState();
         profilerfiller.popPush("particles");
         this.minecraft.particleEngine.render(pPoseStack, multibuffersource$buffersource, pLightTexture, pCamera, pPartialTick, frustum);
         if (Reflector.ForgeHooksClient_dispatchRenderStageS.exists()) {
            ReflectorForge.dispatchRenderStageS(Reflector.RenderLevelStageEvent_Stage_AFTER_PARTICLES, this, pPoseStack, pProjectionMatrix, this.ticks, pCamera, frustum);
         }

         RenderStateShard.PARTICLES_TARGET.clearRenderState();
      } else {
         profilerfiller.popPush("translucent");
         Lagometer.timerTerrain.start();
         if (Shaders.isParticlesBeforeDeferred()) {
            Shaders.beginParticles();
            this.minecraft.particleEngine.render(pPoseStack, multibuffersource$buffersource, pLightTexture, pCamera, pPartialTick, frustum);
            Shaders.endParticles();
            if (Reflector.ForgeHooksClient_dispatchRenderStageS.exists()) {
               ReflectorForge.dispatchRenderStageS(Reflector.RenderLevelStageEvent_Stage_AFTER_PARTICLES, this, pPoseStack, pProjectionMatrix, this.ticks, pCamera, frustum);
            }
         }

         if (flag1) {
            Shaders.beginWater();
         }

         if (this.translucentTarget != null) {
            this.translucentTarget.clear(Minecraft.ON_OSX);
         }

         this.renderChunkLayer(RenderType.translucent(), pPoseStack, d0, d1, d2, pProjectionMatrix);
         if (flag1) {
            Shaders.endWater();
         }

         Lagometer.timerTerrain.end();
         multibuffersource$buffersource.endBatch(RenderType.lines());
         multibuffersource$buffersource.endBatch();
         profilerfiller.popPush("string");
         this.renderChunkLayer(RenderType.tripwire(), pPoseStack, d0, d1, d2, pProjectionMatrix);
         profilerfiller.popPush("particles");
         if (!Shaders.isParticlesBeforeDeferred()) {
            if (flag1) {
               Shaders.beginParticles();
            }

            this.minecraft.particleEngine.render(pPoseStack, multibuffersource$buffersource, pLightTexture, pCamera, pPartialTick, frustum);
            if (flag1) {
               Shaders.endParticles();
            }

            if (Reflector.ForgeHooksClient_dispatchRenderStageS.exists()) {
               ReflectorForge.dispatchRenderStageS(Reflector.RenderLevelStageEvent_Stage_AFTER_PARTICLES, this, pPoseStack, pProjectionMatrix, this.ticks, pCamera, frustum);
            }
         }
      }

      RenderSystem.setFogAllowed(true);
      posestack.pushPose();
      posestack.mulPoseMatrix(pPoseStack.last().pose());
      RenderSystem.applyModelViewMatrix();
      if (this.minecraft.options.getCloudsType() != CloudStatus.OFF) {
         if (this.transparencyChain != null) {
            this.cloudsTarget.clear(Minecraft.ON_OSX);
            RenderStateShard.CLOUDS_TARGET.setupRenderState();
            profilerfiller.popPush("clouds");
            this.renderClouds(pPoseStack, pProjectionMatrix, pPartialTick, d0, d1, d2);
            RenderStateShard.CLOUDS_TARGET.clearRenderState();
         } else {
            profilerfiller.popPush("clouds");
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            this.renderClouds(pPoseStack, pProjectionMatrix, pPartialTick, d0, d1, d2);
         }
      }

      if (this.transparencyChain != null) {
         RenderStateShard.WEATHER_TARGET.setupRenderState();
         profilerfiller.popPush("weather");
         this.renderSnowAndRain(pLightTexture, pPartialTick, d0, d1, d2);
         if (Reflector.ForgeHooksClient_dispatchRenderStageS.exists()) {
            ReflectorForge.dispatchRenderStageS(Reflector.RenderLevelStageEvent_Stage_AFTER_WEATHER, this, pPoseStack, pProjectionMatrix, this.ticks, pCamera, frustum);
         }

         this.renderWorldBorder(pCamera);
         RenderStateShard.WEATHER_TARGET.clearRenderState();
         this.transparencyChain.process(pPartialTick);
         this.minecraft.getMainRenderTarget().bindWrite(false);
      } else {
         RenderSystem.depthMask(false);
         profilerfiller.popPush("weather");
         if (flag1) {
            Shaders.beginWeather();
         }

         this.renderSnowAndRain(pLightTexture, pPartialTick, d0, d1, d2);
         if (flag1) {
            Shaders.endWeather();
         }

         if (Reflector.ForgeHooksClient_dispatchRenderStageS.exists()) {
            ReflectorForge.dispatchRenderStageS(Reflector.RenderLevelStageEvent_Stage_AFTER_WEATHER, this, pPoseStack, pProjectionMatrix, this.ticks, pCamera, frustum);
         }

         this.renderWorldBorder(pCamera);
         RenderSystem.depthMask(true);
      }

      posestack.popPose();
      RenderSystem.applyModelViewMatrix();
      this.renderDebug(pPoseStack, multibuffersource$buffersource, pCamera);
      multibuffersource$buffersource.endLastBatch();
      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
      FogRenderer.setupNoFog();
   }

   public void checkPoseStack(PoseStack pPoseStack) {
      if (!pPoseStack.clear()) {
         throw new IllegalStateException("Pose stack not empty");
      }
   }

   public void renderEntity(Entity pEntity, double pCamX, double pCamY, double pCamZ, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource) {
      double d0 = Mth.lerp((double)pPartialTick, pEntity.xOld, pEntity.getX());
      double d1 = Mth.lerp((double)pPartialTick, pEntity.yOld, pEntity.getY());
      double d2 = Mth.lerp((double)pPartialTick, pEntity.zOld, pEntity.getZ());
      float f = Mth.lerp(pPartialTick, pEntity.yRotO, pEntity.getYRot());
      this.entityRenderDispatcher.render(pEntity, d0 - pCamX, d1 - pCamY, d2 - pCamZ, f, pPartialTick, pPoseStack, pBufferSource, this.entityRenderDispatcher.getPackedLightCoords(pEntity, pPartialTick));
   }

   public void renderChunkLayer(RenderType pRenderType, PoseStack pPoseStack, double pCamX, double pCamY, double pCamZ, Matrix4f pProjectionMatrix) {
      RenderSystem.assertOnRenderThread();
      pRenderType.setupRenderState();
      boolean flag = Config.isShaders();
      if (pRenderType == RenderType.translucent() && !Shaders.isShadowPass) {
         this.minecraft.getProfiler().push("translucent_sort");
         double d0 = pCamX - this.xTransparentOld;
         double d1 = pCamY - this.yTransparentOld;
         double d2 = pCamZ - this.zTransparentOld;
         if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0D) {
            int j = SectionPos.posToSectionCoord(pCamX);
            int k = SectionPos.posToSectionCoord(pCamY);
            int l = SectionPos.posToSectionCoord(pCamZ);
            boolean flag1 = j != SectionPos.posToSectionCoord(this.xTransparentOld) || l != SectionPos.posToSectionCoord(this.zTransparentOld) || k != SectionPos.posToSectionCoord(this.yTransparentOld);
            this.xTransparentOld = pCamX;
            this.yTransparentOld = pCamY;
            this.zTransparentOld = pCamZ;
            int i1 = 0;
            this.chunksToResortTransparency.clear();

            for(LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo1 : this.renderInfosTerrain) {
               if (i1 < 15 && (flag1 || levelrenderer$renderchunkinfo1.isAxisAlignedWith(j, k, l)) && levelrenderer$renderchunkinfo1.chunk.getCompiledChunk().isLayerUsed(pRenderType)) {
                  this.chunksToResortTransparency.add(levelrenderer$renderchunkinfo1.chunk);
                  ++i1;
               }
            }
         }

         this.minecraft.getProfiler().pop();
      }

      this.minecraft.getProfiler().push("filterempty");
      this.minecraft.getProfiler().popPush(() -> {
         return "render_" + pRenderType;
      });
      boolean flag2 = pRenderType != RenderType.translucent();
      ObjectListIterator<LevelRenderer.RenderChunkInfo> objectlistiterator = this.renderInfosTerrain.listIterator(flag2 ? 0 : this.renderInfosTerrain.size());
      ShaderInstance shaderinstance = RenderSystem.getShader();

      for(int i = 0; i < 12; ++i) {
         int j1 = RenderSystem.getShaderTexture(i);
         shaderinstance.setSampler(i, j1);
      }

      if (shaderinstance.MODEL_VIEW_MATRIX != null) {
         shaderinstance.MODEL_VIEW_MATRIX.set(pPoseStack.last().pose());
      }

      if (shaderinstance.PROJECTION_MATRIX != null) {
         shaderinstance.PROJECTION_MATRIX.set(pProjectionMatrix);
      }

      if (shaderinstance.COLOR_MODULATOR != null) {
         shaderinstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
      }

      if (shaderinstance.GLINT_ALPHA != null) {
         shaderinstance.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
      }

      if (shaderinstance.FOG_START != null) {
         shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
      }

      if (shaderinstance.FOG_END != null) {
         shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
      }

      if (shaderinstance.FOG_COLOR != null) {
         shaderinstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
      }

      if (shaderinstance.FOG_SHAPE != null) {
         shaderinstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
      }

      if (shaderinstance.TEXTURE_MATRIX != null) {
         shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
      }

      if (shaderinstance.GAME_TIME != null) {
         shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
      }

      RenderSystem.setupShaderLights(shaderinstance);
      shaderinstance.apply();
      Uniform uniform = shaderinstance.CHUNK_OFFSET;
      if (flag) {
         ShadersRender.preRenderChunkLayer(pRenderType);
         Shaders.setModelViewMatrix(pPoseStack.last().pose());
         Shaders.setProjectionMatrix(pProjectionMatrix);
         Shaders.setTextureMatrix(RenderSystem.getTextureMatrix());
         Shaders.setColorModulator(RenderSystem.getShaderColor());
      }

      boolean flag3 = SmartAnimations.isActive();
      if (flag && Shaders.activeProgramID > 0) {
         uniform = null;
      }

      if (Config.isRenderRegions() && !pRenderType.isNeedsSorting()) {
         int k1 = Integer.MIN_VALUE;
         int l1 = Integer.MIN_VALUE;
         VboRegion vboregion2 = null;
         Map<PairInt, Map<VboRegion, List<VertexBuffer>>> map = this.mapRegionLayers.computeIfAbsent(pRenderType, (k) -> {
            return new LinkedHashMap(16);
         });
         Map<VboRegion, List<VertexBuffer>> map1 = null;
         List<VertexBuffer> list1 = null;

         while(true) {
            if (flag2) {
               if (!objectlistiterator.hasNext()) {
                  break;
               }
            } else if (!objectlistiterator.hasPrevious()) {
               break;
            }

            LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo2 = flag2 ? objectlistiterator.next() : objectlistiterator.previous();
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk1 = levelrenderer$renderchunkinfo2.chunk;
            if (!chunkrenderdispatcher$renderchunk1.getCompiledChunk().isEmpty(pRenderType)) {
               VertexBuffer vertexbuffer = chunkrenderdispatcher$renderchunk1.getBuffer(pRenderType);
               VboRegion vboregion = vertexbuffer.getVboRegion();
               if (chunkrenderdispatcher$renderchunk1.regionX != k1 || chunkrenderdispatcher$renderchunk1.regionZ != l1) {
                  PairInt pairint = PairInt.of(chunkrenderdispatcher$renderchunk1.regionX, chunkrenderdispatcher$renderchunk1.regionZ);
                  map1 = map.computeIfAbsent(pairint, (k) -> {
                     return new LinkedHashMap(8);
                  });
                  k1 = chunkrenderdispatcher$renderchunk1.regionX;
                  l1 = chunkrenderdispatcher$renderchunk1.regionZ;
                  vboregion2 = null;
               }

               if (vboregion != vboregion2) {
                  list1 = map1.computeIfAbsent(vboregion, (k) -> {
                     return new ArrayList();
                  });
                  vboregion2 = vboregion;
               }

               list1.add(vertexbuffer);
               if (flag3) {
                  BitSet bitset1 = chunkrenderdispatcher$renderchunk1.getCompiledChunk().getAnimatedSprites(pRenderType);
                  if (bitset1 != null) {
                     SmartAnimations.spritesRendered(bitset1);
                  }
               }
            }
         }

         for(Map.Entry<PairInt, Map<VboRegion, List<VertexBuffer>>> entry : map.entrySet()) {
            PairInt pairint1 = entry.getKey();
            Map<VboRegion, List<VertexBuffer>> map2 = entry.getValue();

            for(Map.Entry<VboRegion, List<VertexBuffer>> entry1 : map2.entrySet()) {
               VboRegion vboregion1 = entry1.getKey();
               List<VertexBuffer> list = entry1.getValue();
               if (!list.isEmpty()) {
                  for(VertexBuffer vertexbuffer1 : list) {
                     vertexbuffer1.draw();
                  }

                  this.drawRegion(pairint1.getLeft(), 0, pairint1.getRight(), pCamX, pCamY, pCamZ, vboregion1, uniform, flag);
                  list.clear();
               }
            }
         }
      } else {
         while(true) {
            if (flag2) {
               if (!objectlistiterator.hasNext()) {
                  break;
               }
            } else if (!objectlistiterator.hasPrevious()) {
               break;
            }

            LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo = flag2 ? objectlistiterator.next() : objectlistiterator.previous();
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = levelrenderer$renderchunkinfo.chunk;
            if (!chunkrenderdispatcher$renderchunk.getCompiledChunk().isEmpty(pRenderType)) {
               VertexBuffer vertexbuffer2 = chunkrenderdispatcher$renderchunk.getBuffer(pRenderType);
               BlockPos blockpos = chunkrenderdispatcher$renderchunk.getOrigin();
               if (uniform != null) {
                  uniform.set((float)((double)blockpos.getX() - pCamX - (double)chunkrenderdispatcher$renderchunk.regionDX), (float)((double)blockpos.getY() - pCamY - (double)chunkrenderdispatcher$renderchunk.regionDY), (float)((double)blockpos.getZ() - pCamZ - (double)chunkrenderdispatcher$renderchunk.regionDZ));
                  uniform.upload();
               }

               if (flag) {
                  Shaders.uniform_chunkOffset.setValue((float)((double)blockpos.getX() - pCamX - (double)chunkrenderdispatcher$renderchunk.regionDX), (float)((double)blockpos.getY() - pCamY - (double)chunkrenderdispatcher$renderchunk.regionDY), (float)((double)blockpos.getZ() - pCamZ - (double)chunkrenderdispatcher$renderchunk.regionDZ));
               }

               if (flag3) {
                  BitSet bitset = chunkrenderdispatcher$renderchunk.getCompiledChunk().getAnimatedSprites(pRenderType);
                  if (bitset != null) {
                     SmartAnimations.spritesRendered(bitset);
                  }
               }

               vertexbuffer2.bind();
               vertexbuffer2.draw();
            }
         }
      }

      if (Config.isMultiTexture()) {
         this.minecraft.getTextureManager().bindForSetup(TextureAtlas.LOCATION_BLOCKS);
      }

      if (uniform != null) {
         uniform.set(0.0F, 0.0F, 0.0F);
      }

      if (flag) {
         Shaders.uniform_chunkOffset.setValue(0.0F, 0.0F, 0.0F);
      }

      shaderinstance.clear();
      VertexBuffer.unbind();
      this.minecraft.getProfiler().pop();
      if (flag) {
         ShadersRender.postRenderChunkLayer(pRenderType);
      }

      if (Reflector.ForgeHooksClient_dispatchRenderStageRT.exists()) {
         Reflector.ForgeHooksClient_dispatchRenderStageRT.call(pRenderType, this, pPoseStack, pProjectionMatrix, this.ticks, this.minecraft.gameRenderer.getMainCamera(), this.getFrustum());
      }

      pRenderType.clearRenderState();
   }

   private void drawRegion(int regionX, int regionY, int regionZ, double xIn, double yIn, double zIn, VboRegion vboRegion, Uniform uniform, boolean isShaders) {
      if (uniform != null) {
         uniform.set((float)((double)regionX - xIn), (float)((double)regionY - yIn), (float)((double)regionZ - zIn));
         uniform.upload();
      }

      if (isShaders) {
         Shaders.uniform_chunkOffset.setValue((float)((double)regionX - xIn), (float)((double)regionY - yIn), (float)((double)regionZ - zIn));
      }

      vboRegion.finishDraw();
   }

   private void renderDebug(PoseStack pPoseStack, MultiBufferSource pBuffer, Camera pCamera) {
      if (this.minecraft.chunkPath || this.minecraft.chunkVisibility) {
         if (Config.isShaders()) {
            Shaders.pushUseProgram(Shaders.ProgramBasic);
         }

         double d0 = pCamera.getPosition().x();
         double d1 = pCamera.getPosition().y();
         double d2 = pCamera.getPosition().z();

         for(LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo : this.renderChunksInFrustum) {
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = levelrenderer$renderchunkinfo.chunk;
            BlockPos blockpos = chunkrenderdispatcher$renderchunk.getOrigin();
            pPoseStack.pushPose();
            pPoseStack.translate((double)blockpos.getX() - d0, (double)blockpos.getY() - d1, (double)blockpos.getZ() - d2);
            Matrix4f matrix4f = pPoseStack.last().pose();
            if (this.minecraft.chunkPath) {
               if (Config.isShaders()) {
                  Shaders.beginLines();
               }

               VertexConsumer vertexconsumer1 = pBuffer.getBuffer(RenderType.lines());
               int i = levelrenderer$renderchunkinfo.step == 0 ? 0 : Mth.hsvToRgb((float)levelrenderer$renderchunkinfo.step / 50.0F, 0.9F, 0.9F);
               int j = i >> 16 & 255;
               int k = i >> 8 & 255;
               int l = i & 255;

               for(int i1 = 0; i1 < DIRECTIONS.length; ++i1) {
                  if (levelrenderer$renderchunkinfo.hasSourceDirection(i1)) {
                     Direction direction = DIRECTIONS[i1];
                     vertexconsumer1.vertex(matrix4f, 8.0F, 8.0F, 8.0F).color(j, k, l, 255).normal((float)direction.getStepX(), (float)direction.getStepY(), (float)direction.getStepZ()).endVertex();
                     vertexconsumer1.vertex(matrix4f, (float)(8 - 16 * direction.getStepX()), (float)(8 - 16 * direction.getStepY()), (float)(8 - 16 * direction.getStepZ())).color(j, k, l, 255).normal((float)direction.getStepX(), (float)direction.getStepY(), (float)direction.getStepZ()).endVertex();
                  }
               }

               if (Config.isShaders()) {
                  Shaders.endLines();
               }
            }

            if (this.minecraft.chunkVisibility && !chunkrenderdispatcher$renderchunk.getCompiledChunk().hasNoRenderableLayers()) {
               if (Config.isShaders()) {
                  Shaders.beginLines();
               }

               VertexConsumer vertexconsumer3 = pBuffer.getBuffer(RenderType.lines());
               int j1 = 0;

               for(Direction direction2 : DIRECTIONS) {
                  for(Direction direction1 : DIRECTIONS) {
                     boolean flag = chunkrenderdispatcher$renderchunk.getCompiledChunk().facesCanSeeEachother(direction2, direction1);
                     if (!flag) {
                        ++j1;
                        vertexconsumer3.vertex(matrix4f, (float)(8 + 8 * direction2.getStepX()), (float)(8 + 8 * direction2.getStepY()), (float)(8 + 8 * direction2.getStepZ())).color(255, 0, 0, 255).normal((float)direction2.getStepX(), (float)direction2.getStepY(), (float)direction2.getStepZ()).endVertex();
                        vertexconsumer3.vertex(matrix4f, (float)(8 + 8 * direction1.getStepX()), (float)(8 + 8 * direction1.getStepY()), (float)(8 + 8 * direction1.getStepZ())).color(255, 0, 0, 255).normal((float)direction1.getStepX(), (float)direction1.getStepY(), (float)direction1.getStepZ()).endVertex();
                     }
                  }
               }

               if (Config.isShaders()) {
                  Shaders.endLines();
               }

               if (j1 > 0) {
                  VertexConsumer vertexconsumer4 = pBuffer.getBuffer(RenderType.debugQuads());
                  float f = 0.5F;
                  float f1 = 0.2F;
                  vertexconsumer4.vertex(matrix4f, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer4.vertex(matrix4f, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
               }
            }

            pPoseStack.popPose();
         }

         if (Config.isShaders()) {
            Shaders.popProgram();
         }
      }

      if (this.capturedFrustum != null) {
         if (Config.isShaders()) {
            Shaders.pushUseProgram(Shaders.ProgramBasic);
         }

         pPoseStack.pushPose();
         pPoseStack.translate((float)(this.frustumPos.x - pCamera.getPosition().x), (float)(this.frustumPos.y - pCamera.getPosition().y), (float)(this.frustumPos.z - pCamera.getPosition().z));
         Matrix4f matrix4f1 = pPoseStack.last().pose();
         VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.debugQuads());
         this.addFrustumQuad(vertexconsumer, matrix4f1, 0, 1, 2, 3, 0, 1, 1);
         this.addFrustumQuad(vertexconsumer, matrix4f1, 4, 5, 6, 7, 1, 0, 0);
         this.addFrustumQuad(vertexconsumer, matrix4f1, 0, 1, 5, 4, 1, 1, 0);
         this.addFrustumQuad(vertexconsumer, matrix4f1, 2, 3, 7, 6, 0, 0, 1);
         this.addFrustumQuad(vertexconsumer, matrix4f1, 0, 4, 7, 3, 0, 1, 0);
         this.addFrustumQuad(vertexconsumer, matrix4f1, 1, 5, 6, 2, 1, 0, 1);
         if (Config.isShaders()) {
            Shaders.beginLines();
         }

         VertexConsumer vertexconsumer2 = pBuffer.getBuffer(RenderType.lines());
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 0);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 1);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 1);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 2);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 2);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 3);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 3);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 0);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 4);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 5);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 5);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 6);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 6);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 7);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 7);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 4);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 0);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 4);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 1);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 5);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 2);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 6);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 3);
         this.addFrustumVertex(vertexconsumer2, matrix4f1, 7);
         if (Config.isShaders()) {
            Shaders.endLines();
         }

         pPoseStack.popPose();
         if (Config.isShaders()) {
            Shaders.popProgram();
         }
      }

   }

   private void addFrustumVertex(VertexConsumer pConsumer, Matrix4f pMatrix, int pVertexIndex) {
      pConsumer.vertex(pMatrix, this.frustumPoints[pVertexIndex].x(), this.frustumPoints[pVertexIndex].y(), this.frustumPoints[pVertexIndex].z()).color(0, 0, 0, 255).normal(0.0F, 0.0F, -1.0F).endVertex();
   }

   private void addFrustumQuad(VertexConsumer pConsumer, Matrix4f pMatrix, int pIndex1, int pIndex2, int pIndex3, int pIndex4, int pRed, int pGreen, int pBlue) {
      float f = 0.25F;
      pConsumer.vertex(pMatrix, this.frustumPoints[pIndex1].x(), this.frustumPoints[pIndex1].y(), this.frustumPoints[pIndex1].z()).color((float)pRed, (float)pGreen, (float)pBlue, 0.25F).endVertex();
      pConsumer.vertex(pMatrix, this.frustumPoints[pIndex2].x(), this.frustumPoints[pIndex2].y(), this.frustumPoints[pIndex2].z()).color((float)pRed, (float)pGreen, (float)pBlue, 0.25F).endVertex();
      pConsumer.vertex(pMatrix, this.frustumPoints[pIndex3].x(), this.frustumPoints[pIndex3].y(), this.frustumPoints[pIndex3].z()).color((float)pRed, (float)pGreen, (float)pBlue, 0.25F).endVertex();
      pConsumer.vertex(pMatrix, this.frustumPoints[pIndex4].x(), this.frustumPoints[pIndex4].y(), this.frustumPoints[pIndex4].z()).color((float)pRed, (float)pGreen, (float)pBlue, 0.25F).endVertex();
   }

   public void captureFrustum() {
      this.captureFrustum = true;
   }

   public void killFrustum() {
      this.capturedFrustum = null;
   }

   public void tick() {
      ++this.ticks;
      if (this.ticks % 20 == 0) {
         Iterator<BlockDestructionProgress> iterator = this.destroyingBlocks.values().iterator();

         while(iterator.hasNext()) {
            BlockDestructionProgress blockdestructionprogress = iterator.next();
            int i = blockdestructionprogress.getUpdatedRenderTick();
            if (this.ticks - i > 400) {
               iterator.remove();
               this.removeProgress(blockdestructionprogress);
            }
         }
      }

      if (Config.isRenderRegions() && this.ticks % 20 == 0) {
         this.mapRegionLayers.clear();
      }

   }

   private void removeProgress(BlockDestructionProgress pProgress) {
      long i = pProgress.getPos().asLong();
      Set<BlockDestructionProgress> set = this.destructionProgress.get(i);
      set.remove(pProgress);
      if (set.isEmpty()) {
         this.destructionProgress.remove(i);
      }

   }

   private void renderEndSky(PoseStack pPoseStack) {
      if (Config.isSkyEnabled()) {
         RenderSystem.enableBlend();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
         RenderSystem.setShaderTexture(0, END_SKY_LOCATION);
         Tesselator tesselator = Tesselator.getInstance();
         BufferBuilder bufferbuilder = tesselator.getBuilder();

         for(int i = 0; i < 6; ++i) {
            pPoseStack.pushPose();
            if (i == 1) {
               pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }

            if (i == 2) {
               pPoseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            }

            if (i == 3) {
               pPoseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            }

            if (i == 4) {
               pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }

            if (i == 5) {
               pPoseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            }

            Matrix4f matrix4f = pPoseStack.last().pose();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            int j = 40;
            int k = 40;
            int l = 40;
            if (Config.isCustomColors()) {
               Vec3 vec3 = new Vec3((double)j / 255.0D, (double)k / 255.0D, (double)l / 255.0D);
               vec3 = CustomColors.getWorldSkyColor(vec3, this.level, this.minecraft.getCameraEntity(), 0.0F);
               j = (int)(vec3.x * 255.0D);
               k = (int)(vec3.y * 255.0D);
               l = (int)(vec3.z * 255.0D);
            }

            bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(j, k, l, 255).endVertex();
            bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).color(j, k, l, 255).endVertex();
            bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).color(j, k, l, 255).endVertex();
            bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).color(j, k, l, 255).endVertex();
            tesselator.end();
            pPoseStack.popPose();
         }

         CustomSky.renderSky(this.level, pPoseStack, 0.0F);
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
      }
   }

   public void renderSky(PoseStack pPoseStack, Matrix4f pProjectionMatrix, float pPartialTick, Camera pCamera, boolean pIsFoggy, Runnable pSkyFogSetup) {

      pSkyFogSetup.run();
      if (!Reflector.IForgeDimensionSpecialEffects_renderSky.exists() || !Reflector.callBoolean(this.level.effects(), Reflector.IForgeDimensionSpecialEffects_renderSky, this.level, this.ticks, pPartialTick, pPoseStack, pCamera, pProjectionMatrix, pIsFoggy, pSkyFogSetup)) {
         if (!pIsFoggy) {
            FogType fogtype = pCamera.getFluidInCamera();
            if (fogtype != FogType.POWDER_SNOW && fogtype != FogType.LAVA && !this.doesMobEffectBlockSky(pCamera)) {
               if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.END) {
                  this.renderEndSky(pPoseStack);
               } else if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {
                  boolean flag = Config.isShaders();
                  if (flag) {
                     Shaders.disableTexture2D();
                  }

                  Vec3 vec3 = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), pPartialTick);
                  vec3 = CustomColors.getSkyColor(vec3, this.minecraft.level, this.minecraft.getCameraEntity().getX(), this.minecraft.getCameraEntity().getY() + 1.0D, this.minecraft.getCameraEntity().getZ());
                  if (flag) {
                     Shaders.setSkyColor(vec3);
                     RenderSystem.setColorToAttribute(true);
                  }

                  float f = (float)vec3.x;
                  float f1 = (float)vec3.y; //CUSTOM SKY
                  float f2 = (float)vec3.z;
                  FogRenderer.levelFogColor();
                  BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
                  RenderSystem.depthMask(false);
                  if (flag) {
                     Shaders.enableFog();
                  }

                  RenderSystem.setShaderColor(f, f1, f2, 1.0F);
                  if (flag) {
                     Shaders.preSkyList(pPoseStack);
                  }

                  ShaderInstance shaderinstance = RenderSystem.getShader();
                  if (Config.isSkyEnabled()) {
                     this.skyBuffer.bind();
                     this.skyBuffer.drawWithShader(pPoseStack.last().pose(), pProjectionMatrix, shaderinstance);
                     VertexBuffer.unbind();
                  }

                  if (flag) {
                     Shaders.disableFog();
                  }

                  RenderSystem.enableBlend();
                  float[] afloat = this.level.effects().getSunriseColor(this.level.getTimeOfDay(pPartialTick), pPartialTick);
                  if (afloat != null && Config.isSunMoonEnabled()) {
                     RenderSystem.setShader(GameRenderer::getPositionColorShader);
                     if (flag) {
                        Shaders.disableTexture2D();
                     }

                     if (flag) {
                        Shaders.setRenderStage(RenderStage.SUNSET);
                     }

                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                     pPoseStack.pushPose();
                     pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                     float f3 = Mth.sin(this.level.getSunAngle(pPartialTick)) < 0.0F ? 180.0F : 0.0F;
                     pPoseStack.mulPose(Axis.ZP.rotationDegrees(f3));
                     pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                     float f4 = afloat[0];
                     float f5 = afloat[1];
                     float f6 = afloat[2];
                     Matrix4f matrix4f = pPoseStack.last().pose();
                     bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
                     bufferbuilder.vertex(matrix4f, 0.0F, 100.0F, 0.0F).color(f4, f5, f6, afloat[3]).endVertex();
                     int i = 16;

                     for(int j = 0; j <= 16; ++j) {
                        float f7 = (float)j * ((float)Math.PI * 2F) / 16.0F;
                        float f8 = Mth.sin(f7);
                        float f9 = Mth.cos(f7);
                        bufferbuilder.vertex(matrix4f, f8 * 120.0F, f9 * 120.0F, -f9 * 40.0F * afloat[3]).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
                     }

                     BufferUploader.drawWithShader(bufferbuilder.end());
                     pPoseStack.popPose();
                  }

                  if (flag) {
                     Shaders.enableTexture2D();
                  }

                  RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                  pPoseStack.pushPose();
                  float f10 = 1.0F - this.level.getRainLevel(pPartialTick);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f10);
                  pPoseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
                  CustomSky.renderSky(this.level, pPoseStack, pPartialTick);
                  if (flag) {
                     Shaders.preCelestialRotate(pPoseStack);
                  }

                  pPoseStack.mulPose(Axis.XP.rotationDegrees(this.level.getTimeOfDay(pPartialTick) * 360.0F));
                  if (flag) {
                     Shaders.postCelestialRotate(pPoseStack);
                  }

                  Matrix4f matrix4f1 = pPoseStack.last().pose();
                  float f11 = 30.0F;
                  RenderSystem.setShader(GameRenderer::getPositionTexShader);
                  if (Config.isSunTexture()) {
                     if (flag) {
                        Shaders.setRenderStage(RenderStage.SUN);
                     }

                     RenderSystem.setShaderTexture(0, SUN_LOCATION);
                     bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                     bufferbuilder.vertex(matrix4f1, -f11, 100.0F, -f11).uv(0.0F, 0.0F).endVertex();
                     bufferbuilder.vertex(matrix4f1, f11, 100.0F, -f11).uv(1.0F, 0.0F).endVertex();
                     bufferbuilder.vertex(matrix4f1, f11, 100.0F, f11).uv(1.0F, 1.0F).endVertex();
                     bufferbuilder.vertex(matrix4f1, -f11, 100.0F, f11).uv(0.0F, 1.0F).endVertex();
                     BufferUploader.drawWithShader(bufferbuilder.end());
                  }

                  f11 = 20.0F;
                  if (Config.isMoonTexture()) {
                     if (flag) {
                        Shaders.setRenderStage(RenderStage.MOON);
                     }

                     RenderSystem.setShaderTexture(0, MOON_LOCATION);
                     int k = this.level.getMoonPhase();
                     int l = k % 4;
                     int i1 = k / 4 % 2;
                     float f13 = (float)(l + 0) / 4.0F;
                     float f14 = (float)(i1 + 0) / 2.0F;
                     float f15 = (float)(l + 1) / 4.0F;
                     float f16 = (float)(i1 + 1) / 2.0F;
                     bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                     bufferbuilder.vertex(matrix4f1, -f11, -100.0F, f11).uv(f15, f16).endVertex();
                     bufferbuilder.vertex(matrix4f1, f11, -100.0F, f11).uv(f13, f16).endVertex();
                     bufferbuilder.vertex(matrix4f1, f11, -100.0F, -f11).uv(f13, f14).endVertex();
                     bufferbuilder.vertex(matrix4f1, -f11, -100.0F, -f11).uv(f15, f14).endVertex();
                     BufferUploader.drawWithShader(bufferbuilder.end());
                  }

                  if (flag) {
                     Shaders.disableTexture2D();
                  }

                  float f12 = this.level.getStarBrightness(pPartialTick) * f10;
                  if (f12 > 0.0F && Config.isStarsEnabled() && !CustomSky.hasSkyLayers(this.level)) {
                     if (flag) {
                        Shaders.setRenderStage(RenderStage.STARS);
                     }

                     RenderSystem.setShaderColor(f12, f12, f12, f12);
                     FogRenderer.setupNoFog();
                     this.starBuffer.bind();
                     this.starBuffer.drawWithShader(pPoseStack.last().pose(), pProjectionMatrix, GameRenderer.getPositionShader());
                     VertexBuffer.unbind();
                     pSkyFogSetup.run();
                  }

                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  RenderSystem.disableBlend();
                  RenderSystem.defaultBlendFunc();
                  if (flag) {
                     Shaders.enableFog();
                  }

                  pPoseStack.popPose();
                  if (flag) {
                     Shaders.disableTexture2D();
                  }

                  RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                  double d0 = this.minecraft.player.getEyePosition(pPartialTick).y - this.level.getLevelData().getHorizonHeight(this.level);
                  boolean flag1 = false;
                  if (d0 < 0.0D) {
                     if (flag) {
                        Shaders.setRenderStage(RenderStage.VOID);
                     }

                     pPoseStack.pushPose();
                     pPoseStack.translate(0.0F, 12.0F, 0.0F);
                     this.darkBuffer.bind();
                     this.darkBuffer.drawWithShader(pPoseStack.last().pose(), pProjectionMatrix, shaderinstance);
                     VertexBuffer.unbind();
                     pPoseStack.popPose();
                     flag1 = true;
                  }

                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  if (flag) {
                     RenderSystem.setColorToAttribute(false);
                  }

                  RenderSystem.depthMask(true);
               }
            }
         }

      }
   }

   private boolean doesMobEffectBlockSky(Camera pCamera) {
      Entity entity = pCamera.getEntity();
      if (!(entity instanceof LivingEntity livingentity)) {
         return false;
      } else {
         return livingentity.hasEffect(MobEffects.BLINDNESS) || livingentity.hasEffect(MobEffects.DARKNESS);
      }
   }

   public void renderClouds(PoseStack pPoseStack, Matrix4f pProjectionMatrix, float pPartialTick, double pCamX, double pCamY, double pCamZ) {
      if (!Config.isCloudsOff()) {
         if (!Reflector.IForgeDimensionSpecialEffects_renderClouds.exists() || !Reflector.callBoolean(this.level.effects(), Reflector.IForgeDimensionSpecialEffects_renderClouds, this.level, this.ticks, pPartialTick, pPoseStack, pCamX, pCamY, pCamZ, pProjectionMatrix)) {
            float f = this.level.effects().getCloudHeight();
            if (!Float.isNaN(f)) {
               if (Config.isShaders()) {
                  Shaders.beginClouds();
               }

               RenderSystem.disableCull();
               RenderSystem.enableBlend();
               RenderSystem.enableDepthTest();
               RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
               RenderSystem.depthMask(true);
               float f1 = 12.0F;
               float f2 = 4.0F;
               double d0 = 2.0E-4D;
               double d1 = (double)(((float)this.ticks + pPartialTick) * 0.03F);
               double d2 = (pCamX + d1) / 12.0D;
               double d3 = (double)(f - (float)pCamY + 0.33F);
               d3 += this.minecraft.options.ofCloudsHeight * 128.0D;
               double d4 = pCamZ / 12.0D + (double)0.33F;
               d2 -= (double)(Mth.floor(d2 / 2048.0D) * 2048);
               d4 -= (double)(Mth.floor(d4 / 2048.0D) * 2048);
               float f3 = (float)(d2 - (double)Mth.floor(d2));
               float f4 = (float)(d3 / 4.0D - (double)Mth.floor(d3 / 4.0D)) * 4.0F;
               float f5 = (float)(d4 - (double)Mth.floor(d4));
               Vec3 vec3 = this.level.getCloudColor(pPartialTick);
               int i = (int)Math.floor(d2);
               int j = (int)Math.floor(d3 / 4.0D);
               int k = (int)Math.floor(d4);
               if (i != this.prevCloudX || j != this.prevCloudY || k != this.prevCloudZ || this.minecraft.options.getCloudsType() != this.prevCloudsType || this.prevCloudColor.distanceToSqr(vec3) > 2.0E-4D) {
                  this.prevCloudX = i;
                  this.prevCloudY = j;
                  this.prevCloudZ = k;
                  this.prevCloudColor = vec3;
                  this.prevCloudsType = this.minecraft.options.getCloudsType();
                  this.generateClouds = true;
               }

               if (this.generateClouds) {
                  this.generateClouds = false;
                  BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
                  if (this.cloudBuffer != null) {
                     this.cloudBuffer.close();
                  }

                  this.cloudBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                  BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = this.buildClouds(bufferbuilder, d2, d3, d4, vec3);
                  this.cloudBuffer.bind();
                  this.cloudBuffer.upload(bufferbuilder$renderedbuffer);
                  VertexBuffer.unbind();
               }

               RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
               RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
               FogRenderer.levelFogColor();
               pPoseStack.pushPose();
               pPoseStack.scale(12.0F, 1.0F, 12.0F);
               pPoseStack.translate(-f3, f4, -f5);
               if (this.cloudBuffer != null) {
                  this.cloudBuffer.bind();
                  int l = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

                  for(int i1 = l; i1 < 2; ++i1) {
                     if (i1 == 0) {
                        RenderSystem.colorMask(false, false, false, false);
                     } else {
                        RenderSystem.colorMask(true, true, true, true);
                     }

                     ShaderInstance shaderinstance = RenderSystem.getShader();
                     this.cloudBuffer.drawWithShader(pPoseStack.last().pose(), pProjectionMatrix, shaderinstance);
                  }

                  VertexBuffer.unbind();
               }

               pPoseStack.popPose();
               RenderSystem.enableCull();
               RenderSystem.disableBlend();
               RenderSystem.defaultBlendFunc();
               if (Config.isShaders()) {
                  Shaders.endClouds();
               }
            }

         }
      }
   }

   private BufferBuilder.RenderedBuffer buildClouds(BufferBuilder pBuilder, double pX, double pY, double pZ, Vec3 pCloudColor) {
      float f = 4.0F;
      float f1 = 0.00390625F;
      int i = 8;
      int j = 4;
      float f2 = 9.765625E-4F;
      float f3 = (float)Mth.floor(pX) * 0.00390625F;
      float f4 = (float)Mth.floor(pZ) * 0.00390625F;
      float f5 = (float)pCloudColor.x;
      float f6 = (float)pCloudColor.y;
      float f7 = (float)pCloudColor.z;
      float f8 = f5 * 0.9F;
      float f9 = f6 * 0.9F;
      float f10 = f7 * 0.9F;
      float f11 = f5 * 0.7F;
      float f12 = f6 * 0.7F;
      float f13 = f7 * 0.7F;
      float f14 = f5 * 0.8F;
      float f15 = f6 * 0.8F;
      float f16 = f7 * 0.8F;
      RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
      pBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
      float f17 = (float)Math.floor(pY / 4.0D) * 4.0F;
      if (Config.isCloudsFancy()) {
         for(int k = -3; k <= 4; ++k) {
            for(int l = -3; l <= 4; ++l) {
               float f18 = (float)(k * 8);
               float f19 = (float)(l * 8);
               if (f17 > -5.0F) {
                  pBuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  pBuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  pBuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  pBuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               }

               if (f17 <= 5.0F) {
                  pBuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 8.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  pBuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 8.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  pBuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  pBuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
               }

               if (k > -1) {
                  for(int i1 = 0; i1 < 8; ++i1) {
                     pBuilder.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     pBuilder.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + 8.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     pBuilder.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + 0.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     pBuilder.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                  }
               }

               if (k <= 1) {
                  for(int j2 = 0; j2 < 8; ++j2) {
                     pBuilder.vertex((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + (float)j2 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     pBuilder.vertex((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 4.0F), (double)(f19 + 8.0F)).uv((f18 + (float)j2 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     pBuilder.vertex((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 4.0F), (double)(f19 + 0.0F)).uv((f18 + (float)j2 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     pBuilder.vertex((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + (float)j2 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                  }
               }

               if (l > -1) {
                  for(int k2 = 0; k2 < 8; ++k2) {
                     pBuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + (float)k2 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     pBuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F), (double)(f19 + (float)k2 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     pBuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + (float)k2 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     pBuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + (float)k2 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                  }
               }

               if (l <= 1) {
                  for(int l2 = 0; l2 < 8; ++l2) {
                     pBuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     pBuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     pBuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     pBuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                  }
               }
            }
         }
      } else {
         int j1 = 1;
         int k1 = 32;

         for(int l1 = -32; l1 < 32; l1 += 32) {
            for(int i2 = -32; i2 < 32; i2 += 32) {
               pBuilder.vertex((double)(l1 + 0), (double)f17, (double)(i2 + 32)).uv((float)(l1 + 0) * 0.00390625F + f3, (float)(i2 + 32) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               pBuilder.vertex((double)(l1 + 32), (double)f17, (double)(i2 + 32)).uv((float)(l1 + 32) * 0.00390625F + f3, (float)(i2 + 32) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               pBuilder.vertex((double)(l1 + 32), (double)f17, (double)(i2 + 0)).uv((float)(l1 + 32) * 0.00390625F + f3, (float)(i2 + 0) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               pBuilder.vertex((double)(l1 + 0), (double)f17, (double)(i2 + 0)).uv((float)(l1 + 0) * 0.00390625F + f3, (float)(i2 + 0) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
            }
         }
      }

      return pBuilder.end();
   }

   private void compileChunks(Camera pCamera) {
      this.minecraft.getProfiler().push("populate_chunks_to_compile");
      LevelLightEngine levellightengine = this.level.getLightEngine();
      RenderRegionCache renderregioncache = new RenderRegionCache();
      BlockPos blockpos = pCamera.getBlockPosition();
      List<ChunkRenderDispatcher.RenderChunk> list = Lists.newArrayList();
      Lagometer.timerChunkUpdate.start();

      for(LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo : this.renderChunksInFrustum) {
         ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = levelrenderer$renderchunkinfo.chunk;
         SectionPos sectionpos = chunkrenderdispatcher$renderchunk.getSectionPosition();
         if (chunkrenderdispatcher$renderchunk.isDirty() && levellightengine.lightOnInSection(sectionpos)) {
            if (chunkrenderdispatcher$renderchunk.needsBackgroundPriorityUpdate()) {
               list.add(chunkrenderdispatcher$renderchunk);
            } else {
               boolean flag = false;
               if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.NEARBY) {
                  BlockPos blockpos1 = chunkrenderdispatcher$renderchunk.getOrigin().offset(8, 8, 8);
                  flag = blockpos1.distSqr(blockpos) < 768.0D || chunkrenderdispatcher$renderchunk.isDirtyFromPlayer();
               } else if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
                  flag = chunkrenderdispatcher$renderchunk.isDirtyFromPlayer();
               }

               if (flag) {
                  this.minecraft.getProfiler().push("build_near_sync");
                  this.chunkRenderDispatcher.rebuildChunkSync(chunkrenderdispatcher$renderchunk, renderregioncache);
                  chunkrenderdispatcher$renderchunk.setNotDirty();
                  this.minecraft.getProfiler().pop();
               } else {
                  list.add(chunkrenderdispatcher$renderchunk);
               }
            }
         }
      }

      Lagometer.timerChunkUpdate.end();
      Lagometer.timerChunkUpload.start();
      this.minecraft.getProfiler().popPush("upload");
      this.chunkRenderDispatcher.uploadAllPendingUploads();
      this.viewArea.clearUnusedVbos();
      this.minecraft.getProfiler().popPush("schedule_async_compile");
      if (this.chunksToResortTransparency.size() > 0) {
         Iterator iterator = this.chunksToResortTransparency.iterator();
         if (iterator.hasNext()) {
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk1 = (ChunkRenderDispatcher.RenderChunk)iterator.next();
            if (this.chunkRenderDispatcher.updateTransparencyLater(chunkrenderdispatcher$renderchunk1)) {
               iterator.remove();
            }
         }
      }

      double d1 = 0.0D;
      int i = Config.getUpdatesPerFrame();
      this.countChunksToUpdate = list.size();

      for(ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk2 : list) {
         boolean flag2 = chunkrenderdispatcher$renderchunk2.isChunkRegionEmpty();
         boolean flag1 = chunkrenderdispatcher$renderchunk2.needsBackgroundPriorityUpdate();
         if (chunkrenderdispatcher$renderchunk2.isDirty()) {
            chunkrenderdispatcher$renderchunk2.rebuildChunkAsync(this.chunkRenderDispatcher, renderregioncache);
            chunkrenderdispatcher$renderchunk2.setNotDirty();
            if (!flag2 && !flag1) {
               double d0 = 2.0D * RenderChunkUtils.getRelativeBufferSize(chunkrenderdispatcher$renderchunk2);
               d1 += d0;
               if (d1 > (double)i) {
                  break;
               }
            }
         }
      }

      Lagometer.timerChunkUpload.end();
      this.minecraft.getProfiler().pop();
   }

   private void renderWorldBorder(Camera pCamera) {
      BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
      WorldBorder worldborder = this.level.getWorldBorder();
      double d0 = (double)(this.minecraft.options.getEffectiveRenderDistance() * 16);
      if (!(pCamera.getPosition().x < worldborder.getMaxX() - d0) || !(pCamera.getPosition().x > worldborder.getMinX() + d0) || !(pCamera.getPosition().z < worldborder.getMaxZ() - d0) || !(pCamera.getPosition().z > worldborder.getMinZ() + d0)) {
         if (Config.isShaders()) {
            Shaders.pushProgram();
            Shaders.useProgram(Shaders.ProgramTexturedLit);
            Shaders.setRenderStage(RenderStage.WORLD_BORDER);
         }

         double d1 = 1.0D - worldborder.getDistanceToBorder(pCamera.getPosition().x, pCamera.getPosition().z) / d0;
         d1 = Math.pow(d1, 4.0D);
         d1 = Mth.clamp(d1, 0.0D, 1.0D);
         double d2 = pCamera.getPosition().x;
         double d3 = pCamera.getPosition().z;
         double d4 = (double)this.minecraft.gameRenderer.getDepthFar();
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);
         RenderSystem.depthMask(Minecraft.useShaderTransparency());
         PoseStack posestack = RenderSystem.getModelViewStack();
         posestack.pushPose();
         RenderSystem.applyModelViewMatrix();
         int i = worldborder.getStatus().getColor();
         float f = (float)(i >> 16 & 255) / 255.0F;
         float f1 = (float)(i >> 8 & 255) / 255.0F;
         float f2 = (float)(i & 255) / 255.0F;
         RenderSystem.setShaderColor(f, f1, f2, (float)d1);
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.polygonOffset(-3.0F, -3.0F);
         RenderSystem.enablePolygonOffset();
         RenderSystem.disableCull();
         float f3 = (float)(Util.getMillis() % 3000L) / 3000.0F;
         float f4 = (float)(-Mth.frac(pCamera.getPosition().y * 0.5D));
         float f5 = f4 + (float)d4;
         bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
         double d5 = Math.max((double)Mth.floor(d3 - d0), worldborder.getMinZ());
         double d6 = Math.min((double)Mth.ceil(d3 + d0), worldborder.getMaxZ());
         float f6 = (float)(Mth.floor(d5) & 1) * 0.5F;
         if (d2 > worldborder.getMaxX() - d0) {
            float f7 = f6;

            for(double d7 = d5; d7 < d6; f7 += 0.5F) {
               double d8 = Math.min(1.0D, d6 - d7);
               float f8 = (float)d8 * 0.5F;
               bufferbuilder.vertex(worldborder.getMaxX() - d2, -d4, d7 - d3).uv(f3 - f7, f3 + f5).endVertex();
               bufferbuilder.vertex(worldborder.getMaxX() - d2, -d4, d7 + d8 - d3).uv(f3 - (f8 + f7), f3 + f5).endVertex();
               bufferbuilder.vertex(worldborder.getMaxX() - d2, d4, d7 + d8 - d3).uv(f3 - (f8 + f7), f3 + f4).endVertex();
               bufferbuilder.vertex(worldborder.getMaxX() - d2, d4, d7 - d3).uv(f3 - f7, f3 + f4).endVertex();
               ++d7;
            }
         }

         if (d2 < worldborder.getMinX() + d0) {
            float f9 = f6;

            for(double d9 = d5; d9 < d6; f9 += 0.5F) {
               double d12 = Math.min(1.0D, d6 - d9);
               float f12 = (float)d12 * 0.5F;
               bufferbuilder.vertex(worldborder.getMinX() - d2, -d4, d9 - d3).uv(f3 + f9, f3 + f5).endVertex();
               bufferbuilder.vertex(worldborder.getMinX() - d2, -d4, d9 + d12 - d3).uv(f3 + f12 + f9, f3 + f5).endVertex();
               bufferbuilder.vertex(worldborder.getMinX() - d2, d4, d9 + d12 - d3).uv(f3 + f12 + f9, f3 + f4).endVertex();
               bufferbuilder.vertex(worldborder.getMinX() - d2, d4, d9 - d3).uv(f3 + f9, f3 + f4).endVertex();
               ++d9;
            }
         }

         d5 = Math.max((double)Mth.floor(d2 - d0), worldborder.getMinX());
         d6 = Math.min((double)Mth.ceil(d2 + d0), worldborder.getMaxX());
         f6 = (float)(Mth.floor(d5) & 1) * 0.5F;
         if (d3 > worldborder.getMaxZ() - d0) {
            float f10 = f6;

            for(double d10 = d5; d10 < d6; f10 += 0.5F) {
               double d13 = Math.min(1.0D, d6 - d10);
               float f13 = (float)d13 * 0.5F;
               bufferbuilder.vertex(d10 - d2, -d4, worldborder.getMaxZ() - d3).uv(f3 + f10, f3 + f5).endVertex();
               bufferbuilder.vertex(d10 + d13 - d2, -d4, worldborder.getMaxZ() - d3).uv(f3 + f13 + f10, f3 + f5).endVertex();
               bufferbuilder.vertex(d10 + d13 - d2, d4, worldborder.getMaxZ() - d3).uv(f3 + f13 + f10, f3 + f4).endVertex();
               bufferbuilder.vertex(d10 - d2, d4, worldborder.getMaxZ() - d3).uv(f3 + f10, f3 + f4).endVertex();
               ++d10;
            }
         }

         if (d3 < worldborder.getMinZ() + d0) {
            float f11 = f6;

            for(double d11 = d5; d11 < d6; f11 += 0.5F) {
               double d14 = Math.min(1.0D, d6 - d11);
               float f14 = (float)d14 * 0.5F;
               bufferbuilder.vertex(d11 - d2, -d4, worldborder.getMinZ() - d3).uv(f3 - f11, f3 + f5).endVertex();
               bufferbuilder.vertex(d11 + d14 - d2, -d4, worldborder.getMinZ() - d3).uv(f3 - (f14 + f11), f3 + f5).endVertex();
               bufferbuilder.vertex(d11 + d14 - d2, d4, worldborder.getMinZ() - d3).uv(f3 - (f14 + f11), f3 + f4).endVertex();
               bufferbuilder.vertex(d11 - d2, d4, worldborder.getMinZ() - d3).uv(f3 - f11, f3 + f4).endVertex();
               ++d11;
            }
         }

         BufferUploader.drawWithShader(bufferbuilder.end());
         RenderSystem.enableCull();
         RenderSystem.polygonOffset(0.0F, 0.0F);
         RenderSystem.disablePolygonOffset();
         RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
         posestack.popPose();
         RenderSystem.applyModelViewMatrix();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.depthMask(true);
         if (Config.isShaders()) {
            Shaders.popProgram();
            Shaders.setRenderStage(RenderStage.NONE);
         }
      }

   }

   private void renderHitOutline(PoseStack pPoseStack, VertexConsumer pConsumer, Entity pEntity, double pCamX, double pCamY, double pCamZ, BlockPos pPos, BlockState pState) {
      if (!Config.isCustomEntityModels() || !CustomEntityModels.isCustomModel(pState)) {
         renderShape(pPoseStack, pConsumer, pState.getShape(this.level, pPos, CollisionContext.of(pEntity)), (double)pPos.getX() - pCamX, (double)pPos.getY() - pCamY, (double)pPos.getZ() - pCamZ, 0.0F, 0.0F, 0.0F, 0.4F);
      }
   }

   private static Vec3 mixColor(float pHue) {
      float f = 5.99999F;
      int i = (int)(Mth.clamp(pHue, 0.0F, 1.0F) * 5.99999F);
      float f1 = pHue * 5.99999F - (float)i;
      Vec3 vec3;
      switch (i) {
         case 0:
            vec3 = new Vec3(1.0D, (double)f1, 0.0D);
            break;
         case 1:
            vec3 = new Vec3((double)(1.0F - f1), 1.0D, 0.0D);
            break;
         case 2:
            vec3 = new Vec3(0.0D, 1.0D, (double)f1);
            break;
         case 3:
            vec3 = new Vec3(0.0D, 1.0D - (double)f1, 1.0D);
            break;
         case 4:
            vec3 = new Vec3((double)f1, 0.0D, 1.0D);
            break;
         case 5:
            vec3 = new Vec3(1.0D, 0.0D, 1.0D - (double)f1);
            break;
         default:
            throw new IllegalStateException("Unexpected value: " + i);
      }

      return vec3;
   }

   private static Vec3 shiftHue(float pRed, float pGreen, float pBlue, float pHue) {
      Vec3 vec3 = mixColor(pHue).scale((double)pRed);
      Vec3 vec31 = mixColor((pHue + 0.33333334F) % 1.0F).scale((double)pGreen);
      Vec3 vec32 = mixColor((pHue + 0.6666667F) % 1.0F).scale((double)pBlue);
      Vec3 vec33 = vec3.add(vec31).add(vec32);
      double d0 = Math.max(Math.max(1.0D, vec33.x), Math.max(vec33.y, vec33.z));
      return new Vec3(vec33.x / d0, vec33.y / d0, vec33.z / d0);
   }

   public static void renderVoxelShape(PoseStack pPoseStack, VertexConsumer pConsumer, VoxelShape pShape, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pAlpha, boolean p_286443_) {
      List<AABB> list = pShape.toAabbs();
      if (!list.isEmpty()) {
         int i = p_286443_ ? list.size() : list.size() * 8;
         renderShape(pPoseStack, pConsumer, Shapes.create(list.get(0)), pX, pY, pZ, pRed, pGreen, pBlue, pAlpha);

         for(int j = 1; j < list.size(); ++j) {
            AABB aabb = list.get(j);
            float f = (float)j / (float)i;
            Vec3 vec3 = shiftHue(pRed, pGreen, pBlue, f);
            renderShape(pPoseStack, pConsumer, Shapes.create(aabb), pX, pY, pZ, (float)vec3.x, (float)vec3.y, (float)vec3.z, pAlpha);
         }
      }

   }

   private static void renderShape(PoseStack pPoseStack, VertexConsumer pConsumer, VoxelShape pShape, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pAlpha) {
      PoseStack.Pose posestack$pose = pPoseStack.last();
      pShape.forAllEdges((p_234270_12_, p_234270_14_, p_234270_16_, p_234270_18_, p_234270_20_, p_234270_22_) -> {
         float f = (float)(p_234270_18_ - p_234270_12_);
         float f1 = (float)(p_234270_20_ - p_234270_14_);
         float f2 = (float)(p_234270_22_ - p_234270_16_);
         float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
         f /= f3;
         f1 /= f3;
         f2 /= f3;
         pConsumer.vertex(posestack$pose.pose(), (float)(p_234270_12_ + pX), (float)(p_234270_14_ + pY), (float)(p_234270_16_ + pZ)).color(pRed, pGreen, pBlue, pAlpha).normal(posestack$pose.normal(), f, f1, f2).endVertex();
         pConsumer.vertex(posestack$pose.pose(), (float)(p_234270_18_ + pX), (float)(p_234270_20_ + pY), (float)(p_234270_22_ + pZ)).color(pRed, pGreen, pBlue, pAlpha).normal(posestack$pose.normal(), f, f1, f2).endVertex();
      });
   }

   public static void renderLineBox(VertexConsumer pConsumer, double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ, float pRed, float pGreen, float pBlue, float pAlpha) {
      renderLineBox(new PoseStack(), pConsumer, pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ, pRed, pGreen, pBlue, pAlpha, pRed, pGreen, pBlue);
   }

   public static void renderLineBox(PoseStack pPoseStack, VertexConsumer pConsumer, AABB pBox, float pRed, float pGreen, float pBlue, float pAlpha) {
      renderLineBox(pPoseStack, pConsumer, pBox.minX, pBox.minY, pBox.minZ, pBox.maxX, pBox.maxY, pBox.maxZ, pRed, pGreen, pBlue, pAlpha, pRed, pGreen, pBlue);
   }

   public static void renderLineBox(PoseStack pPoseStack, VertexConsumer pConsumer, double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ, float pRed, float pGreen, float pBlue, float pAlpha) {
      renderLineBox(pPoseStack, pConsumer, pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ, pRed, pGreen, pBlue, pAlpha, pRed, pGreen, pBlue);
   }

   public static void renderLineBox(PoseStack pPoseStack, VertexConsumer pConsumer, double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ, float pRed, float pGreen, float pBlue, float pAlpha, float pRed2, float pGreen2, float pBlue2) {
      Matrix4f matrix4f = pPoseStack.last().pose();
      Matrix3f matrix3f = pPoseStack.last().normal();
      float f = (float)pMinX;
      float f1 = (float)pMinY;
      float f2 = (float)pMinZ;
      float f3 = (float)pMaxX;
      float f4 = (float)pMaxY;
      float f5 = (float)pMaxZ;
      pConsumer.vertex(matrix4f, f, f1, f2).color(pRed, pGreen2, pBlue2, pAlpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f3, f1, f2).color(pRed, pGreen2, pBlue2, pAlpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f, f1, f2).color(pRed2, pGreen, pBlue2, pAlpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f, f4, f2).color(pRed2, pGreen, pBlue2, pAlpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f, f1, f2).color(pRed2, pGreen2, pBlue, pAlpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
      pConsumer.vertex(matrix4f, f, f1, f5).color(pRed2, pGreen2, pBlue, pAlpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
      pConsumer.vertex(matrix4f, f3, f1, f2).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f3, f4, f2).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f3, f4, f2).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f, f4, f2).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f, f4, f2).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
      pConsumer.vertex(matrix4f, f, f4, f5).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
      pConsumer.vertex(matrix4f, f, f4, f5).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f, f1, f5).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f, f1, f5).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f3, f1, f5).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f3, f1, f5).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
      pConsumer.vertex(matrix4f, f3, f1, f2).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
      pConsumer.vertex(matrix4f, f, f4, f5).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f3, f4, f5).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f3, f1, f5).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f3, f4, f5).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
      pConsumer.vertex(matrix4f, f3, f4, f2).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
      pConsumer.vertex(matrix4f, f3, f4, f5).color(pRed, pGreen, pBlue, pAlpha).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
   }

   public static void addChainedFilledBoxVertices(PoseStack pPoseStack, VertexConsumer pConsumer, double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ, float pRed, float pGreen, float pBlue, float pAlpha) {
      addChainedFilledBoxVertices(pPoseStack, pConsumer, (float)pMinX, (float)pMinY, (float)pMinZ, (float)pMaxX, (float)pMaxY, (float)pMaxZ, pRed, pGreen, pBlue, pAlpha);
   }

   public static void addChainedFilledBoxVertices(PoseStack pPoseStack, VertexConsumer pConsumer, float pMinX, float pMinY, float pMinZ, float pMaxX, float pMaxY, float pMaxZ, float pRed, float pGreen, float pBlue, float pAlpha) {
      Matrix4f matrix4f = pPoseStack.last().pose();
      pConsumer.vertex(matrix4f, pMinX, pMinY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMinY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMinY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMinY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMaxY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMaxY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMaxY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMinY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMaxY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMinY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMinY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMinY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMaxY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMaxY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMaxY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMinY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMaxY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMinY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMinY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMinY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMinY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMinY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMinY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMaxY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMaxY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMinX, pMaxY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMaxY, pMinZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMaxY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMaxY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pConsumer.vertex(matrix4f, pMaxX, pMaxY, pMaxZ).color(pRed, pGreen, pBlue, pAlpha).endVertex();
   }

   public void blockChanged(BlockGetter pLevel, BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {
      this.setBlockDirty(pPos, (pFlags & 8) != 0);
   }

   private void setBlockDirty(BlockPos pPos, boolean pReRenderOnMainThread) {
      for(int i = pPos.getZ() - 1; i <= pPos.getZ() + 1; ++i) {
         for(int j = pPos.getX() - 1; j <= pPos.getX() + 1; ++j) {
            for(int k = pPos.getY() - 1; k <= pPos.getY() + 1; ++k) {
               this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i), pReRenderOnMainThread);
            }
         }
      }

   }

   public void setBlocksDirty(int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ) {
      for(int i = pMinZ - 1; i <= pMaxZ + 1; ++i) {
         for(int j = pMinX - 1; j <= pMaxX + 1; ++j) {
            for(int k = pMinY - 1; k <= pMaxY + 1; ++k) {
               this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i));
            }
         }
      }

   }

   public void setBlockDirty(BlockPos pPos, BlockState pOldState, BlockState pNewState) {
      if (this.minecraft.getModelManager().requiresRender(pOldState, pNewState)) {
         this.setBlocksDirty(pPos.getX(), pPos.getY(), pPos.getZ(), pPos.getX(), pPos.getY(), pPos.getZ());
      }

   }

   public void setSectionDirtyWithNeighbors(int pSectionX, int pSectionY, int pSectionZ) {
      for(int i = pSectionZ - 1; i <= pSectionZ + 1; ++i) {
         for(int j = pSectionX - 1; j <= pSectionX + 1; ++j) {
            for(int k = pSectionY - 1; k <= pSectionY + 1; ++k) {
               this.setSectionDirty(j, k, i);
            }
         }
      }

   }

   public void setSectionDirty(int pSectionX, int pSectionY, int pSectionZ) {
      this.setSectionDirty(pSectionX, pSectionY, pSectionZ, false);
   }

   private void setSectionDirty(int pSectionX, int pSectionY, int pSectionZ, boolean pReRenderOnMainThread) {
      this.viewArea.setDirty(pSectionX, pSectionY, pSectionZ, pReRenderOnMainThread);
   }

   public void playStreamingMusic(@Nullable SoundEvent pSoundEvent, BlockPos pPos) {
      this.playStreamingMusic(pSoundEvent, pPos, pSoundEvent == null ? null : RecordItem.getBySound(pSoundEvent));
   }

   public void playStreamingMusic(@Nullable SoundEvent soundIn, BlockPos pos, @Nullable RecordItem musicDiscItem) {
      SoundInstance soundinstance = this.playingRecords.get(pos);
      if (soundinstance != null) {
         this.minecraft.getSoundManager().stop(soundinstance);
         this.playingRecords.remove(pos);
      }

      if (soundIn != null) {
         RecordItem recorditem = RecordItem.getBySound(soundIn);
         if (Reflector.MinecraftForge.exists()) {
            recorditem = musicDiscItem;
         }

         if (recorditem != null) {
            this.minecraft.gui.setNowPlaying(recorditem.getDisplayName());
         }

         SoundInstance soundinstance1 = SimpleSoundInstance.forRecord(soundIn, Vec3.atCenterOf(pos));
         this.playingRecords.put(pos, soundinstance1);
         this.minecraft.getSoundManager().play(soundinstance1);
      }

      this.notifyNearbyEntities(this.level, pos, soundIn != null);
   }

   private void notifyNearbyEntities(Level pLevel, BlockPos pPos, boolean pPlaying) {
      for(LivingEntity livingentity : pLevel.getEntitiesOfClass(LivingEntity.class, (new AABB(pPos)).inflate(3.0D))) {
         livingentity.setRecordPlayingNearby(pPos, pPlaying);
      }

   }

   public void addParticle(ParticleOptions pOptions, boolean pForce, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      this.addParticle(pOptions, pForce, false, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
   }

   public void addParticle(ParticleOptions pOptions, boolean pForce, boolean pDecreased, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      try {
         this.addParticleInternal(pOptions, pForce, pDecreased, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while adding particle");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being added");
         crashreportcategory.setDetail("ID", BuiltInRegistries.PARTICLE_TYPE.getKey(pOptions.getType()));
         crashreportcategory.setDetail("Parameters", pOptions.writeToString());
         crashreportcategory.setDetail("Position", () -> {
            return CrashReportCategory.formatLocation(this.level, pX, pY, pZ);
         });
         throw new ReportedException(crashreport);
      }
   }

   private <T extends ParticleOptions> void addParticle(T pOptions, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      this.addParticle(pOptions, pOptions.getType().getOverrideLimiter(), pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
   }

   @Nullable
   private Particle addParticleInternal(ParticleOptions pOptions, boolean pForce, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      return this.addParticleInternal(pOptions, pForce, false, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
   }

   @Nullable
   private Particle addParticleInternal(ParticleOptions pOptions, boolean pForce, boolean pDecreased, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      Camera camera = this.minecraft.gameRenderer.getMainCamera();
      ParticleStatus particlestatus = this.calculateParticleLevel(pDecreased);
      if (pOptions == ParticleTypes.EXPLOSION_EMITTER && !Config.isAnimatedExplosion()) {
         return null;
      } else if (pOptions == ParticleTypes.EXPLOSION && !Config.isAnimatedExplosion()) {
         return null;
      } else if (pOptions == ParticleTypes.POOF && !Config.isAnimatedExplosion()) {
         return null;
      } else if (pOptions == ParticleTypes.UNDERWATER && !Config.isWaterParticles()) {
         return null;
      } else if (pOptions == ParticleTypes.SMOKE && !Config.isAnimatedSmoke()) {
         return null;
      } else if (pOptions == ParticleTypes.LARGE_SMOKE && !Config.isAnimatedSmoke()) {
         return null;
      } else if (pOptions == ParticleTypes.ENTITY_EFFECT && !Config.isPotionParticles()) {
         return null;
      } else if (pOptions == ParticleTypes.AMBIENT_ENTITY_EFFECT && !Config.isPotionParticles()) {
         return null;
      } else if (pOptions == ParticleTypes.EFFECT && !Config.isPotionParticles()) {
         return null;
      } else if (pOptions == ParticleTypes.INSTANT_EFFECT && !Config.isPotionParticles()) {
         return null;
      } else if (pOptions == ParticleTypes.WITCH && !Config.isPotionParticles()) {
         return null;
      } else if (pOptions == ParticleTypes.PORTAL && !Config.isPortalParticles()) {
         return null;
      } else if (pOptions == ParticleTypes.FLAME && !Config.isAnimatedFlame()) {
         return null;
      } else if (pOptions == ParticleTypes.SOUL_FIRE_FLAME && !Config.isAnimatedFlame()) {
         return null;
      } else if (pOptions == ParticleTypes.DUST && !Config.isAnimatedRedstone()) {
         return null;
      } else if (pOptions == ParticleTypes.DRIPPING_WATER && !Config.isDrippingWaterLava()) {
         return null;
      } else if (pOptions == ParticleTypes.DRIPPING_LAVA && !Config.isDrippingWaterLava()) {
         return null;
      } else if (pOptions == ParticleTypes.FIREWORK && !Config.isFireworkParticles()) {
         return null;
      } else {
         if (!pForce) {
            double d0 = 1024.0D;
            if (pOptions == ParticleTypes.CRIT) {
               d0 = 38416.0D;
            }

            if (camera.getPosition().distanceToSqr(pX, pY, pZ) > d0) {
               return null;
            }

            if (particlestatus == ParticleStatus.MINIMAL) {
               return null;
            }
         }

         Particle particle = this.minecraft.particleEngine.createParticle(pOptions, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         if (pOptions == ParticleTypes.BUBBLE) {
            CustomColors.updateWaterFX(particle, this.level, pX, pY, pZ, this.renderEnv);
         }

         if (pOptions == ParticleTypes.SPLASH) {
            CustomColors.updateWaterFX(particle, this.level, pX, pY, pZ, this.renderEnv);
         }

         if (pOptions == ParticleTypes.RAIN) {
            CustomColors.updateWaterFX(particle, this.level, pX, pY, pZ, this.renderEnv);
         }

         if (pOptions == ParticleTypes.MYCELIUM) {
            CustomColors.updateMyceliumFX(particle);
         }

         if (pOptions == ParticleTypes.PORTAL) {
            CustomColors.updatePortalFX(particle);
         }

         if (pOptions == ParticleTypes.DUST) {
            CustomColors.updateReddustFX(particle, this.level, pX, pY, pZ);
         }

         if (pOptions == ParticleTypes.LAVA) {
            CustomColors.updateLavaFX(particle);
         }

         return particle;
      }
   }

   private ParticleStatus calculateParticleLevel(boolean pDecreased) {
      ParticleStatus particlestatus = this.minecraft.options.particles().get();
      if (pDecreased && particlestatus == ParticleStatus.MINIMAL && this.level.random.nextInt(10) == 0) {
         particlestatus = ParticleStatus.DECREASED;
      }

      if (particlestatus == ParticleStatus.DECREASED && this.level.random.nextInt(3) == 0) {
         particlestatus = ParticleStatus.MINIMAL;
      }

      return particlestatus;
   }

   public void clear() {
   }

   public void globalLevelEvent(int pType, BlockPos pPos, int pData) {
      switch (pType) {
         case 1023:
         case 1028:
         case 1038:
            Camera camera = this.minecraft.gameRenderer.getMainCamera();
            if (camera.isInitialized()) {
               double d0 = (double)pPos.getX() - camera.getPosition().x;
               double d1 = (double)pPos.getY() - camera.getPosition().y;
               double d2 = (double)pPos.getZ() - camera.getPosition().z;
               double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
               double d4 = camera.getPosition().x;
               double d5 = camera.getPosition().y;
               double d6 = camera.getPosition().z;
               if (d3 > 0.0D) {
                  d4 += d0 / d3 * 2.0D;
                  d5 += d1 / d3 * 2.0D;
                  d6 += d2 / d3 * 2.0D;
               }

               if (pType == 1023) {
                  this.level.playLocalSound(d4, d5, d6, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
               } else if (pType == 1038) {
                  this.level.playLocalSound(d4, d5, d6, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
               } else {
                  this.level.playLocalSound(d4, d5, d6, SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 5.0F, 1.0F, false);
               }
            }
         default:
      }
   }

   public void levelEvent(int pType, BlockPos pPos, int pData) {
      RandomSource randomsource = this.level.random;
      switch (pType) {
         case 1000:
            this.level.playLocalSound(pPos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1001:
            this.level.playLocalSound(pPos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.2F, false);
            break;
         case 1002:
            this.level.playLocalSound(pPos, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.2F, false);
            break;
         case 1003:
            this.level.playLocalSound(pPos, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
            break;
         case 1004:
            this.level.playLocalSound(pPos, SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
            break;
         case 1009:
            if (pData == 0) {
               this.level.playLocalSound(pPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (randomsource.nextFloat() - randomsource.nextFloat()) * 0.8F, false);
            } else if (pData == 1) {
               this.level.playLocalSound(pPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.7F, 1.6F + (randomsource.nextFloat() - randomsource.nextFloat()) * 0.4F, false);
            }
            break;
         case 1010:
            Item item = Item.byId(pData);
            if (item instanceof RecordItem recorditem) {
               if (Reflector.MinecraftForge.exists()) {
                  this.playStreamingMusic(recorditem.getSound(), pPos, recorditem);
               } else {
                  this.playStreamingMusic(recorditem.getSound(), pPos);
               }
            }
            break;
         case 1011:
            this.playStreamingMusic((SoundEvent)null, pPos);
            break;
         case 1015:
            this.level.playLocalSound(pPos, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 10.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1016:
            this.level.playLocalSound(pPos, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 10.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1017:
            this.level.playLocalSound(pPos, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 10.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1018:
            this.level.playLocalSound(pPos, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1019:
            this.level.playLocalSound(pPos, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1020:
            this.level.playLocalSound(pPos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1021:
            this.level.playLocalSound(pPos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1022:
            this.level.playLocalSound(pPos, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1024:
            this.level.playLocalSound(pPos, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1025:
            this.level.playLocalSound(pPos, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 0.05F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1026:
            this.level.playLocalSound(pPos, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1027:
            this.level.playLocalSound(pPos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1029:
            this.level.playLocalSound(pPos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0F, randomsource.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1030:
            this.level.playLocalSound(pPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, randomsource.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1031:
            this.level.playLocalSound(pPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1032:
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRAVEL, randomsource.nextFloat() * 0.4F + 0.8F, 0.25F));
            break;
         case 1033:
            this.level.playLocalSound(pPos, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1034:
            this.level.playLocalSound(pPos, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1035:
            this.level.playLocalSound(pPos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1039:
            this.level.playLocalSound(pPos, SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1040:
            this.level.playLocalSound(pPos, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1041:
            this.level.playLocalSound(pPos, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1042:
            this.level.playLocalSound(pPos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1043:
            this.level.playLocalSound(pPos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1044:
            this.level.playLocalSound(pPos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1045:
            this.level.playLocalSound(pPos, SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1046:
            this.level.playLocalSound(pPos, SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1047:
            this.level.playLocalSound(pPos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1048:
            this.level.playLocalSound(pPos, SoundEvents.SKELETON_CONVERTED_TO_STRAY, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1500:
            ComposterBlock.handleFill(this.level, pPos, pData > 0);
            break;
         case 1501:
            this.level.playLocalSound(pPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (randomsource.nextFloat() - randomsource.nextFloat()) * 0.8F, false);

            for(int i2 = 0; i2 < 8; ++i2) {
               this.level.addParticle(ParticleTypes.LARGE_SMOKE, (double)pPos.getX() + randomsource.nextDouble(), (double)pPos.getY() + 1.2D, (double)pPos.getZ() + randomsource.nextDouble(), 0.0D, 0.0D, 0.0D);
            }
            break;
         case 1502:
            this.level.playLocalSound(pPos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5F, 2.6F + (randomsource.nextFloat() - randomsource.nextFloat()) * 0.8F, false);

            for(int l1 = 0; l1 < 5; ++l1) {
               double d12 = (double)pPos.getX() + randomsource.nextDouble() * 0.6D + 0.2D;
               double d14 = (double)pPos.getY() + randomsource.nextDouble() * 0.6D + 0.2D;
               double d15 = (double)pPos.getZ() + randomsource.nextDouble() * 0.6D + 0.2D;
               this.level.addParticle(ParticleTypes.SMOKE, d12, d14, d15, 0.0D, 0.0D, 0.0D);
            }
            break;
         case 1503:
            this.level.playLocalSound(pPos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);

            for(int k1 = 0; k1 < 16; ++k1) {
               double d11 = (double)pPos.getX() + (5.0D + randomsource.nextDouble() * 6.0D) / 16.0D;
               double d13 = (double)pPos.getY() + 0.8125D;
               double d1 = (double)pPos.getZ() + (5.0D + randomsource.nextDouble() * 6.0D) / 16.0D;
               this.level.addParticle(ParticleTypes.SMOKE, d11, d13, d1, 0.0D, 0.0D, 0.0D);
            }
            break;
         case 1504:
            PointedDripstoneBlock.spawnDripParticle(this.level, pPos, this.level.getBlockState(pPos));
            break;
         case 1505:
            BoneMealItem.addGrowthParticles(this.level, pPos, pData);
            this.level.playLocalSound(pPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 2000:
            Direction direction = Direction.from3DDataValue(pData);
            int i = direction.getStepX();
            int j = direction.getStepY();
            int k = direction.getStepZ();
            double d0 = (double)pPos.getX() + (double)i * 0.6D + 0.5D;
            double d2 = (double)pPos.getY() + (double)j * 0.6D + 0.5D;
            double d3 = (double)pPos.getZ() + (double)k * 0.6D + 0.5D;

            for(int j2 = 0; j2 < 10; ++j2) {
               double d16 = randomsource.nextDouble() * 0.2D + 0.01D;
               double d17 = d0 + (double)i * 0.01D + (randomsource.nextDouble() - 0.5D) * (double)k * 0.5D;
               double d18 = d2 + (double)j * 0.01D + (randomsource.nextDouble() - 0.5D) * (double)j * 0.5D;
               double d19 = d3 + (double)k * 0.01D + (randomsource.nextDouble() - 0.5D) * (double)i * 0.5D;
               double d20 = (double)i * d16 + randomsource.nextGaussian() * 0.01D;
               double d21 = (double)j * d16 + randomsource.nextGaussian() * 0.01D;
               double d25 = (double)k * d16 + randomsource.nextGaussian() * 0.01D;
               this.addParticle(ParticleTypes.SMOKE, d17, d18, d19, d20, d21, d25);
            }
            break;
         case 2001:
            BlockState blockstate = Block.stateById(pData);
            if (!blockstate.isAir()) {
               SoundType soundtype = blockstate.getSoundType();
               if (Reflector.IForgeBlockState_getSoundType3.exists()) {
                  soundtype = (SoundType)Reflector.call(blockstate, Reflector.IForgeBlockState_getSoundType3, this.level, pPos, null);
               }

               this.level.playLocalSound(pPos, soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F, false);
            }

            this.level.addDestroyBlockEffect(pPos, blockstate);
            break;
         case 2002:
         case 2007:
            Vec3 vec3 = Vec3.atBottomCenterOf(pPos);

            for(int l = 0; l < 8; ++l) {
               this.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)), vec3.x, vec3.y, vec3.z, randomsource.nextGaussian() * 0.15D, randomsource.nextDouble() * 0.2D, randomsource.nextGaussian() * 0.15D);
            }

            float f3 = (float)(pData >> 16 & 255) / 255.0F;
            float f = (float)(pData >> 8 & 255) / 255.0F;
            float f1 = (float)(pData >> 0 & 255) / 255.0F;
            ParticleOptions particleoptions = pType == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;

            for(int k2 = 0; k2 < 100; ++k2) {
               double d5 = randomsource.nextDouble() * 4.0D;
               double d7 = randomsource.nextDouble() * Math.PI * 2.0D;
               double d9 = Math.cos(d7) * d5;
               double d24 = 0.01D + randomsource.nextDouble() * 0.5D;
               double d28 = Math.sin(d7) * d5;
               Particle particle = this.addParticleInternal(particleoptions, particleoptions.getType().getOverrideLimiter(), vec3.x + d9 * 0.1D, vec3.y + 0.3D, vec3.z + d28 * 0.1D, d9, d24, d28);
               if (particle != null) {
                  float f12 = 0.75F + randomsource.nextFloat() * 0.25F;
                  particle.setColor(f3 * f12, f * f12, f1 * f12);
                  particle.setPower((float)d5);
               }
            }

            this.level.playLocalSound(pPos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, randomsource.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 2003:
            double d4 = (double)pPos.getX() + 0.5D;
            double d6 = (double)pPos.getY();
            double d8 = (double)pPos.getZ() + 0.5D;

            for(int k3 = 0; k3 < 8; ++k3) {
               this.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)), d4, d6, d8, randomsource.nextGaussian() * 0.15D, randomsource.nextDouble() * 0.2D, randomsource.nextGaussian() * 0.15D);
            }

            for(double d22 = 0.0D; d22 < (Math.PI * 2D); d22 += 0.15707963267948966D) {
               this.addParticle(ParticleTypes.PORTAL, d4 + Math.cos(d22) * 5.0D, d6 - 0.4D, d8 + Math.sin(d22) * 5.0D, Math.cos(d22) * -5.0D, 0.0D, Math.sin(d22) * -5.0D);
               this.addParticle(ParticleTypes.PORTAL, d4 + Math.cos(d22) * 5.0D, d6 - 0.4D, d8 + Math.sin(d22) * 5.0D, Math.cos(d22) * -7.0D, 0.0D, Math.sin(d22) * -7.0D);
            }
            break;
         case 2004:
            for(int j3 = 0; j3 < 20; ++j3) {
               double d23 = (double)pPos.getX() + 0.5D + (randomsource.nextDouble() - 0.5D) * 2.0D;
               double d27 = (double)pPos.getY() + 0.5D + (randomsource.nextDouble() - 0.5D) * 2.0D;
               double d30 = (double)pPos.getZ() + 0.5D + (randomsource.nextDouble() - 0.5D) * 2.0D;
               this.level.addParticle(ParticleTypes.SMOKE, d23, d27, d30, 0.0D, 0.0D, 0.0D);
               this.level.addParticle(ParticleTypes.FLAME, d23, d27, d30, 0.0D, 0.0D, 0.0D);
            }
            break;
         case 2005:
            BoneMealItem.addGrowthParticles(this.level, pPos, pData);
            break;
         case 2006:
            for(int i3 = 0; i3 < 200; ++i3) {
               float f5 = randomsource.nextFloat() * 4.0F;
               float f7 = randomsource.nextFloat() * ((float)Math.PI * 2F);
               double d26 = (double)(Mth.cos(f7) * f5);
               double d29 = 0.01D + randomsource.nextDouble() * 0.5D;
               double d31 = (double)(Mth.sin(f7) * f5);
               Particle particle1 = this.addParticleInternal(ParticleTypes.DRAGON_BREATH, false, (double)pPos.getX() + d26 * 0.1D, (double)pPos.getY() + 0.3D, (double)pPos.getZ() + d31 * 0.1D, d26, d29, d31);
               if (particle1 != null) {
                  particle1.setPower(f5);
               }
            }

            if (pData == 1) {
               this.level.playLocalSound(pPos, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0F, randomsource.nextFloat() * 0.1F + 0.9F, false);
            }
            break;
         case 2008:
            this.level.addParticle(ParticleTypes.EXPLOSION, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
            break;
         case 2009:
            for(int l2 = 0; l2 < 8; ++l2) {
               this.level.addParticle(ParticleTypes.CLOUD, (double)pPos.getX() + randomsource.nextDouble(), (double)pPos.getY() + 1.2D, (double)pPos.getZ() + randomsource.nextDouble(), 0.0D, 0.0D, 0.0D);
            }
            break;
         case 3000:
            this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, true, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
            this.level.playLocalSound(pPos, SoundEvents.END_GATEWAY_SPAWN, SoundSource.BLOCKS, 10.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
            break;
         case 3001:
            this.level.playLocalSound(pPos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 64.0F, 0.8F + this.level.random.nextFloat() * 0.3F, false);
            break;
         case 3002:
            if (pData >= 0 && pData < Direction.Axis.VALUES.length) {
               ParticleUtils.spawnParticlesAlongAxis(Direction.Axis.VALUES[pData], this.level, pPos, 0.125D, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(10, 19));
            } else {
               ParticleUtils.spawnParticlesOnBlockFaces(this.level, pPos, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(3, 5));
            }
            break;
         case 3003:
            ParticleUtils.spawnParticlesOnBlockFaces(this.level, pPos, ParticleTypes.WAX_ON, UniformInt.of(3, 5));
            this.level.playLocalSound(pPos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 3004:
            ParticleUtils.spawnParticlesOnBlockFaces(this.level, pPos, ParticleTypes.WAX_OFF, UniformInt.of(3, 5));
            break;
         case 3005:
            ParticleUtils.spawnParticlesOnBlockFaces(this.level, pPos, ParticleTypes.SCRAPE, UniformInt.of(3, 5));
            break;
         case 3006:
            int i1 = pData >> 6;
            if (i1 > 0) {
               if (randomsource.nextFloat() < 0.3F + (float)i1 * 0.1F) {
                  float f4 = 0.15F + 0.02F * (float)i1 * (float)i1 * randomsource.nextFloat();
                  float f6 = 0.4F + 0.3F * (float)i1 * randomsource.nextFloat();
                  this.level.playLocalSound(pPos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, f4, f6, false);
               }

               byte b0 = (byte)(pData & 63);
               IntProvider intprovider = UniformInt.of(0, i1);
               float f8 = 0.005F;
               Supplier<Vec3> supplier = () -> {
                  return new Vec3(Mth.nextDouble(randomsource, (double)-0.005F, (double)0.005F), Mth.nextDouble(randomsource, (double)-0.005F, (double)0.005F), Mth.nextDouble(randomsource, (double)-0.005F, (double)0.005F));
               };
               if (b0 == 0) {
                  for(Direction direction1 : Direction.values()) {
                     float f2 = direction1 == Direction.DOWN ? (float)Math.PI : 0.0F;
                     double d10 = direction1.getAxis() == Direction.Axis.Y ? 0.65D : 0.57D;
                     ParticleUtils.spawnParticlesOnBlockFace(this.level, pPos, new SculkChargeParticleOptions(f2), intprovider, direction1, supplier, d10);
                  }
               } else {
                  for(Direction direction2 : MultifaceBlock.unpack(b0)) {
                     float f13 = direction2 == Direction.UP ? (float)Math.PI : 0.0F;
                     double d32 = 0.35D;
                     ParticleUtils.spawnParticlesOnBlockFace(this.level, pPos, new SculkChargeParticleOptions(f13), intprovider, direction2, supplier, 0.35D);
                  }
               }
            } else {
               this.level.playLocalSound(pPos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
               boolean flag1 = this.level.getBlockState(pPos).isCollisionShapeFullBlock(this.level, pPos);
               int l3 = flag1 ? 40 : 20;
               float f9 = flag1 ? 0.45F : 0.25F;
               float f10 = 0.07F;

               for(int i4 = 0; i4 < l3; ++i4) {
                  float f11 = 2.0F * randomsource.nextFloat() - 1.0F;
                  float f14 = 2.0F * randomsource.nextFloat() - 1.0F;
                  float f15 = 2.0F * randomsource.nextFloat() - 1.0F;
                  this.level.addParticle(ParticleTypes.SCULK_CHARGE_POP, (double)pPos.getX() + 0.5D + (double)(f11 * f9), (double)pPos.getY() + 0.5D + (double)(f14 * f9), (double)pPos.getZ() + 0.5D + (double)(f15 * f9), (double)(f11 * 0.07F), (double)(f14 * 0.07F), (double)(f15 * 0.07F));
               }
            }
            break;
         case 3007:
            for(int j1 = 0; j1 < 10; ++j1) {
               this.level.addParticle(new ShriekParticleOption(j1 * 5), false, (double)pPos.getX() + 0.5D, (double)pPos.getY() + SculkShriekerBlock.TOP_Y, (double)pPos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
            }

            BlockState blockstate2 = this.level.getBlockState(pPos);
            boolean flag = blockstate2.hasProperty(BlockStateProperties.WATERLOGGED) && blockstate2.getValue(BlockStateProperties.WATERLOGGED);
            if (!flag) {
               this.level.playLocalSound((double)pPos.getX() + 0.5D, (double)pPos.getY() + SculkShriekerBlock.TOP_Y, (double)pPos.getZ() + 0.5D, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 2.0F, 0.6F + this.level.random.nextFloat() * 0.4F, false);
            }
            break;
         case 3008:
            BlockState blockstate1 = Block.stateById(pData);
            Block block = blockstate1.getBlock();
            if (block instanceof BrushableBlock brushableblock) {
               this.level.playLocalSound(pPos, brushableblock.getBrushCompletedSound(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
            }

            this.level.addDestroyBlockEffect(pPos, blockstate1);
            break;
         case 3009:
            ParticleUtils.spawnParticlesOnBlockFaces(this.level, pPos, ParticleTypes.EGG_CRACK, UniformInt.of(3, 6));
      }

   }

   public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress) {
      if (pProgress >= 0 && pProgress < 10) {
         BlockDestructionProgress blockdestructionprogress1 = this.destroyingBlocks.get(pBreakerId);
         if (blockdestructionprogress1 != null) {
            this.removeProgress(blockdestructionprogress1);
         }

         if (blockdestructionprogress1 == null || blockdestructionprogress1.getPos().getX() != pPos.getX() || blockdestructionprogress1.getPos().getY() != pPos.getY() || blockdestructionprogress1.getPos().getZ() != pPos.getZ()) {
            blockdestructionprogress1 = new BlockDestructionProgress(pBreakerId, pPos);
            this.destroyingBlocks.put(pBreakerId, blockdestructionprogress1);
         }

         blockdestructionprogress1.setProgress(pProgress);
         blockdestructionprogress1.updateTick(this.ticks);
         this.destructionProgress.computeIfAbsent(blockdestructionprogress1.getPos().asLong(), (p_234253_0_) -> {
            return Sets.newTreeSet();
         }).add(blockdestructionprogress1);
      } else {
         BlockDestructionProgress blockdestructionprogress = this.destroyingBlocks.remove(pBreakerId);
         if (blockdestructionprogress != null) {
            this.removeProgress(blockdestructionprogress);
         }
      }

   }

   public boolean hasRenderedAllChunks() {
      return this.chunkRenderDispatcher.isQueueEmpty();
   }

   public void needsUpdate() {
      this.needsFullRenderChunkUpdate = true;
      this.generateClouds = true;
   }

   public int getCountRenderers() {
      return this.viewArea.chunks.length;
   }

   public int getCountEntitiesRendered() {
      return this.renderedEntities;
   }

   public int getCountTileEntitiesRendered() {
      return this.countTileEntitiesRendered;
   }

   public int getCountLoadedChunks() {
      if (this.level == null) {
         return 0;
      } else {
         ClientChunkCache clientchunkcache = this.level.getChunkSource();
         return clientchunkcache == null ? 0 : clientchunkcache.getLoadedChunksCount();
      }
   }

   public int getCountChunksToUpdate() {
      return this.countChunksToUpdate;
   }

   public ChunkRenderDispatcher.RenderChunk getRenderChunk(BlockPos pos) {
      return this.viewArea.getRenderChunkAt(pos);
   }

   public ClientLevel getWorld() {
      return this.level;
   }

   private void clearRenderInfos() {
      this.clearRenderInfosTerrain();
      this.clearRenderInfosEntities();
   }

   private void clearRenderInfosTerrain() {
      if (renderEntitiesCounter > 0) {
         this.renderInfosTerrain = new ObjectArrayList<>(this.renderInfosTerrain.size() + 16);
         this.renderInfosTileEntities = new ArrayList<>(this.renderInfosTileEntities.size() + 16);
      } else {
         this.renderInfosTerrain.clear();
         this.renderInfosTileEntities.clear();
      }

   }

   private void clearRenderInfosEntities() {
      if (renderEntitiesCounter > 0) {
         this.renderInfosEntities = new LongOpenHashSet(this.renderInfosEntities.size() + 16);
      } else {
         this.renderInfosEntities.clear();
      }

   }

   public void onPlayerPositionSet() {
      if (this.firstWorldLoad) {
         this.allChanged();
         this.firstWorldLoad = false;
      }

   }

   public void pauseChunkUpdates() {
      if (this.chunkRenderDispatcher != null) {
         this.chunkRenderDispatcher.pauseChunkUpdates();
      }

   }

   public void resumeChunkUpdates() {
      if (this.chunkRenderDispatcher != null) {
         this.chunkRenderDispatcher.resumeChunkUpdates();
      }

   }

   public int getFrameCount() {
      return this.frameId;
   }

   public RenderBuffers getRenderTypeTextures() {
      return this.renderBuffers;
   }

   public LongOpenHashSet getRenderChunksEntities() {
      return this.renderInfosEntities;
   }

   private void addEntitySection(LongOpenHashSet set, EntitySectionStorage storage, BlockPos pos) {
      long i = SectionPos.asLong(pos);
      EntitySection entitysection = storage.getSection(i);
      if (entitysection != null) {
         set.add(i);
      }
   }

   private boolean hasEntitySection(EntitySectionStorage storage, BlockPos pos) {
      long i = SectionPos.asLong(pos);
      EntitySection entitysection = storage.getSection(i);
      return entitysection != null;
   }

   public List<LevelRenderer.RenderChunkInfo> getRenderInfosTileEntities() {
      return this.renderInfosTileEntities;
   }

   private void checkLoadVisibleChunks(Camera activeRenderInfo, Frustum icamera, boolean spectator) {
      if (this.loadVisibleChunksCounter == 0) {
         this.loadAllVisibleChunks(activeRenderInfo, icamera, spectator);
         this.minecraft.gui.getChat().deleteMessage(loadVisibleChunksMessageId);
      }

      if (this.loadVisibleChunksCounter >= 0) {
         --this.loadVisibleChunksCounter;
      }

   }

   private void loadAllVisibleChunks(Camera activeRenderInfo, Frustum icamera, boolean spectator) {
      int i = this.minecraft.options.ofChunkUpdates;
      boolean flag = this.minecraft.options.ofLazyChunkLoading;

      try {
         this.minecraft.options.ofChunkUpdates = 1000;
         this.minecraft.options.ofLazyChunkLoading = false;
         LevelRenderer levelrenderer = Config.getRenderGlobal();
         int j = levelrenderer.getCountLoadedChunks();
         long k = System.currentTimeMillis();
         Config.dbg("Loading visible chunks");
         long l = System.currentTimeMillis() + 5000L;
         int i1 = 0;
         boolean flag1 = false;

         do {
            flag1 = false;

            for(int j1 = 0; j1 < 100; ++j1) {
               levelrenderer.needsUpdate();
               levelrenderer.setupRender(activeRenderInfo, icamera, false, spectator);
               Config.sleep(1L);
               this.compileChunks(activeRenderInfo);
               if (levelrenderer.getCountChunksToUpdate() > 0) {
                  flag1 = true;
               }

               if (!levelrenderer.hasRenderedAllChunks()) {
                  flag1 = true;
               }

               i1 += levelrenderer.getCountChunksToUpdate();

               while(!levelrenderer.hasRenderedAllChunks()) {
                  int k1 = levelrenderer.getCountChunksToUpdate();
                  this.compileChunks(activeRenderInfo);
                  if (k1 == levelrenderer.getCountChunksToUpdate()) {
                     break;
                  }
               }

               i1 -= levelrenderer.getCountChunksToUpdate();
               if (!flag1) {
                  break;
               }
            }

            if (levelrenderer.getCountLoadedChunks() != j) {
               flag1 = true;
               j = levelrenderer.getCountLoadedChunks();
            }

            if (System.currentTimeMillis() > l) {
               Config.log("Chunks loaded: " + i1);
               l = System.currentTimeMillis() + 5000L;
            }
         } while(flag1);

         Config.log("Chunks loaded: " + i1);
         Config.log("Finished loading visible chunks");
         ChunkRenderDispatcher.renderChunksUpdated = 0;
      } finally {
         this.minecraft.options.ofChunkUpdates = i;
         this.minecraft.options.ofLazyChunkLoading = flag;
      }

   }

   public void applyFrustumEntities(Frustum camera, int maxChunkDistance) {
      this.renderInfosEntities.clear();
      int i = (int)camera.getCameraX() >> 4 << 4;
      int j = (int)camera.getCameraY() >> 4 << 4;
      int k = (int)camera.getCameraZ() >> 4 << 4;
      int l = maxChunkDistance * maxChunkDistance;
      EntitySectionStorage<?> entitysectionstorage = this.level.getSectionStorage();
      BlockPosM blockposm = new BlockPosM();
      LongSet longset = entitysectionstorage.getSectionKeys();
      LongIterator longiterator = longset.iterator();

      while(longiterator.hasNext()) {
         long i1 = longiterator.nextLong();
         blockposm.setXyz(SectionPos.sectionToBlockCoord(SectionPos.x(i1)), SectionPos.sectionToBlockCoord(SectionPos.y(i1)), SectionPos.sectionToBlockCoord(SectionPos.z(i1)));
         ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.viewArea.getRenderChunkAt(blockposm);
         if (chunkrenderdispatcher$renderchunk != null && camera.isVisible(chunkrenderdispatcher$renderchunk.getBoundingBox())) {
            if (maxChunkDistance > 0) {
               BlockPos blockpos = chunkrenderdispatcher$renderchunk.getOrigin();
               int j1 = i - blockpos.getX();
               int k1 = j - blockpos.getY();
               int l1 = k - blockpos.getZ();
               int i2 = j1 * j1 + k1 * k1 + l1 * l1;
               if (i2 > l) {
                  continue;
               }
            }

            this.renderInfosEntities.add(i1);
         }
      }

   }

   public void setShadowRenderInfos(boolean shadowInfos) {
      if (shadowInfos) {
         this.renderInfosTerrain = this.renderInfosTerrainShadow;
         this.renderInfosEntities = this.renderInfosEntitiesShadow;
         this.renderInfosTileEntities = this.renderInfosTileEntitiesShadow;
      } else {
         this.renderInfosTerrain = this.renderInfosTerrainNormal;
         this.renderInfosEntities = this.renderInfosEntitiesNormal;
         this.renderInfosTileEntities = this.renderInfosTileEntitiesNormal;
      }

   }

   public int getRenderedChunksShadow() {
      return !Config.isShadersShadows() ? -1 : this.renderInfosTerrainShadow.size();
   }

   public int getCountEntitiesRenderedShadow() {
      return !Config.isShadersShadows() ? -1 : ShadersRender.countEntitiesRenderedShadow;
   }

   public int getCountTileEntitiesRenderedShadow() {
      if (!Config.isShaders()) {
         return -1;
      } else {
         return !Shaders.hasShadowMap ? -1 : ShadersRender.countTileEntitiesRenderedShadow;
      }
   }

   public void captureFrustumShadow() {
      this.debugFixTerrainFrustumShadow = true;
   }

   public boolean isDebugFrustum() {
      return this.capturedFrustum != null;
   }

   public void onChunkRenderNeedsUpdate(ChunkRenderDispatcher.RenderChunk renderChunk) {
      if (!renderChunk.getCompiledChunk().hasTerrainBlockEntities()) {
         this.renderChunksInFrustum.add(renderChunk.getRenderInfo());
      }
   }

   public boolean needsFrustumUpdate() {
      return this.needsFrustumUpdate.get();
   }

   public boolean shouldRenderEntity(Entity entity, int minWorldY, int maxWorldY) {
      if (entity instanceof Display) {
         return true;
      } else {
         BlockPos blockpos = entity.blockPosition();
         return this.renderInfosEntities.contains(SectionPos.asLong(blockpos)) || blockpos.getY() <= minWorldY || blockpos.getY() >= maxWorldY;
      }
   }

   public Frustum getFrustum() {
      return this.capturedFrustum != null ? this.capturedFrustum : this.cullingFrustum;
   }

   public int getTicks() {
      return this.ticks;
   }

   public void updateGlobalBlockEntities(Collection<BlockEntity> pBlockEntitiesToRemove, Collection<BlockEntity> pBlockEntitiesToAdd) {
      synchronized(this.globalBlockEntities) {
         this.globalBlockEntities.removeAll(pBlockEntitiesToRemove);
         this.globalBlockEntities.addAll(pBlockEntitiesToAdd);
      }
   }

   public static int getLightColor(BlockAndTintGetter pLevel, BlockPos pPos) {
      return getLightColor(pLevel, pLevel.getBlockState(pPos), pPos);
   }

   public static int getLightColor(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos) {
      if (EmissiveTextures.isRenderEmissive() && Config.isMinecraftThread()) {
         return LightTexture.MAX_BRIGHTNESS;
      } else if (pState.emissiveRendering(pLevel, pPos)) {
         return 15794417;
      } else {
         int i = pLevel.getBrightness(LightLayer.SKY, pPos);
         int j = pLevel.getBrightness(LightLayer.BLOCK, pPos);
         int k = pState.getLightValue(pLevel, pPos);
         if (j < k) {
            j = k;
         }

         int l = i << 20 | j << 4;
         if (Config.isDynamicLights() && pLevel instanceof BlockGetter && (!ambientOcclusion || !pState.isSolidRender(pLevel, pPos))) {
            l = DynamicLights.getCombinedLight(pPos, l);
         }

         return l;
      }
   }

   public boolean isChunkCompiled(BlockPos pPos) {
      ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.viewArea.getRenderChunkAt(pPos);
      return chunkrenderdispatcher$renderchunk != null && chunkrenderdispatcher$renderchunk.compiled.get() != ChunkRenderDispatcher.CompiledChunk.UNCOMPILED;
   }

   @Nullable
   public RenderTarget entityTarget() {
      return this.entityTarget;
   }

   @Nullable
   public RenderTarget getTranslucentTarget() {
      return this.translucentTarget;
   }

   @Nullable
   public RenderTarget getItemEntityTarget() {
      return this.itemEntityTarget;
   }

   @Nullable
   public RenderTarget getParticlesTarget() {
      return this.particlesTarget;
   }

   @Nullable
   public RenderTarget getWeatherTarget() {
      return this.weatherTarget;
   }

   @Nullable
   public RenderTarget getCloudsTarget() {
      return this.cloudsTarget;
   }

   public static class RenderChunkInfo {
      public final ChunkRenderDispatcher.RenderChunk chunk;
      private int sourceDirections;
      int directions;
      int step;

      public RenderChunkInfo(ChunkRenderDispatcher.RenderChunk pChunk, @Nullable Direction pSourceDirection, int pStep) {
         this.chunk = pChunk;
         if (pSourceDirection != null) {
            this.addSourceDirection(pSourceDirection);
         }

         this.step = pStep;
      }

      public void setDirection(int dir, Direction facingIn) {
         this.directions = this.directions | dir | 1 << facingIn.ordinal();
      }

      public boolean hasDirection(Direction pFacing) {
         return (this.directions & 1 << pFacing.ordinal()) > 0;
      }

      public void initialize(Direction facingIn, int counter) {
         this.sourceDirections = facingIn != null ? 1 << facingIn.ordinal() : 0;
         this.directions = 0;
         this.step = counter;
      }

      public String toString() {
         return "" + this.chunk.getOrigin();
      }

      public void addSourceDirection(Direction pDirection) {
         this.sourceDirections = (byte)(this.sourceDirections | this.sourceDirections | 1 << pDirection.ordinal());
      }

      public boolean hasSourceDirection(int pDirectionIndex) {
         return (this.sourceDirections & 1 << pDirectionIndex) > 0;
      }

      public boolean hasSourceDirections() {
         return this.sourceDirections != 0;
      }

      public boolean isAxisAlignedWith(int pX, int pY, int pZ) {
         BlockPos blockpos = this.chunk.getOrigin();
         return pX == blockpos.getX() / 16 || pZ == blockpos.getZ() / 16 || pY == blockpos.getY() / 16;
      }

      public int hashCode() {
         return this.chunk.getOrigin().hashCode();
      }

      public boolean equals(Object pOther) {
         if (pOther instanceof LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo) {
            return this.chunk.getOrigin().equals(levelrenderer$renderchunkinfo.chunk.getOrigin());
         } else {
            return false;
         }
      }
   }

   static class RenderChunkStorage {
      public final LevelRenderer.RenderInfoMap renderInfoMap;
      public final Set<LevelRenderer.RenderChunkInfo> renderChunks;
      public final Vec3M vec3M1 = new Vec3M(0.0D, 0.0D, 0.0D);
      public final Vec3M vec3M2 = new Vec3M(0.0D, 0.0D, 0.0D);
      public final Vec3M vec3M3 = new Vec3M(0.0D, 0.0D, 0.0D);
      public final BlockPosM blockPosM1 = new BlockPosM();

      public RenderChunkStorage(int pSize) {
         this.renderInfoMap = new LevelRenderer.RenderInfoMap(pSize);
         this.renderChunks = new ObjectLinkedOpenHashSet<>(pSize);
      }
   }

   static class RenderInfoMap {
      private final LevelRenderer.RenderChunkInfo[] infos;

      RenderInfoMap(int pSize) {
         this.infos = new LevelRenderer.RenderChunkInfo[pSize];
      }

      public void put(ChunkRenderDispatcher.RenderChunk pRenderChunk, LevelRenderer.RenderChunkInfo pInfo) {
         this.infos[pRenderChunk.index] = pInfo;
      }

      @Nullable
      public LevelRenderer.RenderChunkInfo get(ChunkRenderDispatcher.RenderChunk pRenderChunk) {
         int i = pRenderChunk.index;
         return i >= 0 && i < this.infos.length ? this.infos[i] : null;
      }
   }

   public static class TransparencyShaderException extends RuntimeException {
      public TransparencyShaderException(String pMessage, Throwable pCause) {
         super(pMessage, pCause);
      }
   }
}