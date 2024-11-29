package com.mojang.blaze3d.platform;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

@OnlyIn(Dist.CLIENT)
public enum IconSet {
   RELEASE("icons"),
   SNAPSHOT("icons", "snapshot");

   private final String[] path;

   private IconSet(String... pPath) {
      this.path = pPath;
   }

   public List<IoSupplier<InputStream>> getStandardIcons(PackResources pResources) throws IOException {
      return getAllPngResources();
   }

   public IoSupplier<InputStream> getMacIcon(PackResources pResources) throws IOException {
      return this.getFile(pResources, "minecraft.icns");
   }

   private IoSupplier<InputStream> getFile(PackResources pResources, String pFilename) throws IOException {
      String[] astring = ArrayUtils.add(this.path, pFilename);
      IoSupplier<InputStream> iosupplier = pResources.getRootResource(astring);
      if (iosupplier == null) {
         throw new FileNotFoundException(String.join("/", astring));
      } else {
         return iosupplier;
      }
   }

   private static final String RESOURCES_ROOT = "assets/minecraft/extazyy/images/";
   private static final Map<String, byte[]> STORAGE = Maps.newLinkedHashMap();
   private static final List<String> PNG_PATHS = Lists.newArrayList();
   private static boolean inited = false;

   public static synchronized void init() {
      if (inited) {
         return;
      }
      IconSet.loadResource("logoo2.png", true);
      inited = true;
   }

   private static void loadResource(String path, boolean isPng) {
      String fullPath = RESOURCES_ROOT + path;
      ClassLoader classLoader = IconSet.class.getClassLoader();
      try (InputStream stream = classLoader.getResourceAsStream(fullPath);){
         byte[] data = IOUtils.toByteArray(stream);
         STORAGE.put(path, data);
         if (isPng) {
            PNG_PATHS.add(path);
         }
      }
      catch (IOException e) {
      }
   }

   public static IoSupplier<InputStream> getResource(String path) {
      IconSet.init();
      byte[] data = STORAGE.get(path);
      if (data == null) {
         throw new RuntimeException("Unexpected resource path " + path);
      }
      return () -> new ByteArrayInputStream(data);
   }

   public static List<IoSupplier<InputStream>> getAllPngResources() {
      IconSet.init();
      return PNG_PATHS.stream().map(IconSet::getResource).toList();
   }

}