package net.optifine.util;

import java.lang.reflect.Array;

public class ArrayCaches {
   private int[] sizes;
   private Class elementClass;
   private ArrayCache[] caches;

   public ArrayCaches(int[] sizes, Class elementClass, int maxCacheSize) {
      this.sizes = sizes;
      this.elementClass = elementClass;
      this.caches = new ArrayCache[sizes.length];

      for(int i = 0; i < this.caches.length; ++i) {
         this.caches[i] = new ArrayCache(elementClass, maxCacheSize);
      }

   }

   public Object allocate(int size) {
      for(int i = 0; i < this.sizes.length; ++i) {
         if (size == this.sizes[i]) {
            return this.caches[i].allocate(size);
         }
      }

      return Array.newInstance(this.elementClass, size);
   }

   public void free(Object arr) {
      if (arr != null) {
         int i = Array.getLength(arr);

         for(int j = 0; j < this.sizes.length; ++j) {
            if (i == this.sizes[j]) {
               this.caches[j].free(arr);
               return;
            }
         }

      }
   }
}