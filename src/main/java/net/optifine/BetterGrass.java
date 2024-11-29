package net.optifine;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.MyceliumBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.model.BlockModelUtils;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.RandomUtils;

public class BetterGrass {
   private static boolean betterGrass = true;
   private static boolean betterDirtPath = true;
   private static boolean betterMycelium = true;
   private static boolean betterPodzol = true;
   private static boolean betterCrimsonNylium = true;
   private static boolean betterWarpedNylium = true;
   private static boolean betterGrassSnow = true;
   private static boolean betterMyceliumSnow = true;
   private static boolean betterPodzolSnow = true;
   private static boolean grassMultilayer = false;
   private static TextureAtlasSprite spriteGrass = null;
   private static TextureAtlasSprite spriteGrassSide = null;
   private static TextureAtlasSprite spriteDirtPath = null;
   private static TextureAtlasSprite spriteDirtPathSide = null;
   private static TextureAtlasSprite spriteMycelium = null;
   private static TextureAtlasSprite spritePodzol = null;
   private static TextureAtlasSprite spriteCrimsonNylium = null;
   private static TextureAtlasSprite spriteWarpedNylium = null;
   private static TextureAtlasSprite spriteSnow = null;
   private static boolean spritesLoaded = false;
   private static BakedModel modelCubeGrass = null;
   private static BakedModel modelDirtPath = null;
   private static BakedModel modelCubeDirtPath = null;
   private static BakedModel modelCubeMycelium = null;
   private static BakedModel modelCubePodzol = null;
   private static BakedModel modelCubeCrimsonNylium = null;
   private static BakedModel modelCubeWarpedNylium = null;
   private static BakedModel modelCubeSnow = null;
   private static boolean modelsLoaded = false;
   private static final String TEXTURE_GRASS_DEFAULT = "block/grass_block_top";
   private static final String TEXTURE_GRASS_SIDE_DEFAULT = "block/grass_block_side";
   private static final String TEXTURE_DIRT_PATH_DEFAULT = "block/dirt_path_top";
   private static final String TEXTURE_DIRT_PATH_SIDE_DEFAULT = "block/dirt_path_side";
   private static final String TEXTURE_MYCELIUM_DEFAULT = "block/mycelium_top";
   private static final String TEXTURE_PODZOL_DEFAULT = "block/podzol_top";
   private static final String TEXTURE_CRIMSON_NYLIUM = "block/crimson_nylium";
   private static final String TEXTURE_WARPED_NYLIUM = "block/warped_nylium";
   private static final String TEXTURE_SNOW_DEFAULT = "block/snow";
   private static final RandomSource RANDOM = RandomUtils.makeThreadSafeRandomSource(0);

   public static void updateIcons(TextureAtlas textureMap) {
      spritesLoaded = false;
      modelsLoaded = false;
      loadProperties(textureMap);
   }

   public static void update() {
      if (spritesLoaded) {
         modelCubeGrass = BlockModelUtils.makeModelCube(spriteGrass, 0);
         if (grassMultilayer) {
            BakedModel bakedmodel = BlockModelUtils.makeModelCube(spriteGrassSide, -1);
            modelCubeGrass = BlockModelUtils.joinModelsCube(bakedmodel, modelCubeGrass);
         }

         modelDirtPath = BlockModelUtils.makeModel("dirt_path", spriteDirtPathSide, spriteDirtPath);
         modelCubeDirtPath = BlockModelUtils.makeModelCube(spriteDirtPath, -1);
         modelCubeMycelium = BlockModelUtils.makeModelCube(spriteMycelium, -1);
         modelCubePodzol = BlockModelUtils.makeModelCube(spritePodzol, 0);
         modelCubeCrimsonNylium = BlockModelUtils.makeModelCube(spriteCrimsonNylium, -1);
         modelCubeWarpedNylium = BlockModelUtils.makeModelCube(spriteWarpedNylium, -1);
         modelCubeSnow = BlockModelUtils.makeModelCube(spriteSnow, -1);
         modelsLoaded = true;
      }
   }

