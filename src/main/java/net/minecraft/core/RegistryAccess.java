package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public interface RegistryAccess extends HolderLookup.Provider {
   Logger LOGGER = LogUtils.getLogger();
   RegistryAccess.Frozen EMPTY = (new RegistryAccess.ImmutableRegistryAccess(Map.of())).freeze();

   <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> pRegistryKey);

   default <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> pRegistryKey) {
      return this.registry(pRegistryKey).map(Registry::asLookup);
   }

   default <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> pRegistryKey) {
      return this.registry(pRegistryKey).orElseThrow(() -> {
         return new IllegalStateException("Missing registry: " + pRegistryKey);
      });
   }

   Stream<RegistryAccess.RegistryEntry<?>> registries();

   static RegistryAccess.Frozen fromRegistryOfRegistries(final Registry<? extends Registry<?>> pRegistryOfRegistries) {
      return new RegistryAccess.Frozen() {
         public <T> Optional<Registry<T>> registry(ResourceKey<? extends Registry<? extends T>> p_206220_) {
            Registry<Registry<T>> registry = (Registry<Registry<T>>) pRegistryOfRegistries;
            return registry.getOptional((ResourceKey<Registry<T>>) p_206220_);
         }

         public Stream<RegistryAccess.RegistryEntry<?>> registries() {
            return pRegistryOfRegistries.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
         }

         public RegistryAccess.Frozen freeze() {
            return this;
         }
      };
   }

   default RegistryAccess.Frozen freeze() {
      class FrozenAccess extends RegistryAccess.ImmutableRegistryAccess implements RegistryAccess.Frozen {
         protected FrozenAccess(Stream<RegistryAccess.RegistryEntry<?>> p_252031_) {
            super(p_252031_);
         }
      }

      return new FrozenAccess(this.registries().map(RegistryAccess.RegistryEntry::freeze));
   }

   default Lifecycle allRegistriesLifecycle() {
      return this.registries().map((p_258181_) -> {
         return p_258181_.value.registryLifecycle();
      }).reduce(Lifecycle.stable(), Lifecycle::add);
   }

   public interface Frozen extends RegistryAccess {
   }

   public static class ImmutableRegistryAccess implements RegistryAccess {
      private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> registries;

      public ImmutableRegistryAccess(List<? extends Registry<?>> pRegistries) {
         this.registries = pRegistries.stream().collect(Collectors.toUnmodifiableMap(Registry::key, (p_206232_) -> {
            return p_206232_;
         }));
      }

      public ImmutableRegistryAccess(Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> pRegistries) {
         this.registries = Map.copyOf(pRegistries);
      }

      public ImmutableRegistryAccess(Stream<RegistryAccess.RegistryEntry<?>> pRegistries) {
         this.registries = pRegistries.collect(ImmutableMap.toImmutableMap(RegistryAccess.RegistryEntry::key, RegistryAccess.RegistryEntry::value));
      }

      public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> pRegistryKey) {
         return Optional.ofNullable(this.registries.get(pRegistryKey)).map((p_247993_) -> {
            return (Registry<E>) p_247993_;
         });
      }

      public Stream<RegistryAccess.RegistryEntry<?>> registries() {
         return this.registries.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
      }
   }

   public static record RegistryEntry<T>(ResourceKey<? extends Registry<T>> key, Registry<T> value) {
      private static <T, R extends Registry<? extends T>> RegistryAccess.RegistryEntry<T> fromMapEntry(Map.Entry<? extends ResourceKey<? extends Registry<?>>, R> pMapEntry) {
         return fromUntyped(pMapEntry.getKey(), pMapEntry.getValue());
      }

      private static <T> RegistryAccess.RegistryEntry<T> fromUntyped(ResourceKey<? extends Registry<?>> pKey, Registry<?> pValue) {
         return new RegistryAccess.RegistryEntry<T>((ResourceKey<? extends Registry<T>>) pKey, (Registry<T>) pValue);
      }

      private RegistryAccess.RegistryEntry<T> freeze() {
         return new RegistryAccess.RegistryEntry<>(this.key, this.value.freeze());
      }
   }
}