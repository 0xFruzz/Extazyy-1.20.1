package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.datafixers.DataFixUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.optifine.Config;
import net.optifine.SmartAnimations;
import net.optifine.TextureAnimations;
import net.optifine.reflect.Reflector;
import net.optifine.render.RenderCache;
import net.optifine.util.GpuMemory;
import net.optifine.util.MemoryMonitor;
import net.optifine.util.NativeMemory;
import ru.fruzz.extazyy.Extazyy;

public class DebugScreenOverlay {
   private static final int COLOR_GREY = 14737632;
   private static final int MARGIN_RIGHT = 2;
   private static final int MARGIN_LEFT = 2;
   private static final int MARGIN_TOP = 2;
   private static final Map<Heightmap.Types, String> HEIGHTMAP_NAMES = Util.make(new EnumMap<>(Heightmap.Types.class), (p_94069_0_) -> {
      p_94069_0_.put(Heightmap.Types.WORLD_SURFACE_WG, "SW");
      p_94069_0_.put(Heightmap.Types.WORLD_SURFACE, "S");
      p_94069_0_.put(Heightmap.Types.OCEAN_FLOOR_WG, "OW");
      p_94069_0_.put(Heightmap.Types.OCEAN_FLOOR, "O");
      p_94069_0_.put(Heightmap.Types.MOTION_BLOCKING, "M");
      p_94069_0_.put(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, "ML");
   });
   private final Minecraft minecraft;
   private final DebugScreenOverlay.AllocationRateCalculator allocationRateCalculator;
   private final Font font;
   private HitResult block;
   private HitResult liquid;
   @Nullable
   private ChunkPos lastPos;
   @Nullable
   private LevelChunk clientChunk;
   @Nullable
   private CompletableFuture<LevelChunk> serverChunk;
   private static final int RED = -65536;
   private static final int YELLOW = -256;
   private static final int GREEN = -16711936;
   private String debugOF = null;
   private static final Pattern PATTERN_DEBUG_SPACING = Pattern.compile("(\\d|f|c)(fa)");
   private RenderCache renderCache = new RenderCache(100L);

   public DebugScreenOverlay(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
      this.allocationRateCalculator = new DebugScreenOverlay.AllocationRateCalculator();
      this.font = pMinecraft.font;
   }

   public void clearChunkCache() {
      this.serverChunk = null;
      this.clientChunk = null;
   }

