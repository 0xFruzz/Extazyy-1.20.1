package net.minecraft.data.registries;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public class RegistriesDatapackGenerator implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final PackOutput output;
   private final CompletableFuture<HolderLookup.Provider> registries;

   public RegistriesDatapackGenerator(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
      this.registries = pRegistries;
      this.output = pOutput;
   }

   public CompletableFuture<?> run(CachedOutput pOutput) {
      return this.registries.thenCompose((p_256533_) -> {
         DynamicOps<JsonElement> dynamicops = RegistryOps.create(JsonOps.INSTANCE, p_256533_);
         return CompletableFuture.allOf(RegistryDataLoader.WORLDGEN_REGISTRIES.stream().flatMap((p_256552_) -> {
            return this.dumpRegistryCap(pOutput, p_256533_, dynamicops, p_256552_).stream();
         }).toArray((p_255809_) -> {
            return new CompletableFuture[p_255809_];
         }));
      });
   }

   private <T> Optional<CompletableFuture<?>> dumpRegistryCap(CachedOutput pOutput, HolderLookup.Provider pRegistries, DynamicOps<JsonElement> pOps, RegistryDataLoader.RegistryData<T> pRegistryData) {
      ResourceKey<? extends Registry<T>> resourcekey = pRegistryData.key();
      return pRegistries.lookup(resourcekey).map((p_255847_) -> {
         PackOutput.PathProvider packoutput$pathprovider = this.output.createPathProvider(PackOutput.Target.DATA_PACK, resourcekey.location().getPath());
         return CompletableFuture.allOf(p_255847_.listElements().map((p_256105_) -> {
            return dumpValue(packoutput$pathprovider.json(p_256105_.key().location()), pOutput, pOps, pRegistryData.elementCodec(), p_256105_.value());
         }).toArray((p_256279_) -> {
            return new CompletableFuture[p_256279_];
         }));
      });
   }

   private static <E> CompletableFuture<?> dumpValue(Path pValuePath, CachedOutput pOutput, DynamicOps<JsonElement> pOps, Encoder<E> pEncoder, E pValue) {
      Optional<JsonElement> optional = pEncoder.encodeStart(pOps, pValue).resultOrPartial((p_255999_) -> {
         LOGGER.error("Couldn't serialize element {}: {}", pValuePath, p_255999_);
      });
      return optional.isPresent() ? DataProvider.saveStable(pOutput, optional.get(), pValuePath) : CompletableFuture.completedFuture((Object)null);
   }

   public final String getName() {
      return "Registries";
   }
}