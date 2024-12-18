package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;

public class RegistrySynchronization {
   private static final Map<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>> NETWORKABLE_REGISTRIES = Util.make(() -> {
      ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>> builder = ImmutableMap.builder();
      put(builder, Registries.BIOME, Biome.NETWORK_CODEC);
      put(builder, Registries.CHAT_TYPE, ChatType.CODEC);
      put(builder, Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC);
      put(builder, Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC);
      put(builder, Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC);
      put(builder, Registries.DAMAGE_TYPE, DamageType.CODEC);
      return builder.build();
   });
   public static final Codec<RegistryAccess> NETWORK_CODEC = makeNetworkCodec();

   private static <E> void put(ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>> pNetworkableMapBuilder, ResourceKey<? extends Registry<E>> pKey, Codec<E> pNetworkCodec) {
      pNetworkableMapBuilder.put(pKey, new RegistrySynchronization.NetworkedRegistryData<>(pKey, pNetworkCodec));
   }

   private static Stream<RegistryAccess.RegistryEntry<?>> ownedNetworkableRegistries(RegistryAccess pRegistryAccess) {
      return pRegistryAccess.registries().filter((p_250129_) -> {
         return NETWORKABLE_REGISTRIES.containsKey(p_250129_.key());
      });
   }

   private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> pRegistryKey) {
      return Optional.ofNullable(NETWORKABLE_REGISTRIES.get(pRegistryKey)).map((p_250582_) -> {
         return (Codec<E>)p_250582_.networkCodec();
      }).map(DataResult::success).orElseGet(() -> {
         return DataResult.error(() -> {
            return "Unknown or not serializable registry: " + pRegistryKey;
         });
      });
   }

   private static <E> Codec<RegistryAccess> makeNetworkCodec() {
      Codec<ResourceKey<? extends Registry<E>>> codec = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
      Codec<Registry<E>> codec1 = codec.partialDispatch("type", (p_258198_) -> {
         return DataResult.success(p_258198_.key());
      }, (p_250682_) -> {
         return getNetworkCodec(p_250682_).map((p_252116_) -> {
            return RegistryCodecs.networkCodec(p_250682_, Lifecycle.experimental(), p_252116_);
         });
      });
      UnboundedMapCodec<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> unboundedmapcodec = Codec.unboundedMap(codec, codec1);
      return captureMap(unboundedmapcodec);
   }

   private static <K extends ResourceKey<? extends Registry<?>>, V extends Registry<?>> Codec<RegistryAccess> captureMap(UnboundedMapCodec<K, V> pMapCodec) {
      return pMapCodec.xmap(RegistryAccess.ImmutableRegistryAccess::new, (p_251578_) -> {
         return ownedNetworkableRegistries(p_251578_).collect(ImmutableMap.toImmutableMap((p_250395_) -> {
            return (K)p_250395_.key();
         }, (p_248951_) -> {
            return (V)p_248951_.value();
         }));
      });
   }

   public static Stream<RegistryAccess.RegistryEntry<?>> networkedRegistries(LayeredRegistryAccess<RegistryLayer> pRegistryAccess) {
      return ownedNetworkableRegistries(pRegistryAccess.getAccessFrom(RegistryLayer.WORLDGEN));
   }

   public static Stream<RegistryAccess.RegistryEntry<?>> networkSafeRegistries(LayeredRegistryAccess<RegistryLayer> pRegistryAccess) {
      Stream<RegistryAccess.RegistryEntry<?>> stream = pRegistryAccess.getLayer(RegistryLayer.STATIC).registries();
      Stream<RegistryAccess.RegistryEntry<?>> stream1 = networkedRegistries(pRegistryAccess);
      return Stream.concat(stream1, stream);
   }

   static record NetworkedRegistryData<E>(ResourceKey<? extends Registry<E>> key, Codec<E> networkCodec) {
   }
}