package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public abstract class IntrinsicHolderTagsProvider<T> extends TagsProvider<T> {
   private final Function<T, ResourceKey<T>> keyExtractor;

   public IntrinsicHolderTagsProvider(PackOutput pOutput, ResourceKey<? extends Registry<T>> pRegistryKey, CompletableFuture<HolderLookup.Provider> pLookupProvider, Function<T, ResourceKey<T>> pKeyExtractor) {
      super(pOutput, pRegistryKey, pLookupProvider);
      this.keyExtractor = pKeyExtractor;
   }

   public IntrinsicHolderTagsProvider(PackOutput pOutput, ResourceKey<? extends Registry<T>> pRegistryKey, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagsProvider.TagLookup<T>> pParentProvider, Function<T, ResourceKey<T>> pKeyExtractor) {
      super(pOutput, pRegistryKey, pLookupProvider, pParentProvider);
      this.keyExtractor = pKeyExtractor;
   }

   protected IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> tag(TagKey<T> pTag) {
      TagBuilder tagbuilder = this.getOrCreateRawBuilder(pTag);
      return new IntrinsicHolderTagsProvider.IntrinsicTagAppender<>(tagbuilder, this.keyExtractor);
   }

   protected static class IntrinsicTagAppender<T> extends TagsProvider.TagAppender<T> {
      private final Function<T, ResourceKey<T>> keyExtractor;

      IntrinsicTagAppender(TagBuilder pBuilder, Function<T, ResourceKey<T>> pKeyExtractor) {
         super(pBuilder);
         this.keyExtractor = pKeyExtractor;
      }

      public IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addTag(TagKey<T> pTag) {
         super.addTag(pTag);
         return this;
      }

      public final IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> add(T pValue) {
         this.add(this.keyExtractor.apply(pValue));
         return this;
      }

      @SafeVarargs
      public final IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> add(T... pValues) {
         Stream.<T>of(pValues).map(this.keyExtractor).forEach(this::add);
         return this;
      }
   }
}