package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.optifine.shaders.ShadersTextureType;
import net.optifine.texture.SpriteSourceCollector;
import net.optifine.util.StrUtils;
import org.slf4j.Logger;

public class SpriteResourceLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final FileToIdConverter ATLAS_INFO_CONVERTER = new FileToIdConverter("atlases", ".json");
   private final List<SpriteSource> sources;

   private SpriteResourceLoader(List<SpriteSource> pSources) {
      this.sources = pSources;
   }

   public List<Supplier<SpriteContents>> list(ResourceManager pResourceManager) {
      final Map<ResourceLocation, SpriteSource.SpriteSupplier> map = new HashMap<>();
      SpriteSource.Output spritesource$output = new SpriteSource.Output() {
         public void add(ResourceLocation p_262067_, SpriteSource.SpriteSupplier p_261936_) {
            SpriteSource.SpriteSupplier spritesource$spritesupplier = map.put(p_262067_, p_261936_);
            if (spritesource$spritesupplier != null) {
               spritesource$spritesupplier.discard();
            }

         }

         public void removeAll(Predicate<ResourceLocation> p_261939_) {
            Iterator<Map.Entry<ResourceLocation, SpriteSource.SpriteSupplier>> iterator = map.entrySet().iterator();

            while(iterator.hasNext()) {
               Map.Entry<ResourceLocation, SpriteSource.SpriteSupplier> entry = iterator.next();
               if (p_261939_.test(entry.getKey())) {
                  entry.getValue().discard();
                  iterator.remove();
               }
            }

         }
      };
      this.sources.forEach((sourceIn) -> {
         sourceIn.run(pResourceManager, spritesource$output);
      });
      this.filterSpriteNames(map.keySet());
      ImmutableList.Builder<Supplier<SpriteContents>> builder = ImmutableList.builder();
      builder.add(MissingTextureAtlasSprite::create);
      builder.addAll(map.values());
      return builder.build();
   }

   public static SpriteResourceLoader load(ResourceManager pResourceManager, ResourceLocation pLocation) {
      ResourceLocation resourcelocation = ATLAS_INFO_CONVERTER.idToFile(pLocation);
      List<SpriteSource> list = new ArrayList<>();

      for(Resource resource : pResourceManager.getResourceStack(resourcelocation)) {
         try (BufferedReader bufferedreader = resource.openAsReader()) {
            Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, JsonParser.parseReader(bufferedreader));
            list.addAll(SpriteSources.FILE_CODEC.parse(dynamic).getOrThrow(false, LOGGER::error));
         } catch (Exception exception1) {
            LOGGER.warn("Failed to parse atlas definition {} in pack {}", resourcelocation, resource.sourcePackId(), exception1);
         }
      }

      return new SpriteResourceLoader(list);
   }

   public void addSpriteSources(Collection<ResourceLocation> spriteNames) {
      for(ResourceLocation resourcelocation : spriteNames) {
         this.sources.add(new SingleFile(resourcelocation, Optional.empty()));
      }

   }

   public List<SpriteSource> getSpriteSources() {
      return this.sources;
   }

   public Set<ResourceLocation> getSpriteNames(ResourceManager resourceManager) {
      Set<ResourceLocation> set = new LinkedHashSet<>();

      for(SpriteSource spritesource : this.sources) {
         SpriteSource.Output spritesource$output = new SpriteSourceCollector(set);
         spritesource.run(resourceManager, spritesource$output);
      }

      return set;
   }

   public void filterSpriteNames(Set<ResourceLocation> spriteNames) {
      String s = ShadersTextureType.NORMAL.getSuffix();
      String s1 = ShadersTextureType.SPECULAR.getSuffix();
      String[] astring = new String[]{s, s1};
      Iterator iterator = spriteNames.iterator();

      while(iterator.hasNext()) {
         ResourceLocation resourcelocation = (ResourceLocation)iterator.next();
         String s2 = resourcelocation.getPath();
         if (s2.endsWith(s) || s2.endsWith(s1)) {
            String s3 = StrUtils.removeSuffix(s2, astring);
            ResourceLocation resourcelocation1 = new ResourceLocation(resourcelocation.getNamespace(), s3);
            if (spriteNames.contains(resourcelocation1)) {
               iterator.remove();
            }
         }
      }

   }
}