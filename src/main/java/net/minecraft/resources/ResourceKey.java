package net.minecraft.resources;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ResourceKey<T> {
   private static final ConcurrentMap<ResourceKey.InternKey, ResourceKey<?>> VALUES = (new MapMaker()).weakValues().makeMap();
   private final ResourceLocation registryName;
   private final ResourceLocation location;

   public static <T> Codec<ResourceKey<T>> codec(ResourceKey<? extends Registry<T>> pRegistryKey) {
      return ResourceLocation.CODEC.xmap((p_195979_) -> {
         return create(pRegistryKey, p_195979_);
      }, ResourceKey::location);
   }

   public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> pRegistryKey, ResourceLocation pLocation) {
      return create(pRegistryKey.location, pLocation);
   }

   public static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation pLocation) {
      return create(BuiltInRegistries.ROOT_REGISTRY_NAME, pLocation);
   }

   private static <T> ResourceKey<T> create(ResourceLocation pRegistryName, ResourceLocation pLocation) {
      return (ResourceKey<T>)VALUES.computeIfAbsent(new ResourceKey.InternKey(pRegistryName, pLocation), (p_258225_) -> {
         return new ResourceKey(p_258225_.registry, p_258225_.location);
      });
   }

   private ResourceKey(ResourceLocation pRegistryName, ResourceLocation pLocation) {
      this.registryName = pRegistryName;
      this.location = pLocation;
   }

   public String toString() {
      return "ResourceKey[" + this.registryName + " / " + this.location + "]";
   }

   public boolean isFor(ResourceKey<? extends Registry<?>> pRegistryKey) {
      return this.registryName.equals(pRegistryKey.location());
   }

   public <E> Optional<ResourceKey<E>> cast(ResourceKey<? extends Registry<E>> pRegistryKey) {
      return this.isFor(pRegistryKey) ? Optional.of((ResourceKey<E>)this) : Optional.empty();
   }

   public ResourceLocation location() {
      return this.location;
   }

   public ResourceLocation registry() {
      return this.registryName;
   }

   static record InternKey(ResourceLocation registry, ResourceLocation location) {
   }
}