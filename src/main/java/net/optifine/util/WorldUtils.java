package net.optifine.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class WorldUtils {
   public static int getDimensionId(Level world) {
      return world == null ? 0 : getDimensionId(world.dimension());
   }

   public static int getDimensionId(ResourceKey<Level> dimension) {
      if (dimension == Level.NETHER) {
         return -1;
      } else if (dimension == Level.OVERWORLD) {
         return 0;
      } else {
         return dimension == Level.END ? 1 : 0;
      }
   }

   public static boolean isNether(Level world) {
      return world.dimension() == Level.NETHER;
   }

   public static boolean isOverworld(Level world) {
      ResourceKey<Level> resourcekey = world.dimension();
      return getDimensionId(resourcekey) == 0;
   }

   public static boolean isEnd(Level world) {
      return world.dimension() == Level.END;
   }
}