package net.optifine.util;

import com.google.common.collect.Lists;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.optifine.config.BiomeId;
import net.optifine.override.ChunkCacheOF;

public class BiomeUtils {
   private static Registry<Biome> defaultBiomeRegistry = makeDefaultBiomeRegistry();
   private static Registry<Biome> biomeRegistry = getBiomeRegistry(Minecraft.getInstance().level);
   private static Level biomeWorld = Minecraft.getInstance().level;
   public static Biome PLAINS = biomeRegistry.get(Biomes.PLAINS);
   public static Biome SUNFLOWER_PLAINS = biomeRegistry.get(Biomes.SUNFLOWER_PLAINS);
   public static Biome SNOWY_PLAINS = biomeRegistry.get(Biomes.SNOWY_PLAINS);
   public static Biome ICE_SPIKES = biomeRegistry.get(Biomes.ICE_SPIKES);
   public static Biome DESERT = biomeRegistry.get(Biomes.DESERT);
   public static Biome WINDSWEPT_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_HILLS);
   public static Biome WINDSWEPT_GRAVELLY_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_GRAVELLY_HILLS);
   public static Biome MUSHROOM_FIELDS = biomeRegistry.get(Biomes.MUSHROOM_FIELDS);
   public static Biome SWAMP = biomeRegistry.get(Biomes.SWAMP);
   public static Biome MANGROVE_SWAMP = biomeRegistry.get(Biomes.MANGROVE_SWAMP);
   public static Biome THE_VOID = biomeRegistry.get(Biomes.THE_VOID);

   public static void onWorldChanged(Level worldIn) {
      biomeRegistry = getBiomeRegistry(worldIn);
      biomeWorld = worldIn;
      PLAINS = biomeRegistry.get(Biomes.PLAINS);
      SUNFLOWER_PLAINS = biomeRegistry.get(Biomes.SUNFLOWER_PLAINS);
      SNOWY_PLAINS = biomeRegistry.get(Biomes.SNOWY_PLAINS);
      ICE_SPIKES = biomeRegistry.get(Biomes.ICE_SPIKES);
      DESERT = biomeRegistry.get(Biomes.DESERT);
      WINDSWEPT_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_HILLS);
      WINDSWEPT_GRAVELLY_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_GRAVELLY_HILLS);
      MUSHROOM_FIELDS = biomeRegistry.get(Biomes.MUSHROOM_FIELDS);
      SWAMP = biomeRegistry.get(Biomes.SWAMP);
      MANGROVE_SWAMP = biomeRegistry.get(Biomes.MANGROVE_SWAMP);
      THE_VOID = biomeRegistry.get(Biomes.THE_VOID);
   }

   private static Biome getBiomeSafe(Registry<Biome> registry, ResourceKey<Biome> biomeKey, Supplier<Biome> biomeDefault) {
      Biome biome = registry.get(biomeKey);
      if (biome == null) {
         biome = biomeDefault.get();
      }

      return biome;
   }

   public static Registry<Biome> getBiomeRegistry(Level worldIn) {
      if (worldIn != null) {
         if (worldIn == biomeWorld) {
            return biomeRegistry;
         } else {
            Registry<Biome> registry = worldIn.registryAccess().registryOrThrow(Registries.BIOME);
            return fixBiomeIds(defaultBiomeRegistry, registry);
         }
      } else {
         return defaultBiomeRegistry;
      }
   }

   private static Registry<Biome> makeDefaultBiomeRegistry() {
      MappedRegistry<Biome> mappedregistry = new MappedRegistry<>(ResourceKey.createRegistryKey(new ResourceLocation("biomes")), Lifecycle.stable(), true);

      for(ResourceKey<Biome> resourcekey : Biomes.getBiomeKeys()) {
         Biome.BiomeBuilder biome$biomebuilder = new Biome.BiomeBuilder();
         biome$biomebuilder.hasPrecipitation(false);
         biome$biomebuilder.temperature(0.0F);
         biome$biomebuilder.downfall(0.0F);
         biome$biomebuilder.specialEffects((new BiomeSpecialEffects.Builder()).fogColor(0).waterColor(0).waterFogColor(0).skyColor(0).build());
         biome$biomebuilder.mobSpawnSettings((new MobSpawnSettings.Builder()).build());
         biome$biomebuilder.generationSettings((new BiomeGenerationSettings.Builder((HolderGetter<PlacedFeature>)null, (HolderGetter<ConfiguredWorldCarver<?>>)null)).build());
         Biome biome = biome$biomebuilder.build();
         mappedregistry.createIntrusiveHolder(biome);
         Holder.Reference holder$reference = mappedregistry.register(resourcekey, biome, Lifecycle.stable());
      }

      return mappedregistry;
   }

   private static Registry<Biome> fixBiomeIds(Registry<Biome> idRegistry, Registry<Biome> valueRegistry) {
      MappedRegistry<Biome> mappedregistry = new MappedRegistry<>(ResourceKey.createRegistryKey(new ResourceLocation("biomes")), Lifecycle.stable(), true);

      for(ResourceKey<Biome> resourcekey : idRegistry.registryKeySet()) {
         Biome biome = valueRegistry.get(resourcekey);
         if (biome == null) {
            biome = idRegistry.get(resourcekey);
         }

         int i = idRegistry.getId(idRegistry.get(resourcekey));
         mappedregistry.createIntrusiveHolder(biome);
         Holder.Reference holder$reference = mappedregistry.registerMapping(i, resourcekey, biome, Lifecycle.stable());
      }

      for(ResourceKey<Biome> resourcekey1 : valueRegistry.registryKeySet()) {
         if (!mappedregistry.containsKey(resourcekey1)) {
            Biome biome1 = valueRegistry.get(resourcekey1);
            mappedregistry.createIntrusiveHolder(biome1);
            Holder.Reference holder$reference1 = mappedregistry.register(resourcekey1, biome1, Lifecycle.stable());
         }
      }

      return mappedregistry;
   }

   public static Registry<Biome> getBiomeRegistry() {
      return biomeRegistry;
   }

   public static ResourceLocation getLocation(Biome biome) {
      return getBiomeRegistry().getKey(biome);
   }

   public static int getId(Biome biome) {
      return getBiomeRegistry().getId(biome);
   }

   public static int getId(ResourceLocation loc) {
      Biome biome = getBiome(loc);
      return getBiomeRegistry().getId(biome);
   }

   public static BiomeId getBiomeId(ResourceLocation loc) {
      return BiomeId.make(loc);
   }

   public static Biome getBiome(ResourceLocation loc) {
      return getBiomeRegistry().get(loc);
   }

   public static Set<ResourceLocation> getLocations() {
      return getBiomeRegistry().keySet();
   }

   public static List<Biome> getBiomes() {
      return Lists.newArrayList(biomeRegistry);
   }

   public static List<BiomeId> getBiomeIds() {
      return getBiomeIds(getLocations());
   }

   public static List<BiomeId> getBiomeIds(Collection<ResourceLocation> locations) {
      List<BiomeId> list = new ArrayList<>();

      for(ResourceLocation resourcelocation : locations) {
         BiomeId biomeid = BiomeId.make(resourcelocation);
         if (biomeid != null) {
            list.add(biomeid);
         }
      }

      return list;
   }

   public static Biome getBiome(BlockAndTintGetter lightReader, BlockPos blockPos) {
      Biome biome = PLAINS;
      if (lightReader instanceof ChunkCacheOF) {
         biome = ((ChunkCacheOF)lightReader).getBiome(blockPos);
      } else if (lightReader instanceof LevelReader) {
         biome = ((LevelReader)lightReader).getBiome(blockPos).value();
      }

      return biome;
   }

   public static BiomeCategory getBiomeCategory(Holder<Biome> holder) {
      if (holder.value() == THE_VOID) {
         return BiomeCategory.NONE;
      } else if (holder.is(BiomeTags.IS_TAIGA)) {
         return BiomeCategory.TAIGA;
      } else if (holder.value() != WINDSWEPT_HILLS && holder.value() != WINDSWEPT_GRAVELLY_HILLS) {
         if (holder.is(BiomeTags.IS_JUNGLE)) {
            return BiomeCategory.JUNGLE;
         } else if (holder.is(BiomeTags.IS_BADLANDS)) {
            return BiomeCategory.MESA;
         } else if (holder.value() != PLAINS && holder.value() != PLAINS) {
            if (holder.is(BiomeTags.IS_SAVANNA)) {
               return BiomeCategory.SAVANNA;
            } else if (holder.value() != SNOWY_PLAINS && holder.value() != ICE_SPIKES) {
               if (holder.is(BiomeTags.IS_END)) {
                  return BiomeCategory.THEEND;
               } else if (holder.is(BiomeTags.IS_BEACH)) {
                  return BiomeCategory.BEACH;
               } else if (holder.is(BiomeTags.IS_FOREST)) {
                  return BiomeCategory.FOREST;
               } else if (holder.is(BiomeTags.IS_OCEAN)) {
                  return BiomeCategory.OCEAN;
               } else if (holder.value() == DESERT) {
                  return BiomeCategory.DESERT;
               } else if (holder.is(BiomeTags.IS_RIVER)) {
                  return BiomeCategory.RIVER;
               } else if (holder.value() != SWAMP && holder.value() != MANGROVE_SWAMP) {
                  if (holder.value() == MUSHROOM_FIELDS) {
                     return BiomeCategory.MUSHROOM;
                  } else if (holder.is(BiomeTags.IS_NETHER)) {
                     return BiomeCategory.NETHER;
                  } else if (holder.is(BiomeTags.PLAYS_UNDERWATER_MUSIC)) {
                     return BiomeCategory.UNDERGROUND;
                  } else {
                     return holder.is(BiomeTags.IS_MOUNTAIN) ? BiomeCategory.MOUNTAIN : BiomeCategory.PLAINS;
                  }
               } else {
                  return BiomeCategory.SWAMP;
               }
            } else {
               return BiomeCategory.ICY;
            }
         } else {
            return BiomeCategory.PLAINS;
         }
      } else {
         return BiomeCategory.EXTREME_HILLS;
      }
   }

   public static float getDownfall(Biome biome) {
      return Biomes.getDownfall(biome);
   }

   public static Biome.Precipitation getPrecipitation(Biome biome) {
      if (!biome.hasPrecipitation()) {
         return Biome.Precipitation.NONE;
      } else {
         return (double)biome.getBaseTemperature() < 0.1D ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN;
      }
   }
}