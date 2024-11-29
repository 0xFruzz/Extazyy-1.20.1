package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.CatVariantTags;
import net.minecraft.world.entity.animal.CatVariant;

public class CatVariantTagsProvider extends TagsProvider<CatVariant> {
   public CatVariantTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider) {
      super(pOutput, Registries.CAT_VARIANT, pProvider);
   }

   protected void addTags(HolderLookup.Provider pProvider) {
      this.tag(CatVariantTags.DEFAULT_SPAWNS).add(CatVariant.TABBY, CatVariant.BLACK, CatVariant.RED, CatVariant.SIAMESE, CatVariant.BRITISH_SHORTHAIR, CatVariant.CALICO, CatVariant.PERSIAN, CatVariant.RAGDOLL, CatVariant.WHITE, CatVariant.JELLIE);
      this.tag(CatVariantTags.FULL_MOON_SPAWNS).addTag(CatVariantTags.DEFAULT_SPAWNS).add(CatVariant.ALL_BLACK);
   }
}