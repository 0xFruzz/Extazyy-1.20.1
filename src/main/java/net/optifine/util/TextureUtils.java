package net.optifine.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.renderer.entity.layers.MushroomCowMushroomLayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.optifine.BetterGrass;
import net.optifine.BetterSnow;
import net.optifine.Config;
import net.optifine.ConnectedTextures;
import net.optifine.CustomBlockLayers;
import net.optifine.CustomColors;
import net.optifine.CustomGuis;
import net.optifine.CustomItems;
import net.optifine.CustomLoadingScreens;
import net.optifine.CustomPanorama;
import net.optifine.CustomSky;
import net.optifine.EmissiveTextures;
import net.optifine.Lang;
import net.optifine.NaturalTextures;
import net.optifine.RandomEntities;
import net.optifine.SmartLeaves;
import net.optifine.TextureAnimations;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.reflect.ReflectorForge;
import net.optifine.shaders.MultiTexID;
import net.optifine.shaders.Shaders;
import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class TextureUtils {
   private static final String texGrassTop = "grass_block_top";
   private static final String texGrassSide = "grass_block_side";
   private static final String texGrassSideOverlay = "grass_block_side_overlay";
   private static final String texSnow = "snow";
   private static final String texGrassSideSnowed = "grass_block_snow";
   private static final String texMyceliumSide = "mycelium_side";
   private static final String texMyceliumTop = "mycelium_top";
   private static final String texWaterStill = "water_still";
   private static final String texWaterFlow = "water_flow";
   private static final String texLavaStill = "lava_still";
   private static final String texLavaFlow = "lava_flow";
   private static final String texFireLayer0 = "fire_0";
   private static final String texFireLayer1 = "fire_1";
   private static final String texSoulFireLayer0 = "soul_fire_0";
   private static final String texSoulFireLayer1 = "soul_fire_1";
   private static final String texCampFire = "campfire_fire";
   private static final String texCampFireLogLit = "campfire_log_lit";
   private static final String texSoulCampFire = "soul_campfire_fire";
   private static final String texSoulCampFireLogLit = "soul_campfire_log_lit";
   private static final String texPortal = "nether_portal";
   private static final String texGlass = "glass";
   private static final String texGlassPaneTop = "glass_pane_top";
   public static TextureAtlasSprite iconGrassTop;
   public static TextureAtlasSprite iconGrassSide;
   public static TextureAtlasSprite iconGrassSideOverlay;
   public static TextureAtlasSprite iconSnow;
   public static TextureAtlasSprite iconGrassSideSnowed;
   public static TextureAtlasSprite iconMyceliumSide;
   public static TextureAtlasSprite iconMyceliumTop;
   public static TextureAtlasSprite iconWaterStill;
   public static TextureAtlasSprite iconWaterFlow;
   public static TextureAtlasSprite iconLavaStill;
   public static TextureAtlasSprite iconLavaFlow;
   public static TextureAtlasSprite iconFireLayer0;
   public static TextureAtlasSprite iconFireLayer1;
   public static TextureAtlasSprite iconSoulFireLayer0;
   public static TextureAtlasSprite iconSoulFireLayer1;
   public static TextureAtlasSprite iconCampFire;
   public static TextureAtlasSprite iconCampFireLogLit;
   public static TextureAtlasSprite iconSoulCampFire;
   public static TextureAtlasSprite iconSoulCampFireLogLit;
   public static TextureAtlasSprite iconPortal;
   public static TextureAtlasSprite iconGlass;
   public static TextureAtlasSprite iconGlassPaneTop;
   public static final String SPRITE_PREFIX_BLOCKS = "minecraft:block/";
   public static final String SPRITE_PREFIX_ITEMS = "minecraft:item/";
   public static final ResourceLocation LOCATION_SPRITE_EMPTY = new ResourceLocation("optifine/ctm/default/empty");
   public static final ResourceLocation LOCATION_TEXTURE_EMPTY = new ResourceLocation("optifine/ctm/default/empty.png");
   public static final ResourceLocation WHITE_TEXTURE_LOCATION = new ResourceLocation("textures/misc/white.png");
   private static IntBuffer staticBuffer = Config.createDirectIntBuffer(256);
   private static int glMaximumTextureSize = -1;
   private static Map<Integer, String> mapTextureAllocations = new HashMap<>();
   private static Map<ResourceLocation, ResourceLocation> mapSpriteLocations = new HashMap<>();
   private static ResourceLocation LOCATION_ATLAS_PAINTINGS = new ResourceLocation("textures/atlas/paintings.png");

   public static void update() {
      TextureAtlas textureatlas = getTextureMapBlocks();
      if (textureatlas != null) {
         String s = "minecraft:block/";
         iconGrassTop = getSpriteCheck(textureatlas, s + "grass_block_top");
         iconGrassSide = getSpriteCheck(textureatlas, s + "grass_block_side");
         iconGrassSideOverlay = getSpriteCheck(textureatlas, s + "grass_block_side_overlay");
         iconSnow = getSpriteCheck(textureatlas, s + "snow");
         iconGrassSideSnowed = getSpriteCheck(textureatlas, s + "grass_block_snow");
         iconMyceliumSide = getSpriteCheck(textureatlas, s + "mycelium_side");
         iconMyceliumTop = getSpriteCheck(textureatlas, s + "mycelium_top");
         iconWaterStill = getSpriteCheck(textureatlas, s + "water_still");
         iconWaterFlow = getSpriteCheck(textureatlas, s + "water_flow");
         iconLavaStill = getSpriteCheck(textureatlas, s + "lava_still");
         iconLavaFlow = getSpriteCheck(textureatlas, s + "lava_flow");
         iconFireLayer0 = getSpriteCheck(textureatlas, s + "fire_0");
         iconFireLayer1 = getSpriteCheck(textureatlas, s + "fire_1");
         iconSoulFireLayer0 = getSpriteCheck(textureatlas, s + "soul_fire_0");
         iconSoulFireLayer1 = getSpriteCheck(textureatlas, s + "soul_fire_1");
         iconCampFire = getSpriteCheck(textureatlas, s + "campfire_fire");
         iconCampFireLogLit = getSpriteCheck(textureatlas, s + "campfire_log_lit");
         iconSoulCampFire = getSpriteCheck(textureatlas, s + "soul_campfire_fire");
         iconSoulCampFireLogLit = getSpriteCheck(textureatlas, s + "soul_campfire_log_lit");
         iconPortal = getSpriteCheck(textureatlas, s + "nether_portal");
         iconGlass = getSpriteCheck(textureatlas, s + "glass");
         iconGlassPaneTop = getSpriteCheck(textureatlas, s + "glass_pane_top");
         String s1 = "minecraft:item/";
         mapSpriteLocations.clear();
      }
   }

   public static TextureAtlasSprite getSpriteCheck(TextureAtlas textureMap, String name) {
      TextureAtlasSprite textureatlassprite = textureMap.getUploadedSprite(name);
      if (textureatlassprite == null || MissingTextureAtlasSprite.isMisingSprite(textureatlassprite)) {
         Config.warn("Sprite not found: " + name);
      }

      return textureatlassprite;
   }

   public static BufferedImage fixTextureDimensions(String name, BufferedImage bi) {
      if (name.startsWith("/mob/zombie") || name.startsWith("/mob/pigzombie")) {
         int i = bi.getWidth();
         int j = bi.getHeight();
         if (i == j * 2) {
            BufferedImage bufferedimage = new BufferedImage(i, j * 2, 2);
            Graphics2D graphics2d = bufferedimage.createGraphics();
            graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2d.drawImage(bi, 0, 0, i, j, (ImageObserver)null);
            return bufferedimage;
         }
      }

      return bi;
   }

   public static int ceilPowerOfTwo(int val) {
      int i;
      for(i = 1; i < val; i *= 2) {
      }

      return i;
   }

   public static int getPowerOfTwo(int val) {
      int i = 1;

      int j;
      for(j = 0; i < val; ++j) {
         i *= 2;
      }

      return j;
   }

   public static int twoToPower(int power) {
      int i = 1;

      for(int j = 0; j < power; ++j) {
         i *= 2;
      }

      return i;
   }

   public static AbstractTexture getTexture(ResourceLocation loc) {
      AbstractTexture abstracttexture = Config.getTextureManager().getTexture(loc);
      if (abstracttexture != null) {
         return abstracttexture;
      } else if (!Config.hasResource(loc)) {
         return null;
      } else {
         abstracttexture = new SimpleTexture(loc);
         Config.getTextureManager().register(loc, abstracttexture);
         return abstracttexture;
      }
   }

   public static void resourcesPreReload(ResourceManager rm) {
      CustomItems.update();
   }

   public static void resourcesReloaded(ResourceManager rm) {
      if (getTextureMapBlocks() != null) {
         Config.dbg("*** Reloading custom textures ***");
         CustomSky.reset();
         TextureAnimations.reset();
         update();
         NaturalTextures.update();
         BetterGrass.update();
         BetterSnow.update();
         TextureAnimations.update();
         CustomColors.update();
         CustomSky.update();
         CustomItems.updateModels();
         CustomEntityModels.update();
         Shaders.resourcesReloaded();
         Lang.resourcesReloaded();
         Config.updateTexturePackClouds();
         SmartLeaves.updateLeavesModels();
         CustomPanorama.update();
         CustomGuis.update();
         MushroomCowMushroomLayer.update();
         CustomLoadingScreens.update();
         CustomBlockLayers.update();
         Config.getTextureManager().tick();
         Config.dbg("Disable Forge light pipeline");
         ReflectorForge.setForgeLightPipelineEnabled(false);
      }
   }

   public static TextureAtlas getTextureMapBlocks() {
      return Config.getTextureMap();
   }

   public static void registerResourceListener() {
      ResourceManager resourcemanager = Config.getResourceManager();
      if (resourcemanager instanceof ReloadableResourceManager reloadableresourcemanager) {
         SimplePreparableReloadListener simplepreparablereloadlistener = new SimplePreparableReloadListener() {
            protected Object prepare(ResourceManager p_212854_1_, ProfilerFiller p_212854_2_) {
               return null;
            }

            protected void apply(Object p_212853_1_, ResourceManager p_212853_2_, ProfilerFiller p_212853_3_) {
            }
         };
         reloadableresourcemanager.registerReloadListener(simplepreparablereloadlistener);
         ResourceManagerReloadListener resourcemanagerreloadlistener = new ResourceManagerReloadListener() {
            public void onResourceManagerReload(ResourceManager resourceManager) {
               TextureUtils.resourcesReloaded(resourceManager);
            }
         };
         reloadableresourcemanager.registerReloadListener(resourcemanagerreloadlistener);
      }

   }

   public static void registerTickableTextures() {
      TickableTexture tickabletexture = new TickableTexture() {
         public void tick() {
            TextureAnimations.updateAnimations();
         }

         public void load(ResourceManager var1) throws IOException {
         }

         public int getId() {
            return 0;
         }

         public void restoreLastBlurMipmap() {
         }

         public MultiTexID getMultiTexID() {
            return null;
         }
      };
      ResourceLocation resourcelocation = new ResourceLocation("optifine/tickable_textures");
      Config.getTextureManager().register(resourcelocation, tickabletexture);
   }

   public static void registerCustomModels(ModelBakery modelBakery) {
      CustomItems.loadModels(modelBakery);
   }

   public static void registerCustomSprites(TextureAtlas textureMap) {
      if (textureMap.location().equals(TextureAtlas.LOCATION_BLOCKS)) {
         ConnectedTextures.updateIcons(textureMap);
         CustomItems.updateIcons(textureMap);
         BetterGrass.updateIcons(textureMap);
      }

      textureMap.registerSprite(LOCATION_SPRITE_EMPTY);
   }

   public static void registerCustomSpriteLocations(ResourceLocation atlasLocation, Set<ResourceLocation> spriteLocations) {
      RandomEntities.registerSprites(spriteLocations);
   }

   public static void refreshCustomSprites(TextureAtlas textureMap) {
      if (textureMap.location().equals(TextureAtlas.LOCATION_BLOCKS)) {
         ConnectedTextures.refreshIcons(textureMap);
         CustomItems.refreshIcons(textureMap);
         BetterGrass.refreshIcons(textureMap);
      }

      EmissiveTextures.refreshIcons(textureMap);
   }

   public static ResourceLocation fixResourceLocation(ResourceLocation loc, String basePath) {
      if (!loc.getNamespace().equals("minecraft")) {
         return loc;
      } else {
         String s = loc.getPath();
         String s1 = fixResourcePath(s, basePath);
         if (s1 != s) {
            loc = new ResourceLocation(loc.getNamespace(), s1);
         }

         return loc;
      }
   }

   public static String fixResourcePath(String path, String basePath) {
      String s = "assets/minecraft/";
      if (path.startsWith(s)) {
         return path.substring(s.length());
      } else if (path.startsWith("./")) {
         path = path.substring(2);
         if (!basePath.endsWith("/")) {
            basePath = basePath + "/";
         }

         return basePath + path;
      } else {
         if (path.startsWith("/~")) {
            path = path.substring(1);
         }

         String s1 = "optifine/";
         if (path.startsWith("~/")) {
            path = path.substring(2);
            return s1 + path;
         } else {
            return path.startsWith("/") ? s1 + path.substring(1) : path;
         }
      }
   }

   public static String getBasePath(String path) {
      int i = path.lastIndexOf(47);
      return i < 0 ? "" : path.substring(0, i);
   }

   public static void applyAnisotropicLevel() {
      if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
         float f = GL11.glGetFloat(34047);
         float f1 = (float)Config.getAnisotropicFilterLevel();
         f1 = Math.min(f1, f);
         GL11.glTexParameterf(3553, 34046, f1);
      }

   }

   public static void bindTexture(int glTexId) {
      GlStateManager._bindTexture(glTexId);
   }

   public static boolean isPowerOfTwo(int x) {
      int i = Mth.smallestEncompassingPowerOfTwo(x);
      return i == x;
   }

   public static NativeImage scaleImage(NativeImage ni, int w2) {
      BufferedImage bufferedimage = toBufferedImage(ni);
      BufferedImage bufferedimage1 = scaleImage(bufferedimage, w2);
      return toNativeImage(bufferedimage1);
   }

   public static BufferedImage toBufferedImage(NativeImage ni) {
      int i = ni.getWidth();
      int j = ni.getHeight();
      int[] aint = new int[i * j];
      ni.getBufferRGBA().get(aint);
      BufferedImage bufferedimage = new BufferedImage(i, j, 2);
      bufferedimage.setRGB(0, 0, i, j, aint, 0, i);
      return bufferedimage;
   }

   private static NativeImage toNativeImage(BufferedImage bi) {
      int i = bi.getWidth();
      int j = bi.getHeight();
      int[] aint = new int[i * j];
      bi.getRGB(0, 0, i, j, aint, 0, i);
      NativeImage nativeimage = new NativeImage(i, j, false);
      nativeimage.getBufferRGBA().put(aint);
      return nativeimage;
   }

   public static BufferedImage scaleImage(BufferedImage bi, int w2) {
      int i = bi.getWidth();
      int j = bi.getHeight();
      int k = j * w2 / i;
      BufferedImage bufferedimage = new BufferedImage(w2, k, 2);
      Graphics2D graphics2d = bufferedimage.createGraphics();
      Object object = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
      if (w2 < i || w2 % i != 0) {
         object = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
      }

      graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, object);
      graphics2d.drawImage(bi, 0, 0, w2, k, (ImageObserver)null);
      return bufferedimage;
   }

   public static int scaleToGrid(int size, int sizeGrid) {
      if (size == sizeGrid) {
         return size;
      } else {
         int i;
         for(i = size / sizeGrid * sizeGrid; i < size; i += sizeGrid) {
         }

         return i;
      }
   }

   public static int scaleToMin(int size, int sizeMin) {
      if (size >= sizeMin) {
         return size;
      } else {
         int i;
         for(i = sizeMin / size * size; i < sizeMin; i += size) {
         }

         return i;
      }
   }

   public static Dimension getImageSize(InputStream in, String suffix) {
      Iterator iterator = ImageIO.getImageReadersBySuffix(suffix);

      while(true) {
         if (iterator.hasNext()) {
            ImageReader imagereader = (ImageReader)iterator.next();

            Dimension dimension;
            try {
               ImageInputStream imageinputstream = ImageIO.createImageInputStream(in);
               imagereader.setInput(imageinputstream);
               int i = imagereader.getWidth(imagereader.getMinIndex());
               int j = imagereader.getHeight(imagereader.getMinIndex());
               dimension = new Dimension(i, j);
            } catch (IOException ioexception) {
               continue;
            } finally {
               imagereader.dispose();
            }

            return dimension;
         }

         return null;
      }
   }

   public static void dbgMipmaps(TextureAtlasSprite textureatlassprite) {
      NativeImage[] anativeimage = textureatlassprite.getMipmapImages();

      for(int i = 0; i < anativeimage.length; ++i) {
         NativeImage nativeimage = anativeimage[i];
         if (nativeimage == null) {
            Config.dbg(i + ": " + nativeimage);
         } else {
            Config.dbg(i + ": " + nativeimage.getWidth() * nativeimage.getHeight());
         }
      }

   }

   public static void saveGlTexture(String name, int textureId, int mipmapLevels, int width, int height) {
      bindTexture(textureId);
      GL11.glPixelStorei(3333, 1);
      GL11.glPixelStorei(3317, 1);
      name = StrUtils.removeSuffix(name, ".png");
      File file1 = new File(name);
      File file2 = file1.getParentFile();
      if (file2 != null) {
         file2.mkdirs();
      }

      for(int i = 0; i < 16; ++i) {
         String s = name + "_" + i + ".png";
         File file3 = new File(s);
         file3.delete();
      }

      for(int l = 0; l <= mipmapLevels; ++l) {
         File file4 = new File(name + "_" + l + ".png");
         int i1 = width >> l;
         int j = height >> l;
         int k = i1 * j;
         IntBuffer intbuffer = BufferUtils.createIntBuffer(k);
         int[] aint = new int[k];
         GL11.glGetTexImage(3553, l, 32993, 33639, intbuffer);
         intbuffer.get(aint);
         BufferedImage bufferedimage = new BufferedImage(i1, j, 2);
         bufferedimage.setRGB(0, 0, i1, j, aint, 0, i1);

         try {
            ImageIO.write(bufferedimage, "png", file4);
            Config.dbg("Exported: " + file4);
         } catch (Exception exception) {
            Config.warn("Error writing: " + file4);
            Config.warn(exception.getClass().getName() + ": " + exception.getMessage());
         }
      }

   }

   public static int getGLMaximumTextureSize() {
      if (glMaximumTextureSize < 0) {
         glMaximumTextureSize = detectGLMaximumTextureSize();
      }

      return glMaximumTextureSize;
   }

   private static int detectGLMaximumTextureSize() {
      for(int i = 65536; i > 0; i >>= 1) {
         GlStateManager._texImage2D(32868, 0, 6408, i, i, 0, 6408, 5121, (IntBuffer)null);
         int j = GL11.glGetError();
         int k = GlStateManager._getTexLevelParameter(32868, 0, 4096);
         if (k != 0) {
            return i;
         }
      }

      return 0;
   }

   public static BufferedImage readBufferedImage(InputStream imageStream) throws IOException {
      if (imageStream == null) {
         return null;
      } else {
         BufferedImage bufferedimage1;
         try {
            BufferedImage bufferedimage = ImageIO.read(imageStream);
            bufferedimage1 = bufferedimage;
         } finally {
            IOUtils.closeQuietly(imageStream);
         }

         return bufferedimage1;
      }
   }

   public static int toAbgr(int argb) {
      int i = argb >> 24 & 255;
      int j = argb >> 16 & 255;
      int k = argb >> 8 & 255;
      int l = argb >> 0 & 255;
      return i << 24 | l << 16 | k << 8 | j;
   }

   public static void resetDataUnpacking() {
      GlStateManager._pixelStore(3314, 0);
      GlStateManager._pixelStore(3316, 0);
      GlStateManager._pixelStore(3315, 0);
      GlStateManager._pixelStore(3317, 4);
   }

   public static String getStackTrace(Throwable t) {
      CharArrayWriter chararraywriter = new CharArrayWriter();
      t.printStackTrace(new PrintWriter(chararraywriter));
      return chararraywriter.toString();
   }

   public static void debugTextureGenerated(int id) {
      mapTextureAllocations.put(id, getStackTrace(new Throwable("StackTrace")));
      Config.dbg("Textures: " + mapTextureAllocations.size());
   }

   public static void debugTextureDeleted(int id) {
      mapTextureAllocations.remove(id);
      Config.dbg("Textures: " + mapTextureAllocations.size());
   }

   public static TextureAtlasSprite getCustomSprite(TextureAtlasSprite sprite) {
      if (Config.isRandomEntities()) {
         sprite = RandomEntities.getRandomSprite(sprite);
      }

      if (EmissiveTextures.isActive()) {
         sprite = EmissiveTextures.getEmissiveSprite(sprite);
      }

      return sprite;
   }

   public static ResourceLocation getSpriteLocation(ResourceLocation loc) {
      ResourceLocation resourcelocation = mapSpriteLocations.get(loc);
      if (resourcelocation == null) {
         String s = loc.getPath();
         s = StrUtils.removePrefix(s, "textures/");
         s = StrUtils.removeSuffix(s, ".png");
         resourcelocation = new ResourceLocation(loc.getNamespace(), s);
         mapSpriteLocations.put(loc, resourcelocation);
      }

      return resourcelocation;
   }
}