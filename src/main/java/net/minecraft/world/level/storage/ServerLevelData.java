package net.minecraft.world.level.storage;

import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.timers.TimerQueue;

public interface ServerLevelData extends WritableLevelData {
   String getLevelName();

   void setThundering(boolean pThundering);

   int getRainTime();

   void setRainTime(int pTime);

   void setThunderTime(int pTime);

   int getThunderTime();

   default void fillCrashReportCategory(CrashReportCategory pCrashReportCategory, LevelHeightAccessor pLevel) {
      WritableLevelData.super.fillCrashReportCategory(pCrashReportCategory, pLevel);
      pCrashReportCategory.setDetail("Level name", this::getLevelName);
      pCrashReportCategory.setDetail("Level game mode", () -> {
         return String.format(Locale.ROOT, "Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", this.getGameType().getName(), this.getGameType().getId(), this.isHardcore(), this.getAllowCommands());
      });
      pCrashReportCategory.setDetail("Level weather", () -> {
         return String.format(Locale.ROOT, "Rain time: %d (now: %b), thunder time: %d (now: %b)", this.getRainTime(), this.isRaining(), this.getThunderTime(), this.isThundering());
      });
   }

   int getClearWeatherTime();

   void setClearWeatherTime(int pTime);

   int getWanderingTraderSpawnDelay();

   void setWanderingTraderSpawnDelay(int pDelay);

   int getWanderingTraderSpawnChance();

   void setWanderingTraderSpawnChance(int pChance);

   @Nullable
   UUID getWanderingTraderId();

   void setWanderingTraderId(UUID pId);

   GameType getGameType();

   void setWorldBorder(WorldBorder.Settings pSerializer);

   WorldBorder.Settings getWorldBorder();

   boolean isInitialized();

   void setInitialized(boolean pInitialized);

   boolean getAllowCommands();

   void setGameType(GameType pType);

   TimerQueue<MinecraftServer> getScheduledEvents();

   void setGameTime(long pTime);

   void setDayTime(long pTime);
}