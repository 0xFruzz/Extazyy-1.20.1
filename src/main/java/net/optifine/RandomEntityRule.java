package net.optifine;

import java.util.Properties;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.config.BiomeId;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchBlock;
import net.optifine.config.MatchProfession;
import net.optifine.config.Matches;
import net.optifine.config.NbtTagValue;
import net.optifine.config.RangeInt;
import net.optifine.config.RangeListInt;
import net.optifine.config.Weather;
import net.optifine.util.ArrayUtils;
import net.optifine.util.MathUtils;

public class RandomEntityRule<T> {
   private String pathProps = null;
   private ResourceLocation baseResLoc = null;
   private int index;
   private RandomEntityContext<T> context;
   private int[] textures = null;
   private T[] resources = null;
   private int[] weights = null;
   private BiomeId[] biomes = null;
   private RangeListInt heights = null;
   private RangeListInt healthRange = null;
   private boolean healthPercent = false;
   private NbtTagValue nbtName = null;
   public int[] sumWeights = null;
   public int sumAllWeights = 1;
   private MatchProfession[] professions = null;
   private DyeColor[] colors = null;
   private Boolean baby = null;
   private RangeListInt moonPhases = null;
   private RangeListInt dayTimes = null;
   private Weather[] weatherList = null;
   private RangeListInt sizes = null;
   public NbtTagValue[] nbtTagValues = null;
   public MatchBlock[] blocks = null;

   public RandomEntityRule(Properties props, String pathProps, ResourceLocation baseResLoc, int index, String valTextures, RandomEntityContext<T> context) {
      this.pathProps = pathProps;
      this.baseResLoc = baseResLoc;
      this.index = index;
      this.context = context;
      ConnectedParser connectedparser = context.getConnectedParser();
      this.textures = connectedparser.parseIntList(valTextures);
      this.weights = connectedparser.parseIntList(props.getProperty("weights." + index));
      this.biomes = connectedparser.parseBiomes(props.getProperty("biomes." + index));
      this.heights = connectedparser.parseRangeListIntNeg(props.getProperty("heights." + index));
      if (this.heights == null) {
         this.heights = this.parseMinMaxHeight(props, index);
      }

      String s = props.getProperty("health." + index);
      if (s != null) {
         this.healthPercent = s.contains("%");
         s = s.replace("%", "");
         this.healthRange = connectedparser.parseRangeListInt(s);
      }

      this.nbtName = connectedparser.parseNbtTagValue("name", props.getProperty("name." + index));
      this.professions = connectedparser.parseProfessions(props.getProperty("professions." + index));
      this.colors = connectedparser.parseDyeColors(props.getProperty("colors." + index), "color", ConnectedParser.DYE_COLORS_INVALID);
      if (this.colors == null) {
         this.colors = connectedparser.parseDyeColors(props.getProperty("collarColors." + index), "collar color", ConnectedParser.DYE_COLORS_INVALID);
      }

      this.baby = connectedparser.parseBooleanObject(props.getProperty("baby." + index));
      this.moonPhases = connectedparser.parseRangeListInt(props.getProperty("moonPhase." + index));
      this.dayTimes = connectedparser.parseRangeListInt(props.getProperty("dayTime." + index));
      this.weatherList = connectedparser.parseWeather(props.getProperty("weather." + index), "weather." + index, (Weather[])null);
      this.sizes = connectedparser.parseRangeListInt(props.getProperty("sizes." + index));
      this.nbtTagValues = connectedparser.parseNbtTagValues(props, "nbt." + index + ".");
      this.blocks = connectedparser.parseMatchBlocks(props.getProperty("blocks." + index));
   }

   public int getIndex() {
      return this.index;
   }

