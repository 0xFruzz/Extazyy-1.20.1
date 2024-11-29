package net.minecraft.client.particle;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.reflect.Reflector;
import net.optifine.render.RenderEnv;
import org.slf4j.Logger;

public class ParticleEngine implements PreparableReloadListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final FileToIdConverter PARTICLE_LISTER = FileToIdConverter.json("particles");
   private static final ResourceLocation PARTICLES_ATLAS_INFO = new ResourceLocation("particles");
   private static final int MAX_PARTICLES_PER_LAYER = 16384;
   private static final List<ParticleRenderType> RENDER_ORDER = ImmutableList.of(ParticleRenderType.TERRAIN_SHEET, ParticleRenderType.PARTICLE_SHEET_OPAQUE, ParticleRenderType.PARTICLE_SHEET_LIT, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT, ParticleRenderType.CUSTOM);
   protected ClientLevel level;
   private Map<ParticleRenderType, Queue<Particle>> particles = Maps.newIdentityHashMap();
   private final Queue<TrackingEmitter> trackingEmitters = Queues.newArrayDeque();
   private final TextureManager textureManager;
   private final RandomSource random = RandomSource.create();
   private final Map<ResourceLocation, ParticleProvider<?>> providers = new HashMap<>();
   private final Queue<Particle> particlesToAdd = Queues.newArrayDeque();
   private final Map<ResourceLocation, ParticleEngine.MutableSpriteSet> spriteSets = Maps.newHashMap();
   private final TextureAtlas textureAtlas;
   private final Object2IntOpenHashMap<ParticleGroup> trackedParticleCounts = new Object2IntOpenHashMap<>();
   private RenderEnv renderEnv = new RenderEnv((BlockState)null, (BlockPos)null);

   public ParticleEngine(ClientLevel pLevel, TextureManager pTextureManager) {
      if (Reflector.ForgeHooksClient_makeParticleRenderTypeComparator.exists()) {
         Comparator comparator = (Comparator)Reflector.ForgeHooksClient_makeParticleRenderTypeComparator.call((Object)RENDER_ORDER);
         if (comparator != null) {
            this.particles = Maps.newTreeMap(comparator);
         }
      }

      this.textureAtlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);
      pTextureManager.register(this.textureAtlas.location(), this.textureAtlas);
      this.level = pLevel;
      this.textureManager = pTextureManager;
      this.registerProviders();
   }

   private void registerProviders() {
      this.register(ParticleTypes.AMBIENT_ENTITY_EFFECT, SpellParticle.AmbientMobProvider::new);
      this.register(ParticleTypes.ANGRY_VILLAGER, HeartParticle.AngryVillagerProvider::new);
      this.register(ParticleTypes.BLOCK_MARKER, new BlockMarker.Provider());
      this.register(ParticleTypes.BLOCK, new TerrainParticle.Provider());
      this.register(ParticleTypes.BUBBLE, BubbleParticle.Provider::new);
      this.register(ParticleTypes.BUBBLE_COLUMN_UP, BubbleColumnUpParticle.Provider::new);
      this.register(ParticleTypes.BUBBLE_POP, BubblePopParticle.Provider::new);
      this.register(ParticleTypes.CAMPFIRE_COSY_SMOKE, CampfireSmokeParticle.CosyProvider::new);
      this.register(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, CampfireSmokeParticle.SignalProvider::new);
      this.register(ParticleTypes.CLOUD, PlayerCloudParticle.Provider::new);
      this.register(ParticleTypes.COMPOSTER, SuspendedTownParticle.ComposterFillProvider::new);
      this.register(ParticleTypes.CRIT, CritParticle.Provider::new);
      this.register(ParticleTypes.CURRENT_DOWN, WaterCurrentDownParticle.Provider::new);
      this.register(ParticleTypes.DAMAGE_INDICATOR, CritParticle.DamageIndicatorProvider::new);
      this.register(ParticleTypes.DRAGON_BREATH, DragonBreathParticle.Provider::new);
      this.register(ParticleTypes.DOLPHIN, SuspendedTownParticle.DolphinSpeedProvider::new);
      this.register(ParticleTypes.DRIPPING_LAVA, DripParticle::createLavaHangParticle);
      this.register(ParticleTypes.FALLING_LAVA, DripParticle::createLavaFallParticle);
      this.register(ParticleTypes.LANDING_LAVA, DripParticle::createLavaLandParticle);
      this.register(ParticleTypes.DRIPPING_WATER, DripParticle::createWaterHangParticle);
      this.register(ParticleTypes.FALLING_WATER, DripParticle::createWaterFallParticle);
      this.register(ParticleTypes.DUST, DustParticle.Provider::new);
      this.register(ParticleTypes.DUST_COLOR_TRANSITION, DustColorTransitionParticle.Provider::new);
      this.register(ParticleTypes.EFFECT, SpellParticle.Provider::new);
      this.register(ParticleTypes.ELDER_GUARDIAN, new MobAppearanceParticle.Provider());
      this.register(ParticleTypes.ENCHANTED_HIT, CritParticle.MagicProvider::new);
      this.register(ParticleTypes.ENCHANT, EnchantmentTableParticle.Provider::new);
      this.register(ParticleTypes.END_ROD, EndRodParticle.Provider::new);
      this.register(ParticleTypes.ENTITY_EFFECT, SpellParticle.MobProvider::new);
      this.register(ParticleTypes.EXPLOSION_EMITTER, new HugeExplosionSeedParticle.Provider());
      this.register(ParticleTypes.EXPLOSION, HugeExplosionParticle.Provider::new);
      this.register(ParticleTypes.SONIC_BOOM, SonicBoomParticle.Provider::new);
      this.register(ParticleTypes.FALLING_DUST, FallingDustParticle.Provider::new);
      this.register(ParticleTypes.FIREWORK, FireworkParticles.SparkProvider::new);
      this.register(ParticleTypes.FISHING, WakeParticle.Provider::new);
      this.register(ParticleTypes.FLAME, FlameParticle.Provider::new);
      this.register(ParticleTypes.SCULK_SOUL, SoulParticle.EmissiveProvider::new);
      this.register(ParticleTypes.SCULK_CHARGE, SculkChargeParticle.Provider::new);
      this.register(ParticleTypes.SCULK_CHARGE_POP, SculkChargePopParticle.Provider::new);
      this.register(ParticleTypes.SOUL, SoulParticle.Provider::new);
      this.register(ParticleTypes.SOUL_FIRE_FLAME, FlameParticle.Provider::new);
      this.register(ParticleTypes.FLASH, FireworkParticles.FlashProvider::new);
      this.register(ParticleTypes.HAPPY_VILLAGER, SuspendedTownParticle.HappyVillagerProvider::new);
      this.register(ParticleTypes.HEART, HeartParticle.Provider::new);
      this.register(ParticleTypes.INSTANT_EFFECT, SpellParticle.InstantProvider::new);
      this.register(ParticleTypes.ITEM, new BreakingItemParticle.Provider());
      this.register(ParticleTypes.ITEM_SLIME, new BreakingItemParticle.SlimeProvider());
      this.register(ParticleTypes.ITEM_SNOWBALL, new BreakingItemParticle.SnowballProvider());
      this.register(ParticleTypes.LARGE_SMOKE, LargeSmokeParticle.Provider::new);
      this.register(ParticleTypes.LAVA, LavaParticle.Provider::new);
      this.register(ParticleTypes.MYCELIUM, SuspendedTownParticle.Provider::new);
      this.register(ParticleTypes.NAUTILUS, EnchantmentTableParticle.NautilusProvider::new);
      this.register(ParticleTypes.NOTE, NoteParticle.Provider::new);
      this.register(ParticleTypes.POOF, ExplodeParticle.Provider::new);
      this.register(ParticleTypes.PORTAL, PortalParticle.Provider::new);
      this.register(ParticleTypes.RAIN, WaterDropParticle.Provider::new);
      this.register(ParticleTypes.SMOKE, SmokeParticle.Provider::new);
      this.register(ParticleTypes.SNEEZE, PlayerCloudParticle.SneezeProvider::new);
      this.register(ParticleTypes.SNOWFLAKE, SnowflakeParticle.Provider::new);
      this.register(ParticleTypes.SPIT, SpitParticle.Provider::new);
      this.register(ParticleTypes.SWEEP_ATTACK, AttackSweepParticle.Provider::new);
      this.register(ParticleTypes.TOTEM_OF_UNDYING, TotemParticle.Provider::new);
      this.register(ParticleTypes.SQUID_INK, SquidInkParticle.Provider::new);
      this.register(ParticleTypes.UNDERWATER, SuspendedParticle.UnderwaterProvider::new);
      this.register(ParticleTypes.SPLASH, SplashParticle.Provider::new);
      this.register(ParticleTypes.WITCH, SpellParticle.WitchProvider::new);
      this.register(ParticleTypes.DRIPPING_HONEY, DripParticle::createHoneyHangParticle);
      this.register(ParticleTypes.FALLING_HONEY, DripParticle::createHoneyFallParticle);
      this.register(ParticleTypes.LANDING_HONEY, DripParticle::createHoneyLandParticle);
      this.register(ParticleTypes.FALLING_NECTAR, DripParticle::createNectarFallParticle);
      this.register(ParticleTypes.FALLING_SPORE_BLOSSOM, DripParticle::createSporeBlossomFallParticle);
      this.register(ParticleTypes.SPORE_BLOSSOM_AIR, SuspendedParticle.SporeBlossomAirProvider::new);
      this.register(ParticleTypes.ASH, AshParticle.Provider::new);
      this.register(ParticleTypes.CRIMSON_SPORE, SuspendedParticle.CrimsonSporeProvider::new);
      this.register(ParticleTypes.WARPED_SPORE, SuspendedParticle.WarpedSporeProvider::new);
      this.register(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, DripParticle::createObsidianTearHangParticle);
      this.register(ParticleTypes.FALLING_OBSIDIAN_TEAR, DripParticle::createObsidianTearFallParticle);
      this.register(ParticleTypes.LANDING_OBSIDIAN_TEAR, DripParticle::createObsidianTearLandParticle);
      this.register(ParticleTypes.REVERSE_PORTAL, ReversePortalParticle.ReversePortalProvider::new);
      this.register(ParticleTypes.WHITE_ASH, WhiteAshParticle.Provider::new);
      this.register(ParticleTypes.SMALL_FLAME, FlameParticle.SmallFlameProvider::new);
      this.register(ParticleTypes.DRIPPING_DRIPSTONE_WATER, DripParticle::createDripstoneWaterHangParticle);
      this.register(ParticleTypes.FALLING_DRIPSTONE_WATER, DripParticle::createDripstoneWaterFallParticle);
      this.register(ParticleTypes.CHERRY_LEAVES, (p_276702_0_) -> {
         return (p_276703_1_, p_276703_2_, p_276703_3_, p_276703_5_, p_276703_7_, p_276703_9_, p_276703_11_, p_276703_13_) -> {
            return new CherryParticle(p_276703_2_, p_276703_3_, p_276703_5_, p_276703_7_, p_276702_0_);
         };
      });
      this.register(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, DripParticle::createDripstoneLavaHangParticle);
      this.register(ParticleTypes.FALLING_DRIPSTONE_LAVA, DripParticle::createDripstoneLavaFallParticle);
      this.register(ParticleTypes.VIBRATION, VibrationSignalParticle.Provider::new);
      this.register(ParticleTypes.GLOW_SQUID_INK, SquidInkParticle.GlowInkProvider::new);
      this.register(ParticleTypes.GLOW, GlowParticle.GlowSquidProvider::new);
      this.register(ParticleTypes.WAX_ON, GlowParticle.WaxOnProvider::new);
      this.register(ParticleTypes.WAX_OFF, GlowParticle.WaxOffProvider::new);
      this.register(ParticleTypes.ELECTRIC_SPARK, GlowParticle.ElectricSparkProvider::new);
      this.register(ParticleTypes.SCRAPE, GlowParticle.ScrapeProvider::new);
      this.register(ParticleTypes.SHRIEK, ShriekParticle.Provider::new);
      this.register(ParticleTypes.EGG_CRACK, SuspendedTownParticle.EggCrackProvider::new);
   }

   private <T extends ParticleOptions> void register(ParticleType<T> pParticleType, ParticleProvider<T> pParticleFactory) {
      this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getKey(pParticleType), pParticleFactory);
   }

   private <T extends ParticleOptions> void register(ParticleType<T> pParticleType, ParticleProvider.Sprite<T> pSprite) {
      this.register(pParticleType, (p_271560_1_) -> {
         return (p_271561_2_, p_271561_3_, p_271561_4_, p_271561_6_, p_271561_8_, p_271561_10_, p_271561_12_, p_271561_14_) -> {
            TextureSheetParticle texturesheetparticle = pSprite.createParticle(p_271561_2_, p_271561_3_, p_271561_4_, p_271561_6_, p_271561_8_, p_271561_10_, p_271561_12_, p_271561_14_);
            if (texturesheetparticle != null) {
               texturesheetparticle.pickSprite(p_271560_1_);
            }

            return texturesheetparticle;
         };
      });
   }

   private <T extends ParticleOptions> void register(ParticleType<T> pParticleType, ParticleEngine.SpriteParticleRegistration<T> pParticleMetaFactory) {
      ParticleEngine.MutableSpriteSet particleengine$mutablespriteset = new ParticleEngine.MutableSpriteSet();
      this.spriteSets.put(BuiltInRegistries.PARTICLE_TYPE.getKey(pParticleType), particleengine$mutablespriteset);
      this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getKey(pParticleType), pParticleMetaFactory.create(particleengine$mutablespriteset));
   }

   public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier pStage, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
      CompletableFuture<List<ParticleEngine$1ParticleDefinition>> completablefuture = CompletableFuture.supplyAsync(() -> {
         return PARTICLE_LISTER.listMatchingResources(pResourceManager);
      }, pBackgroundExecutor).thenCompose((p_244721_2_) -> {
         List<CompletableFuture<ParticleEngine$1ParticleDefinition>> list = new ArrayList<>(p_244721_2_.size());
         p_244721_2_.forEach((p_244716_3_, p_244716_4_) -> {
            ResourceLocation resourcelocation = PARTICLE_LISTER.fileToId(p_244716_3_);
            list.add(CompletableFuture.supplyAsync(() -> {
               return new ParticleEngine$1ParticleDefinition(resourcelocation, this.loadParticleDescription(resourcelocation, p_244716_4_));
            }, pBackgroundExecutor));
         });
         return Util.sequence(list);
      });
      CompletableFuture<SpriteLoader.Preparations> completablefuture1 = SpriteLoader.create(this.textureAtlas).loadAndStitch(pResourceManager, PARTICLES_ATLAS_INFO, 0, pBackgroundExecutor).thenCompose(SpriteLoader.Preparations::waitForUpload);
      return CompletableFuture.allOf(completablefuture1, completablefuture).thenCompose(pStage::wait).thenAcceptAsync((p_244715_4_) -> {
         this.clearParticles();
         pReloadProfiler.startTick();
         pReloadProfiler.push("upload");
         SpriteLoader.Preparations spriteloader$preparations = completablefuture1.join();
         this.textureAtlas.upload(spriteloader$preparations);
         pReloadProfiler.popPush("bindSpriteSets");
         Set<ResourceLocation> set = new HashSet<>();
         TextureAtlasSprite textureatlassprite = spriteloader$preparations.missing();
         completablefuture.join().forEach((p_244719_4_) -> {
            Optional<List<ResourceLocation>> optional = p_244719_4_.sprites();
            if (!optional.isEmpty()) {
               List<TextureAtlasSprite> list = new ArrayList<>();

               for(ResourceLocation resourcelocation : optional.get()) {
                  TextureAtlasSprite textureatlassprite1 = spriteloader$preparations.regions().get(resourcelocation);
                  if (textureatlassprite1 == null) {
                     set.add(resourcelocation);
                     list.add(textureatlassprite);
                  } else {
                     list.add(textureatlassprite1);
                  }
               }

               if (list.isEmpty()) {
                  list.add(textureatlassprite);
               }

               this.spriteSets.get(p_244719_4_.id()).rebind(list);
            }

         });
         if (!set.isEmpty()) {
            LOGGER.warn("Missing particle sprites: {}", set.stream().sorted().map(ResourceLocation::toString).collect(Collectors.joining(",")));
         }

         pReloadProfiler.pop();
         pReloadProfiler.endTick();
      }, pGameExecutor);
   }

   public void close() {
      this.textureAtlas.clearTextureData();
   }

   private Optional<List<ResourceLocation>> loadParticleDescription(ResourceLocation pRegistryName, Resource pResource) {
      if (!this.spriteSets.containsKey(pRegistryName)) {
         LOGGER.debug("Redundant texture list for particle: {}", (Object)pRegistryName);
         return Optional.empty();
      } else {
         try (Reader reader = pResource.openAsReader()) {
            ParticleDescription particledescription = ParticleDescription.fromJson(GsonHelper.parse(reader));
            return Optional.of(particledescription.getTextures());
         } catch (IOException ioexception1) {
            throw new IllegalStateException("Failed to load description for particle " + pRegistryName, ioexception1);
         }
      }
   }

   public void createTrackingEmitter(Entity pEntity, ParticleOptions pParticleData) {
      this.trackingEmitters.add(new TrackingEmitter(this.level, pEntity, pParticleData));
   }

   public void createTrackingEmitter(Entity pEntity, ParticleOptions pData, int pLifetime) {
      this.trackingEmitters.add(new TrackingEmitter(this.level, pEntity, pData, pLifetime));
   }

   @Nullable
   public Particle createParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      Particle particle = this.makeParticle(pParticleData, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
      if (particle != null) {
         this.add(particle);
         return particle;
      } else {
         return null;
      }
   }

   @Nullable
   private <T extends ParticleOptions> Particle makeParticle(T pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      ParticleProvider<T> particleprovider = (ParticleProvider<T>) this.providers.get(BuiltInRegistries.PARTICLE_TYPE.getKey(pParticleData.getType()));
      return particleprovider == null ? null : particleprovider.createParticle(pParticleData, this.level, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
   }

   public void add(Particle pEffect) {
      if (pEffect != null) {
         if (!(pEffect instanceof FireworkParticles.SparkParticle) || Config.isFireworkParticles()) {
            Optional<ParticleGroup> optional = pEffect.getParticleGroup();
            if (optional.isPresent()) {
               if (this.hasSpaceInParticleLimit(optional.get())) {
                  this.particlesToAdd.add(pEffect);
                  this.updateCount(optional.get(), 1);
               }
            } else {
               this.particlesToAdd.add(pEffect);
            }

         }
      }
   }

   public void tick() {
      this.particles.forEach((p_287801_1_, p_287801_2_) -> {
         this.level.getProfiler().push(p_287801_1_.toString());
         this.tickParticleList(p_287801_2_);
         this.level.getProfiler().pop();
      });
      if (!this.trackingEmitters.isEmpty()) {
         List<TrackingEmitter> list = Lists.newArrayList();

         for(TrackingEmitter trackingemitter : this.trackingEmitters) {
            trackingemitter.tick();
            if (!trackingemitter.isAlive()) {
               list.add(trackingemitter);
            }
         }

         this.trackingEmitters.removeAll(list);
      }

      Particle particle;
      if (!this.particlesToAdd.isEmpty()) {
         while((particle = this.particlesToAdd.poll()) != null) {
            Queue<Particle> queue = this.particles.computeIfAbsent(particle.getRenderType(), (renderTypeIn) -> {
               return EvictingQueue.create(16384);
            });
            queue.add(particle);
         }
      }

   }

   private void tickParticleList(Collection<Particle> pParticles) {
      if (!pParticles.isEmpty()) {
         long i = System.currentTimeMillis();
         int j = pParticles.size();
         Iterator<Particle> iterator = pParticles.iterator();

         while(iterator.hasNext()) {
            Particle particle = iterator.next();
            this.tickParticle(particle);
            if (!particle.isAlive()) {
               particle.getParticleGroup().ifPresent((groupIn) -> {
                  this.updateCount(groupIn, -1);
               });
               iterator.remove();
            }

            --j;
            if (System.currentTimeMillis() > i + 20L) {
               break;
            }
         }

         if (j > 0) {
            int k = j;

            for(Iterator iterator1 = pParticles.iterator(); iterator1.hasNext() && k > 0; --k) {
               Particle particle1 = (Particle)iterator1.next();
               particle1.remove();
               iterator1.remove();
            }
         }
      }

   }

   private void updateCount(ParticleGroup pGroup, int pCount) {
      this.trackedParticleCounts.addTo(pGroup, pCount);
   }

   private void tickParticle(Particle pParticle) {
      try {
         pParticle.tick();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking Particle");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being ticked");
         crashreportcategory.setDetail("Particle", pParticle::toString);
         crashreportcategory.setDetail("Particle Type", pParticle.getRenderType()::toString);
         throw new ReportedException(crashreport);
      }
   }

   public void render(PoseStack pPoseStack, MultiBufferSource.BufferSource pBuffer, LightTexture pLightTexture, Camera pActiveRenderInfo, float pPartialTicks) {
      this.render(pPoseStack, pBuffer, pLightTexture, pActiveRenderInfo, pPartialTicks, (Frustum)null);
   }

   public void render(PoseStack matrixStackIn, MultiBufferSource.BufferSource bufferIn, LightTexture lightTextureIn, Camera activeRenderInfoIn, float partialTicks, Frustum clippingHelper) {
      lightTextureIn.turnOnLightLayer();
      RenderSystem.enableDepthTest();
      RenderSystem.activeTexture(33986);
      RenderSystem.activeTexture(33984);
      FogType fogtype = activeRenderInfoIn.getFluidInCamera();
      boolean flag = fogtype == FogType.WATER;
      PoseStack posestack = RenderSystem.getModelViewStack();
      posestack.pushPose();
      posestack.mulPoseMatrix(matrixStackIn.last().pose());
      RenderSystem.applyModelViewMatrix();
      Collection<ParticleRenderType> collection = RENDER_ORDER;
      if (Reflector.ForgeHooksClient.exists()) {
         collection = this.particles.keySet();
      }

      for(ParticleRenderType particlerendertype : collection) {
         if (particlerendertype != ParticleRenderType.NO_RENDER) {
            Iterable<Particle> iterable = this.particles.get(particlerendertype);
            if (iterable != null) {
               RenderSystem.setShader(GameRenderer::getParticleShader);
               Tesselator tesselator = Tesselator.getInstance();
               BufferBuilder bufferbuilder = tesselator.getBuilder();
               particlerendertype.begin(bufferbuilder, this.textureManager);

               for(Particle particle : iterable) {
                  if (clippingHelper == null || !particle.shouldCull() || clippingHelper.isVisible(particle.getBoundingBox())) {
                     try {
                        if (flag || !(particle instanceof SuspendedParticle) || particle.xd != 0.0D || particle.yd != 0.0D || particle.zd != 0.0D) {
                           particle.render(bufferbuilder, activeRenderInfoIn, partialTicks);
                        }
                     } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                        crashreportcategory.setDetail("Particle", particle::toString);
                        crashreportcategory.setDetail("Particle Type", particlerendertype::toString);
                        throw new ReportedException(crashreport);
                     }
                  }
               }

               particlerendertype.end(tesselator);
            }
         }
      }

      posestack.popPose();
      RenderSystem.applyModelViewMatrix();
      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
      lightTextureIn.turnOffLightLayer();
      RenderSystem.enableDepthTest();
      GlStateManager._glUseProgram(0);
   }

   public void setLevel(@Nullable ClientLevel pLevel) {
      this.level = pLevel;
      this.clearParticles();
      this.trackingEmitters.clear();
   }

   public void destroy(BlockPos pPos, BlockState pState) {
      boolean flag = false;
      IClientBlockExtensions iclientblockextensions = IClientBlockExtensions.of(pState);
      if (iclientblockextensions != null) {
         flag = iclientblockextensions.addDestroyEffects(pState, this.level, pPos, this);
      }

      if (!pState.isAir() && pState.shouldSpawnParticlesOnBreak() && !flag) {
         VoxelShape voxelshape = pState.getShape(this.level, pPos);
         double d0 = 0.25D;
         voxelshape.forAllBoxes((p_172270_3_, p_172270_5_, p_172270_7_, p_172270_9_, p_172270_11_, p_172270_13_) -> {
            double d1 = Math.min(1.0D, p_172270_9_ - p_172270_3_);
            double d2 = Math.min(1.0D, p_172270_11_ - p_172270_5_);
            double d3 = Math.min(1.0D, p_172270_13_ - p_172270_7_);
            int i = Math.max(2, Mth.ceil(d1 / 0.25D));
            int j = Math.max(2, Mth.ceil(d2 / 0.25D));
            int k = Math.max(2, Mth.ceil(d3 / 0.25D));

            for(int l = 0; l < i; ++l) {
               for(int i1 = 0; i1 < j; ++i1) {
                  for(int j1 = 0; j1 < k; ++j1) {
                     double d4 = ((double)l + 0.5D) / (double)i;
                     double d5 = ((double)i1 + 0.5D) / (double)j;
                     double d6 = ((double)j1 + 0.5D) / (double)k;
                     double d7 = d4 * d1 + p_172270_3_;
                     double d8 = d5 * d2 + p_172270_5_;
                     double d9 = d6 * d3 + p_172270_7_;
                     Particle particle = new TerrainParticle(this.level, (double)pPos.getX() + d7, (double)pPos.getY() + d8, (double)pPos.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, pState, pPos);
                     if (Reflector.TerrainParticle_updateSprite.exists()) {
                        Reflector.call(particle, Reflector.TerrainParticle_updateSprite, pState, pPos);
                     }

                     if (Config.isCustomColors()) {
                        updateTerrainParticleColor(particle, pState, this.level, pPos, this.renderEnv);
                     }

                     this.add(particle);
                  }
               }
            }

         });
      }

   }

   public void crack(BlockPos pPos, Direction pSide) {
      BlockState blockstate = this.level.getBlockState(pPos);
      if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
         int i = pPos.getX();
         int j = pPos.getY();
         int k = pPos.getZ();
         float f = 0.1F;
         AABB aabb = blockstate.getShape(this.level, pPos).bounds();
         double d0 = (double)i + this.random.nextDouble() * (aabb.maxX - aabb.minX - (double)0.2F) + (double)0.1F + aabb.minX;
         double d1 = (double)j + this.random.nextDouble() * (aabb.maxY - aabb.minY - (double)0.2F) + (double)0.1F + aabb.minY;
         double d2 = (double)k + this.random.nextDouble() * (aabb.maxZ - aabb.minZ - (double)0.2F) + (double)0.1F + aabb.minZ;
         if (pSide == Direction.DOWN) {
            d1 = (double)j + aabb.minY - (double)0.1F;
         }

         if (pSide == Direction.UP) {
            d1 = (double)j + aabb.maxY + (double)0.1F;
         }

         if (pSide == Direction.NORTH) {
            d2 = (double)k + aabb.minZ - (double)0.1F;
         }

         if (pSide == Direction.SOUTH) {
            d2 = (double)k + aabb.maxZ + (double)0.1F;
         }

         if (pSide == Direction.WEST) {
            d0 = (double)i + aabb.minX - (double)0.1F;
         }

         if (pSide == Direction.EAST) {
            d0 = (double)i + aabb.maxX + (double)0.1F;
         }

         Particle particle = (new TerrainParticle(this.level, d0, d1, d2, 0.0D, 0.0D, 0.0D, blockstate, pPos)).setPower(0.2F).scale(0.6F);
         if (Reflector.TerrainParticle_updateSprite.exists()) {
            Reflector.call(particle, Reflector.TerrainParticle_updateSprite, blockstate, pPos);
         }

         if (Config.isCustomColors()) {
            updateTerrainParticleColor(particle, blockstate, this.level, pPos, this.renderEnv);
         }

         this.add(particle);
      }

   }

   public String countParticles() {
      return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
   }

   private boolean hasSpaceInParticleLimit(ParticleGroup pGroup) {
      return this.trackedParticleCounts.getInt(pGroup) < pGroup.getLimit();
   }

   private void clearParticles() {
      this.particles.clear();
      this.particlesToAdd.clear();
      this.trackingEmitters.clear();
      this.trackedParticleCounts.clear();
   }

   private boolean reuseBarrierParticle(Particle entityfx, Queue<Particle> deque) {
      for (Particle particle : deque) {
      }
      return false;
   }


   public static void updateTerrainParticleColor(Particle particle, BlockState state, BlockAndTintGetter world, BlockPos pos, RenderEnv renderEnv) {
      renderEnv.reset(state, pos);
      int i = CustomColors.getColorMultiplier(true, state, world, pos, renderEnv);
      if (i != -1) {
         particle.rCol = 0.6F * (float)(i >> 16 & 255) / 255.0F;
         particle.gCol = 0.6F * (float)(i >> 8 & 255) / 255.0F;
         particle.bCol = 0.6F * (float)(i & 255) / 255.0F;
      }

   }

   public void addBlockHitEffects(BlockPos pos, BlockHitResult target) {
      BlockState blockstate = this.level.getBlockState(pos);
      if (!IClientBlockExtensions.of(blockstate).addHitEffects(blockstate, this.level, target, this)) {
         this.crack(pos, target.getDirection());
      }

   }

   static class MutableSpriteSet implements SpriteSet {
      private List<TextureAtlasSprite> sprites;

      public TextureAtlasSprite get(int pParticleAge, int pParticleMaxAge) {
         return this.sprites.get(pParticleAge * (this.sprites.size() - 1) / pParticleMaxAge);
      }

      public TextureAtlasSprite get(RandomSource pRandom) {
         return this.sprites.get(pRandom.nextInt(this.sprites.size()));
      }

      public void rebind(List<TextureAtlasSprite> pSprites) {
         this.sprites = ImmutableList.copyOf(pSprites);
      }
   }

   @FunctionalInterface
   interface SpriteParticleRegistration<T extends ParticleOptions> {
      ParticleProvider<T> create(SpriteSet pSprites);
   }
}
