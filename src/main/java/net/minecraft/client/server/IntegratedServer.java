package net.minecraft.client.server;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.stats.Stats;
import net.minecraft.util.ModCheck;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.optifine.Config;
import net.optifine.reflect.Reflector;
import org.slf4j.Logger;

public class IntegratedServer extends MinecraftServer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MIN_SIM_DISTANCE = 2;
   private final Minecraft minecraft;
   private boolean paused = true;
   private int publishedPort = -1;
   @Nullable
   private GameType publishedGameType;
   @Nullable
   private LanServerPinger lanPinger;
   @Nullable
   private UUID uuid;
   private int previousSimulationDistance = 0;
   private long ticksSaveLast = 0L;
   public Level difficultyUpdateWorld = null;
   public BlockPos difficultyUpdatePos = null;
   public DifficultyInstance difficultyLast = null;

   public IntegratedServer(Thread pServerThread, Minecraft pMinecraft, LevelStorageSource.LevelStorageAccess pStorageSource, PackRepository pPackRepository, WorldStem pWorldStem, Services pServices, ChunkProgressListenerFactory pProgressListenerFactory) {
      super(pServerThread, pStorageSource, pPackRepository, pWorldStem, pMinecraft.getProxy(), pMinecraft.getFixerUpper(), pServices, pProgressListenerFactory);
      this.setSingleplayerProfile(pMinecraft.getUser().getGameProfile());
      this.setDemo(pMinecraft.isDemo());
      this.setPlayerList(new IntegratedPlayerList(this, this.registries(), this.playerDataStorage));
      this.minecraft = pMinecraft;
   }

   public boolean initServer() {
      LOGGER.info("Starting integrated minecraft server version {}", (Object)SharedConstants.getCurrentVersion().getName());
      this.setUsesAuthentication(true);
      this.setPvpAllowed(true);
      this.setFlightAllowed(true);
      this.initializeKeyPair();
      if (Reflector.ServerLifecycleHooks_handleServerAboutToStart.exists() && !Reflector.callBoolean(Reflector.ServerLifecycleHooks_handleServerAboutToStart, this)) {
         return false;
      } else {
         this.loadLevel();
         GameProfile gameprofile = this.getSingleplayerProfile();
         String s = this.getWorldData().getLevelName();
         this.setMotd(gameprofile != null ? gameprofile.getName() + " - " + s : s);
         return Reflector.ServerLifecycleHooks_handleServerStarting.exists() ? Reflector.callBoolean(Reflector.ServerLifecycleHooks_handleServerStarting, this) : true;
      }
   }

   public void tickServer(BooleanSupplier pHasTimeLeft) {
      this.onTick();
      boolean flag = this.paused;
      this.paused = Minecraft.getInstance().isPaused();
      ProfilerFiller profilerfiller = this.getProfiler();
      if (!flag && this.paused) {
         profilerfiller.push("autoSave");
         LOGGER.info("Saving and pausing game...");
         this.saveEverything(false, false, false);
         profilerfiller.pop();
      }

      boolean flag1 = Minecraft.getInstance().getConnection() != null;
      if (flag1 && this.paused) {
         this.tickPaused();
      } else {
         if (flag && !this.paused) {
            this.forceTimeSynchronization();
         }

         super.tickServer(pHasTimeLeft);
         int i = Math.max(2, this.minecraft.options.renderDistance().get());
         if (i != this.getPlayerList().getViewDistance()) {
            LOGGER.info("Changing view distance to {}, from {}", i, this.getPlayerList().getViewDistance());
            this.getPlayerList().setViewDistance(i);
         }

         int j = Math.max(2, this.minecraft.options.simulationDistance().get());
         if (j != this.previousSimulationDistance) {
            LOGGER.info("Changing simulation distance to {}, from {}", j, this.previousSimulationDistance);
            this.getPlayerList().setSimulationDistance(j);
            this.previousSimulationDistance = j;
         }
      }

   }

   private void tickPaused() {
      for(ServerPlayer serverplayer : this.getPlayerList().getPlayers()) {
         serverplayer.awardStat(Stats.TOTAL_WORLD_TIME);
      }

   }

   public boolean shouldRconBroadcast() {
      return true;
   }

   public boolean shouldInformAdmins() {
      return true;
   }

   public File getServerDirectory() {
      return this.minecraft.gameDirectory;
   }

   public boolean isDedicatedServer() {
      return false;
   }

   public int getRateLimitPacketsPerSecond() {
      return 0;
   }

   public boolean isEpollEnabled() {
      return false;
   }

   public void onServerCrash(CrashReport pReport) {
      this.minecraft.delayCrashRaw(pReport);
   }

   public SystemReport fillServerSystemReport(SystemReport pReport) {
      pReport.setDetail("Type", "Integrated Server (map_client.txt)");
      pReport.setDetail("Is Modded", () -> {
         return this.getModdedStatus().fullDescription();
      });
      pReport.setDetail("Launched Version", this.minecraft::getLaunchedVersion);
      return pReport;
   }

   public ModCheck getModdedStatus() {
      return Minecraft.checkModStatus().merge(super.getModdedStatus());
   }

   public boolean publishServer(@Nullable GameType pGameMode, boolean pCheats, int pPort) {
      try {
         this.minecraft.prepareForMultiplayer();
         this.minecraft.getProfileKeyPairManager().prepareKeyPair().thenAcceptAsync((optionalIn) -> {
            optionalIn.ifPresent((keyPairIn) -> {
               ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
               if (clientpacketlistener != null) {
                  clientpacketlistener.setKeyPair(keyPairIn);
               }

            });
         }, this.minecraft);
         this.getConnection().startTcpServerListener((InetAddress)null, pPort);
         LOGGER.info("Started serving on {}", (int)pPort);
         this.publishedPort = pPort;
         this.lanPinger = new LanServerPinger(this.getMotd(), "" + pPort);
         this.lanPinger.start();
         this.publishedGameType = pGameMode;
         this.getPlayerList().setAllowCheatsForAllPlayers(pCheats);
         int i = this.getProfilePermissions(this.minecraft.player.getGameProfile());
         this.minecraft.player.setPermissionLevel(i);

         for(ServerPlayer serverplayer : this.getPlayerList().getPlayers()) {
            this.getCommands().sendCommands(serverplayer);
         }

         return true;
      } catch (IOException ioexception1) {
         return false;
      }
   }

   public void stopServer() {
      super.stopServer();
      if (this.lanPinger != null) {
         this.lanPinger.interrupt();
         this.lanPinger = null;
      }

   }

   public void halt(boolean pWaitForServer) {
      if (!Reflector.MinecraftForge.exists() || this.isRunning()) {
         this.executeBlocking(() -> {
            for(ServerPlayer serverplayer : Lists.newArrayList(this.getPlayerList().getPlayers())) {
               if (!serverplayer.getUUID().equals(this.uuid)) {
                  this.getPlayerList().remove(serverplayer);
               }
            }

         });
      }

      super.halt(pWaitForServer);
      if (this.lanPinger != null) {
         this.lanPinger.interrupt();
         this.lanPinger = null;
      }

   }

   public boolean isPublished() {
      return this.publishedPort > -1;
   }

   public int getPort() {
      return this.publishedPort;
   }

   public void setDefaultGameType(GameType pGameMode) {
      super.setDefaultGameType(pGameMode);
      this.publishedGameType = null;
   }

   public boolean isCommandBlockEnabled() {
      return true;
   }

   public int getOperatorUserPermissionLevel() {
      return 2;
   }

   public int getFunctionCompilationLevel() {
      return 2;
   }

   public void setUUID(UUID pUuid) {
      this.uuid = pUuid;
   }

   public boolean isSingleplayerOwner(GameProfile pProfile) {
      return this.getSingleplayerProfile() != null && pProfile.getName().equalsIgnoreCase(this.getSingleplayerProfile().getName());
   }

   public int getScaledTrackingDistance(int pTrackingDistance) {
      return (int)(this.minecraft.options.entityDistanceScaling().get() * (double)pTrackingDistance);
   }

   public boolean forceSynchronousWrites() {
      return this.minecraft.options.syncWrites;
   }

   @Nullable
   public GameType getForcedGameType() {
      return this.isPublished() ? MoreObjects.firstNonNull(this.publishedGameType, this.worldData.getGameType()) : null;
   }

   private void onTick() {
      for(ServerLevel serverlevel : this.getAllLevels()) {
         this.onTick(serverlevel);
      }

   }

   private void onTick(ServerLevel ws) {
      if (!Config.isTimeDefault()) {
         this.fixWorldTime(ws);
      }

      if (!Config.isWeatherEnabled()) {
         this.fixWorldWeather(ws);
      }

      if (this.difficultyUpdateWorld == ws && this.difficultyUpdatePos != null) {
         this.difficultyLast = ws.getCurrentDifficultyAt(this.difficultyUpdatePos);
         this.difficultyUpdateWorld = null;
         this.difficultyUpdatePos = null;
      }

   }

   public DifficultyInstance getDifficultyAsync(Level world, BlockPos blockPos) {
      this.difficultyUpdateWorld = world;
      this.difficultyUpdatePos = blockPos;
      return this.difficultyLast;
   }

   private void fixWorldWeather(ServerLevel ws) {
      if (ws.getRainLevel(1.0F) > 0.0F || ws.isThundering()) {
         ws.setWeatherParameters(6000, 0, false, false);
      }

   }

   private void fixWorldTime(ServerLevel ws) {
      if (this.getDefaultGameType() == GameType.CREATIVE) {
         long i = ws.getDayTime();
         long j = i % 24000L;
         if (Config.isTimeDayOnly()) {
            if (j <= 1000L) {
               ws.setDayTime(i - j + 1001L);
            }

            if (j >= 11000L) {
               ws.setDayTime(i - j + 24001L);
            }
         }

         if (Config.isTimeNightOnly()) {
            if (j <= 14000L) {
               ws.setDayTime(i - j + 14001L);
            }

            if (j >= 22000L) {
               ws.setDayTime(i - j + 24000L + 14001L);
            }
         }

      }
   }

   public boolean saveAllChunks(boolean silentIn, boolean flushIn, boolean commandIn) {
      if (silentIn) {
         int i = this.getTickCount();
         int j = this.minecraft.options.ofAutoSaveTicks;
         if ((long)i < this.ticksSaveLast + (long)j) {
            return false;
         }

         this.ticksSaveLast = (long)i;
      }

      return super.saveAllChunks(silentIn, flushIn, commandIn);
   }
}