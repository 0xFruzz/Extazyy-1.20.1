package net.optifine.texture;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;

public class SpriteSourceCollector implements SpriteSource.Output {
   private Set<ResourceLocation> spriteNames;

   public SpriteSourceCollector(Set<ResourceLocation> spriteNames) {
      this.spriteNames = spriteNames;
   }

   public void add(ResourceLocation locIn, SpriteSource.SpriteSupplier supplierIn) {
      this.spriteNames.add(locIn);
   }

   public void removeAll(Predicate<ResourceLocation> checkIn) {
   }

   public Set<ResourceLocation> getSpriteNames() {
      return this.spriteNames;
   }
}