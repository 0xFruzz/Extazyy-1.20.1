package net.optifine.util;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.optifine.reflect.Reflector;

public class KeyUtils {
   public static void fixKeyConflicts(KeyMapping[] keys, KeyMapping[] keysPrio) {
      Set<String> set = new HashSet<>();

      for(int i = 0; i < keysPrio.length; ++i) {
         KeyMapping keymapping = keysPrio[i];
         set.add(getId(keymapping));
      }

      Set<KeyMapping> set1 = new HashSet<>(Arrays.asList(keys));
      set1.removeAll(Arrays.asList(keysPrio));

      for(KeyMapping keymapping1 : set1) {
         String s = getId(keymapping1);
         if (set.contains(s)) {
            keymapping1.setKey(InputConstants.UNKNOWN);
         }
      }

   }

   public static String getId(KeyMapping keyMapping) {
      if (Reflector.ForgeKeyBinding_getKeyModifier.exists()) {
         Object object = Reflector.call(keyMapping, Reflector.ForgeKeyBinding_getKeyModifier);
         Object object1 = Reflector.getFieldValue(Reflector.KeyModifier_NONE);
         if (object != object1) {
            return object + "+" + keyMapping.saveString();
         }
      }

      return keyMapping.saveString();
   }
}