package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class FontSet implements AutoCloseable {
   private static final RandomSource RANDOM = RandomSource.create();
   private static final float LARGE_FORWARD_ADVANCE = 32.0F;
   private final TextureManager textureManager;
   private final ResourceLocation name;
   private BakedGlyph missingGlyph;
   private BakedGlyph whiteGlyph;
   private final List<GlyphProvider> providers = Lists.newArrayList();
   private final CodepointMap<BakedGlyph> glyphs = new CodepointMap<>((p_283973_0_) -> {
      return new BakedGlyph[p_283973_0_];
   }, (p_283972_0_) -> {
      return new BakedGlyph[p_283972_0_][];
   });
   private final CodepointMap<FontSet.GlyphInfoFilter> glyphInfos = new CodepointMap<>((p_283974_0_) -> {
      return new FontSet.GlyphInfoFilter[p_283974_0_];
   }, (p_283975_0_) -> {
      return new FontSet.GlyphInfoFilter[p_283975_0_][];
   });
   private final Int2ObjectMap<IntList> glyphsByWidth = new Int2ObjectOpenHashMap<>();
   private final List<FontTexture> textures = Lists.newArrayList();

   public FontSet(TextureManager pTextureManager, ResourceLocation pName) {
      this.textureManager = pTextureManager;
      this.name = pName;
   }

   public void reload(List<GlyphProvider> pGlyphProviders) {
      this.closeProviders();
      this.closeTextures();
      this.glyphs.clear();
      this.glyphInfos.clear();
      this.glyphsByWidth.clear();
      this.missingGlyph = SpecialGlyphs.MISSING.bake(this::stitch);
      this.whiteGlyph = SpecialGlyphs.WHITE.bake(this::stitch);
      IntSet intset = new IntOpenHashSet();

      for(GlyphProvider glyphprovider : pGlyphProviders) {
         intset.addAll(glyphprovider.getSupportedGlyphs());
      }

      Set<GlyphProvider> set = Sets.newHashSet();
      intset.forEach((p_232558_3_) -> {
         for(GlyphProvider glyphprovider1 : pGlyphProviders) {
            GlyphInfo glyphinfo = glyphprovider1.getGlyph(p_232558_3_);
            if (glyphinfo != null) {
               set.add(glyphprovider1);
               if (glyphinfo != SpecialGlyphs.MISSING) {
                  this.glyphsByWidth.computeIfAbsent(Mth.ceil(glyphinfo.getAdvance(false)), (p_232566_0_) -> {
                     return new IntArrayList();
                  }).add(p_232558_3_);
               }
               break;
            }
         }

      });
      pGlyphProviders.stream().filter(set::contains).forEach(this.providers::add);
   }

   public void close() {
      this.closeProviders();
      this.closeTextures();
   }

   private void closeProviders() {
      for(GlyphProvider glyphprovider : this.providers) {
         glyphprovider.close();
      }

      this.providers.clear();
   }

   private void closeTextures() {
      for(FontTexture fonttexture : this.textures) {
         fonttexture.close();
      }

      this.textures.clear();
   }

   private static boolean hasFishyAdvance(GlyphInfo pGlyph) {
      float f = pGlyph.getAdvance(false);
      if (!(f < 0.0F) && !(f > 32.0F)) {
         float f1 = pGlyph.getAdvance(true);
         return f1 < 0.0F || f1 > 32.0F;
      } else {
         return true;
      }
   }

   private FontSet.GlyphInfoFilter computeGlyphInfo(int p_243321_) {
      GlyphInfo glyphinfo = null;

      for(GlyphProvider glyphprovider : this.providers) {
         GlyphInfo glyphinfo1 = glyphprovider.getGlyph(p_243321_);
         if (glyphinfo1 != null) {
            if (glyphinfo == null) {
               glyphinfo = glyphinfo1;
            }

            if (!hasFishyAdvance(glyphinfo1)) {
               return new FontSet.GlyphInfoFilter(glyphinfo, glyphinfo1);
            }
         }
      }

      return glyphinfo != null ? new FontSet.GlyphInfoFilter(glyphinfo, SpecialGlyphs.MISSING) : FontSet.GlyphInfoFilter.MISSING;
   }

   public GlyphInfo getGlyphInfo(int pCharacter, boolean pFilterFishyGlyphs) {
      FontSet.GlyphInfoFilter fontset$glyphinfofilter = this.glyphInfos.get(pCharacter);
      return fontset$glyphinfofilter != null ? fontset$glyphinfofilter.select(pFilterFishyGlyphs) : this.glyphInfos.computeIfAbsent(pCharacter, this::computeGlyphInfo).select(pFilterFishyGlyphs);
   }

   private BakedGlyph computeBakedGlyph(int p_232565_) {
      for(GlyphProvider glyphprovider : this.providers) {
         GlyphInfo glyphinfo = glyphprovider.getGlyph(p_232565_);
         if (glyphinfo != null) {
            return glyphinfo.bake(this::stitch);
         }
      }

      return this.missingGlyph;
   }

   public BakedGlyph getGlyph(int pCharacter) {
      BakedGlyph bakedglyph = this.glyphs.get(pCharacter);
      return bakedglyph != null ? bakedglyph : this.glyphs.computeIfAbsent(pCharacter, this::computeBakedGlyph);
   }

   private BakedGlyph stitch(SheetGlyphInfo p_232557_) {
      for(FontTexture fonttexture : this.textures) {
         BakedGlyph bakedglyph = fonttexture.add(p_232557_);
         if (bakedglyph != null) {
            return bakedglyph;
         }
      }

      ResourceLocation resourcelocation = this.name.withSuffix("/" + this.textures.size());
      boolean flag = p_232557_.isColored();
      GlyphRenderTypes glyphrendertypes = flag ? GlyphRenderTypes.createForColorTexture(resourcelocation) : GlyphRenderTypes.createForIntensityTexture(resourcelocation);
      FontTexture fonttexture1 = new FontTexture(glyphrendertypes, flag);
      this.textures.add(fonttexture1);
      this.textureManager.register(resourcelocation, fonttexture1);
      BakedGlyph bakedglyph1 = fonttexture1.add(p_232557_);
      return bakedglyph1 == null ? this.missingGlyph : bakedglyph1;
   }

   public BakedGlyph getRandomGlyph(GlyphInfo pGlyph) {
      IntList intlist = this.glyphsByWidth.get(Mth.ceil(pGlyph.getAdvance(false)));
      return intlist != null && !intlist.isEmpty() ? this.getGlyph(intlist.getInt(RANDOM.nextInt(intlist.size()))) : this.missingGlyph;
   }

   public BakedGlyph whiteGlyph() {
      return this.whiteGlyph;
   }

   static record GlyphInfoFilter(GlyphInfo glyphInfo, GlyphInfo glyphInfoNotFishy) {
      static final FontSet.GlyphInfoFilter MISSING = new FontSet.GlyphInfoFilter(SpecialGlyphs.MISSING, SpecialGlyphs.MISSING);

      GlyphInfo select(boolean pFilterFishyGlyphs) {
         return pFilterFishyGlyphs ? this.glyphInfoNotFishy : this.glyphInfo;
      }

      public GlyphInfo glyphInfo() {
         return this.glyphInfo;
      }

      public GlyphInfo glyphInfoNotFishy() {
         return this.glyphInfoNotFishy;
      }
   }
}
