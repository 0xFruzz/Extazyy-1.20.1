package net.optifine;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchBlock;
import net.optifine.render.RenderEnv;
import net.optifine.util.BiomeUtils;
import net.optifine.util.EntityUtils;
import net.optifine.util.PotionUtils;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;
import net.optifine.util.WorldUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.fruzz.extazyy.misc.util.color.ColorUtils;

public class CustomColors {
   private static String paletteFormatDefault = "vanilla";
   private static CustomColormap waterColors = null;
   private static CustomColormap foliagePineColors = null;
   private static CustomColormap foliageBirchColors = null;
   private static CustomColormap swampFoliageColors = null;
   private static CustomColormap swampGrassColors = null;
   private static CustomColormap[] colorsBlockColormaps = null;
   private static CustomColormap[][] blockColormaps = null;
   private static CustomColormap skyColors = null;
   private static CustomColorFader skyColorFader = new CustomColorFader();
   private static CustomColormap fogColors = null;
   private static CustomColorFader fogColorFader = new CustomColorFader();
   private static CustomColormap underwaterColors = null;
   private static CustomColorFader underwaterColorFader = new CustomColorFader();
   private static CustomColormap underlavaColors = null;
   private static CustomColorFader underlavaColorFader = new CustomColorFader();
   private static LightMapPack[] lightMapPacks = null;
   private static int lightmapMinDimensionId = 0;
   private static CustomColormap redstoneColors = null;
   private static CustomColormap xpOrbColors = null;
   private static int xpOrbTime = -1;
   private static CustomColormap durabilityColors = null;
   private static CustomColormap stemColors = null;
   private static CustomColormap stemMelonColors = null;
   private static CustomColormap stemPumpkinColors = null;
   private static CustomColormap lavaDropColors = null;
   private static CustomColormap myceliumParticleColors = null;
   private static boolean useDefaultGrassFoliageColors = true;
   private static int particleWaterColor = -1;
   private static int particlePortalColor = -1;
   private static int lilyPadColor = -1;
   private static int expBarTextColor = -1;
   private static int bossTextColor = -1;
   private static int signTextColor = -1;
   private static Vec3 fogColorNether = null;
   private static Vec3 fogColorEnd = null;
   private static Vec3 skyColorEnd = null;
   private static int[] spawnEggPrimaryColors = null;
   private static int[] spawnEggSecondaryColors = null;
   private static float[][] wolfCollarColors = null;
   private static float[][] sheepColors = null;
   private static int[] textColors = null;
   private static int[] mapColorsOriginal = null;
   private static float[][] dyeColorsOriginal = null;
   private static int[] potionColors = null;
   private static final BlockState BLOCK_STATE_DIRT = Blocks.DIRT.defaultBlockState();
   private static final BlockState BLOCK_STATE_WATER = Blocks.WATER.defaultBlockState();
   public static Random random = new Random();
   private static final CustomColors.IColorizer COLORIZER_GRASS = new CustomColors.IColorizer() {
      public int getColor(BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos) {
         Biome biome = CustomColors.getColorBiome(blockAccess, blockPos);
         return CustomColors.swampGrassColors != null && biome == BiomeUtils.SWAMP ? CustomColors.swampGrassColors.getColor(biome, blockPos) : biome.getGrassColor((double)blockPos.getX(), (double)blockPos.getZ());
      }

      public boolean isColorConstant() {
         return false;
      }
   };
   private static final CustomColors.IColorizer COLORIZER_FOLIAGE = new CustomColors.IColorizer() {
      public int getColor(BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos) {
         Biome biome = CustomColors.getColorBiome(blockAccess, blockPos);
         return CustomColors.swampFoliageColors != null && biome == BiomeUtils.SWAMP ? CustomColors.swampFoliageColors.getColor(biome, blockPos) : biome.getFoliageColor();
      }

      public boolean isColorConstant() {
         return false;
      }
   };
   private static final CustomColors.IColorizer COLORIZER_FOLIAGE_PINE = new CustomColors.IColorizer() {
      public int getColor(BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos) {
         return CustomColors.foliagePineColors != null ? CustomColors.foliagePineColors.getColor(blockAccess, blockPos) : FoliageColor.getEvergreenColor();
      }

      public boolean isColorConstant() {
         return CustomColors.foliagePineColors == null;
      }
   };
   private static final CustomColors.IColorizer COLORIZER_FOLIAGE_BIRCH = new CustomColors.IColorizer() {
      public int getColor(BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos) {
         return CustomColors.foliageBirchColors != null ? CustomColors.foliageBirchColors.getColor(blockAccess, blockPos) : FoliageColor.getBirchColor();
      }

      public boolean isColorConstant() {
         return CustomColors.foliageBirchColors == null;
      }
   };
   private static final CustomColors.IColorizer COLORIZER_WATER = new CustomColors.IColorizer() {
      public int getColor(BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos) {
         Biome biome = CustomColors.getColorBiome(blockAccess, blockPos);
         return CustomColors.waterColors != null ? CustomColors.waterColors.getColor(biome, blockPos) : biome.getWaterColor();
      }

      public boolean isColorConstant() {
         return false;
      }
   };

   public static void update() {
      paletteFormatDefault = "vanilla";
      waterColors = null;
      foliageBirchColors = null;
      foliagePineColors = null;
      swampGrassColors = null;
      swampFoliageColors = null;
      skyColors = null;
      fogColors = null;
      underwaterColors = null;
      underlavaColors = null;
      redstoneColors = null;
      xpOrbColors = null;
      xpOrbTime = -1;
      durabilityColors = null;
      stemColors = null;
      lavaDropColors = null;
      myceliumParticleColors = null;
      lightMapPacks = null;
      particleWaterColor = -1;
      particlePortalColor = -1;
      lilyPadColor = -1;
      expBarTextColor = -1;
      bossTextColor = -1;
      signTextColor = -1;
      fogColorNether = null;
      fogColorEnd = null;
      skyColorEnd = null;
      colorsBlockColormaps = null;
      blockColormaps = null;
      useDefaultGrassFoliageColors = true;
      spawnEggPrimaryColors = null;
      spawnEggSecondaryColors = null;
      wolfCollarColors = null;
      sheepColors = null;
      textColors = null;
      setMapColors(mapColorsOriginal);
      setDyeColors(dyeColorsOriginal);
      potionColors = null;
      paletteFormatDefault = getValidProperty("optifine/color.properties", "palette.format", CustomColormap.FORMAT_STRINGS, "vanilla");
      String s = "optifine/colormap/";
      String[] astring = new String[]{"water.png", "watercolorx.png"};
      waterColors = getCustomColors(s, astring, 256, -1);
      updateUseDefaultGrassFoliageColors();
      if (Config.isCustomColors()) {
         String[] astring1 = new String[]{"pine.png", "pinecolor.png"};
         foliagePineColors = getCustomColors(s, astring1, 256, -1);
         String[] astring2 = new String[]{"birch.png", "birchcolor.png"};
         foliageBirchColors = getCustomColors(s, astring2, 256, -1);
         String[] astring3 = new String[]{"swampgrass.png", "swampgrasscolor.png"};
         swampGrassColors = getCustomColors(s, astring3, 256, -1);
         String[] astring4 = new String[]{"swampfoliage.png", "swampfoliagecolor.png"};
         swampFoliageColors = getCustomColors(s, astring4, 256, -1);
         String[] astring5 = new String[]{"sky0.png", "skycolor0.png"};
         skyColors = getCustomColors(s, astring5, 256, -1);
         String[] astring6 = new String[]{"fog0.png", "fogcolor0.png"};
         fogColors = getCustomColors(s, astring6, 256, -1);
         String[] astring7 = new String[]{"underwater.png", "underwatercolor.png"};
         underwaterColors = getCustomColors(s, astring7, 256, -1);
         String[] astring8 = new String[]{"underlava.png", "underlavacolor.png"};
         underlavaColors = getCustomColors(s, astring8, 256, -1);
         String[] astring9 = new String[]{"redstone.png", "redstonecolor.png"};
         redstoneColors = getCustomColors(s, astring9, 16, 1);
         xpOrbColors = getCustomColors(s + "xporb.png", -1, -1);
         durabilityColors = getCustomColors(s + "durability.png", -1, -1);
         String[] astring10 = new String[]{"stem.png", "stemcolor.png"};
         stemColors = getCustomColors(s, astring10, 8, 1);
         stemPumpkinColors = getCustomColors(s + "pumpkinstem.png", 8, 1);
         stemMelonColors = getCustomColors(s + "melonstem.png", 8, 1);
         lavaDropColors = getCustomColors(s + "lavadrop.png", -1, 1);
         String[] astring11 = new String[]{"myceliumparticle.png", "myceliumparticlecolor.png"};
         myceliumParticleColors = getCustomColors(s, astring11, -1, -1);
         Pair<LightMapPack[], Integer> pair = parseLightMapPacks();
         lightMapPacks = pair.getLeft();
         lightmapMinDimensionId = pair.getRight();
         readColorProperties("optifine/color.properties");
         blockColormaps = readBlockColormaps(new String[]{s + "custom/", s + "blocks/"}, colorsBlockColormaps, 256, -1);
         updateUseDefaultGrassFoliageColors();
      }
   }

