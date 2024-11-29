package net.optifine;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

public class Lang {
   private static final Splitter splitter = Splitter.on('=').limit(2);
   private static final Pattern pattern = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");

   public static void resourcesReloaded() {
      Map map = new HashMap();
      List<String> list = new ArrayList<>();
      String s = "optifine/lang/";
      String s1 = "en_us";
      String s2 = ".lang";
      list.add(s + s1 + s2);
      if (!Config.getGameSettings().languageCode.equals(s1)) {
         list.add(s + Config.getGameSettings().languageCode + s2);
      }

      String[] astring = list.toArray(new String[list.size()]);
      loadResources(Config.getDefaultResourcePack(), astring, map);
      PackResources[] apackresources = Config.getResourcePacks();

      for(int i = 0; i < apackresources.length; ++i) {
         PackResources packresources = apackresources[i];
         loadResources(packresources, astring, map);
      }

   }

   private static void loadResources(PackResources rp, String[] files, Map localeProperties) {
      try {
         for(int i = 0; i < files.length; ++i) {
            String s = files[i];
            ResourceLocation resourcelocation = new ResourceLocation(s);
            IoSupplier<InputStream> iosupplier = rp.getResource(PackType.CLIENT_RESOURCES, resourcelocation);
            if (iosupplier != null) {
               InputStream inputstream = iosupplier.get();
               if (inputstream != null) {
                  loadLocaleData(inputstream, localeProperties);
               }
            }
         }
      } catch (IOException ioexception) {
         ioexception.printStackTrace();
      }

   }

   public static void loadLocaleData(InputStream is, Map localeProperties) throws IOException {
      Iterator iterator = IOUtils.readLines(is, Charsets.UTF_8).iterator();
      is.close();

      while(iterator.hasNext()) {
         String s = (String)iterator.next();
         if (!s.isEmpty() && s.charAt(0) != '#') {
            String[] astring = Iterables.toArray(splitter.split(s), String.class);
            if (astring != null && astring.length == 2) {
               String s1 = astring[0];
               String s2 = pattern.matcher(astring[1]).replaceAll("%$1s");
               localeProperties.put(s1, s2);
            }
         }
      }

   }

   public static void loadResources(ResourceManager resourceManager, String langCode, Map<String, String> map) {
      try {
         String s = "optifine/lang/" + langCode + ".lang";
         ResourceLocation resourcelocation = new ResourceLocation(s);
         Resource resource = resourceManager.getResourceOrThrow(resourcelocation);
         InputStream inputstream = resource.open();
         loadLocaleData(inputstream, map);
      } catch (IOException ioexception) {
      }

   }

   public static String get(String key) {
      return I18n.get(key);
   }

   public static MutableComponent getComponent(String key) {
      return Component.translatable(key);
   }

   public static String get(String key, String def) {
      String s = I18n.get(key);
      return s != null && !s.equals(key) ? s : def;
   }

   public static String getOn() {
      return I18n.get("options.on");
   }

   public static String getOff() {
      return I18n.get("options.off");
   }

   public static String getFast() {
      return I18n.get("options.graphics.fast");
   }

   public static String getFancy() {
      return I18n.get("options.graphics.fancy");
   }

   public static String getDefault() {
      return I18n.get("generator.minecraft.normal");
   }
}