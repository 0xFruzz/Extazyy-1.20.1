package net.optifine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.optifine.reflect.ReflectorRaw;
import net.optifine.util.ArrayUtils;
import net.optifine.util.ResUtils;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;

public class RandomEntities {
   private static Map<String, RandomEntityProperties<ResourceLocation>> mapProperties = new HashMap<>();
   private static Map<String, RandomEntityProperties<ResourceLocation>> mapSpriteProperties = new HashMap<>();
   private static boolean active = false;
   private static EntityRenderDispatcher entityRenderDispatcher;
   private static RandomEntity randomEntity = new RandomEntity();
   private static BlockEntityRenderDispatcher tileEntityRendererDispatcher;
   private static RandomTileEntity randomTileEntity = new RandomTileEntity();
   private static boolean working = false;
   public static final String SUFFIX_PNG = ".png";
   public static final String SUFFIX_PROPERTIES = ".properties";
   public static final String SEPARATOR_DIGITS = ".";
   public static final String PREFIX_TEXTURES_ENTITY = "textures/entity/";
   public static final String PREFIX_TEXTURES_PAINTING = "textures/painting/";
   public static final String PREFIX_TEXTURES = "textures/";
   public static final String PREFIX_OPTIFINE_RANDOM = "optifine/random/";
   public static final String PREFIX_OPTIFINE = "optifine/";
   public static final String PREFIX_OPTIFINE_MOB = "optifine/mob/";
   private static final String[] DEPENDANT_SUFFIXES = new String[]{"_armor", "_eyes", "_exploding", "_shooting", "_fur", "_eyes", "_invulnerable", "_angry", "_tame", "_collar"};
   private static final String PREFIX_DYNAMIC_TEXTURE_HORSE = "horse/";
   private static final String[] HORSE_TEXTURES = (String[])ReflectorRaw.getFieldValue((Object)null, Horse.class, String[].class, 0);
   private static final String[] HORSE_TEXTURES_ABBR = (String[])ReflectorRaw.getFieldValue((Object)null, Horse.class, String[].class, 1);

   public static void entityLoaded(Entity entity, Level world) {
      if (world != null) {
         SynchedEntityData synchedentitydata = entity.getEntityData();
         synchedentitydata.spawnPosition = entity.blockPosition();
         synchedentitydata.spawnBiome = world.getBiome(synchedentitydata.spawnPosition).value();
         if (entity instanceof ShoulderRidingEntity) {
            ShoulderRidingEntity shoulderridingentity = (ShoulderRidingEntity)entity;
            checkEntityShoulder(shoulderridingentity, false);
         }

      }
   }

   public static void entityUnloaded(Entity entity, Level world) {
      if (entity instanceof ShoulderRidingEntity shoulderridingentity) {
         checkEntityShoulder(shoulderridingentity, true);
      }

   }

   public static void checkEntityShoulder(ShoulderRidingEntity entity, boolean attach) {
      LivingEntity livingentity = entity.getOwner();
      if (livingentity == null) {
         livingentity = Config.getMinecraft().player;
      }

      if (livingentity instanceof AbstractClientPlayer abstractclientplayer) {
         UUID uuid = entity.getUUID();
         if (attach) {
            abstractclientplayer.lastAttachedEntity = entity;
            CompoundTag compoundtag = abstractclientplayer.getShoulderEntityLeft();
            if (compoundtag != null && compoundtag.contains("UUID") && Config.equals(compoundtag.getUUID("UUID"), uuid)) {
               abstractclientplayer.entityShoulderLeft = entity;
               abstractclientplayer.lastAttachedEntity = null;
            }

            CompoundTag compoundtag1 = abstractclientplayer.getShoulderEntityRight();
            if (compoundtag1 != null && compoundtag1.contains("UUID") && Config.equals(compoundtag1.getUUID("UUID"), uuid)) {
               abstractclientplayer.entityShoulderRight = entity;
               abstractclientplayer.lastAttachedEntity = null;
            }
         } else {
            SynchedEntityData synchedentitydata = entity.getEntityData();
            if (abstractclientplayer.entityShoulderLeft != null && Config.equals(abstractclientplayer.entityShoulderLeft.getUUID(), uuid)) {
               SynchedEntityData synchedentitydata1 = abstractclientplayer.entityShoulderLeft.getEntityData();
               synchedentitydata.spawnPosition = synchedentitydata1.spawnPosition;
               synchedentitydata.spawnBiome = synchedentitydata1.spawnBiome;
               abstractclientplayer.entityShoulderLeft = null;
            }

            if (abstractclientplayer.entityShoulderRight != null && Config.equals(abstractclientplayer.entityShoulderRight.getUUID(), uuid)) {
               SynchedEntityData synchedentitydata2 = abstractclientplayer.entityShoulderRight.getEntityData();
               synchedentitydata.spawnPosition = synchedentitydata2.spawnPosition;
               synchedentitydata.spawnBiome = synchedentitydata2.spawnBiome;
               abstractclientplayer.entityShoulderRight = null;
            }
         }

      }
   }

