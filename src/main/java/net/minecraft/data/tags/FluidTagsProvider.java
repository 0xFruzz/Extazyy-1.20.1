package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidTagsProvider extends IntrinsicHolderTagsProvider<Fluid> {
   public FluidTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider) {
      super(pOutput, Registries.FLUID, pProvider, (p_256474_) -> {
         return p_256474_.builtInRegistryHolder().key();
      });
   }

   protected void addTags(HolderLookup.Provider pProvider) {
      this.tag(FluidTags.WATER).add(Fluids.WATER, Fluids.FLOWING_WATER);
      this.tag(FluidTags.LAVA).add(Fluids.LAVA, Fluids.FLOWING_LAVA);
   }
}