   private RangeListInt parseMinMaxHeight(Properties props, int index) {
      String s = props.getProperty("minHeight." + index);
      String s1 = props.getProperty("maxHeight." + index);
      if (s == null && s1 == null) {
         return null;
      } else {
         int i = 0;
         if (s != null) {
            i = Config.parseInt(s, -1);
            if (i < 0) {
               Config.warn("Invalid minHeight: " + s);
               return null;
            }
         }

         int j = 256;
         if (s1 != null) {
            j = Config.parseInt(s1, -1);
            if (j < 0) {
               Config.warn("Invalid maxHeight: " + s1);
               return null;
            }
         }

         if (j < 0) {
            Config.warn("Invalid minHeight, maxHeight: " + s + ", " + s1);
            return null;
         } else {
            RangeListInt rangelistint = new RangeListInt();
            rangelistint.addRange(new RangeInt(i, j));
            return rangelistint;
         }
      }
   }

   public boolean isValid(String path) {
      String s = this.context.getResourceName();
      String s1 = this.context.getResourceNamePlural();
      if (this.textures != null && this.textures.length != 0) {
         this.resources = (T[])(new Object[this.textures.length]);

         for(int i = 0; i < this.textures.length; ++i) {
            int j = this.textures[i];
            T t = this.context.makeResource(this.baseResLoc, j);
            if (t == null) {
               return false;
            }

            this.resources[i] = t;
         }

         if (this.weights != null) {
            if (this.weights.length > this.resources.length) {
               Config.warn("More weights defined than " + s1 + ", trimming weights: " + path);
               int[] aint = new int[this.resources.length];
               System.arraycopy(this.weights, 0, aint, 0, aint.length);
               this.weights = aint;
            }

            if (this.weights.length < this.resources.length) {
               Config.warn("Less weights defined than " + s1 + ", expanding weights: " + path);
               int[] aint1 = new int[this.resources.length];
               System.arraycopy(this.weights, 0, aint1, 0, this.weights.length);
               int l = MathUtils.getAverage(this.weights);

               for(int j1 = this.weights.length; j1 < aint1.length; ++j1) {
                  aint1[j1] = l;
               }

               this.weights = aint1;
            }

            this.sumWeights = new int[this.weights.length];
            int k = 0;

            for(int i1 = 0; i1 < this.weights.length; ++i1) {
               if (this.weights[i1] < 0) {
                  Config.warn("Invalid weight: " + this.weights[i1]);
                  return false;
               }

               k += this.weights[i1];
               this.sumWeights[i1] = k;
            }

            this.sumAllWeights = k;
            if (this.sumAllWeights <= 0) {
               Config.warn("Invalid sum of all weights: " + k);
               this.sumAllWeights = 1;
            }
         }

         if (this.professions == ConnectedParser.PROFESSIONS_INVALID) {
            Config.warn("Invalid professions or careers: " + path);
            return false;
         } else if (this.colors == ConnectedParser.DYE_COLORS_INVALID) {
            Config.warn("Invalid colors: " + path);
            return false;
         } else {
            return true;
         }
      } else {
         Config.warn("Invalid " + s1 + " for rule: " + this.index);
         return false;
      }
   }

