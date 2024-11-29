package net.minecraft.world.level.storage;

import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.timers.TimerQueue;

public class DerivedLevelData implements ServerLevelData {
   private final WorldData worldData;
   private final ServerLevelData wrapped;

   public DerivedLevelData(WorldData pWorldData, ServerLevelData pWrapped) {
      this.worldData = pWorldData;
      this.wrapped = pWrapped;
   }

   public int getXSpawn() {
      return this.wrapped.getXSpawn();
   }

   public int getYSpawn() {
      return this.wrapped.getYSpawn();
   }

   public int getZSpawn() {
      return this.wrapped.getZSpawn();
   }

   public float getSpawnAngle() {
      return this.wrapped.getSpawnAngle();
   }

   public long getGameTime() {
      return this.wrapped.getGameTime();
   }

   public long getDayTime() {
      return this.wrapped.getDayTime();
   }

   public String getLevelName() {
      return this.worldData.getLevelName();
   }

   public int getClearWeatherTime() {
      return this.wrapped.getClearWeatherTime();
   }

   public void setClearWeatherTime(int pTime) {
   }

   public boolean isThundering() {
      return this.wrapped.isThundering();
   }

   public int getThunderTime() {
      return this.wrapped.getThunderTime();
   }

   public boolean isRaining() {
      return this.wrapped.isRaining();
   }

   public int getRainTime() {
      return this.wrapped.getRainTime();
   }

   public GameType getGameType() {
      return this.worldData.getGameType();
   }

   public void setXSpawn(int pX) {
   }

   public void setYSpawn(int pY) {
   }

   public void setZSpawn(int pZ) {
   }

   public void setSpawnAngle(float pAngle) {
   }

   public void setGameTime(long pTime) {
   }

   public void setDayTime(long pTime) {
   }

   public void setSpawn(BlockPos pSpawnPoint, float pAngle) {
   }

   public void setThundering(boolean pThundering) {
   }

   public void setThunderTime(int pTime) {
   }

   public void setRaining(boolean pIsRaining) {
   }

   public void setRainTime(int pTime) {
   }

   public void setGameType(GameType pType) {
   }

   public boolean isHardcore() {
      return this.worldData.isHardcore();
   }

   public boolean getAllowCommands() {
      return this.worldData.getAllowCommands();
   }

   public boolean isInitialized() {
      return this.wrapped.isInitialized();
   }

   public void setInitialized(boolean pInitialized) {
   }

   public GameRules getGameRules() {
      return this.worldData.getGameRules();
   }

   public WorldBorder.Settings getWorldBorder() {
      return this.wrapped.getWorldBorder();
   }

   public void setWorldBorder(WorldBorder.Settings pSerializer) {
   }

   public Difficulty getDifficulty() {
      return this.worldData.getDifficulty();
   }

   public boolean isDifficultyLocked() {
      return this.worldData.isDifficultyLocked();
   }

   public TimerQueue<MinecraftServer> getScheduledEvents() {
      return this.wrapped.getScheduledEvents();
   }

   public int getWanderingTraderSpawnDelay() {
      return 0;
   }

   public void setWanderingTraderSpawnDelay(int pDelay) {
   }

   public int getWanderingTraderSpawnChance() {
      return 0;
   }

   public void setWanderingTraderSpawnChance(int pChance) {
   }

   public UUID getWanderingTraderId() {
      return null;
   }

   public void setWanderingTraderId(UUID pId) {
   }

   public void fillCrashReportCategory(CrashReportCategory pCrashReportCategory, LevelHeightAccessor pLevel) {
      pCrashReportCategory.setDetail("Derived", true);
      this.wrapped.fillCrashReportCategory(pCrashReportCategory, pLevel);
   }
}