   private static String getValidProperty(String fileName, String key, String[] validValues, String valDef) {
      try {
         ResourceLocation resourcelocation = new ResourceLocation(fileName);
         InputStream inputstream = Config.getResourceStream(resourcelocation);
         if (inputstream == null) {
            return valDef;
         } else {
            Properties properties = new PropertiesOrdered();
            properties.load(inputstream);
            inputstream.close();
            String s = properties.getProperty(key);
            if (s == null) {
               return valDef;
            } else {
               List<String> list = Arrays.asList(validValues);
               if (!list.contains(s)) {
                  warn("Invalid value: " + key + "=" + s);
                  warn("Expected values: " + Config.arrayToString((Object[])validValues));
                  return valDef;
               } else {
                  dbg(key + "=" + s);
                  return s;
               }
            }
         }
      } catch (FileNotFoundException filenotfoundexception) {
         return valDef;
      } catch (IOException ioexception) {
         ioexception.printStackTrace();
         return valDef;
      }
   }

   private static Pair<LightMapPack[], Integer> parseLightMapPacks() {
      String s = "optifine/lightmap/world";
      String s1 = ".png";
      String[] astring = ResUtils.collectFiles(s, s1);
      Map<Integer, String> map = new HashMap<>();

      for(int i = 0; i < astring.length; ++i) {
         String s2 = astring[i];
         String s3 = StrUtils.removePrefixSuffix(s2, s, s1);
         int j = Config.parseInt(s3, Integer.MIN_VALUE);
         if (j == Integer.MIN_VALUE) {
            warn("Invalid dimension ID: " + s3 + ", path: " + s2);
         } else {
            map.put(j, s2);
         }
      }

      Set<Integer> set = map.keySet();
      Integer[] ainteger = set.toArray(new Integer[set.size()]);
      Arrays.sort((Object[])ainteger);
      if (ainteger.length <= 0) {
         return new ImmutablePair<>((LightMapPack[])null, 0);
      } else {
         int j1 = ainteger[0];
         int k1 = ainteger[ainteger.length - 1];
         int k = k1 - j1 + 1;
         CustomColormap[] acustomcolormap = new CustomColormap[k];

         for(int l = 0; l < ainteger.length; ++l) {
            Integer integer = ainteger[l];
            String s4 = map.get(integer);
            CustomColormap customcolormap = getCustomColors(s4, -1, -1);
            if (customcolormap != null) {
               if (customcolormap.getWidth() < 16) {
                  warn("Invalid lightmap width: " + customcolormap.getWidth() + ", path: " + s4);
               } else {
                  int i1 = integer - j1;
                  acustomcolormap[i1] = customcolormap;
               }
            }
         }

         LightMapPack[] alightmappack = new LightMapPack[acustomcolormap.length];

         for(int l1 = 0; l1 < acustomcolormap.length; ++l1) {
            CustomColormap customcolormap3 = acustomcolormap[l1];
            if (customcolormap3 != null) {
               String s5 = customcolormap3.name;
               String s6 = customcolormap3.basePath;
               CustomColormap customcolormap1 = getCustomColors(s6 + "/" + s5 + "_rain.png", -1, -1);
               CustomColormap customcolormap2 = getCustomColors(s6 + "/" + s5 + "_thunder.png", -1, -1);
               LightMap lightmap = new LightMap(customcolormap3);
               LightMap lightmap1 = customcolormap1 != null ? new LightMap(customcolormap1) : null;
               LightMap lightmap2 = customcolormap2 != null ? new LightMap(customcolormap2) : null;
               LightMapPack lightmappack = new LightMapPack(lightmap, lightmap1, lightmap2);
               alightmappack[l1] = lightmappack;
            }
         }

         return new ImmutablePair<>(alightmappack, j1);
      }
   }

   private static int getTextureHeight(String path, int defHeight) {
      try {
         InputStream inputstream = Config.getResourceStream(new ResourceLocation(path));
         if (inputstream == null) {
            return defHeight;
         } else {
            BufferedImage bufferedimage = ImageIO.read(inputstream);
            inputstream.close();
            return bufferedimage == null ? defHeight : bufferedimage.getHeight();
         }
      } catch (IOException ioexception) {
         return defHeight;
      }
   }

