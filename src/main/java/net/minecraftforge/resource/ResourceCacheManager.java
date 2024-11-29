package net.minecraftforge.resource;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.ForgeConfigSpec;

public class ResourceCacheManager {
   public ResourceCacheManager(boolean supportsReloading, ForgeConfigSpec.BooleanValue indexOffThreadConfig, BiFunction<PackType, String, Path> pathBuilder) {
   }

   public ResourceCacheManager(boolean supportsReloading, String indexOnThreadConfigurationKey, BiFunction<PackType, String, Path> pathBuilder) {
   }

   public static boolean shouldUseCache() {
      return false;
   }

   public boolean hasCached(PackType packType, String namespace) {
      return false;
   }

   public Collection<ResourceLocation> getResources(PackType type, String resourceNamespace, Path inputPath, Predicate<ResourceLocation> filter) {
      return null;
   }

   public void index(String nameSpace) {
   }
}