   private static void loadProperties(TextureAtlas textureMap) {
      betterGrass = true;
      betterDirtPath = true;
      betterMycelium = true;
      betterPodzol = true;
      betterCrimsonNylium = true;
      betterWarpedNylium = true;
      betterGrassSnow = true;
      betterMyceliumSnow = true;
      betterPodzolSnow = true;
      spriteGrass = textureMap.registerSprite(new ResourceLocation("block/grass_block_top"));
      spriteGrassSide = textureMap.registerSprite(new ResourceLocation("block/grass_block_side"));
      spriteDirtPath = textureMap.registerSprite(new ResourceLocation("block/dirt_path_top"));
      spriteDirtPathSide = textureMap.registerSprite(new ResourceLocation("block/dirt_path_side"));
      spriteMycelium = textureMap.registerSprite(new ResourceLocation("block/mycelium_top"));
      spritePodzol = textureMap.registerSprite(new ResourceLocation("block/podzol_top"));
      spriteCrimsonNylium = textureMap.registerSprite(new ResourceLocation("block/crimson_nylium"));
      spriteWarpedNylium = textureMap.registerSprite(new ResourceLocation("block/warped_nylium"));
      spriteSnow = textureMap.registerSprite(new ResourceLocation("block/snow"));
      spritesLoaded = true;
      String s = "optifine/bettergrass.properties";

      try {
         ResourceLocation resourcelocation = new ResourceLocation(s);
         if (!Config.hasResource(resourcelocation)) {
            return;
         }

         InputStream inputstream = Config.getResourceStream(resourcelocation);
         if (inputstream == null) {
            return;
         }

         boolean flag = Config.isFromDefaultResourcePack(resourcelocation);
         if (flag) {
            Config.dbg("BetterGrass: Parsing default configuration " + s);
         } else {
            Config.dbg("BetterGrass: Parsing configuration " + s);
         }

         Properties properties = new PropertiesOrdered();
         properties.load(inputstream);
         inputstream.close();
         betterGrass = getBoolean(properties, "grass", true);
         betterDirtPath = getBoolean(properties, "dirt_path", true);
         betterMycelium = getBoolean(properties, "mycelium", true);
         betterPodzol = getBoolean(properties, "podzol", true);
         betterCrimsonNylium = getBoolean(properties, "crimson_nylium", true);
         betterWarpedNylium = getBoolean(properties, "warped_nylium", true);
         betterGrassSnow = getBoolean(properties, "grass.snow", true);
         betterMyceliumSnow = getBoolean(properties, "mycelium.snow", true);
         betterPodzolSnow = getBoolean(properties, "podzol.snow", true);
         grassMultilayer = getBoolean(properties, "grass.multilayer", false);
         spriteGrass = registerSprite(properties, "texture.grass", "block/grass_block_top", textureMap);
         spriteGrassSide = registerSprite(properties, "texture.grass_side", "block/grass_block_side", textureMap);
         spriteDirtPath = registerSprite(properties, "texture.dirt_path", "block/dirt_path_top", textureMap);
         spriteDirtPathSide = registerSprite(properties, "texture.dirt_path_side", "block/dirt_path_side", textureMap);
         spriteMycelium = registerSprite(properties, "texture.mycelium", "block/mycelium_top", textureMap);
         spritePodzol = registerSprite(properties, "texture.podzol", "block/podzol_top", textureMap);
         spriteCrimsonNylium = registerSprite(properties, "texture.crimson_nylium", "block/crimson_nylium", textureMap);
         spriteWarpedNylium = registerSprite(properties, "texture.warped_nylium", "block/warped_nylium", textureMap);
         spriteSnow = registerSprite(properties, "texture.snow", "block/snow", textureMap);
      } catch (IOException ioexception) {
         Config.warn("Error reading: " + s + ", " + ioexception.getClass().getName() + ": " + ioexception.getMessage());
      }

   }

