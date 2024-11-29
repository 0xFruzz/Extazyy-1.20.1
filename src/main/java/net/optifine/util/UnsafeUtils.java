package net.optifine.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import sun.misc.Unsafe;

public class UnsafeUtils {
   private static Unsafe unsafe;
   private static boolean checked;

   private static Unsafe getUnsafe() {
      if (checked) {
         return unsafe;
      } else {
         try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe)field.get((Object)null);
         } catch (Exception exception) {
            exception.printStackTrace();
         }

         return unsafe;
      }
   }

   public static void setStaticInt(Field field, int value) {
      if (field != null) {
         if (field.getType() == Integer.TYPE) {
            if (Modifier.isStatic(field.getModifiers())) {
               Unsafe unsafe = getUnsafe();
               if (unsafe != null) {
                  Object object = unsafe.staticFieldBase(field);
                  long i = unsafe.staticFieldOffset(field);
                  if (object != null) {
                     unsafe.putInt(object, i, value);
                  }
               }
            }
         }
      }
   }
}