   private static void readColorProperties(String fileName) {
      try {
         ResourceLocation resourcelocation = new ResourceLocation(fileName);
         InputStream inputstream = Config.getResourceStream(resourcelocation);
         if (inputstream == null) {
            return;
         }

         dbg("Loading " + fileName);
         Properties properties = new PropertiesOrdered();
         properties.load(inputstream);
         inputstream.close();
         particleWaterColor = readColor(properties, new String[]{"particle.water", "drop.water"});
         particlePortalColor = readColor(properties, "particle.portal");
         lilyPadColor = readColor(properties, "lilypad");
         expBarTextColor = readColor(properties, "text.xpbar");
         bossTextColor = readColor(properties, "text.boss");
         signTextColor = readColor(properties, "text.sign");
         fogColorNether = readColorVec3(properties, "fog.nether");
         fogColorEnd = readColorVec3(properties, "fog.end");
         skyColorEnd = readColorVec3(properties, "sky.end");
         colorsBlockColormaps = readCustomColormaps(properties, fileName);
         spawnEggPrimaryColors = readSpawnEggColors(properties, fileName, "egg.shell.", "Spawn egg shell");
         spawnEggSecondaryColors = readSpawnEggColors(properties, fileName, "egg.spots.", "Spawn egg spot");
         wolfCollarColors = readDyeColors(properties, fileName, "collar.", "Wolf collar");
         sheepColors = readDyeColors(properties, fileName, "sheep.", "Sheep");
         textColors = readTextColors(properties, fileName, "text.code.", "Text");
         int[] aint = readMapColors(properties, fileName, "map.", "Map");
         if (aint != null) {
            if (mapColorsOriginal == null) {
               mapColorsOriginal = getMapColors();
            }

            setMapColors(aint);
         }

         float[][] afloat = readDyeColors(properties, fileName, "dye.", "Dye");
         if (afloat != null) {
            if (dyeColorsOriginal == null) {
               dyeColorsOriginal = getDyeColors();
            }

            setDyeColors(afloat);
         }

         potionColors = readPotionColors(properties, fileName, "potion.", "Potion");
         xpOrbTime = Config.parseInt(properties.getProperty("xporb.time"), -1);
      } catch (FileNotFoundException filenotfoundexception) {
         return;
      } catch (IOException ioexception) {
         Config.warn("Error parsing: " + fileName);
         Config.warn(ioexception.getClass().getName() + ": " + ioexception.getMessage());
      }

   }

   private static CustomColormap[] readCustomColormaps(Properties props, String fileName) {
      List list = new ArrayList();
      String s = "palette.block.";
      Map map = new HashMap();

      for(String s1 : (Set<String>)(Set<?>) props.keySet()) {
         String s2 = props.getProperty(s1);
         if (s1.startsWith(s)) {
            map.put(s1, s2);
         }
      }

      String[] astring = (String[]) map.keySet().toArray(new String[map.size()]);

      for(int j = 0; j < astring.length; ++j) {
         String s6 = astring[j];
         String s3 = props.getProperty(s6);
         dbg("Block palette: " + s6 + " = " + s3);
         String s4 = s6.substring(s.length());
         String s5 = TextureUtils.getBasePath(fileName);
         s4 = TextureUtils.fixResourcePath(s4, s5);
         CustomColormap customcolormap = getCustomColors(s4, 256, -1);
         if (customcolormap == null) {
            warn("Colormap not found: " + s4);
         } else {
            ConnectedParser connectedparser = new ConnectedParser("CustomColors");
            MatchBlock[] amatchblock = connectedparser.parseMatchBlocks(s3);
            if (amatchblock != null && amatchblock.length > 0) {
               for(int i = 0; i < amatchblock.length; ++i) {
                  MatchBlock matchblock = amatchblock[i];
                  customcolormap.addMatchBlock(matchblock);
               }

               list.add(customcolormap);
            } else {
               warn("Invalid match blocks: " + s3);
            }
         }
      }

      return list.size() <= 0 ? null : (CustomColormap[]) list.toArray(new CustomColormap[list.size()]);
   }

   private static CustomColormap[][] readBlockColormaps(String[] basePaths, CustomColormap[] basePalettes, int width, int height) {
      String[] astring = ResUtils.collectFiles(basePaths, new String[]{".properties"});
      Arrays.sort((Object[])astring);
      List list = new ArrayList();

      for(int i = 0; i < astring.length; ++i) {
         String s = astring[i];
         dbg("Block colormap: " + s);

         try {
            ResourceLocation resourcelocation = new ResourceLocation("minecraft", s);
            InputStream inputstream = Config.getResourceStream(resourcelocation);
            if (inputstream == null) {
               warn("File not found: " + s);
            } else {
               Properties properties = new PropertiesOrdered();
               properties.load(inputstream);
               inputstream.close();
               CustomColormap customcolormap = new CustomColormap(properties, s, width, height, paletteFormatDefault);
               if (customcolormap.isValid(s) && customcolormap.isValidMatchBlocks(s)) {
                  addToBlockList(customcolormap, list);
               }
            }
         } catch (FileNotFoundException filenotfoundexception) {
            warn("File not found: " + s);
         } catch (Exception exception) {
            exception.printStackTrace();
         }
      }

      if (basePalettes != null) {
         for(int j = 0; j < basePalettes.length; ++j) {
            CustomColormap customcolormap1 = basePalettes[j];
            addToBlockList(customcolormap1, list);
         }
      }

      return list.size() <= 0 ? null : blockListToArray(list);
   }

   private static void addToBlockList(CustomColormap cm, List blockList) {
      int[] aint = cm.getMatchBlockIds();
      if (aint != null && aint.length > 0) {
         for(int i = 0; i < aint.length; ++i) {
            int j = aint[i];
            if (j < 0) {
               warn("Invalid block ID: " + j);
            } else {
               addToList(cm, blockList, j);
            }
         }

      } else {
         warn("No match blocks: " + Config.arrayToString(aint));
      }
   }

   private static void addToList(CustomColormap cm, List list, int id) {
      while (id >= list.size()) {
         list.add(null);
      }
      ArrayList<CustomColormap> subList = (ArrayList<CustomColormap>)list.get(id);
      if (subList == null) {
         subList = new ArrayList<CustomColormap>();
         list.set(id, subList);
      }
      subList.add(cm);
   }

   private static CustomColormap[][] blockListToArray(List list) {
      CustomColormap[][] colArr = new CustomColormap[list.size()][];
      for (int i = 0; i < list.size(); ++i) {
         List subList = (List)list.get(i);
         if (subList == null) continue;
         CustomColormap[] subArr = (CustomColormap[]) subList.toArray(new CustomColormap[subList.size()]);
         colArr[i] = subArr;
      }
      return colArr;
   }

   private static int readColor(Properties props, String[] names) {
      for(int i = 0; i < names.length; ++i) {
         String s = names[i];
         int j = readColor(props, s);
         if (j >= 0) {
            return j;
         }
      }

      return -1;
   }

   private static int readColor(Properties props, String name) {
      String s = props.getProperty(name);
      if (s == null) {
         return -1;
      } else {
         s = s.trim();
         int i = parseColor(s);
         if (i < 0) {
            warn("Invalid color: " + name + " = " + s);
            return i;
         } else {
            dbg(name + " = " + s);
            return i;
         }
      }
   }

   private static int parseColor(String str) {
      if (str == null) {
         return -1;
      } else {
         str = str.trim();

         try {
            return Integer.parseInt(str, 16) & 16777215;
         } catch (NumberFormatException numberformatexception) {
            return -1;
         }
      }
   }

   private static Vec3 readColorVec3(Properties props, String name) {
      int i = readColor(props, name);
      if (i < 0) {
         return null;
      } else {
         int j = i >> 16 & 255;
         int k = i >> 8 & 255;
         int l = i & 255;
         float f = (float)j / 255.0F;
         float f1 = (float)k / 255.0F;
         float f2 = (float)l / 255.0F;
         return new Vec3((double)f, (double)f1, (double)f2);
      }
   }

   private static CustomColormap getCustomColors(String basePath, String[] paths, int width, int height) {
      for(int i = 0; i < paths.length; ++i) {
         String s = paths[i];
         s = basePath + s;
         CustomColormap customcolormap = getCustomColors(s, width, height);
         if (customcolormap != null) {
            return customcolormap;
         }
      }

      return null;
   }

