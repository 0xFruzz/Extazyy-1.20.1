package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.optifine.Config;
import net.optifine.EmissiveTextures;
import net.optifine.SmartAnimations;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.ITextureFormat;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersTex;
import net.optifine.shaders.ShadersTextureType;
import net.optifine.texture.ColorBlenderLinear;
import net.optifine.texture.IColorBlender;
import net.optifine.util.CounterInt;
import net.optifine.util.TextureUtils;
import org.slf4j.Logger;

public class TextureAtlas extends AbstractTexture implements Dumpable, Tickable {
   private static final Logger LOGGER = LogUtils.getLogger();
   /** @deprecated */
   @Deprecated
   public static final ResourceLocation LOCATION_BLOCKS = InventoryMenu.BLOCK_ATLAS;
   /** @deprecated */
   @Deprecated
   public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
   private List<SpriteContents> sprites = List.of();
   private List<TextureAtlasSprite.Ticker> animatedTextures = List.of();
   private Map<ResourceLocation, TextureAtlasSprite> texturesByName = Map.of();
   private final ResourceLocation location;
   private final int maxSupportedTextureSize;
   private int width;
   private int height;
   private int mipLevel;
   private Map<ResourceLocation, TextureAtlasSprite> mapRegisteredSprites = new LinkedHashMap<>();
   private Map<ResourceLocation, TextureAtlasSprite> mapMissingSprites = new LinkedHashMap<>();
   private TextureAtlasSprite[] iconGrid = null;
   private int iconGridSize = -1;
   private int iconGridCountX = -1;
   private int iconGridCountY = -1;
   private double iconGridSizeU = -1.0D;
   private double iconGridSizeV = -1.0D;
   private CounterInt counterIndexInMap = new CounterInt(0);
   public int atlasWidth = 0;
   public int atlasHeight = 0;
   public int mipmapLevel = 0;
   private int countAnimationsActive;
   private int frameCountAnimations;
   private boolean terrain;
   private boolean shaders;
   private boolean multiTexture;
   private ITextureFormat textureFormat;

   public TextureAtlas(ResourceLocation pLocation) {
      this.location = pLocation;
      this.maxSupportedTextureSize = RenderSystem.maxSupportedTextureSize();
      this.terrain = pLocation.equals(LOCATION_BLOCKS);
      this.shaders = Config.isShaders();
      this.multiTexture = Config.isMultiTexture();
      if (this.terrain) {
         Config.setTextureMap(this);
      }

   }

   public void load(ResourceManager pResourceManager) {
   }

   public void upload(SpriteLoader.Preparations pPreparations) {
      LOGGER.info("Created: {}x{}x{} {}-atlas", pPreparations.width(), pPreparations.height(), pPreparations.mipLevel(), this.location);
      TextureUtil.prepareImage(this.getId(), pPreparations.mipLevel(), pPreparations.width(), pPreparations.height());
      this.width = pPreparations.width();
      this.height = pPreparations.height();
      this.mipLevel = pPreparations.mipLevel();
      this.atlasWidth = pPreparations.width();
      this.atlasHeight = pPreparations.height();
      this.mipmapLevel = pPreparations.mipLevel();
      if (this.shaders) {
         ShadersTex.allocateTextureMapNS(this.mipmapLevel, this.atlasWidth, this.atlasHeight, this);
      }

      this.clearTextureData();
      this.texturesByName = Map.copyOf(pPreparations.regions());
      List<SpriteContents> list = new ArrayList<>();
      List<TextureAtlasSprite.Ticker> list1 = new ArrayList<>();

      for(TextureAtlasSprite textureatlassprite : pPreparations.regions().values()) {
         list.add(textureatlassprite.contents());
         textureatlassprite.setTextureAtlas(this);

         try {
            textureatlassprite.uploadFirstFrame();
         } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Stitching texture atlas");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Texture being stitched together");
            crashreportcategory.setDetail("Atlas path", this.location);
            crashreportcategory.setDetail("Sprite", textureatlassprite);
            throw new ReportedException(crashreport);
         }