   public void render(GuiGraphics pGuiGraphics) {
      this.minecraft.getProfiler().push("debug");
      Entity entity = this.minecraft.getCameraEntity();
      this.block = entity.pick(20.0D, 0.0F, false);
      this.liquid = entity.pick(20.0D, 0.0F, true);
      pGuiGraphics.drawManaged(() -> {
         if (!this.renderCache.drawCached(pGuiGraphics)) {
            this.renderCache.startRender(pGuiGraphics);
            this.drawGameInformation(pGuiGraphics);
            this.drawSystemInformation(pGuiGraphics);
            this.renderCache.stopRender(pGuiGraphics);
         }

         if (this.minecraft.options.renderFpsChart) {
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0.0F, 0.0F, 400.0F);
            int i = pGuiGraphics.guiWidth();
            this.drawChart(pGuiGraphics, this.minecraft.getFrameTimer(), 0, i / 2, true);
            IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
            if (integratedserver != null) {
               this.drawChart(pGuiGraphics, integratedserver.getFrameTimer(), i - Math.min(i / 2, 240), i / 2, false);
            }

            pGuiGraphics.pose().popPose();
         }

      });
      this.minecraft.getProfiler().pop();
   }

   protected void drawGameInformation(GuiGraphics pGuiGraphics) {
      List<String> list = this.getGameInformation();
      list.add("");
      boolean flag = this.minecraft.getSingleplayerServer() != null;
      list.add("Debug: Pie [shift]: " + (this.minecraft.options.renderDebugCharts ? "visible" : "hidden") + (flag ? " FPS + TPS" : " FPS") + " [alt]: " + (this.minecraft.options.renderFpsChart ? "visible" : "hidden"));
      list.add("For help: press F3 + Q");
      this.renderLines(pGuiGraphics, list, true);
   }

   protected void drawSystemInformation(GuiGraphics pGuiGraphics) {
      List<String> list = this.getSystemInformation();
      this.renderLines(pGuiGraphics, list, false);
   }

   private void renderLines(GuiGraphics pGuiGraphics, List<String> pLines, boolean pLeftSide) {
      int i = 9;

      for(int j = 0; j < pLines.size(); ++j) {
         String s = pLines.get(j);
         if (!Strings.isNullOrEmpty(s)) {
            int k = this.font.width(s);
            int l = pLeftSide ? 2 : pGuiGraphics.guiWidth() - 2 - k;
            int i1 = 2 + i * j;
            pGuiGraphics.fill(l - 1, i1 - 1, l + k + 1, i1 + i - 1, -1873784752);
         }
      }

      for(int j1 = 0; j1 < pLines.size(); ++j1) {
         String s1 = pLines.get(j1);
         if (!Strings.isNullOrEmpty(s1)) {
            int k1 = this.font.width(s1);
            int l1 = pLeftSide ? 2 : pGuiGraphics.guiWidth() - 2 - k1;
            int i2 = 2 + i * j1;
            pGuiGraphics.drawString(this.font, s1, l1, i2, 14737632, false);
         }
      }

   }

   protected List<String> getGameInformation() {
      if (this.minecraft.fpsString != this.debugOF) {
         StringBuffer stringbuffer = new StringBuffer(this.minecraft.fpsString);
         Matcher matcher = PATTERN_DEBUG_SPACING.matcher(this.minecraft.fpsString);
         if (matcher.find()) {
            stringbuffer.insert(matcher.start(2), ' ');
         }

         int i = Config.getChunkUpdates();
         int j = this.minecraft.fpsString.indexOf("T: ");
         if (j >= 0) {
            stringbuffer.insert(j, "(" + i + " updates) ");
         }

         int k = Config.getFpsMin();
         int l = this.minecraft.fpsString.indexOf(" fps ");
         if (l >= 0) {
            stringbuffer.replace(0, l + 4, Config.getFpsString());
         }

         stringbuffer.append("\u00a7r");
         if (Config.isSmoothFps()) {
            stringbuffer.append(" sf");
         }

         if (Config.isFastRender()) {
            stringbuffer.append(" fr");
         }

         if (Config.isAnisotropicFiltering()) {
            stringbuffer.append(" af");
         }

         if (Config.isAntialiasing()) {
            stringbuffer.append(" aa");
         }

         if (Config.isRenderRegions()) {
            stringbuffer.append(" rr");
         }

         if (Config.isShaders()) {
            stringbuffer.append(" sh");
         }

         this.minecraft.fpsString = stringbuffer.toString();
         this.debugOF = this.minecraft.fpsString;
      }

      List<String> list = this.getInfoLeft();
      StringBuilder stringbuilder = new StringBuilder();
      TextureAtlas textureatlas = Config.getTextureMap();
      stringbuilder.append(", A: ");
      if (SmartAnimations.isActive()) {
         stringbuilder.append(textureatlas.getCountAnimationsActive() + TextureAnimations.getCountAnimationsActive());
         stringbuilder.append("/");
      }

      stringbuilder.append(textureatlas.getCountAnimations() + TextureAnimations.getCountAnimations());
      String s = stringbuilder.toString();
      String s1 = null;
      if (Config.isShadersShadows()) {
         int k1 = this.minecraft.levelRenderer.getRenderedChunksShadow();
         int i1 = this.minecraft.levelRenderer.getCountEntitiesRenderedShadow();
         int j1 = this.minecraft.levelRenderer.getCountTileEntitiesRenderedShadow();
         s1 = "Shadow C: " + k1 + ", E: " + i1 + "+" + j1;
      }

      for(int l1 = 0; l1 < list.size(); ++l1) {
         String s2 = list.get(l1);
         if (s2 != null && s2.startsWith("P: ")) {
            s2 = s2 + s;
            list.set(l1, s2);
            if (s1 != null) {
               list.add(l1 + 1, s1);
            }
            break;
         }
      }

      return list;
   }

   protected List<String> getInfoLeft() {
      IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
      Connection connection = this.minecraft.getConnection().getConnection();
      float f = connection.getAverageSentPackets();
      float f1 = connection.getAverageReceivedPackets();
      String s;
      if (integratedserver != null) {
         s = String.format(Locale.ROOT, "Integrated server @ %.0f ms ticks, %.0f tx, %.0f rx", integratedserver.getAverageTickTime(), f, f1);
      } else {
         s = String.format(Locale.ROOT, "\"%s\" server, %.0f tx, %.0f rx", this.minecraft.player.getServerBrand(), f, f1);
      }

      BlockPos blockpos = this.minecraft.getCameraEntity().blockPosition();
      if (this.minecraft.showOnlyReducedInfo()) {
         return Lists.newArrayList("Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.minecraft.fpsString, s, this.minecraft.levelRenderer.getChunkStatistics(), this.minecraft.levelRenderer.getEntityStatistics(), "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(), this.minecraft.level.gatherChunkSourceStats(), "", String.format(Locale.ROOT, "Chunk-relative: %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15));
      } else {
         Entity entity = this.minecraft.getCameraEntity();
         Direction direction = entity.getDirection();
         String s1;
         switch (direction) {
            case NORTH:
               s1 = "Towards negative Z";
               break;
            case SOUTH:
               s1 = "Towards positive Z";
               break;
            case WEST:
               s1 = "Towards negative X";
               break;
            case EAST:
               s1 = "Towards positive X";
               break;
            default:
               s1 = "Invalid";
         }

         ChunkPos chunkpos = new ChunkPos(blockpos);
         if (!Objects.equals(this.lastPos, chunkpos)) {
            this.lastPos = chunkpos;
            this.clearChunkCache();
         }

         Level level = this.getLevel();
         LongSet longset = (LongSet)(level instanceof ServerLevel ? ((ServerLevel)level).getForcedChunks() : LongSets.EMPTY_SET);
         List<String> list = Lists.newArrayList("Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType()) + ")", this.minecraft.fpsString, s, this.minecraft.levelRenderer.getChunkStatistics(), this.minecraft.levelRenderer.getEntityStatistics(), "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(), this.minecraft.level.gatherChunkSourceStats());
         String s2 = this.getServerChunkStats();
         if (s2 != null) {
            list.add(s2);
         }

         list.add(this.minecraft.level.dimension().location() + " FC: " + longset.size());
         list.add("");
         if(!Extazyy.getModuleManager().get("StreamerMode").state) {
            list.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", this.minecraft.getCameraEntity().getX(), this.minecraft.getCameraEntity().getY(), this.minecraft.getCameraEntity().getZ()));
            list.add(String.format(Locale.ROOT, "Block: %d %d %d [%d %d %d]", blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15));
            list.add(String.format(Locale.ROOT, "Chunk: %d %d %d [%d %d in r.%d.%d.mca]", chunkpos.x, SectionPos.blockToSectionCoord(blockpos.getY()), chunkpos.z, chunkpos.getRegionLocalX(), chunkpos.getRegionLocalZ(), chunkpos.getRegionX(), chunkpos.getRegionZ()));
            list.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", direction, s1, Mth.wrapDegrees(entity.getYRot()), Mth.wrapDegrees(entity.getXRot())));
         }
         LevelChunk levelchunk = this.getClientChunk();
         if (levelchunk.isEmpty()) {
            list.add("Waiting for chunk...");
         } else {
            int i = this.minecraft.level.getChunkSource().getLightEngine().getRawBrightness(blockpos, 0);
            int j = this.minecraft.level.getBrightness(LightLayer.SKY, blockpos);
            int k = this.minecraft.level.getBrightness(LightLayer.BLOCK, blockpos);
            list.add("Client Light: " + i + " (" + j + " sky, " + k + " block)");
            LevelChunk levelchunk1 = this.getServerChunk();
            StringBuilder stringbuilder = new StringBuilder("CH");

            for(Heightmap.Types heightmap$types : Heightmap.Types.values()) {
               if (heightmap$types.sendToClient()) {
                  //stringbuilder.append(" ").append(HEIGHTMAP_NAMES.get(heightmap$types)).append(": ").append(levelchunk.getHeight(heightmap$types, blockpos.getX(), blockpos.getZ()));
               }
            }

            list.add(stringbuilder.toString());
            stringbuilder.setLength(0);
            stringbuilder.append("SH");

            for(Heightmap.Types heightmap$types1 : Heightmap.Types.values()) {
               if (heightmap$types1.keepAfterWorldgen()) {
                  stringbuilder.append(" ").append(HEIGHTMAP_NAMES.get(heightmap$types1)).append(": ");
                  if (levelchunk1 != null) {
                    // stringbuilder.append(levelchunk1.getHeight(heightmap$types1, blockpos.getX(), blockpos.getZ()));
                  } else {
                     stringbuilder.append("??");
                  }
               }
            }

            list.add(stringbuilder.toString());
            if (blockpos.getY() >= this.minecraft.level.getMinBuildHeight() && blockpos.getY() < this.minecraft.level.getMaxBuildHeight()) {
               list.add("Biome: " + printBiome(this.minecraft.level.getBiome(blockpos)));
               long l = 0L;
               float f2 = 0.0F;
               if (levelchunk1 != null) {
                  f2 = level.getMoonBrightness();
                  l = levelchunk1.getInhabitedTime();
               }

               DifficultyInstance difficultyinstance = new DifficultyInstance(level.getDifficulty(), level.getDayTime(), l, f2);
               list.add(String.format(Locale.ROOT, "Local Difficulty: %.2f // %.2f (Day %d)", difficultyinstance.getEffectiveDifficulty(), difficultyinstance.getSpecialMultiplier(), this.minecraft.level.getDayTime() / 24000L));
            }

            if (levelchunk1 != null && levelchunk1.isOldNoiseGeneration()) {
               list.add("Blending: Old");
            }
         }

         ServerLevel serverlevel = this.getServerLevel();
         if (serverlevel != null) {
            ServerChunkCache serverchunkcache = serverlevel.getChunkSource();
            ChunkGenerator chunkgenerator = serverchunkcache.getGenerator();
            RandomState randomstate = serverchunkcache.randomState();
            chunkgenerator.addDebugScreenInfo(list, randomstate, blockpos);
            Climate.Sampler climate$sampler = randomstate.sampler();
            BiomeSource biomesource = chunkgenerator.getBiomeSource();
            biomesource.addDebugInfo(list, blockpos, climate$sampler);
            NaturalSpawner.SpawnState naturalspawner$spawnstate = serverchunkcache.getLastSpawnState();
            if (naturalspawner$spawnstate != null) {
               Object2IntMap<MobCategory> object2intmap = naturalspawner$spawnstate.getMobCategoryCounts();
               int i1 = naturalspawner$spawnstate.getSpawnableChunkCount();
               list.add("SC: " + i1 + ", " + (String)Stream.of(MobCategory.values()).map((p_94066_1_) -> {
                  return Character.toUpperCase(p_94066_1_.getName().charAt(0)) + ": " + object2intmap.getInt(p_94066_1_);
               }).collect(Collectors.joining(", ")));
            } else {
               list.add("SC: N/A");
            }
         }

         PostChain postchain = this.minecraft.gameRenderer.currentEffect();
         if (postchain != null) {
            list.add("Shader: " + postchain.getName());
         }

         list.add(this.minecraft.getSoundManager().getDebugString() + String.format(Locale.ROOT, " (Mood %d%%)", Math.round(this.minecraft.player.getCurrentMood() * 100.0F)));
         return list;
      }
   }

   private static String printBiome(Holder<Biome> pBiomeHolder) {
      return pBiomeHolder.unwrap().map((p_205376_0_) -> {
         return p_205376_0_.location().toString();
      }, (p_205366_0_) -> {
         return "[unregistered " + p_205366_0_ + "]";
      });
   }

   @Nullable
   private ServerLevel getServerLevel() {
      IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
      return integratedserver != null ? integratedserver.getLevel(this.minecraft.level.dimension()) : null;
   }

   @Nullable
   private String getServerChunkStats() {
      ServerLevel serverlevel = this.getServerLevel();
      return serverlevel != null ? serverlevel.gatherChunkSourceStats() : null;
   }

   private Level getLevel() {
      return DataFixUtils.orElse(Optional.ofNullable(this.minecraft.getSingleplayerServer()).flatMap((p_287797_1_) -> {
         return Optional.ofNullable(p_287797_1_.getLevel(this.minecraft.level.dimension()));
      }), this.minecraft.level);
   }

   @Nullable
   private LevelChunk getServerChunk() {
      if (this.serverChunk == null) {
         ServerLevel serverlevel = this.getServerLevel();
         if (serverlevel != null) {
            this.serverChunk = serverlevel.getChunkSource().getChunkFuture(this.lastPos.x, this.lastPos.z, ChunkStatus.FULL, false).thenApply((p_205368_0_) -> {
               return p_205368_0_.map((p_205370_0_) -> {
                  return (LevelChunk)p_205370_0_;
               }, (p_205362_0_) -> {
                  return null;
               });
            });
         }

         if (this.serverChunk == null) {
            this.serverChunk = CompletableFuture.completedFuture(this.getClientChunk());
         }
      }

      return this.serverChunk.getNow((LevelChunk)null);
   }

   private LevelChunk getClientChunk() {
      if (this.clientChunk == null) {
         this.clientChunk = this.minecraft.level.getChunk(this.lastPos.x, this.lastPos.z);
      }

      return this.clientChunk;
   }

   protected List<String> getSystemInformation() {
      long i = Runtime.getRuntime().maxMemory();
      long j = Runtime.getRuntime().totalMemory();
      long k = Runtime.getRuntime().freeMemory();
      long l = j - k;
      List<String> list = Lists.newArrayList(String.format(Locale.ROOT, "Java: %s %dbit", System.getProperty("java.version"), this.minecraft.is64Bit() ? 64 : 32), String.format(Locale.ROOT, "Mem: % 2d%% %03d/%03dMB", l * 100L / i, bytesToMegabytes(l), bytesToMegabytes(i)), String.format(Locale.ROOT, "Allocation: %dMB/s", MemoryMonitor.getAllocationRateAvgMb()), String.format(Locale.ROOT, "Allocated: % 2d%% %03dMB", j * 100L / i, bytesToMegabytes(j)), "", String.format(Locale.ROOT, "CPU: %s", GlUtil.getCpuInfo()), "", String.format(Locale.ROOT, "Display: %dx%d (%s)", Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), GlUtil.getVendor()), GlUtil.getRenderer(), GlUtil.getOpenGLVersion());
      long i1 = NativeMemory.getBufferAllocated();
      long j1 = NativeMemory.getBufferMaximum();
      long k1 = NativeMemory.getImageAllocated();
      String s = "Native: " + bytesToMegabytes(i1) + "/" + bytesToMegabytes(j1) + "+" + bytesToMegabytes(k1) + "MB";
      list.add(3, s);
      long l1 = GpuMemory.getBufferAllocated();
      long i2 = GpuMemory.getTextureAllocated();
      list.set(4, "GPU: " + bytesToMegabytes(l1) + "+" + bytesToMegabytes(i2) + "MB");
      if (Reflector.BrandingControl_getBrandings.exists()) {
         list.add("");

         for(String s1 : (Collection<String>)Reflector.call(Reflector.BrandingControl_getBrandings, true, false)) {
            if (!s1.startsWith("Minecraft ")) {
               list.add(s1);
            }
         }
      }

      if (this.minecraft.showOnlyReducedInfo()) {
         return list;
      } else {
         if (this.block.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult)this.block).getBlockPos();
            BlockState blockstate = this.minecraft.level.getBlockState(blockpos);
            list.add("");
            //list.add(ChatFormatting.UNDERLINE + "Targeted Block: " + blockpos.getX() + ", " + blockpos.getY() + ", " + blockpos.getZ());
            //list.add(String.valueOf((Object)BuiltInRegistries.BLOCK.getKey(blockstate.getBlock())));

            for(Map.Entry<Property<?>, Comparable<?>> entry : blockstate.getValues().entrySet()) {
               list.add(this.getPropertyValueString(entry));
            }

            blockstate.getTags().map((p_205364_0_) -> {
               return "#" + p_205364_0_.location();
            }).forEach(list::add);
         }

         if (this.liquid.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos1 = ((BlockHitResult)this.liquid).getBlockPos();
            FluidState fluidstate = this.minecraft.level.getFluidState(blockpos1);
            list.add("");
            //list.add(ChatFormatting.UNDERLINE + "Targeted Fluid: " + blockpos1.getX() + ", " + blockpos1.getY() + ", " + blockpos1.getZ());
            //list.add(String.valueOf((Object)BuiltInRegistries.FLUID.getKey(fluidstate.getType())));

            for(Map.Entry<Property<?>, Comparable<?>> entry1 : fluidstate.getValues().entrySet()) {
               list.add(this.getPropertyValueString(entry1));
            }

            fluidstate.getTags().map((p_205378_0_) -> {
               return "#" + p_205378_0_.location();
            }).forEach(list::add);
         }

         Entity entity = this.minecraft.crosshairPickEntity;
         if (entity != null) {
            list.add("");
            list.add(ChatFormatting.UNDERLINE + "Targeted Entity");
            list.add(String.valueOf((Object)BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())));
            entity.getType().builtInRegistryHolder().tags().forEach((t) -> {
               list.add("#" + t.location());
            });
         }

         return list;
      }
   }

   private String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> pEntry) {
      Property<?> property = pEntry.getKey();
      Comparable<?> comparable = pEntry.getValue();
      String s = Util.getPropertyName(property, comparable);
      if (Boolean.TRUE.equals(comparable)) {
         s = ChatFormatting.GREEN + s;
      } else if (Boolean.FALSE.equals(comparable)) {
         s = ChatFormatting.RED + s;
      }

      return property.getName() + ": " + s;
   }

   private void drawChart(GuiGraphics pGuiGraphics, FrameTimer pDrawTimer, int pX, int p_282726_, boolean pDrawForFps) {
      if (!pDrawForFps) {
         int i = (int)(512.0D / this.minecraft.getWindow().getGuiScale());
         pX = Math.max(pX, i);
         p_282726_ = this.minecraft.getWindow().getGuiScaledWidth() - pX;
         int j = pDrawTimer.getLogStart();
         int k = pDrawTimer.getLogEnd();
         long[] along = pDrawTimer.getLog();
         int l = pX;
         int i1 = Math.max(0, along.length - p_282726_);
         int j1 = along.length - i1;
         int k1 = pDrawTimer.wrapIndex(j + i1);
         long l1 = 0L;
         int i2 = Integer.MAX_VALUE;
         int j2 = Integer.MIN_VALUE;

         for(int k2 = 0; k2 < j1; ++k2) {
            int l2 = (int)(along[pDrawTimer.wrapIndex(k1 + k2)] / 1000000L);
            i2 = Math.min(i2, l2);
            j2 = Math.max(j2, l2);
            l1 += (long)l2;
         }

         int k3 = pGuiGraphics.guiHeight();
         pGuiGraphics.fill(RenderType.guiOverlay(), pX, k3 - 60, pX + j1, k3, -1873784752);

         while(k1 != k) {
            int l3 = pDrawTimer.scaleSampleTo(along[k1], pDrawForFps ? 30 : 60, pDrawForFps ? 60 : 20);
            int i3 = pDrawForFps ? 100 : 60;
            int j3 = this.getSampleColor(Mth.clamp(l3, 0, i3), 0, i3 / 2, i3);
            pGuiGraphics.fill(RenderType.guiOverlay(), l, k3 - l3, l + 1, k3, j3);
            ++l;
            k1 = pDrawTimer.wrapIndex(k1 + 1);
         }

         if (pDrawForFps) {
            pGuiGraphics.fill(RenderType.guiOverlay(), pX + 1, k3 - 30 + 1, pX + 14, k3 - 30 + 10, -1873784752);
            pGuiGraphics.drawString(this.font, "60 FPS", pX + 2, k3 - 30 + 2, 14737632, false);
            pGuiGraphics.hLine(RenderType.guiOverlay(), pX, pX + j1 - 1, k3 - 30, -1);
            pGuiGraphics.fill(RenderType.guiOverlay(), pX + 1, k3 - 60 + 1, pX + 14, k3 - 60 + 10, -1873784752);
            pGuiGraphics.drawString(this.font, "30 FPS", pX + 2, k3 - 60 + 2, 14737632, false);
            pGuiGraphics.hLine(RenderType.guiOverlay(), pX, pX + j1 - 1, k3 - 60, -1);
         } else {
            pGuiGraphics.fill(RenderType.guiOverlay(), pX + 1, k3 - 60 + 1, pX + 14, k3 - 60 + 10, -1873784752);
            pGuiGraphics.drawString(this.font, "20 TPS", pX + 2, k3 - 60 + 2, 14737632, false);
            pGuiGraphics.hLine(RenderType.guiOverlay(), pX, pX + j1 - 1, k3 - 60, -1);
         }

         pGuiGraphics.hLine(RenderType.guiOverlay(), pX, pX + j1 - 1, k3 - 1, -1);
         pGuiGraphics.vLine(RenderType.guiOverlay(), pX, k3 - 60, k3, -1);
         pGuiGraphics.vLine(RenderType.guiOverlay(), pX + j1 - 1, k3 - 60, k3, -1);
         int i4 = this.minecraft.options.framerateLimit().get();
         if (pDrawForFps && i4 > 0 && i4 <= 250) {
            pGuiGraphics.hLine(RenderType.guiOverlay(), pX, pX + j1 - 1, k3 - 1 - (int)(1800.0D / (double)i4), -16711681);
         }

         String s1 = i2 + " ms min";
         String s2 = l1 / (long)j1 + " ms avg";
         String s = j2 + " ms max";
         pGuiGraphics.drawString(this.font, s1, pX + 2, k3 - 60 - 9, 14737632);
         pGuiGraphics.drawCenteredString(this.font, s2, pX + j1 / 2, k3 - 60 - 9, 14737632);
         pGuiGraphics.drawString(this.font, s, pX + j1 - this.font.width(s), k3 - 60 - 9, 14737632);
      }
   }

   private int getSampleColor(int pHeight, int pHeightMin, int pHeightMid, int pHeightMax) {
      return pHeight < pHeightMid ? this.colorLerp(-16711936, -256, (float)pHeight / (float)pHeightMid) : this.colorLerp(-256, -65536, (float)(pHeight - pHeightMid) / (float)(pHeightMax - pHeightMid));
   }

   private int colorLerp(int pCol1, int pCol2, float pFactor) {
      int i = pCol1 >> 24 & 255;
      int j = pCol1 >> 16 & 255;
      int k = pCol1 >> 8 & 255;
      int l = pCol1 & 255;
      int i1 = pCol2 >> 24 & 255;
      int j1 = pCol2 >> 16 & 255;
      int k1 = pCol2 >> 8 & 255;
      int l1 = pCol2 & 255;
      int i2 = Mth.clamp((int)Mth.lerp(pFactor, (float)i, (float)i1), 0, 255);
      int j2 = Mth.clamp((int)Mth.lerp(pFactor, (float)j, (float)j1), 0, 255);
      int k2 = Mth.clamp((int)Mth.lerp(pFactor, (float)k, (float)k1), 0, 255);
      int l2 = Mth.clamp((int)Mth.lerp(pFactor, (float)l, (float)l1), 0, 255);
      return i2 << 24 | j2 << 16 | k2 << 8 | l2;
   }

   private static long bytesToMegabytes(long pBytes) {
      return pBytes / 1024L / 1024L;
   }

   static class AllocationRateCalculator {
      private static final int UPDATE_INTERVAL_MS = 500;
      private static final List<GarbageCollectorMXBean> GC_MBEANS = ManagementFactory.getGarbageCollectorMXBeans();
      private long lastTime = 0L;
      private long lastHeapUsage = -1L;
      private long lastGcCounts = -1L;
      private long lastRate = 0L;

      long bytesAllocatedPerSecond(long pHeapUsage) {
         long i = System.currentTimeMillis();
         if (i - this.lastTime < 500L) {
            return this.lastRate;
         } else {
            long j = gcCounts();
            if (this.lastTime != 0L && j == this.lastGcCounts) {
               double d0 = (double)TimeUnit.SECONDS.toMillis(1L) / (double)(i - this.lastTime);
               long k = pHeapUsage - this.lastHeapUsage;
               this.lastRate = Math.round((double)k * d0);
            }

            this.lastTime = i;
            this.lastHeapUsage = pHeapUsage;
            this.lastGcCounts = j;
            return this.lastRate;
         }
      }

      private static long gcCounts() {
         long i = 0L;

         for(GarbageCollectorMXBean garbagecollectormxbean : GC_MBEANS) {
            i += garbagecollectormxbean.getCollectionCount();
         }

         return i;
      }
   }
}
