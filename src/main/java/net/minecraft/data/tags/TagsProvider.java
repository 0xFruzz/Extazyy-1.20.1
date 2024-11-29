package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import org.slf4j.Logger;

public abstract class TagsProvider<T> implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final PackOutput.PathProvider pathProvider;
   private final CompletableFuture<HolderLookup.Provider> lookupProvider;
   private final CompletableFuture<Void> contentsDone = new CompletableFuture<>();
   private final CompletableFuture<TagsProvider.TagLookup<T>> parentProvider;
   protected final ResourceKey<? extends Registry<T>> registryKey;
   private final Map<ResourceLocation, TagBuilder> builders = Maps.newLinkedHashMap();

   protected TagsProvider(PackOutput pOutput, ResourceKey<? extends Registry<T>> pRegistryKey, CompletableFuture<HolderLookup.Provider> pLookupProvider) {
      this(pOutput, pRegistryKey, pLookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()));
   }

   protected TagsProvider(PackOutput pOutput, ResourceKey<? extends Registry<T>> pRegistryKey, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagsProvider.TagLookup<T>> pParentProvider) {
      this.pathProvider = pOutput.createPathProvider(PackOutput.Target.DATA_PACK, TagManager.getTagDir(pRegistryKey));
      this.registryKey = pRegistryKey;
      this.parentProvider = pParentProvider;
      this.lookupProvider = pLookupProvider;
   }

   public final String getName() {
      return "Tags for " + this.registryKey.location();
   }

   protected abstract void addTags(HolderLookup.Provider pProvider);

   public CompletableFuture<?> run(CachedOutput pOutput) {
         record CombinedData<T>(HolderLookup.Provider contents, TagsProvider.TagLookup<T> parent) {
         }
      return this.createContentsProvider().thenApply((p_275895_) -> {
         this.contentsDone.complete((Void)null);
         return p_275895_;
      }).thenCombineAsync(this.parentProvider, (p_274778_, p_274779_) -> {

         return new CombinedData<>(p_274778_, p_274779_);
      }).thenCompose((p_274774_) -> {
         HolderLookup.RegistryLookup<T> registrylookup = p_274774_.contents.lookupOrThrow(this.registryKey);
         Predicate<ResourceLocation> predicate = (p_255496_) -> {
            return registrylookup.get(ResourceKey.create(this.registryKey, p_255496_)).isPresent();
         };
         Predicate<ResourceLocation> predicate1 = (p_274776_) -> {
            return this.builders.containsKey(p_274776_) || p_274774_.parent.contains(TagKey.create(this.registryKey, p_274776_));
         };
         return CompletableFuture.allOf(this.builders.entrySet().stream().map((p_255499_) -> {
            ResourceLocation resourcelocation = p_255499_.getKey();
            TagBuilder tagbuilder = p_255499_.getValue();
            List<TagEntry> list = tagbuilder.build();
            List<TagEntry> list1 = list.stream().filter((p_274771_) -> {
               return !p_274771_.verifyIfPresent(predicate, predicate1);
            }).toList();
            if (!list1.isEmpty()) {
               throw new IllegalArgumentException(String.format(Locale.ROOT, "Couldn't define tag %s as it is missing following references: %s", resourcelocation, list1.stream().map(Objects::toString).collect(Collectors.joining(","))));
            } else {
               JsonElement jsonelement = TagFile.CODEC.encodeStart(JsonOps.INSTANCE, new TagFile(list, false)).getOrThrow(false, LOGGER::error);
               Path path = this.pathProvider.json(resourcelocation);
               return DataProvider.saveStable(pOutput, jsonelement, path);
            }
         }).toArray((p_253442_) -> {
            return new CompletableFuture[p_253442_];
         }));
      });
   }

   protected TagsProvider.TagAppender<T> tag(TagKey<T> pTag) {
      TagBuilder tagbuilder = this.getOrCreateRawBuilder(pTag);
      return new TagsProvider.TagAppender<>(tagbuilder);
   }

   protected TagBuilder getOrCreateRawBuilder(TagKey<T> pTag) {
      return this.builders.computeIfAbsent(pTag.location(), (p_236442_) -> {
         return TagBuilder.create();
      });
   }

   public CompletableFuture<TagsProvider.TagLookup<T>> contentsGetter() {
      return this.contentsDone.thenApply((p_276016_) -> {
         return (p_274772_) -> {
            return Optional.ofNullable(this.builders.get(p_274772_.location()));
         };
      });
   }

   protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
      return this.lookupProvider.thenApply((p_274768_) -> {
         this.builders.clear();
         this.addTags(p_274768_);
         return p_274768_;
      });
   }

   protected static class TagAppender<T> {
      private final TagBuilder builder;

      protected TagAppender(TagBuilder pBuilder) {
         this.builder = pBuilder;
      }

      public final TagsProvider.TagAppender<T> add(ResourceKey<T> pKey) {
         this.builder.addElement(pKey.location());
         return this;
      }

      @SafeVarargs
      public final TagsProvider.TagAppender<T> add(ResourceKey<T>... pToAdd) {
         for(ResourceKey<T> resourcekey : pToAdd) {
            this.builder.addElement(resourcekey.location());
         }

         return this;
      }

      public TagsProvider.TagAppender<T> addOptional(ResourceLocation pLocation) {
         this.builder.addOptionalElement(pLocation);
         return this;
      }

      public TagsProvider.TagAppender<T> addTag(TagKey<T> pTag) {
         this.builder.addTag(pTag.location());
         return this;
      }

      public TagsProvider.TagAppender<T> addOptionalTag(ResourceLocation pLocation) {
         this.builder.addOptionalTag(pLocation);
         return this;
      }
   }

   @FunctionalInterface
   public interface TagLookup<T> extends Function<TagKey<T>, Optional<TagBuilder>> {
      static <T> TagsProvider.TagLookup<T> empty() {
         return (p_275247_) -> {
            return Optional.empty();
         };
      }

      default boolean contains(TagKey<T> pKey) {
         return this.apply(pKey).isPresent();
      }
   }
}