         TextureAtlasSprite.Ticker textureatlassprite$ticker = textureatlassprite.createTicker();
         if (textureatlassprite$ticker != null) {
            textureatlassprite.setTicker(textureatlassprite$ticker);
            textureatlassprite.setAnimationIndex(list1.size());
            list1.add(textureatlassprite$ticker);
         }
      }

      this.sprites = List.copyOf(list);
      this.animatedTextures = List.copyOf(list1);
      TextureUtils.refreshCustomSprites(this);
      Config.log("Animated sprites: " + this.animatedTextures.size());
      if (Config.isMultiTexture()) {
         for(TextureAtlasSprite textureatlassprite1 : pPreparations.regions().values()) {
            uploadMipmapsSingle(textureatlassprite1);
            if (textureatlassprite1.spriteNormal != null) {
               uploadMipmapsSingle(textureatlassprite1.spriteNormal);
            }

            if (textureatlassprite1.spriteSpecular != null) {
               uploadMipmapsSingle(textureatlassprite1.spriteSpecular);
            }
         }

         GlStateManager._bindTexture(this.getId());
      }

      if (Config.isShaders()) {
         // TODO: just > Collection
         Collection<TextureAtlasSprite> collection = pPreparations.regions().values();
         if (Shaders.configNormalMap) {
            GlStateManager._bindTexture(this.getMultiTexID().norm);

            for(TextureAtlasSprite textureatlassprite2 : collection) {
               TextureAtlasSprite textureatlassprite4 = textureatlassprite2.spriteNormal;
               if (textureatlassprite4 != null) {
                  textureatlassprite4.uploadFirstFrame();
                  TextureAtlasSprite.Ticker textureatlassprite$ticker1 = textureatlassprite4.createTicker();
                  if (textureatlassprite$ticker1 != null) {
                     textureatlassprite4.setTicker(textureatlassprite$ticker1);
                  }
               }
            }
         }

         if (Shaders.configSpecularMap) {
            GlStateManager._bindTexture(this.getMultiTexID().spec);

            for(TextureAtlasSprite textureatlassprite3 : collection) {
               TextureAtlasSprite textureatlassprite5 = textureatlassprite3.spriteSpecular;
               if (textureatlassprite5 != null) {
                  textureatlassprite5.uploadFirstFrame();
                  TextureAtlasSprite.Ticker textureatlassprite$ticker2 = textureatlassprite5.createTicker();
                  if (textureatlassprite$ticker2 != null) {
                     textureatlassprite5.setTicker(textureatlassprite$ticker2);
                  }
               }
            }
         }

         GlStateManager._bindTexture(this.getId());
      }

      Reflector.callVoid(Reflector.ForgeHooksClient_onTextureStitchedPost, this);
      this.updateIconGrid(this.atlasWidth, this.atlasHeight);
      if (Config.equals(System.getProperty("saveTextureMap"), "true")) {
         Config.dbg("Exporting texture map: " + this.location);
         TextureUtils.saveGlTexture("debug/" + this.location.getPath().replaceAll("/", "_"), this.getId(), this.mipmapLevel, this.atlasWidth, this.atlasHeight);
         if (this.shaders) {
            if (Shaders.configNormalMap) {
               TextureUtils.saveGlTexture("debug/" + this.location.getPath().replaceAll("/", "_").replace(".png", "_n.png"), this.multiTex.norm, this.mipmapLevel, this.atlasWidth, this.atlasHeight);
            }

            if (Shaders.configSpecularMap) {
               TextureUtils.saveGlTexture("debug/" + this.location.getPath().replaceAll("/", "_").replace(".png", "_s.png"), this.multiTex.spec, this.mipmapLevel, this.atlasWidth, this.atlasHeight);
            }

            GlStateManager._bindTexture(this.getId());
         }
      }

   }

   //TODO: Maybe error, changed set to set2
   public void preStitch(Set<ResourceLocation> set, ResourceManager resourceManagerIn, int mipmapLevelIn) {
      this.terrain = this.location.equals(LOCATION_BLOCKS);
      this.shaders = Config.isShaders();
      this.multiTexture = Config.isMultiTexture();
      this.mipmapLevel = mipmapLevelIn;
      Config.dbg("Pre-stitch: " + this.location);
      this.textureFormat = ITextureFormat.readConfiguration();
      this.mapRegisteredSprites.clear();
      this.mapMissingSprites.clear();
      this.counterIndexInMap.reset();
      Config.dbg("Multitexture: " + Config.isMultiTexture());
      TextureUtils.registerCustomSpriteLocations(this.location(), set);
      TextureUtils.registerCustomSprites(this);
      set.addAll(this.mapRegisteredSprites.keySet());
      Set<ResourceLocation> set2 = newHashSet(set, this.mapRegisteredSprites.keySet());
      EmissiveTextures.updateIcons(this, set2);
      set2.addAll(this.mapRegisteredSprites.keySet());
      if (this.mipmapLevel >= 4) {
         this.mipmapLevel = this.detectMaxMipmapLevel(set2, resourceManagerIn);
         Config.log("Mipmap levels: " + this.mipmapLevel);
      }

      int i = getMinSpriteSize(this.mipmapLevel);
      this.iconGridSize = i;
   }

   public void dumpContents(ResourceLocation pResourceLocation, Path pPath) throws IOException {
      String s = pResourceLocation.toDebugFileName();
      TextureUtil.writeAsPNG(pPath, s, this.getId(), this.mipLevel, this.width, this.height);
      dumpSpriteNames(pPath, s, this.texturesByName);
   }

   private static void dumpSpriteNames(Path pOutputDir, String pOutputFilename, Map<ResourceLocation, TextureAtlasSprite> p_261722_) {
      Path path = pOutputDir.resolve(pOutputFilename + ".txt");

      try (Writer writer = Files.newBufferedWriter(path)) {
         for(Map.Entry<ResourceLocation, TextureAtlasSprite> entry : p_261722_.entrySet().stream().sorted(Entry.comparingByKey()).toList()) {
            TextureAtlasSprite textureatlassprite = entry.getValue();
            writer.write(String.format(Locale.ROOT, "%s\tx=%d\ty=%d\tw=%d\th=%d%n", entry.getKey(), textureatlassprite.getX(), textureatlassprite.getY(), textureatlassprite.contents().width(), textureatlassprite.contents().height()));
         }
      } catch (IOException ioexception1) {
         LOGGER.warn("Failed to write file {}", path, ioexception1);
      }

   }

   public void cycleAnimationFrames() {
      boolean flag = false;
      boolean flag1 = false;
      if (!this.animatedTextures.isEmpty()) {
         this.bind();
      }

      int i = 0;

      for(TextureAtlasSprite.Ticker textureatlassprite$ticker : this.animatedTextures) {
         TextureAtlasSprite textureatlassprite = textureatlassprite$ticker.getSprite();
         if (textureatlassprite != null) {
            if (this.isAnimationEnabled(textureatlassprite)) {
               textureatlassprite$ticker.tickAndUpload();
               if (textureatlassprite.isAnimationActive()) {
                  ++i;
               }

               if (textureatlassprite.spriteNormal != null) {
                  flag = true;
               }

               if (textureatlassprite.spriteSpecular != null) {
                  flag1 = true;
               }
            }
         } else {
            textureatlassprite$ticker.tickAndUpload();
         }
      }

      if (Config.isShaders()) {
         if (flag) {
            GlStateManager._bindTexture(this.getMultiTexID().norm);

            for(TextureAtlasSprite.Ticker textureatlassprite$ticker1 : this.animatedTextures) {
               TextureAtlasSprite textureatlassprite1 = textureatlassprite$ticker1.getSprite();
               if (textureatlassprite1 != null && textureatlassprite1.spriteNormal != null && this.isAnimationEnabled(textureatlassprite1) && textureatlassprite1.isAnimationActive()) {
                  textureatlassprite1.spriteNormal.updateAnimation();
                  if (textureatlassprite1.spriteNormal.isAnimationActive()) {
                     ++i;
                  }
               }
            }
         }

         if (flag1) {
            GlStateManager._bindTexture(this.getMultiTexID().spec);

            for(TextureAtlasSprite.Ticker textureatlassprite$ticker2 : this.animatedTextures) {
               TextureAtlasSprite textureatlassprite2 = textureatlassprite$ticker2.getSprite();
               if (textureatlassprite2 != null && textureatlassprite2.spriteSpecular != null && this.isAnimationEnabled(textureatlassprite2) && textureatlassprite2.isAnimationActive()) {
                  textureatlassprite2.spriteSpecular.updateAnimation();
                  if (textureatlassprite2.spriteSpecular.isAnimationActive()) {
                     ++i;
                  }
               }
            }
         }

         if (flag || flag1) {
            GlStateManager._bindTexture(this.getId());
         }
      }

      if (Config.isMultiTexture()) {
         for(TextureAtlasSprite.Ticker textureatlassprite$ticker3 : this.animatedTextures) {
            TextureAtlasSprite textureatlassprite3 = textureatlassprite$ticker3.getSprite();
            if (textureatlassprite3 != null && this.isAnimationEnabled(textureatlassprite3) && textureatlassprite3.isAnimationActive()) {
               i += updateAnimationSingle(textureatlassprite3);
               if (textureatlassprite3.spriteNormal != null) {
                  i += updateAnimationSingle(textureatlassprite3.spriteNormal);
               }

               if (textureatlassprite3.spriteSpecular != null) {
                  i += updateAnimationSingle(textureatlassprite3.spriteSpecular);
               }
            }
         }

         GlStateManager._bindTexture(this.getId());
      }

      if (this.terrain) {
         int j = Config.getMinecraft().levelRenderer.getFrameCount();
         if (j != this.frameCountAnimations) {
            this.countAnimationsActive = i;
            this.frameCountAnimations = j;
         }

         if (SmartAnimations.isActive()) {
            SmartAnimations.resetSpritesRendered(this);
         }
      }

   }

   public void tick() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::cycleAnimationFrames);
      } else {
         this.cycleAnimationFrames();
      }

   }

   public TextureAtlasSprite getSprite(ResourceLocation pName) {
      TextureAtlasSprite textureatlassprite = this.texturesByName.get(pName);
      return textureatlassprite == null ? this.texturesByName.get(MissingTextureAtlasSprite.getLocation()) : textureatlassprite;
   }

   public void clearTextureData() {
      if (this.multiTexture) {
         for(TextureAtlasSprite textureatlassprite : this.texturesByName.values()) {
            textureatlassprite.deleteSpriteTexture();
            if (textureatlassprite.spriteNormal != null) {
               textureatlassprite.spriteNormal.deleteSpriteTexture();
            }

            if (textureatlassprite.spriteSpecular != null) {
               textureatlassprite.spriteSpecular.deleteSpriteTexture();
            }
         }
      }

      this.sprites.forEach(SpriteContents::close);
      this.animatedTextures.forEach(TextureAtlasSprite.Ticker::close);
      this.sprites = List.of();
      this.animatedTextures = List.of();
      this.texturesByName = Map.of();
   }

   public ResourceLocation location() {
      return this.location;
   }

   public int maxSupportedTextureSize() {
      return this.maxSupportedTextureSize;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public void updateFilter(SpriteLoader.Preparations pPreparations) {
      this.setFilter(false, pPreparations.mipLevel() > 0);
   }

   public static boolean isAbsoluteLocation(ResourceLocation loc) {
      String s = loc.getPath();
      return isAbsoluteLocationPath(s);
   }

   private static boolean isAbsoluteLocationPath(String resPath) {
      String s = resPath.toLowerCase();
      return s.startsWith("optifine/");
   }

   public TextureAtlasSprite getRegisteredSprite(String name) {
      ResourceLocation resourcelocation = new ResourceLocation(name);
      return this.getRegisteredSprite(resourcelocation);
   }

   public TextureAtlasSprite getRegisteredSprite(ResourceLocation loc) {
      return this.mapRegisteredSprites.get(loc);
   }

   public TextureAtlasSprite getUploadedSprite(String name) {
      ResourceLocation resourcelocation = new ResourceLocation(name);
      return this.getUploadedSprite(resourcelocation);
   }

   public TextureAtlasSprite getUploadedSprite(ResourceLocation loc) {
      return this.texturesByName.get(loc);
   }

   private boolean isAnimationEnabled(TextureAtlasSprite ts) {
      if (!this.terrain) {
         return true;
      } else if (ts != TextureUtils.iconWaterStill && ts != TextureUtils.iconWaterFlow) {
         if (ts != TextureUtils.iconLavaStill && ts != TextureUtils.iconLavaFlow) {
            if (ts != TextureUtils.iconFireLayer0 && ts != TextureUtils.iconFireLayer1) {
               if (ts != TextureUtils.iconSoulFireLayer0 && ts != TextureUtils.iconSoulFireLayer1) {
                  if (ts != TextureUtils.iconCampFire && ts != TextureUtils.iconCampFireLogLit) {
                     if (ts != TextureUtils.iconSoulCampFire && ts != TextureUtils.iconSoulCampFireLogLit) {
                        return ts == TextureUtils.iconPortal ? Config.isAnimatedPortal() : Config.isAnimatedTerrain();
                     } else {
                        return Config.isAnimatedFire();
                     }
                  } else {
                     return Config.isAnimatedFire();
                  }
               } else {
                  return Config.isAnimatedFire();
               }
            } else {
               return Config.isAnimatedFire();
            }
         } else {
            return Config.isAnimatedLava();
         }
      } else {
         return Config.isAnimatedWater();
      }
   }

   private static void uploadMipmapsSingle(TextureAtlasSprite tas) {
      TextureAtlasSprite textureatlassprite = tas.spriteSingle;
      if (textureatlassprite != null) {
         textureatlassprite.setAnimationIndex(tas.getAnimationIndex());
         TextureAtlasSprite.Ticker textureatlassprite$ticker = textureatlassprite.createTicker();
         if (textureatlassprite$ticker != null) {
            textureatlassprite.setTicker(textureatlassprite$ticker);
         }

         tas.bindSpriteTexture();

         try {
            textureatlassprite.uploadFirstFrame();
         } catch (Exception exception) {
            Config.dbg("Error uploading sprite single: " + textureatlassprite + ", parent: " + tas);
            exception.printStackTrace();
         }
      }

   }

   private static int updateAnimationSingle(TextureAtlasSprite tas) {
      TextureAtlasSprite textureatlassprite = tas.spriteSingle;
      if (textureatlassprite != null) {
         tas.bindSpriteTexture();
         NativeImage.setUpdateBlurMipmap(false);
         textureatlassprite.updateAnimation();
         NativeImage.setUpdateBlurMipmap(true);
         if (textureatlassprite.isAnimationActive()) {
            return 1;
         }
      }

      return 0;
   }

   public int getCountRegisteredSprites() {
      return this.counterIndexInMap.getValue();
   }

   private int detectMaxMipmapLevel(Set<ResourceLocation> setSpriteLocations, ResourceManager rm) {
      int i = this.detectMinimumSpriteSize(setSpriteLocations, rm, 20);
      if (i < 16) {
         i = 16;
      }

      i = Mth.smallestEncompassingPowerOfTwo(i);
      if (i > 16) {
         Config.log("Sprite size: " + i);
      }

      int j = Mth.log2(i);
      if (j < 4) {
         j = 4;
      }

      return j;
   }

   private int detectMinimumSpriteSize(Set<ResourceLocation> setSpriteLocations, ResourceManager rm, int percentScale) {
      Map map = new HashMap();

      for(ResourceLocation resourcelocation : setSpriteLocations) {
         ResourceLocation resourcelocation1 = this.getSpritePath(resourcelocation);

         try {
            Resource resource = rm.getResourceOrThrow(resourcelocation1);
            if (resource != null) {
               InputStream inputstream = resource.open();
               if (inputstream != null) {
                  Dimension dimension = TextureUtils.getImageSize(inputstream, "png");
                  inputstream.close();
                  if (dimension != null) {
                     int i = dimension.width;
                     int j = Mth.smallestEncompassingPowerOfTwo(i);
                     if (!map.containsKey(j)) {
                        map.put(j, 1);
                     } else {
                        int k = (int) map.get(j);
                        map.put(j, k + 1);
                     }
                  }
               }
            }
         } catch (Exception exception) {
         }
      }

      int l = 0;
      Set set = map.keySet();
      Set set1 = new TreeSet(set);

      for(int j1 : (Set<Integer>)(Set<?>) set1) {
         int l1 = (int) map.get(j1);
         l += l1;
      }

      int i1 = 16;
      int k1 = 0;
      int i2 = l * percentScale / 100;

      for(int j2 : (Set<Integer>)(Set<?>) set1) {
         int k2 = (int) map.get(j2);
         k1 += k2;
         if (j2 > i1) {
            i1 = j2;
         }

         if (k1 > i2) {
            return i1;
         }
      }

      return i1;
   }

   private static int getMinSpriteSize(int mipmapLevels) {
      int i = 1 << mipmapLevels;
      if (i < 8) {
         i = 8;
      }

      return i;
   }

   private static FrameSize fixSpriteSize(FrameSize info, int minSpriteSize) {
      if (info.width() >= minSpriteSize && info.height() >= minSpriteSize) {
         return info;
      } else {
         int i = Math.max(info.width(), minSpriteSize);
         int j = Math.max(info.height(), minSpriteSize);
         return new FrameSize(i, j);
      }
   }

   public boolean isTextureBound() {
      int i = GlStateManager.getBoundTexture();
      int j = this.getId();
      return i == j;
   }

   private void updateIconGrid(int sheetWidth, int sheetHeight) {
      this.iconGridCountX = -1;
      this.iconGridCountY = -1;
      this.iconGrid = null;
      if (this.iconGridSize > 0) {
         this.iconGridCountX = sheetWidth / this.iconGridSize;
         this.iconGridCountY = sheetHeight / this.iconGridSize;
         this.iconGrid = new TextureAtlasSprite[this.iconGridCountX * this.iconGridCountY];
         this.iconGridSizeU = 1.0D / (double)this.iconGridCountX;
         this.iconGridSizeV = 1.0D / (double)this.iconGridCountY;

         for(TextureAtlasSprite textureatlassprite : this.texturesByName.values()) {
            double d0 = 0.5D / (double)sheetWidth;
            double d1 = 0.5D / (double)sheetHeight;
            double d2 = (double)Math.min(textureatlassprite.getU0(), textureatlassprite.getU1()) + d0;
            double d3 = (double)Math.min(textureatlassprite.getV0(), textureatlassprite.getV1()) + d1;
            double d4 = (double)Math.max(textureatlassprite.getU0(), textureatlassprite.getU1()) - d0;
            double d5 = (double)Math.max(textureatlassprite.getV0(), textureatlassprite.getV1()) - d1;
            int i = (int)(d2 / this.iconGridSizeU);
            int j = (int)(d3 / this.iconGridSizeV);
            int k = (int)(d4 / this.iconGridSizeU);
            int l = (int)(d5 / this.iconGridSizeV);

            for(int i1 = i; i1 <= k; ++i1) {
               if (i1 >= 0 && i1 < this.iconGridCountX) {
                  for(int j1 = j; j1 <= l; ++j1) {
                     if (j1 >= 0 && j1 < this.iconGridCountX) {
                        int k1 = j1 * this.iconGridCountX + i1;
                        this.iconGrid[k1] = textureatlassprite;
                     } else {
                        Config.warn("Invalid grid V: " + j1 + ", icon: " + textureatlassprite.getName());
                     }
                  }
               } else {
                  Config.warn("Invalid grid U: " + i1 + ", icon: " + textureatlassprite.getName());
               }
            }
         }

      }
   }

   public TextureAtlasSprite getIconByUV(double u, double v) {
      if (this.iconGrid == null) {
         return null;
      } else {
         int i = (int)(u / this.iconGridSizeU);
         int j = (int)(v / this.iconGridSizeV);
         int k = j * this.iconGridCountX + i;
         return k >= 0 && k <= this.iconGrid.length ? this.iconGrid[k] : null;
      }
   }

   public int getCountAnimations() {
      return this.animatedTextures.size();
   }

   public int getCountAnimationsActive() {
      return this.countAnimationsActive;
   }

   public int getIconGridSize() {
      return this.iconGridSize;
   }

   public TextureAtlasSprite registerSprite(ResourceLocation location) {
      if (location == null) {
         throw new IllegalArgumentException("Location cannot be null!");
      } else {
         TextureAtlasSprite textureatlassprite = this.mapRegisteredSprites.get(location);
         if (textureatlassprite != null) {
            return textureatlassprite;
         } else {
            textureatlassprite = new TextureAtlasSprite(this.location, location);
            textureatlassprite.setTextureAtlas(this);
            this.mapRegisteredSprites.put(location, textureatlassprite);
            textureatlassprite.updateIndexInMap(this.counterIndexInMap);
            return textureatlassprite;
         }
      }
   }

   public Collection<TextureAtlasSprite> getRegisteredSprites() {
      return Collections.unmodifiableCollection(this.mapRegisteredSprites.values());
   }

   public Collection<ResourceLocation> getRegisteredSpriteNames() {
      return Collections.unmodifiableCollection(this.mapRegisteredSprites.keySet());
   }

   public boolean isTerrain() {
      return this.terrain;
   }

   public CounterInt getCounterIndexInMap() {
      return this.counterIndexInMap;
   }

   private void onSpriteMissing(ResourceLocation loc) {
      TextureAtlasSprite textureatlassprite = this.mapRegisteredSprites.get(loc);
      if (textureatlassprite != null) {
         this.mapMissingSprites.put(loc, textureatlassprite);
      }
   }

   private static <T> Set<T> newHashSet(Set<T> set1, Set<T> set2) {
      Set<T> set = new HashSet<>();
      set.addAll(set1);
      set.addAll(set2);
      return set;
   }

   public int getMipmapLevel() {
      return this.mipmapLevel;
   }

   public boolean isMipmaps() {
      return this.mipmapLevel > 0;
   }

   public ITextureFormat getTextureFormat() {
      return this.textureFormat;
   }

   public IColorBlender getShadersColorBlender(ShadersTextureType typeIn) {
      if (typeIn == null) {
         return null;
      } else {
         return (IColorBlender)(this.textureFormat != null ? this.textureFormat.getColorBlender(typeIn) : new ColorBlenderLinear());
      }
   }

   public boolean isTextureBlend(ShadersTextureType typeIn) {
      if (typeIn == null) {
         return true;
      } else {
         return this.textureFormat != null ? this.textureFormat.isTextureBlend(typeIn) : true;
      }
   }

   public boolean isNormalBlend() {
      return this.isTextureBlend(ShadersTextureType.NORMAL);
   }

   public boolean isSpecularBlend() {
      return this.isTextureBlend(ShadersTextureType.SPECULAR);
   }

   public ResourceLocation getSpritePath(ResourceLocation location) {
      return isAbsoluteLocation(location) ? new ResourceLocation(location.getNamespace(), location.getPath() + ".png") : new ResourceLocation(location.getNamespace(), String.format(Locale.ROOT, "textures/%s%s", location.getPath(), ".png"));
   }

   public String toString() {
      return "" + this.location;
   }

   public Set<ResourceLocation> getTextureLocations() {
      return Collections.unmodifiableSet(this.texturesByName.keySet());
   }
}