   public static CustomColormap getCustomColors(String pathImage, int width, int height) {
      try {
         ResourceLocation resourcelocation = new ResourceLocation(pathImage);
         if (!Config.hasResource(resourcelocation)) {
            return null;
         } else {
            dbg("Colormap " + pathImage);
            Properties properties = new PropertiesOrdered();
            String s = StrUtils.replaceSuffix(pathImage, ".png", ".properties");
            ResourceLocation resourcelocation1 = new ResourceLocation(s);
            if (Config.hasResource(resourcelocation1)) {
               InputStream inputstream = Config.getResourceStream(resourcelocation1);
               properties.load(inputstream);
               inputstream.close();
               dbg("Colormap properties: " + s);
            } else {
               properties.put("format", paletteFormatDefault);
               properties.put("source", pathImage);
               s = pathImage;
            }

            CustomColormap customcolormap = new CustomColormap(properties, s, width, height, paletteFormatDefault);
            return !customcolormap.isValid(s) ? null : customcolormap;
         }
      } catch (Exception exception) {
         exception.printStackTrace();
         return null;
      }
   }

   public static void updateUseDefaultGrassFoliageColors() {
      useDefaultGrassFoliageColors = foliageBirchColors == null && foliagePineColors == null && swampGrassColors == null && swampFoliageColors == null;
   }

   public static int getColorMultiplier(BakedQuad quad, BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos, RenderEnv renderEnv) {
      return getColorMultiplier(quad.isTinted(), blockState, blockAccess, blockPos, renderEnv);
   }

   public static int getColorMultiplier(boolean quadHasTintIndex, BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos, RenderEnv renderEnv) {
      Block block = blockState.getBlock();
      BlockState blockstate = blockState;
      if (blockColormaps != null) {
         if (!quadHasTintIndex) {
            if (block == Blocks.GRASS_BLOCK) {
               blockstate = BLOCK_STATE_DIRT;
            }

            if (block == Blocks.REDSTONE_WIRE) {
               return -1;
            }
         }

         if (block instanceof DoublePlantBlock && blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            blockPos = blockPos.below();
            blockstate = blockAccess.getBlockState(blockPos);
         }

         CustomColormap customcolormap = getBlockColormap(blockstate);
         if (customcolormap != null) {
            if (Config.isSmoothBiomes() && !customcolormap.isColorConstant()) {
               return getSmoothColorMultiplier(blockState, blockAccess, blockPos, customcolormap, renderEnv.getColorizerBlockPosM());
            }

            return customcolormap.getColor(blockAccess, blockPos);
         }
      }

      if (!quadHasTintIndex) {
         return -1;
      } else if (block == Blocks.LILY_PAD) {
         return getLilypadColorMultiplier(blockAccess, blockPos);
      } else if (block == Blocks.REDSTONE_WIRE) {
         return getRedstoneColor(renderEnv.getBlockState());
      } else if (block instanceof StemBlock) {
         return getStemColorMultiplier(blockState, blockAccess, blockPos, renderEnv);
      } else if (useDefaultGrassFoliageColors) {
         return -1;
      } else {
         CustomColors.IColorizer customcolors$icolorizer;
         if (block != Blocks.GRASS_BLOCK && !(block instanceof TallGrassBlock) && !(block instanceof DoublePlantBlock) && block != Blocks.SUGAR_CANE) {
            if (block instanceof DoublePlantBlock) {
               customcolors$icolorizer = COLORIZER_GRASS;
               if (blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                  blockPos = blockPos.below();
               }
            } else if (block instanceof LeavesBlock) {
               if (block == Blocks.SPRUCE_LEAVES) {
                  customcolors$icolorizer = COLORIZER_FOLIAGE_PINE;
               } else if (block == Blocks.BIRCH_LEAVES) {
                  customcolors$icolorizer = COLORIZER_FOLIAGE_BIRCH;
               } else {
                  if (block == Blocks.CHERRY_LEAVES) {
                     return -1;
                  }

                  if (!blockState.getBlockLocation().isDefaultNamespace()) {
                     return -1;
                  }

                  customcolors$icolorizer = COLORIZER_FOLIAGE;
               }
            } else {
               if (block != Blocks.VINE) {
                  return -1;
               }

               customcolors$icolorizer = COLORIZER_FOLIAGE;
            }
         } else {
            customcolors$icolorizer = COLORIZER_GRASS;
         }

         return Config.isSmoothBiomes() && !customcolors$icolorizer.isColorConstant() ? getSmoothColorMultiplier(blockState, blockAccess, blockPos, customcolors$icolorizer, renderEnv.getColorizerBlockPosM()) : customcolors$icolorizer.getColor(blockstate, blockAccess, blockPos);
      }
   }

   protected static Biome getColorBiome(BlockAndTintGetter blockAccess, BlockPos blockPos) {
      Biome biome = BiomeUtils.getBiome(blockAccess, blockPos);
      return fixBiome(biome);
   }

   public static Biome fixBiome(Biome biome) {
      return (biome == BiomeUtils.SWAMP || biome == BiomeUtils.MANGROVE_SWAMP) && !Config.isSwampColors() ? BiomeUtils.PLAINS : biome;
   }

   private static CustomColormap getBlockColormap(BlockState blockState) {
      if (blockColormaps == null) {
         return null;
      } else if (!(blockState instanceof BlockState)) {
         return null;
      } else {
         BlockState blockstate = blockState;
         int i = blockState.getBlockId();
         if (i >= 0 && i < blockColormaps.length) {
            CustomColormap[] acustomcolormap = blockColormaps[i];
            if (acustomcolormap == null) {
               return null;
            } else {
               for(int j = 0; j < acustomcolormap.length; ++j) {
                  CustomColormap customcolormap = acustomcolormap[j];
                  if (customcolormap.matchesBlock(blockstate)) {
                     return customcolormap;
                  }
               }

               return null;
            }
         } else {
            return null;
         }
      }
   }

   private static int getSmoothColorMultiplier(BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos, CustomColors.IColorizer colorizer, BlockPosM blockPosM) {
      int i = 0;
      int j = 0;
      int k = 0;
      int l = blockPos.getX();
      int i1 = blockPos.getY();
      int j1 = blockPos.getZ();
      BlockPosM blockposm = blockPosM;
      int k1 = Config.getBiomeBlendRadius();
      int l1 = k1 * 2 + 1;
      int i2 = l1 * l1;

      for(int j2 = l - k1; j2 <= l + k1; ++j2) {
         for(int k2 = j1 - k1; k2 <= j1 + k1; ++k2) {
            blockposm.setXyz(j2, i1, k2);
            int l2 = colorizer.getColor(blockState, blockAccess, blockposm);
            i += l2 >> 16 & 255;
            j += l2 >> 8 & 255;
            k += l2 & 255;
         }
      }

      int i3 = i / i2;
      int j3 = j / i2;
      int k3 = k / i2;
      return i3 << 16 | j3 << 8 | k3;
   }