   public static void worldChanged(Level oldWorld, Level newWorld) {
      if (newWorld instanceof ClientLevel clientlevel) {
         for(Entity entity : clientlevel.entitiesForRendering()) {
            entityLoaded(entity, newWorld);
         }
      }

      randomEntity.setEntity((Entity)null);
      randomTileEntity.setTileEntity((BlockEntity)null);
   }

   public static ResourceLocation getTextureLocation(ResourceLocation loc) {
      if (!active) {
         return loc;
      } else {
         IRandomEntity irandomentity = getRandomEntityRendered();
         if (irandomentity == null) {
            return loc;
         } else if (working) {
            return loc;
         } else {
            ResourceLocation props;
            try {
               working = true;
               String s = loc.getPath();
               if (s.startsWith("horse/")) {
                  s = getHorseTexturePath(s, "horse/".length());
               }

               if (s.startsWith("textures/entity/") || s.startsWith("textures/painting/")) {
                  RandomEntityProperties<ResourceLocation> randomentityproperties = mapProperties.get(s);
                  if (randomentityproperties == null) {
                     return loc;
                  }

                  return randomentityproperties.getResource(irandomentity, loc);
               }

               props = loc;
            } finally {
               working = false;
            }

            return props;
         }
      }
   }

   private static String getHorseTexturePath(String path, int pos) {
      if (HORSE_TEXTURES != null && HORSE_TEXTURES_ABBR != null) {
         for(int i = 0; i < HORSE_TEXTURES_ABBR.length; ++i) {
            String s = HORSE_TEXTURES_ABBR[i];
            if (path.startsWith(s, pos)) {
               return HORSE_TEXTURES[i];
            }
         }

         return path;
      } else {
         return path;
      }
   }

   public static IRandomEntity getRandomEntityRendered() {
      if (entityRenderDispatcher.getRenderedEntity() != null) {
         randomEntity.setEntity(entityRenderDispatcher.getRenderedEntity());
         return randomEntity;
      } else {
         BlockEntityRenderDispatcher blockentityrenderdispatcher = tileEntityRendererDispatcher;
         if (BlockEntityRenderDispatcher.tileEntityRendered != null) {
            blockentityrenderdispatcher = tileEntityRendererDispatcher;
            BlockEntity blockentity = BlockEntityRenderDispatcher.tileEntityRendered;
            if (blockentity.getLevel() != null) {
               randomTileEntity.setTileEntity(blockentity);
               return randomTileEntity;
            }
         }

         return null;
      }
   }

   public static IRandomEntity getRandomEntity(Entity entityIn) {
      randomEntity.setEntity(entityIn);
      return randomEntity;
   }

   public static IRandomEntity getRandomBlockEntity(BlockEntity tileEntityIn) {
      randomTileEntity.setTileEntity(tileEntityIn);
      return randomTileEntity;
   }

