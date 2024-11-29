package net.optifine.util;

import com.mojang.blaze3d.platform.NativeImage;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import net.optifine.Config;

public class NativeMemory {
   private static long imageAllocated = 0L;
   private static LongSupplier bufferAllocatedSupplier = makeLongSupplier(new String[][]{{"sun.misc.SharedSecrets", "getJavaNioAccess", "getDirectBufferPool", "getMemoryUsed"}, {"jdk.internal.misc.SharedSecrets", "getJavaNioAccess", "getDirectBufferPool", "getMemoryUsed"}}, makeDefaultAllocatedSupplier());
   private static LongSupplier bufferMaximumSupplier = makeLongSupplier(new String[][]{{"sun.misc.VM", "maxDirectMemory"}, {"jdk.internal.misc.VM", "maxDirectMemory"}}, makeDefaultMaximumSupplier());

   public static long getBufferAllocated() {
      return bufferAllocatedSupplier == null ? -1L : bufferAllocatedSupplier.getAsLong();
   }

   public static long getBufferMaximum() {
      return bufferMaximumSupplier == null ? -1L : bufferMaximumSupplier.getAsLong();
   }

   public static synchronized void imageAllocated(NativeImage nativeImage) {
      imageAllocated += nativeImage.getSize();
   }

   public static synchronized void imageFreed(NativeImage nativeImage) {
      imageAllocated -= nativeImage.getSize();
   }

   public static long getImageAllocated() {
      return imageAllocated;
   }

   private static BufferPoolMXBean getDirectBufferPoolMXBean() {
      for(BufferPoolMXBean bufferpoolmxbean : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
         if (Config.equals(bufferpoolmxbean.getName(), "direct")) {
            return bufferpoolmxbean;
         }
      }

      return null;
   }

   private static LongSupplier makeDefaultAllocatedSupplier() {
      final BufferPoolMXBean bufferpoolmxbean = getDirectBufferPoolMXBean();
      return bufferpoolmxbean == null ? null : new LongSupplier() {
         public long getAsLong() {
            return bufferpoolmxbean.getMemoryUsed();
         }
      };
   }

   private static LongSupplier makeDefaultMaximumSupplier() {
      return new LongSupplier() {
         public long getAsLong() {
            return Runtime.getRuntime().maxMemory();
         }
      };
   }

   private static LongSupplier makeLongSupplier(String[][] paths, LongSupplier defaultSupplier) {
      List<Throwable> list = new ArrayList<>();

      for(int i = 0; i < paths.length; ++i) {
         String[] astring = paths[i];

         try {
            LongSupplier longsupplier = makeLongSupplier(astring);
            if (longsupplier != null) {
               return longsupplier;
            }
         } catch (Throwable throwable) {
            list.add(throwable);
         }
      }

      for(Throwable throwable1 : list) {
         Config.warn("(Reflector) " + throwable1.getClass().getName() + ": " + throwable1.getMessage());
      }

      return defaultSupplier;
   }

   private static LongSupplier makeLongSupplier(String[] path) throws Exception {
      if (path.length < 2) {
         return null;
      } else {
         Class oclass = Class.forName(path[0]);
         Method method = oclass.getMethod(path[1]);
         method.setAccessible(true);
         Object object = null;

         for(int i = 2; i < path.length; ++i) {
            String s = path[i];
            object = method.invoke(object);
            method = object.getClass().getMethod(s);
            method.setAccessible(true);
         }

         final Method method1 = method;
         Object finalObject = object;
         return new LongSupplier() {
            private boolean disabled = false;

            public long getAsLong() {
               if (this.disabled) {
                  return -1L;
               } else {
                  try {
                     return (long) method1.invoke(finalObject);
                  } catch (Throwable throwable) {
                     Config.warn("(Reflector) " + throwable.getClass().getName() + ": " + throwable.getMessage());
                     this.disabled = true;
                     return -1L;
                  }
               }
            }
         };
      }
   }
}