   public static int getFluidColor(BlockAndTintGetter blockAccess, BlockState blockState, BlockPos blockPos, RenderEnv renderEnv) {
      Block block = blockState.getBlock();
      CustomColors.IColorizer customcolors$icolorizer = getBlockColormap(blockState);
      if (customcolors$icolorizer == null && blockState.getBlock() == Blocks.WATER) {
         customcolors$icolorizer = COLORIZER_WATER;
      }

      if (customcolors$icolorizer == null) {
         return getBlockColors().getColor(blockState, blockAccess, blockPos, 0);
      } else {
         return Config.isSmoothBiomes() && !customcolors$icolorizer.isColorConstant() ? getSmoothColorMultiplier(blockState, blockAccess, blockPos, customcolors$icolorizer, renderEnv.getColorizerBlockPosM()) : customcolors$icolorizer.getColor(blockState, blockAccess, blockPos);
      }
   }

   public static BlockColors getBlockColors() {
      return Minecraft.getInstance().getBlockColors();
   }

   public static void updatePortalFX(Particle fx) {
      if (particlePortalColor >= 0) {
         int i = particlePortalColor;
         int j = i >> 16 & 255;
         int k = i >> 8 & 255;
         int l = i & 255;
         float f = (float)j / 255.0F;
         float f1 = (float)k / 255.0F;
         float f2 = (float)l / 255.0F;
         fx.setColor(f, f1, f2);
      }
   }

   public static void updateLavaFX(Particle fx) {
      if (lavaDropColors != null) {
         int i = fx.getAge();
         int j = lavaDropColors.getColor(i);
         int k = j >> 16 & 255;
         int l = j >> 8 & 255;
         int i1 = j & 255;
         float f = (float)k / 255.0F;
         float f1 = (float)l / 255.0F;
         float f2 = (float)i1 / 255.0F;
         fx.setColor(f, f1, f2);
      }
   }

   public static void updateMyceliumFX(Particle fx) {
      if (myceliumParticleColors != null) {
         int i = myceliumParticleColors.getColorRandom();
         int j = i >> 16 & 255;
         int k = i >> 8 & 255;
         int l = i & 255;
         float f = (float)j / 255.0F;
         float f1 = (float)k / 255.0F;
         float f2 = (float)l / 255.0F;
         fx.setColor(f, f1, f2);
      }
   }

   private static int getRedstoneColor(BlockState blockState) {
      if (redstoneColors == null) {
         return -1;
      } else {
         int i = getRedstoneLevel(blockState, 15);
         return redstoneColors.getColor(i);
      }
   }

   public static void updateReddustFX(Particle fx, BlockAndTintGetter blockAccess, double x, double y, double z) {
      if (redstoneColors != null) {
         BlockState blockstate = blockAccess.getBlockState(BlockPos.containing(x, y, z));
         int i = getRedstoneLevel(blockstate, 15);
         int j = redstoneColors.getColor(i);
         int k = j >> 16 & 255;
         int l = j >> 8 & 255;
         int i1 = j & 255;
         float f = (float)k / 255.0F;
         float f1 = (float)l / 255.0F;
         float f2 = (float)i1 / 255.0F;
         fx.setColor(f, f1, f2);
      }
   }

   private static int getRedstoneLevel(BlockState state, int def) {
      Block block = state.getBlock();
      if (!(block instanceof RedStoneWireBlock)) {
         return def;
      } else {
         Object object = state.getValue(RedStoneWireBlock.POWER);
         if (!(object instanceof Integer)) {
            return def;
         } else {
            Integer integer = (Integer)object;
            return integer;
         }
      }
   }

   public static float getXpOrbTimer(float timer) {
      if (xpOrbTime <= 0) {
         return timer;
      } else {
         float f = 628.0F / (float)xpOrbTime;
         return timer * f;
      }
   }

   public static int getXpOrbColor(float timer) {
      if (xpOrbColors == null) {
         return -1;
      } else {
         int i = (int)Math.round((double)((Mth.sin(timer) + 1.0F) * (float)(xpOrbColors.getLength() - 1)) / 2.0D);
         return xpOrbColors.getColor(i);
      }
   }

   public static int getDurabilityColor(float dur, int color) {
      if (durabilityColors == null) {
         return color;
      } else {
         int i = (int)(dur * (float)durabilityColors.getLength());
         return durabilityColors.getColor(i);
      }
   }

   public static void updateWaterFX(Particle fx, BlockAndTintGetter blockAccess, double x, double y, double z, RenderEnv renderEnv) {
      if (waterColors != null || blockColormaps != null || particleWaterColor >= 0) {
         BlockPos blockpos = BlockPos.containing(x, y, z);
         renderEnv.reset(BLOCK_STATE_WATER, blockpos);
         int i = getFluidColor(blockAccess, BLOCK_STATE_WATER, blockpos, renderEnv);
         int j = i >> 16 & 255;
         int k = i >> 8 & 255;
         int l = i & 255;
         float f = (float)j / 255.0F;
         float f1 = (float)k / 255.0F;
         float f2 = (float)l / 255.0F;
         if (particleWaterColor >= 0) {
            int i1 = particleWaterColor >> 16 & 255;
            int j1 = particleWaterColor >> 8 & 255;
            int k1 = particleWaterColor & 255;
            f = (float)i1 / 255.0F;
            f1 = (float)j1 / 255.0F;
            f2 = (float)k1 / 255.0F;
            f *= (float)i1 / 255.0F;
            f1 *= (float)j1 / 255.0F;
            f2 *= (float)k1 / 255.0F;
         }

         fx.setColor(f, f1, f2);
      }
   }

   private static int getLilypadColorMultiplier(BlockAndTintGetter blockAccess, BlockPos blockPos) {
      return lilyPadColor < 0 ? getBlockColors().getColor(Blocks.LILY_PAD.defaultBlockState(), blockAccess, blockPos, 0) : lilyPadColor;
   }

   private static Vec3 getFogColorNether(Vec3 col) {
      return fogColorNether == null ? col : fogColorNether;
   }

   private static Vec3 getFogColorEnd(Vec3 col) {
      return fogColorEnd == null ? col : fogColorEnd;
   }

   private static Vec3 getSkyColorEnd(Vec3 col) {
      return skyColorEnd == null ? col : skyColorEnd;
   }

   public static Vec3 getSkyColor(Vec3 skyColor3d, BlockAndTintGetter blockAccess, double x, double y, double z) {
      if (skyColors == null) {
         return skyColor3d;
      } else {
         int i = skyColors.getColorSmooth(blockAccess, x, y, z, 3);
         int j = i >> 16 & 255;
         int k = i >> 8 & 255;
         int l = i & 255;
         float f = (float)j / 255.0F;
         float f1 = (float)k / 255.0F;
         float f2 = (float)l / 255.0F;
         float f3 = (float)skyColor3d.x / 0.5F;
         float f4 = (float)skyColor3d.y / 0.66275F;
         float f5 = (float)skyColor3d.z;
         f *= f3;
         f1 *= f4;
         f2 *= f5;
         return skyColorFader.getColor((double)f, (double)f1, (double)f2);
      }
   }

   private static Vec3 getFogColor(Vec3 fogColor3d, BlockAndTintGetter blockAccess, double x, double y, double z) {
      if (fogColors == null) {
         return fogColor3d;
      } else {
         int color = ColorUtils.getColorStyleLowSpeed(0);

// ��������� ���������� R, G � B �� ���������� color
         int j = (color >> 16) & 255; // R
         int k = (color >> 8) & 255;  // G
         int l = color & 255;         // B

// ����������� ���������� � �������� �� 0 �� 1
         float f = (float)j / 255.0F;
         float f1 = (float)k / 255.0F;
         float f2 = (float)l / 255.0F;

// ������������ ����� � ������������ � fogColor3d
         float f3 = (float)fogColor3d.x / 0.753F;
         float f4 = (float)fogColor3d.y / 0.8471F;
         float f5 = (float)fogColor3d.z;

// ��������� ��������������� � ����������� �����
         f *= f3;
         f1 *= f4;
         f2 *= f5;

// ���������� �������� ����
         return fogColorFader.getColor((double)f, (double)f1, (double)f2);

      }
   }