   private static RandomEntityProperties<ResourceLocation> makeProperties(ResourceLocation loc, RandomEntityContext.Textures context) {
      String s = loc.getPath();
      ResourceLocation resourcelocation = getLocationProperties(loc, context.isLegacy());
      if (resourcelocation != null) {
         RandomEntityProperties randomentityproperties = RandomEntityProperties.parse(resourcelocation, loc, context);
         if (randomentityproperties != null) {
            return randomentityproperties;
         }
      }

      int[] aint = getLocationsVariants(loc, context.isLegacy(), context);
      return aint == null ? null : new RandomEntityProperties<>(s, loc, aint, context);
   }

   private static ResourceLocation getLocationProperties(ResourceLocation loc, boolean legacy) {
      ResourceLocation resourcelocation = getLocationRandom(loc, legacy);
      if (resourcelocation == null) {
         return null;
      } else {
         String s = resourcelocation.getNamespace();
         String s1 = resourcelocation.getPath();
         String s2 = StrUtils.removeSuffix(s1, ".png");
         String s3 = s2 + ".properties";
         ResourceLocation resourcelocation1 = new ResourceLocation(s, s3);
         if (Config.hasResource(resourcelocation1)) {
            return resourcelocation1;
         } else {
            String s4 = getParentTexturePath(s2);
            if (s4 == null) {
               return null;
            } else {
               ResourceLocation resourcelocation2 = new ResourceLocation(s, s4 + ".properties");
               return Config.hasResource(resourcelocation2) ? resourcelocation2 : null;
            }
         }
      }
   }

   protected static ResourceLocation getLocationRandom(ResourceLocation loc, boolean legacy) {
      String s = loc.getNamespace();
      String s1 = loc.getPath();
      if (s1.startsWith("optifine/")) {
         return loc;
      } else {
         String s2 = "textures/";
         String s3 = "optifine/random/";
         if (legacy) {
            s2 = "textures/entity/";
            s3 = "optifine/mob/";
         }

         if (!s1.startsWith(s2)) {
            return null;
         } else {
            String s4 = StrUtils.replacePrefix(s1, s2, s3);
            return new ResourceLocation(s, s4);
         }
      }
   }

   private static String getPathBase(String pathRandom) {
      if (pathRandom.startsWith("optifine/random/")) {
         return StrUtils.replacePrefix(pathRandom, "optifine/random/", "textures/");
      } else {
         return pathRandom.startsWith("optifine/mob/") ? StrUtils.replacePrefix(pathRandom, "optifine/mob/", "textures/entity/") : null;
      }
   }

   protected static ResourceLocation getLocationIndexed(ResourceLocation loc, int index) {
      if (loc == null) {
         return null;
      } else {
         String s = loc.getPath();
         int i = s.lastIndexOf(46);
         if (i < 0) {
            return null;
         } else {
            String s1 = s.substring(0, i);
            String s2 = s.substring(i);
            String s3 = StrUtils.endsWithDigit(s1) ? "." : "";
            String s4 = s1 + s3 + index + s2;
            return new ResourceLocation(loc.getNamespace(), s4);
         }
      }
   }

   private static String getParentTexturePath(String path) {
      for(int i = 0; i < DEPENDANT_SUFFIXES.length; ++i) {
         String s = DEPENDANT_SUFFIXES[i];
         if (path.endsWith(s)) {
            return StrUtils.removeSuffix(path, s);
         }
      }

      return null;
   }

   public static int[] getLocationsVariants(ResourceLocation loc, boolean legacy, RandomEntityContext context) {
      List<Integer> list = new ArrayList<>();
      list.add(1);
      ResourceLocation resourcelocation = getLocationRandom(loc, legacy);
      if (resourcelocation == null) {
         return null;
      } else {
         for(int i = 1; i < list.size() + 10; ++i) {
            int j = i + 1;
            ResourceLocation resourcelocation1 = getLocationIndexed(resourcelocation, j);
            if (Config.hasResource(resourcelocation1)) {
               list.add(j);
            }
         }

         if (list.size() <= 1) {
            return null;
         } else {
            Integer[] ainteger = list.toArray(new Integer[list.size()]);
            int[] aint = ArrayUtils.toPrimitive(ainteger);
            Config.dbg(context.getName() + ": " + loc.getPath() + ", variants: " + aint.length);
            return aint;
         }
      }
   }