   public boolean matches(IRandomEntity randomEntity) {
      if (this.biomes != null && !Matches.biome(randomEntity.getSpawnBiome(), this.biomes)) {
         return false;
      } else {
         if (this.heights != null) {
            BlockPos blockpos = randomEntity.getSpawnPosition();
            if (blockpos != null && !this.heights.isInRange(blockpos.getY())) {
               return false;
            }
         }

         if (this.healthRange != null) {
            int k = randomEntity.getHealth();
            if (this.healthPercent) {
               int i = randomEntity.getMaxHealth();
               if (i > 0) {
                  k = (int)((double)(k * 100) / (double)i);
               }
            }

            if (!this.healthRange.isInRange(k)) {
               return false;
            }
         }

         if (this.nbtName != null) {
            String s = randomEntity.getName();
            if (!this.nbtName.matchesValue(s)) {
               return false;
            }
         }

         if (this.professions != null && randomEntity instanceof RandomEntity) {
            RandomEntity randomentity = (RandomEntity)randomEntity;
            Entity entity = randomentity.getEntity();
            if (entity instanceof Villager) {
               Villager villager = (Villager)entity;
               VillagerData villagerdata = villager.getVillagerData();
               VillagerProfession villagerprofession = villagerdata.getProfession();
               int j = villagerdata.getLevel();
               if (!MatchProfession.matchesOne(villagerprofession, j, this.professions)) {
                  return false;
               }
            }
         }

         if (this.colors != null) {
            DyeColor dyecolor = randomEntity.getColor();
            if (dyecolor != null && !Config.equalsOne(dyecolor, this.colors)) {
               return false;
            }
         }

         if (this.baby != null && randomEntity instanceof RandomEntity) {
            RandomEntity randomentity1 = (RandomEntity)randomEntity;
            Entity entity1 = randomentity1.getEntity();
            if (entity1 instanceof LivingEntity) {
               LivingEntity livingentity = (LivingEntity)entity1;
               if (livingentity.isBaby() != this.baby) {
                  return false;
               }
            }
         }

         if (this.moonPhases != null) {
            Level level = Config.getMinecraft().level;
            if (level != null) {
               int l = level.getMoonPhase();
               if (!this.moonPhases.isInRange(l)) {
                  return false;
               }
            }
         }

         if (this.dayTimes != null) {
            Level level1 = Config.getMinecraft().level;
            if (level1 != null) {
               int i1 = (int)(level1.getDayTime() % 24000L);
               if (!this.dayTimes.isInRange(i1)) {
                  return false;
               }
            }
         }

         if (this.weatherList != null) {
            Level level2 = Config.getMinecraft().level;
            if (level2 != null) {
               Weather weather = Weather.getWeather(level2, 0.0F);
               if (!ArrayUtils.contains(this.weatherList, weather)) {
                  return false;
               }
            }
         }

         if (this.sizes != null && randomEntity instanceof RandomEntity) {
            RandomEntity randomentity2 = (RandomEntity)randomEntity;
            Entity entity2 = randomentity2.getEntity();
            int k1 = this.getEntitySize(entity2);
            if (k1 >= 0 && !this.sizes.isInRange(k1)) {
               return false;
            }
         }

         if (this.nbtTagValues != null) {
            CompoundTag compoundtag = randomEntity.getNbtTag();
            if (compoundtag != null) {
               for(int j1 = 0; j1 < this.nbtTagValues.length; ++j1) {
                  NbtTagValue nbttagvalue = this.nbtTagValues[j1];
                  if (!nbttagvalue.matches(compoundtag)) {
                     return false;
                  }
               }
            }
         }

         if (this.blocks != null) {
            BlockState blockstate = randomEntity.getBlockState();
            if (blockstate != null && !Matches.block(blockstate, this.blocks)) {
               return false;
            }
         }

         return true;
      }
   }

   public static DyeColor getEntityColor(Entity entity) {
      if (entity instanceof Wolf wolf) {
         return !wolf.isTame() ? null : wolf.getCollarColor();
      } else if (entity instanceof Cat cat) {
         return !cat.isTame() ? null : cat.getCollarColor();
      } else if (entity instanceof Sheep sheep) {
         return sheep.getColor();
      } else if (entity instanceof Llama llama) {
         return llama.getSwag();
      } else {
         return null;
      }
   }

   public static DyeColor getBlockEntityColor(BlockEntity entity) {
      if (entity instanceof BedBlockEntity bedblockentity) {
         return bedblockentity.getColor();
      } else if (entity instanceof ShulkerBoxBlockEntity shulkerboxblockentity) {
         return shulkerboxblockentity.getColor();
      } else {
         return null;
      }
   }

   private int getEntitySize(Entity entity) {
      if (entity instanceof Slime slime) {
         return slime.getSize() - 1;
      } else if (entity instanceof Phantom phantom) {
         return phantom.getPhantomSize();
      } else {
         return -1;
      }
   }

   public T getResource(int randomId, T resDef) {
      if (this.resources != null && this.resources.length != 0) {
         int i = this.getResourceIndex(randomId);
         return this.resources[i];
      } else {
         return resDef;
      }
   }

   private int getResourceIndex(int randomId) {
      int i = 0;
      if (this.weights == null) {
         i = randomId % this.resources.length;
      } else {
         int j = randomId % this.sumAllWeights;

         for(int k = 0; k < this.sumWeights.length; ++k) {
            if (this.sumWeights[k] > j) {
               i = k;
               break;
            }
         }
      }

      return i;
   }

   public T[] getResources() {
      return this.resources;
   }
}