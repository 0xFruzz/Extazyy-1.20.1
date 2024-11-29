package net.minecraft.world.level.storage;

import java.util.Locale;
import net.minecraft.CrashReportCategory;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelHeightAccessor;

public interface LevelData {
   int getXSpawn();

   int getYSpawn();

   int getZSpawn();

   float getSpawnAngle();

   long getGameTime();

   long getDayTime();

   boolean isThundering();

   boolean isRaining();

   void setRaining(boolean pRaining);

   boolean isHardcore();

   GameRules getGameRules();

   Difficulty getDifficulty();

   boolean isDifficultyLocked();

   default void fillCrashReportCategory(CrashReportCategory pCrashReportCategory, LevelHeightAccessor pLevel) {
      pCrashReportCategory.setDetail("Level spawn location", () -> {
         return CrashReportCategory.formatLocation(pLevel, this.getXSpawn(), this.getYSpawn(), this.getZSpawn());
      });
      pCrashReportCategory.setDetail("Level time", () -> {
         return String.format(Locale.ROOT, "%d game time, %d day time", this.getGameTime(), this.getDayTime());
      });
   }
}