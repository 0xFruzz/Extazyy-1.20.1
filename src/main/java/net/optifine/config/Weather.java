package net.optifine.config;

import net.minecraft.world.level.Level;

public enum Weather {
   CLEAR,
   RAIN,
   THUNDER;

   public static Weather getWeather(Level world, float partialTicks) {
      float f = world.getThunderLevel(partialTicks);
      if (f > 0.5F) {
         return THUNDER;
      } else {
         float f1 = world.getRainLevel(partialTicks);
         return f1 > 0.5F ? RAIN : CLEAR;
      }
   }
}