   public static Vec3 getUnderwaterColor(BlockAndTintGetter blockAccess, double x, double y, double z) {
      return getUnderFluidColor(blockAccess, x, y, z, underwaterColors, underwaterColorFader);
   }

   public static Vec3 getUnderlavaColor(BlockAndTintGetter blockAccess, double x, double y, double z) {
      return getUnderFluidColor(blockAccess, x, y, z, underlavaColors, underlavaColorFader);
   }

   public static Vec3 getUnderFluidColor(BlockAndTintGetter blockAccess, double x, double y, double z, CustomColormap underFluidColors, CustomColorFader underFluidColorFader) {
      if (underFluidColors == null) {
         return null;
      } else {
         int i = underFluidColors.getColorSmooth(blockAccess, x, y, z, 3);
         int j = i >> 16 & 255;
         int k = i >> 8 & 255;
         int l = i & 255;
         float f = (float)j / 255.0F;
         float f1 = (float)k / 255.0F;
         float f2 = (float)l / 255.0F;
         return underFluidColorFader.getColor((double)f, (double)f1, (double)f2);
      }
   }

   private static int getStemColorMultiplier(BlockState blockState, BlockGetter blockAccess, BlockPos blockPos, RenderEnv renderEnv) {
      CustomColormap customcolormap = stemColors;
      Block block = blockState.getBlock();
      if (block == Blocks.PUMPKIN_STEM && stemPumpkinColors != null) {
         customcolormap = stemPumpkinColors;
      }

      if (block == Blocks.MELON_STEM && stemMelonColors != null) {
         customcolormap = stemMelonColors;
      }

      if (customcolormap == null) {
         return -1;
      } else if (!(block instanceof StemBlock)) {
         return -1;
      } else {
         int i = blockState.getValue(StemBlock.AGE);
         return customcolormap.getColor(i);
      }
   }

   public static boolean updateLightmap(ClientLevel world, float torchFlickerX, NativeImage lmColors, boolean nightvision, float darkLight, float partialTicks) {
      if (world == null) {
         return false;
      } else if (lightMapPacks == null) {
         return false;
      } else {
         int i = WorldUtils.getDimensionId(world);
         int j = i - lightmapMinDimensionId;
         if (j >= 0 && j < lightMapPacks.length) {
            LightMapPack lightmappack = lightMapPacks[j];
            return lightmappack == null ? false : lightmappack.updateLightmap(world, torchFlickerX, lmColors, nightvision, darkLight, partialTicks);
         } else {
            return false;
         }
      }
   }

   public static Vec3 getWorldFogColor(Vec3 fogVec, Level world, Entity renderViewEntity, float partialTicks) {
      Minecraft minecraft = Minecraft.getInstance();
      if (WorldUtils.isNether(world)) {
         return getFogColorNether(fogVec);
      } else if (WorldUtils.isOverworld(world)) {
         return getFogColor(fogVec, minecraft.level, renderViewEntity.getX(), renderViewEntity.getY() + 1.0D, renderViewEntity.getZ());
      } else {
         return WorldUtils.isEnd(world) ? getFogColorEnd(fogVec) : fogVec;
      }
   }

   public static Vec3 getWorldSkyColor(Vec3 skyVec, Level world, Entity renderViewEntity, float partialTicks) {
      Minecraft minecraft = Minecraft.getInstance();
      if (WorldUtils.isOverworld(world) && renderViewEntity != null) {
         return getSkyColor(skyVec, minecraft.level, renderViewEntity.getX(), renderViewEntity.getY() + 1.0D, renderViewEntity.getZ());
      } else {
         return WorldUtils.isEnd(world) ? getSkyColorEnd(skyVec) : skyVec;
      }
   }

   private static int[] readSpawnEggColors(Properties props, String fileName, String prefix, String logName) {
      List<Integer> list = new ArrayList<>();
      Set set =  props.keySet();
      int i = 0;

      for(String s : (Set<String>)(Set<?>) set) {
         String s1 = props.getProperty(s);
         if (s.startsWith(prefix)) {
            String s2 = StrUtils.removePrefix(s, prefix);
            int j = EntityUtils.getEntityIdByName(s2);

            try {
               if (j < 0) {
                  j = EntityUtils.getEntityIdByLocation((new ResourceLocation(s2)).toString());
               }
            } catch (ResourceLocationException resourcelocationexception) {
               Config.warn("ResourceLocationException: " + resourcelocationexception.getMessage());
            }

            if (j < 0) {
               warn("Invalid spawn egg name: " + s);
            } else {
               int k = parseColor(s1);
               if (k < 0) {
                  warn("Invalid spawn egg color: " + s + " = " + s1);
               } else {
                  while(list.size() <= j) {
                     list.add(-1);
                  }

                  list.set(j, k);
                  ++i;
               }
            }
         }
      }

      if (i <= 0) {
         return null;
      } else {
         dbg(logName + " colors: " + i);
         int[] aint = new int[list.size()];

         for(int l = 0; l < aint.length; ++l) {
            aint[l] = list.get(l);
         }

         return aint;
      }
   }

   private static int getSpawnEggColor(SpawnEggItem item, ItemStack itemStack, int layer, int color) {
      if (spawnEggPrimaryColors == null && spawnEggSecondaryColors == null) {
         return color;
      } else {
         EntityType entitytype = item.getType(itemStack.getTag());
         if (entitytype == null) {
            return color;
         } else {
            int i = BuiltInRegistries.ENTITY_TYPE.getId(entitytype);
            if (i < 0) {
               return color;
            } else {
               int[] aint = layer == 0 ? spawnEggPrimaryColors : spawnEggSecondaryColors;
               if (aint == null) {
                  return color;
               } else if (i >= 0 && i < aint.length) {
                  int j = aint[i];
                  return j < 0 ? color : j;
               } else {
                  return color;
               }
            }
         }
      }
   }

   public static int getColorFromItemStack(ItemStack itemStack, int layer, int color) {
      if (itemStack == null) {
         return color;
      } else {
         Item item = itemStack.getItem();
         if (item == null) {
            return color;
         } else if (item instanceof SpawnEggItem) {
            return getSpawnEggColor((SpawnEggItem)item, itemStack, layer, color);
         } else {
            return item == Items.LILY_PAD && lilyPadColor != -1 ? lilyPadColor : color;
         }
      }
   }