   public static void refreshIcons(TextureAtlas textureMap) {
      spriteGrass = getSprite(textureMap, spriteGrass.getName());
      spriteGrassSide = getSprite(textureMap, spriteGrassSide.getName());
      spriteDirtPath = getSprite(textureMap, spriteDirtPath.getName());
      spriteDirtPathSide = getSprite(textureMap, spriteDirtPathSide.getName());
      spriteMycelium = getSprite(textureMap, spriteMycelium.getName());
      spritePodzol = getSprite(textureMap, spritePodzol.getName());
      spriteCrimsonNylium = getSprite(textureMap, spriteCrimsonNylium.getName());
      spriteWarpedNylium = getSprite(textureMap, spriteWarpedNylium.getName());
      spriteSnow = getSprite(textureMap, spriteSnow.getName());
   }

   private static TextureAtlasSprite getSprite(TextureAtlas textureMap, ResourceLocation loc) {
      TextureAtlasSprite textureatlassprite = textureMap.getSprite(loc);
      if (textureatlassprite == null || MissingTextureAtlasSprite.isMisingSprite(textureatlassprite)) {
         Config.warn("Missing BetterGrass sprite: " + loc);
      }

      return textureatlassprite;
   }

   private static TextureAtlasSprite registerSprite(Properties props, String key, String textureDefault, TextureAtlas textureMap) {
      String s = props.getProperty(key);
      if (s == null) {
         s = textureDefault;
      }

      ResourceLocation resourcelocation = new ResourceLocation("textures/" + s + ".png");
      if (!Config.hasResource(resourcelocation)) {
         Config.warn("BetterGrass texture not found: " + resourcelocation);
         s = textureDefault;
      }

      ResourceLocation resourcelocation1 = new ResourceLocation(s);
      return textureMap.registerSprite(resourcelocation1);
   }

   public static List getFaceQuads(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
      if (facing != Direction.UP && facing != Direction.DOWN) {
         if (!modelsLoaded) {
            return quads;
         } else {
            Block block = blockState.getBlock();
            if (block instanceof MyceliumBlock) {
               return getFaceQuadsMycelium(blockAccess, blockState, blockPos, facing, quads);
            } else if (block instanceof DirtPathBlock) {
               return getFaceQuadsDirtPath(blockAccess, blockState, blockPos, facing, quads);
            } else if (block == Blocks.PODZOL) {
               return getFaceQuadsPodzol(blockAccess, blockState, blockPos, facing, quads);
            } else if (block == Blocks.CRIMSON_NYLIUM) {
               return getFaceQuadsCrimsonNylium(blockAccess, blockState, blockPos, facing, quads);
            } else if (block == Blocks.WARPED_NYLIUM) {
               return getFaceQuadsWarpedNylium(blockAccess, blockState, blockPos, facing, quads);
            } else if (block == Blocks.DIRT) {
               return getFaceQuadsDirt(blockAccess, blockState, blockPos, facing, quads);
            } else {
               return block instanceof GrassBlock ? getFaceQuadsGrass(blockAccess, blockState, blockPos, facing, quads) : quads;
            }
         }
      } else {
         return quads;
      }
   }

   private static List getFaceQuadsMycelium(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
      Block block = blockAccess.getBlockState(blockPos.above()).getBlock();
      boolean flag = block == Blocks.SNOW_BLOCK || block == Blocks.SNOW;
      if (Config.isBetterGrassFancy()) {
         if (flag) {
            if (betterMyceliumSnow && getBlockAt(blockPos, facing, blockAccess) == Blocks.SNOW) {
               return modelCubeSnow.getQuads(blockState, facing, RANDOM);
            }
         } else if (betterMycelium && getBlockAt(blockPos.below(), facing, blockAccess) == Blocks.MYCELIUM) {
            return modelCubeMycelium.getQuads(blockState, facing, RANDOM);
         }
      } else if (flag) {
         if (betterMyceliumSnow) {
            return modelCubeSnow.getQuads(blockState, facing, RANDOM);
         }
      } else if (betterMycelium) {
         return modelCubeMycelium.getQuads(blockState, facing, RANDOM);
      }

      return quads;
   }