   public static void update() {
      entityRenderDispatcher = Config.getEntityRenderDispatcher();
      tileEntityRendererDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
      mapProperties.clear();
      mapSpriteProperties.clear();
      active = false;
      if (Config.isRandomEntities()) {
         initialize();
      }
   }

   private static void initialize() {
      String[] astring = new String[]{"optifine/random/", "optifine/mob/"};
      String[] astring1 = new String[]{".png", ".properties"};
      String[] astring2 = ResUtils.collectFiles(astring, astring1);
      Set set = new HashSet();

      for(int i = 0; i < astring2.length; ++i) {
         String s = astring2[i];
         s = StrUtils.removeSuffix(s, astring1);
         s = StrUtils.trimTrailing(s, "0123456789");
         s = StrUtils.removeSuffix(s, ".");
         s = s + ".png";
         String s1 = getPathBase(s);
         if (!set.contains(s1)) {
            set.add(s1);
            ResourceLocation resourcelocation = new ResourceLocation(s1);
            if (Config.hasResource(resourcelocation)) {
               RandomEntityProperties<ResourceLocation> randomentityproperties = mapProperties.get(s1);
               if (randomentityproperties == null) {
                  randomentityproperties = makeProperties(resourcelocation, new RandomEntityContext.Textures(false));
                  if (randomentityproperties == null) {
                     randomentityproperties = makeProperties(resourcelocation, new RandomEntityContext.Textures(true));
                  }

                  if (randomentityproperties != null) {
                     mapProperties.put(s1, randomentityproperties);
                  }
               }
            }
         }
      }

      active = !mapProperties.isEmpty();
   }

   public static synchronized void registerSprites(Set<ResourceLocation> spriteLocations) {
      if (!mapProperties.isEmpty()) {
         Set<ResourceLocation> set = new HashSet<>();

         for(ResourceLocation resourcelocation : spriteLocations) {
            String s = "textures/" + resourcelocation.getPath() + ".png";
            RandomEntityProperties<ResourceLocation> randomentityproperties = mapProperties.get(s);
            if (randomentityproperties != null) {
               mapSpriteProperties.put(resourcelocation.getPath(), randomentityproperties);
               List<ResourceLocation> list = randomentityproperties.getAllResources();
               if (list != null) {
                  for(int i = 0; i < list.size(); ++i) {
                     ResourceLocation resourcelocation1 = list.get(i);
                     ResourceLocation resourcelocation2 = TextureUtils.getSpriteLocation(resourcelocation1);
                     set.add(resourcelocation2);
                     mapSpriteProperties.put(resourcelocation2.getPath(), randomentityproperties);
                  }
               }
            }
         }

         spriteLocations.addAll(set);
      }
   }

   public static TextureAtlasSprite getRandomSprite(TextureAtlasSprite spriteIn) {
      if (!active) {
         return spriteIn;
      } else {
         IRandomEntity irandomentity = getRandomEntityRendered();
         if (irandomentity == null) {
            return spriteIn;
         } else if (working) {
            return spriteIn;
         } else {
            TextureAtlasSprite loc;
            try {
               working = true;
               ResourceLocation resourcelocation = spriteIn.getName();
               String s = resourcelocation.getPath();
               RandomEntityProperties<ResourceLocation> randomentityproperties = mapSpriteProperties.get(s);
               if (randomentityproperties != null) {
                  ResourceLocation resourcelocation2 = randomentityproperties.getResource(irandomentity, resourcelocation);
                  if (resourcelocation2 == resourcelocation) {
                     return spriteIn;
                  }

                  ResourceLocation resourcelocation1 = TextureUtils.getSpriteLocation(resourcelocation2);
                  return spriteIn.getTextureAtlas().getSprite(resourcelocation1);
               }

               loc = spriteIn;
            } finally {
               working = false;
            }

            return loc;
         }
      }
   }

   public static void dbg(String str) {
      Config.dbg("RandomEntities: " + str);
   }

   public static void warn(String str) {
      Config.warn("RandomEntities: " + str);
   }
}