   private static float[][] readDyeColors(Properties props, String fileName, String prefix, String logName) {
      DyeColor[] adyecolor = DyeColor.values();
      Map<String, DyeColor> map = new HashMap<>();

      for(int i = 0; i < adyecolor.length; ++i) {
         DyeColor dyecolor = adyecolor[i];
         map.put(dyecolor.getSerializedName(), dyecolor);
      }

      map.put("lightBlue", DyeColor.LIGHT_BLUE);
      map.put("silver", DyeColor.LIGHT_GRAY);
      float[][] afloat1 = new float[adyecolor.length][];
      int k = 0;

      for(String s : (Set<String>)(Set<?>) props.keySet()) {
         String s1 = props.getProperty(s);
         if (s.startsWith(prefix)) {
            String s2 = StrUtils.removePrefix(s, prefix);
            DyeColor dyecolor1 = map.get(s2);
            int j = parseColor(s1);
            if (dyecolor1 != null && j >= 0) {
               float[] afloat = new float[]{(float)(j >> 16 & 255) / 255.0F, (float)(j >> 8 & 255) / 255.0F, (float)(j & 255) / 255.0F};
               afloat1[dyecolor1.ordinal()] = afloat;
               ++k;
            } else {
               warn("Invalid color: " + s + " = " + s1);
            }
         }
      }

      if (k <= 0) {
         return null;
      } else {
         dbg(logName + " colors: " + k);
         return afloat1;
      }
   }

   private static float[] getDyeColors(DyeColor dye, float[][] dyeColors, float[] colors) {
      if (dyeColors == null) {
         return colors;
      } else if (dye == null) {
         return colors;
      } else {
         float[] afloat = dyeColors[dye.ordinal()];
         return afloat == null ? colors : afloat;
      }
   }

   public static float[] getWolfCollarColors(DyeColor dye, float[] colors) {
      return getDyeColors(dye, wolfCollarColors, colors);
   }

   public static float[] getSheepColors(DyeColor dye, float[] colors) {
      return getDyeColors(dye, sheepColors, colors);
   }

   private static int[] readTextColors(Properties props, String fileName, String prefix, String logName) {
      int[] aint = new int[32];
      Arrays.fill(aint, -1);
      int i = 0;

      for(String s : (Set<String>)(Set<?>) props.keySet()) {
         String s1 = props.getProperty(s);
         if (s.startsWith(prefix)) {
            String s2 = StrUtils.removePrefix(s, prefix);
            int j = Config.parseInt(s2, -1);
            int k = parseColor(s1);
            if (j >= 0 && j < aint.length && k >= 0) {
               aint[j] = k;
               ++i;
            } else {
               warn("Invalid color: " + s + " = " + s1);
            }
         }
      }

      if (i <= 0) {
         return null;
      } else {
         dbg(logName + " colors: " + i);
         return aint;
      }
   }

   public static int getTextColor(int index, int color) {
      if (textColors == null) {
         return color;
      } else if (index >= 0 && index < textColors.length) {
         int i = textColors[index];
         return i < 0 ? color : i;
      } else {
         return color;
      }
   }

   private static int[] readMapColors(Properties props, String fileName, String prefix, String logName) {
      int[] aint = new int[MapColor.MATERIAL_COLORS.length];
      Arrays.fill(aint, -1);
      int i = 0;

      for(String s : (Set<String>)(Set<?>) props.keySet()) {
         String s1 = props.getProperty(s);
         if (s.startsWith(prefix)) {
            String s2 = StrUtils.removePrefix(s, prefix);
            int j = getMapColorIndex(s2);
            int k = parseColor(s1);
            if (j >= 0 && j < aint.length && k >= 0) {
               aint[j] = k;
               ++i;
            } else {
               warn("Invalid color: " + s + " = " + s1);
            }
         }
      }

      if (i <= 0) {
         return null;
      } else {
         dbg(logName + " colors: " + i);
         return aint;
      }
   }

   private static int[] readPotionColors(Properties props, String fileName, String prefix, String logName) {
      int[] aint = new int[getMaxPotionId()];
      Arrays.fill(aint, -1);
      int i = 0;

      for(String s : (Set<String>)(Set<?>) props.keySet()) {
         String s1 = props.getProperty(s);
         if (s.startsWith(prefix)) {
            int j = getPotionId(s);
            int k = parseColor(s1);
            if (j >= 0 && j < aint.length && k >= 0) {
               aint[j] = k;
               ++i;
            } else {
               warn("Invalid color: " + s + " = " + s1);
            }
         }
      }

      if (i <= 0) {
         return null;
      } else {
         dbg(logName + " colors: " + i);
         return aint;
      }
   }

   private static int getMaxPotionId() {
      int i = 0;

      for(ResourceLocation resourcelocation : BuiltInRegistries.MOB_EFFECT.keySet()) {
         MobEffect mobeffect = PotionUtils.getPotion(resourcelocation);
         int j = MobEffect.getId(mobeffect);
         if (j > i) {
            i = j;
         }
      }

      return i;
   }

   private static int getPotionId(String name) {
      if (name.equals("potion.water")) {
         return 0;
      } else {
         name = StrUtils.replacePrefix(name, "potion.", "effect.");
         String s = StrUtils.replacePrefix(name, "effect.", "effect.minecraft.");

         for(ResourceLocation resourcelocation : BuiltInRegistries.MOB_EFFECT.keySet()) {
            MobEffect mobeffect = PotionUtils.getPotion(resourcelocation);
            if (mobeffect.getDescriptionId().equals(name)) {
               return MobEffect.getId(mobeffect);
            }

            if (mobeffect.getDescriptionId().equals(s)) {
               return MobEffect.getId(mobeffect);
            }
         }

         return -1;
      }
   }

   public static int getPotionColor(MobEffect potion, int color) {
      int i = 0;
      if (potion != null) {
         i = MobEffect.getId(potion);
      }

      return getPotionColor(i, color);
   }

   public static int getPotionColor(int potionId, int color) {
      if (potionColors == null) {
         return color;
      } else if (potionId >= 0 && potionId < potionColors.length) {
         int i = potionColors[potionId];
         return i < 0 ? color : i;
      } else {
         return color;
      }
   }