   private static List getFaceQuadsDirtPath(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
      if (!betterDirtPath) {
         return quads;
      } else if (Config.isBetterGrassFancy()) {
         return getBlockAt(blockPos.below(), facing, blockAccess) == Blocks.DIRT_PATH ? modelDirtPath.getQuads(blockState, facing, RANDOM) : quads;
      } else {
         return modelDirtPath.getQuads(blockState, facing, RANDOM);
      }
   }

   private static List getFaceQuadsPodzol(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
      Block block = getBlockAt(blockPos, Direction.UP, blockAccess);
      boolean flag = block == Blocks.SNOW_BLOCK || block == Blocks.SNOW;
      if (Config.isBetterGrassFancy()) {
         if (flag) {
            if (betterPodzolSnow && getBlockAt(blockPos, facing, blockAccess) == Blocks.SNOW) {
               return modelCubeSnow.getQuads(blockState, facing, RANDOM);
            }
         } else if (betterPodzol) {
            BlockPos blockpos = blockPos.below().relative(facing);
            BlockState blockstate = blockAccess.getBlockState(blockpos);
            if (blockstate.getBlock() == Blocks.PODZOL) {
               return modelCubePodzol.getQuads(blockState, facing, RANDOM);
            }
         }
      } else if (flag) {
         if (betterPodzolSnow) {
            return modelCubeSnow.getQuads(blockState, facing, RANDOM);
         }
      } else if (betterPodzol) {
         return modelCubePodzol.getQuads(blockState, facing, RANDOM);
      }

      return quads;
   }

   private static List getFaceQuadsCrimsonNylium(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
      if (!betterCrimsonNylium) {
         return quads;
      } else if (Config.isBetterGrassFancy()) {
         return getBlockAt(blockPos.below(), facing, blockAccess) == Blocks.CRIMSON_NYLIUM ? modelCubeCrimsonNylium.getQuads(blockState, facing, RANDOM) : quads;
      } else {
         return modelCubeCrimsonNylium.getQuads(blockState, facing, RANDOM);
      }
   }

   private static List getFaceQuadsWarpedNylium(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
      if (!betterWarpedNylium) {
         return quads;
      } else if (Config.isBetterGrassFancy()) {
         return getBlockAt(blockPos.below(), facing, blockAccess) == Blocks.WARPED_NYLIUM ? modelCubeWarpedNylium.getQuads(blockState, facing, RANDOM) : quads;
      } else {
         return modelCubeWarpedNylium.getQuads(blockState, facing, RANDOM);
      }
   }

   private static List getFaceQuadsDirt(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
      Block block = getBlockAt(blockPos, Direction.UP, blockAccess);
      return block == Blocks.DIRT_PATH && betterDirtPath && getBlockAt(blockPos, facing, blockAccess) == Blocks.DIRT_PATH ? modelCubeDirtPath.getQuads(blockState, facing, RANDOM) : quads;
   }

   private static List getFaceQuadsGrass(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
      Block block = blockAccess.getBlockState(blockPos.above()).getBlock();
      boolean flag = block == Blocks.SNOW_BLOCK || block == Blocks.SNOW;
      if (Config.isBetterGrassFancy()) {
         if (flag) {
            if (betterGrassSnow && getBlockAt(blockPos, facing, blockAccess) == Blocks.SNOW) {
               return modelCubeSnow.getQuads(blockState, facing, RANDOM);
            }
         } else if (betterGrass && getBlockAt(blockPos.below(), facing, blockAccess) == Blocks.GRASS_BLOCK) {
            return modelCubeGrass.getQuads(blockState, facing, RANDOM);
         }
      } else if (flag) {
         if (betterGrassSnow) {
            return modelCubeSnow.getQuads(blockState, facing, RANDOM);
         }
      } else if (betterGrass) {
         return modelCubeGrass.getQuads(blockState, facing, RANDOM);
      }

      return quads;
   }

   private static Block getBlockAt(BlockPos blockPos, Direction facing, BlockGetter blockAccess) {
      BlockPos blockpos = blockPos.relative(facing);
      return blockAccess.getBlockState(blockpos).getBlock();
   }

   private static boolean getBoolean(Properties props, String key, boolean def) {
      String s = props.getProperty(key);
      return s == null ? def : Boolean.parseBoolean(s);
   }
}