   private static int getMapColorIndex(String name) {
      if (name == null) {
         return -1;
      } else if (name.equals("air")) {
         return MapColor.NONE.id;
      } else if (name.equals("grass")) {
         return MapColor.GRASS.id;
      } else if (name.equals("sand")) {
         return MapColor.SAND.id;
      } else if (name.equals("cloth")) {
         return MapColor.WOOL.id;
      } else if (name.equals("tnt")) {
         return MapColor.FIRE.id;
      } else if (name.equals("ice")) {
         return MapColor.ICE.id;
      } else if (name.equals("iron")) {
         return MapColor.METAL.id;
      } else if (name.equals("foliage")) {
         return MapColor.PLANT.id;
      } else if (name.equals("clay")) {
         return MapColor.CLAY.id;
      } else if (name.equals("dirt")) {
         return MapColor.DIRT.id;
      } else if (name.equals("stone")) {
         return MapColor.STONE.id;
      } else if (name.equals("water")) {
         return MapColor.WATER.id;
      } else if (name.equals("wood")) {
         return MapColor.WOOD.id;
      } else if (name.equals("quartz")) {
         return MapColor.QUARTZ.id;
      } else if (name.equals("gold")) {
         return MapColor.GOLD.id;
      } else if (name.equals("diamond")) {
         return MapColor.DIAMOND.id;
      } else if (name.equals("lapis")) {
         return MapColor.LAPIS.id;
      } else if (name.equals("emerald")) {
         return MapColor.EMERALD.id;
      } else if (name.equals("podzol")) {
         return MapColor.PODZOL.id;
      } else if (name.equals("netherrack")) {
         return MapColor.NETHER.id;
      } else if (!name.equals("snow") && !name.equals("white")) {
         if (!name.equals("adobe") && !name.equals("orange")) {
            if (name.equals("magenta")) {
               return MapColor.COLOR_MAGENTA.id;
            } else if (!name.equals("light_blue") && !name.equals("lightBlue")) {
               if (name.equals("yellow")) {
                  return MapColor.COLOR_YELLOW.id;
               } else if (name.equals("lime")) {
                  return MapColor.COLOR_LIGHT_GREEN.id;
               } else if (name.equals("pink")) {
                  return MapColor.COLOR_PINK.id;
               } else if (name.equals("gray")) {
                  return MapColor.COLOR_GRAY.id;
               } else if (!name.equals("silver") && !name.equals("light_gray")) {
                  if (name.equals("cyan")) {
                     return MapColor.COLOR_CYAN.id;
                  } else if (name.equals("purple")) {
                     return MapColor.COLOR_PURPLE.id;
                  } else if (name.equals("blue")) {
                     return MapColor.COLOR_BLUE.id;
                  } else if (name.equals("brown")) {
                     return MapColor.COLOR_BROWN.id;
                  } else if (name.equals("green")) {
                     return MapColor.COLOR_GREEN.id;
                  } else if (name.equals("red")) {
                     return MapColor.COLOR_RED.id;
                  } else if (name.equals("black")) {
                     return MapColor.COLOR_BLACK.id;
                  } else if (name.equals("white_terracotta")) {
                     return MapColor.TERRACOTTA_WHITE.id;
                  } else if (name.equals("orange_terracotta")) {
                     return MapColor.TERRACOTTA_ORANGE.id;
                  } else if (name.equals("magenta_terracotta")) {
                     return MapColor.TERRACOTTA_MAGENTA.id;
                  } else if (name.equals("light_blue_terracotta")) {
                     return MapColor.TERRACOTTA_LIGHT_BLUE.id;
                  } else if (name.equals("yellow_terracotta")) {
                     return MapColor.TERRACOTTA_YELLOW.id;
                  } else if (name.equals("lime_terracotta")) {
                     return MapColor.TERRACOTTA_LIGHT_GREEN.id;
                  } else if (name.equals("pink_terracotta")) {
                     return MapColor.TERRACOTTA_PINK.id;
                  } else if (name.equals("gray_terracotta")) {
                     return MapColor.TERRACOTTA_GRAY.id;
                  } else if (name.equals("light_gray_terracotta")) {
                     return MapColor.TERRACOTTA_LIGHT_GRAY.id;
                  } else if (name.equals("cyan_terracotta")) {
                     return MapColor.TERRACOTTA_CYAN.id;
                  } else if (name.equals("purple_terracotta")) {
                     return MapColor.TERRACOTTA_PURPLE.id;
                  } else if (name.equals("blue_terracotta")) {
                     return MapColor.TERRACOTTA_BLUE.id;
                  } else if (name.equals("brown_terracotta")) {
                     return MapColor.TERRACOTTA_BROWN.id;
                  } else if (name.equals("green_terracotta")) {
                     return MapColor.TERRACOTTA_GREEN.id;
                  } else if (name.equals("red_terracotta")) {
                     return MapColor.TERRACOTTA_RED.id;
                  } else if (name.equals("black_terracotta")) {
                     return MapColor.TERRACOTTA_BLACK.id;
                  } else if (name.equals("crimson_nylium")) {
                     return MapColor.CRIMSON_NYLIUM.id;
                  } else if (name.equals("crimson_stem")) {
                     return MapColor.CRIMSON_STEM.id;
                  } else if (name.equals("crimson_hyphae")) {
                     return MapColor.CRIMSON_HYPHAE.id;
                  } else if (name.equals("warped_nylium")) {
                     return MapColor.WARPED_NYLIUM.id;
                  } else if (name.equals("warped_stem")) {
                     return MapColor.WARPED_STEM.id;
                  } else if (name.equals("warped_hyphae")) {
                     return MapColor.WARPED_HYPHAE.id;
                  } else if (name.equals("warped_wart_block")) {
                     return MapColor.WARPED_WART_BLOCK.id;
                  } else if (name.equals("deepslate")) {
                     return MapColor.DEEPSLATE.id;
                  } else if (name.equals("raw_iron")) {
                     return MapColor.RAW_IRON.id;
                  } else {
                     return name.equals("glow_lichen") ? MapColor.GLOW_LICHEN.id : -1;
                  }
               } else {
                  return MapColor.COLOR_LIGHT_GRAY.id;
               }
            } else {
               return MapColor.COLOR_LIGHT_BLUE.id;
            }
         } else {
            return MapColor.COLOR_ORANGE.id;
         }
      } else {
         return MapColor.SNOW.id;
      }
   }

   private static int[] getMapColors() {
      MapColor[] amapcolor = MapColor.MATERIAL_COLORS;
      int[] aint = new int[amapcolor.length];
      Arrays.fill(aint, -1);

      for(int i = 0; i < amapcolor.length && i < aint.length; ++i) {
         MapColor mapcolor = amapcolor[i];
         if (mapcolor != null) {
            aint[i] = mapcolor.col;
         }
      }

      return aint;
   }

   private static void setMapColors(int[] colors) {
      if (colors != null) {
         MapColor[] amapcolor = MapColor.MATERIAL_COLORS;

         for(int i = 0; i < amapcolor.length && i < colors.length; ++i) {
            MapColor mapcolor = amapcolor[i];
            if (mapcolor != null) {
               int j = colors[i];
               if (j >= 0 && mapcolor.col != j) {
                  mapcolor.col = j;
               }
            }
         }

      }
   }

   private static float[][] getDyeColors() {
      DyeColor[] adyecolor = DyeColor.values();
      float[][] afloat = new float[adyecolor.length][];

      for(int i = 0; i < adyecolor.length && i < afloat.length; ++i) {
         DyeColor dyecolor = adyecolor[i];
         if (dyecolor != null) {
            afloat[i] = dyecolor.getTextureDiffuseColors();
         }
      }

      return afloat;
   }

   private static void setDyeColors(float[][] colors) {
      if (colors != null) {
         DyeColor[] adyecolor = DyeColor.values();

         for(int i = 0; i < adyecolor.length && i < colors.length; ++i) {
            DyeColor dyecolor = adyecolor[i];
            if (dyecolor != null) {
               float[] afloat = colors[i];
               if (afloat != null && !dyecolor.getTextureDiffuseColors().equals(afloat)) {
                  dyecolor.setColorComponentValues(afloat);
               }
            }
         }

      }
   }

   private static void dbg(String str) {
      Config.dbg("CustomColors: " + str);
   }

   private static void warn(String str) {
      Config.warn("CustomColors: " + str);
   }

   public static int getExpBarTextColor(int color) {
      return expBarTextColor < 0 ? color : expBarTextColor;
   }

   public static int getBossTextColor(int color) {
      return bossTextColor < 0 ? color : bossTextColor;
   }

   public static int getSignTextColor(int color) {
      if (color != 0) {
         return color;
      } else {
         return signTextColor < 0 ? color : signTextColor;
      }
   }

   public interface IColorizer {
      int getColor(BlockState var1, BlockAndTintGetter var2, BlockPos var3);

      boolean isColorConstant();
   }
}
