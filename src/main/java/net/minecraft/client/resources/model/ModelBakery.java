package net.minecraft.client.resources.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.optifine.Config;
import net.optifine.reflect.Reflector;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;
import org.slf4j.Logger;

public class ModelBakery {
   public static final Material FIRE_0 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/fire_0"));
   public static final Material FIRE_1 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/fire_1"));
   public static final Material LAVA_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/lava_flow"));
   public static final Material WATER_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/water_flow"));
   public static final Material WATER_OVERLAY = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/water_overlay"));
   public static final Material BANNER_BASE = new Material(Sheets.BANNER_SHEET, new ResourceLocation("entity/banner_base"));
   public static final Material SHIELD_BASE = new Material(Sheets.SHIELD_SHEET, new ResourceLocation("entity/shield_base"));
   public static final Material NO_PATTERN_SHIELD = new Material(Sheets.SHIELD_SHEET, new ResourceLocation("entity/shield_base_nopattern"));
   public static final int DESTROY_STAGE_COUNT = 10;
   public static final List<ResourceLocation> DESTROY_STAGES = IntStream.range(0, 10).mapToObj((indexIn) -> {
      return new ResourceLocation("block/destroy_stage_" + indexIn);
   }).collect(Collectors.toList());
   public static final List<ResourceLocation> BREAKING_LOCATIONS = DESTROY_STAGES.stream().map((locationIn) -> {
      return new ResourceLocation("textures/" + locationIn.getPath() + ".png");
   }).collect(Collectors.toList());
   public static final List<RenderType> DESTROY_TYPES = BREAKING_LOCATIONS.stream().map(RenderType::crumbling).collect(Collectors.toList());
   static final int SINGLETON_MODEL_GROUP = -1;
   private static final int INVISIBLE_MODEL_GROUP = 0;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String BUILTIN_SLASH = "builtin/";
   private static final String BUILTIN_SLASH_GENERATED = "builtin/generated";
   private static final String BUILTIN_BLOCK_ENTITY = "builtin/entity";
   private static final String MISSING_MODEL_NAME = "missing";
   public static final ModelResourceLocation MISSING_MODEL_LOCATION = ModelResourceLocation.vanilla("builtin/missing", "missing");
   public static final FileToIdConverter BLOCKSTATE_LISTER = FileToIdConverter.json("blockstates");
   public static final FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models");
   @VisibleForTesting
   public static final String MISSING_MODEL_MESH = ("{    'textures': {       'particle': '" + MissingTextureAtlasSprite.getLocation().getPath() + "',       'missingno': '" + MissingTextureAtlasSprite.getLocation().getPath() + "'    },    'elements': [         {  'from': [ 0, 0, 0 ],            'to': [ 16, 16, 16 ],            'faces': {                'down':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'down',  'texture': '#missingno' },                'up':    { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'up',    'texture': '#missingno' },                'north': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'north', 'texture': '#missingno' },                'south': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'south', 'texture': '#missingno' },                'west':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'west',  'texture': '#missingno' },                'east':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'east',  'texture': '#missingno' }            }        }    ]}").replace('\'', '"');
   private static final Map<String, String> BUILTIN_MODELS = Maps.newHashMap(ImmutableMap.of("missing", MISSING_MODEL_MESH));
   private static final Splitter COMMA_SPLITTER = Splitter.on(',');
   private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);
   public static final BlockModel GENERATION_MARKER = Util.make(BlockModel.fromString("{\"gui_light\": \"front\"}"), (modelIn) -> {
      modelIn.name = "generation marker";
   });
   public static final BlockModel BLOCK_ENTITY_MARKER = Util.make(BlockModel.fromString("{\"gui_light\": \"side\"}"), (modelIn) -> {
      modelIn.name = "block entity marker";
   });
   private static final StateDefinition<Block, BlockState> ITEM_FRAME_FAKE_DEFINITION = (new StateDefinition.Builder<Block, BlockState>(Blocks.AIR)).add(BooleanProperty.create("map")).create(Block::defaultBlockState, BlockState::new);
   static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
   private static final Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS = ImmutableMap.of(new ResourceLocation("item_frame"), ITEM_FRAME_FAKE_DEFINITION, new ResourceLocation("glow_item_frame"), ITEM_FRAME_FAKE_DEFINITION);
   private final BlockColors blockColors;
   private final Map<ResourceLocation, BlockModel> modelResources;
   private final Map<ResourceLocation, List<ModelBakery.LoadedJson>> blockStateResources;
   private final Set<ResourceLocation> loadingStack = Sets.newHashSet();
   private final BlockModelDefinition.Context context = new BlockModelDefinition.Context();
   private final Map<ResourceLocation, UnbakedModel> unbakedCache = Maps.newHashMap();
   final Map<ModelBakery.BakedCacheKey, BakedModel> bakedCache = Maps.newHashMap();
   private final Map<ResourceLocation, UnbakedModel> topLevelModels = Maps.newHashMap();
   private final Map<ResourceLocation, BakedModel> bakedTopLevelModels = Maps.newHashMap();
   private int nextModelGroup = 1;
   private final Object2IntMap<BlockState> modelGroups = Util.make(new Object2IntOpenHashMap<>(), (mapIn) -> {
      mapIn.defaultReturnValue(-1);
   });
   public Map<ResourceLocation, UnbakedModel> mapUnbakedModels;

   public ModelBakery(BlockColors pBlockColors, ProfilerFiller pProfilerFiller, Map<ResourceLocation, BlockModel> pModelResources, Map<ResourceLocation, List<ModelBakery.LoadedJson>> pBlockStateResources) {
      this.blockColors = pBlockColors;
      this.modelResources = new HashMap<>(pModelResources);
      this.blockStateResources = pBlockStateResources;
      pProfilerFiller.push("missing_model");

      try {
         this.unbakedCache.put(MISSING_MODEL_LOCATION, this.loadBlockModel(MISSING_MODEL_LOCATION));
         this.loadTopLevel(MISSING_MODEL_LOCATION);
      } catch (IOException ioexception) {
         LOGGER.error("Error loading missing model, should never happen :(", (Throwable)ioexception);
         throw new RuntimeException(ioexception);
      }

      pProfilerFiller.popPush("static_definitions");
      STATIC_DEFINITIONS.forEach((locationIn, definitionIn) -> {
         definitionIn.getPossibleStates().forEach((stateIn) -> {
            this.loadTopLevel(BlockModelShaper.stateToModelLocation(locationIn, stateIn));
         });
      });
      pProfilerFiller.popPush("blocks");

      for(Block block : BuiltInRegistries.BLOCK) {
         block.getStateDefinition().getPossibleStates().forEach((stateIn) -> {
            this.loadTopLevel(BlockModelShaper.stateToModelLocation(stateIn));
         });
      }

      pProfilerFiller.popPush("items");

      for(ResourceLocation resourcelocation1 : BuiltInRegistries.ITEM.keySet()) {
         this.loadTopLevel(new ModelResourceLocation(resourcelocation1, "inventory"));
      }

      pProfilerFiller.popPush("special");
      this.loadTopLevel(ItemRenderer.TRIDENT_IN_HAND_MODEL);
      this.loadTopLevel(ItemRenderer.SPYGLASS_IN_HAND_MODEL);
      Set<ResourceLocation> set = Sets.newHashSet();
      Reflector.ForgeHooksClient_onRegisterAdditionalModels.call((Object)set);

      for(ResourceLocation resourcelocation : set) {
         UnbakedModel unbakedmodel = this.getModel(resourcelocation);
         this.unbakedCache.put(resourcelocation, unbakedmodel);
         this.topLevelModels.put(resourcelocation, unbakedmodel);
      }

      this.mapUnbakedModels = this.unbakedCache;
      TextureUtils.registerCustomModels(this);
      this.topLevelModels.values().forEach((modelIn) -> {
         modelIn.resolveParents(this::getModel);
      });
      pProfilerFiller.pop();
   }

   public void bakeModels(BiFunction<ResourceLocation, Material, TextureAtlasSprite> p_248669_) {
      this.topLevelModels.keySet().forEach((locIn) -> {
         BakedModel bakedmodel = null;

         try {
            bakedmodel = (new ModelBakery.ModelBakerImpl(p_248669_, locIn)).bake(locIn, BlockModelRotation.X0_Y0);
         } catch (Exception exception) {
            LOGGER.warn("Unable to bake model: '{}': {}", locIn, exception);
         }

         if (bakedmodel != null) {
            this.bakedTopLevelModels.put(locIn, bakedmodel);
         }

      });
   }

   private static Predicate<BlockState> predicate(StateDefinition<Block, BlockState> pContainer, String pVariant) {
      Map<Property<?>, Comparable<?>> map = Maps.newHashMap();

      for(String s : COMMA_SPLITTER.split(pVariant)) {
         Iterator<String> iterator = EQUAL_SPLITTER.split(s).iterator();
         if (iterator.hasNext()) {
            String s1 = iterator.next();
            Property<?> property = pContainer.getProperty(s1);
            if (property != null && iterator.hasNext()) {
               String s2 = iterator.next();
               Comparable<?> comparable = getValueHelper(property, s2);
               if (comparable == null) {
                  throw new RuntimeException("Unknown value: '" + s2 + "' for blockstate property: '" + s1 + "' " + property.getPossibleValues());
               }

               map.put(property, comparable);
            } else if (!s1.isEmpty()) {
               throw new RuntimeException("Unknown blockstate property: '" + s1 + "'");
            }
         }
      }

      Block block = pContainer.getOwner();
      return (stateIn) -> {
         if (stateIn != null && stateIn.is(block)) {
            for(Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
               if (!Objects.equals(stateIn.getValue(entry.getKey()), entry.getValue())) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      };
   }

   @Nullable
   static <T extends Comparable<T>> T getValueHelper(Property<T> pProperty, String pValue) {
      return pProperty.getValue(pValue).orElse((T)(null));
   }

   public UnbakedModel getModel(ResourceLocation p_119342_) {
      if (this.unbakedCache.containsKey(p_119342_)) {
         return this.unbakedCache.get(p_119342_);
      } else if (this.loadingStack.contains(p_119342_)) {
         throw new IllegalStateException("Circular reference while loading " + p_119342_);
      } else {
         this.loadingStack.add(p_119342_);
         UnbakedModel unbakedmodel = this.unbakedCache.get(MISSING_MODEL_LOCATION);

         while(!this.loadingStack.isEmpty()) {
            ResourceLocation resourcelocation = this.loadingStack.iterator().next();

            try {
               if (!this.unbakedCache.containsKey(resourcelocation)) {
                  this.loadModel(resourcelocation);
               }
            } catch (ModelBakery.BlockStateDefinitionException modelbakery$blockstatedefinitionexception) {
               LOGGER.warn(modelbakery$blockstatedefinitionexception.getMessage());
               this.unbakedCache.put(resourcelocation, unbakedmodel);
            } catch (Exception exception) {
               LOGGER.warn("Unable to load model: '{}' referenced from: {}", resourcelocation, p_119342_);
               LOGGER.warn(exception.getClass().getName() + ": " + exception.getMessage());
               this.unbakedCache.put(resourcelocation, unbakedmodel);
            } finally {
               this.loadingStack.remove(resourcelocation);
            }
         }

         return this.unbakedCache.getOrDefault(p_119342_, unbakedmodel);
      }
   }

   private void loadModel(ResourceLocation pBlockstateLocation) throws Exception {
      if (pBlockstateLocation instanceof ModelResourceLocation modelresourcelocation) {
         if (Objects.equals(modelresourcelocation.getVariant(), "inventory")) {
            ResourceLocation resourcelocation = pBlockstateLocation.withPrefix("item/");
            String s = pBlockstateLocation.getPath();
            if (s.startsWith("optifine/") || s.startsWith("item/")) {
               resourcelocation = pBlockstateLocation.withPrefix("");
            }

            BlockModel blockmodel = this.loadBlockModel(resourcelocation);
            this.cacheAndQueueDependencies(modelresourcelocation, blockmodel);
            this.unbakedCache.put(resourcelocation, blockmodel);
         } else {
            ResourceLocation resourcelocation2 = new ResourceLocation(pBlockstateLocation.getNamespace(), pBlockstateLocation.getPath());
            StateDefinition<Block, BlockState> statedefinition = Optional.ofNullable(STATIC_DEFINITIONS.get(resourcelocation2)).orElseGet(() -> {
               return BuiltInRegistries.BLOCK.get(resourcelocation2).getStateDefinition();
            });
            this.context.setDefinition(statedefinition);
            List<Property<?>> list = ImmutableList.copyOf(this.blockColors.getColoringProperties(statedefinition.getOwner()));
            ImmutableList<BlockState> immutablelist = statedefinition.getPossibleStates();
            Map<ModelResourceLocation, BlockState> map = Maps.newHashMap();
            immutablelist.forEach((stateIn) -> {
               map.put(BlockModelShaper.stateToModelLocation(resourcelocation2, stateIn), stateIn);
            });
            Map<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> map1 = Maps.newHashMap();
            ResourceLocation resourcelocation1 = BLOCKSTATE_LISTER.idToFile(pBlockstateLocation);
            UnbakedModel unbakedmodel = this.unbakedCache.get(MISSING_MODEL_LOCATION);
            ModelBakery.ModelGroupKey modelbakery$modelgroupkey = new ModelBakery.ModelGroupKey(ImmutableList.of(unbakedmodel), ImmutableList.of());
            Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair = Pair.of(unbakedmodel, () -> {
               return modelbakery$modelgroupkey;
            });

            try {
               for(Pair<String, BlockModelDefinition> pair1 : this.blockStateResources.getOrDefault(resourcelocation1, List.of()).stream().map((jsonIn) -> {
                  try {
                     return Pair.of(jsonIn.source, BlockModelDefinition.fromJsonElement(this.context, jsonIn.data));
                  } catch (Exception exception1) {
                     throw new ModelBakery.BlockStateDefinitionException(String.format(Locale.ROOT, "Exception loading blockstate definition: '%s' in resourcepack: '%s': %s", resourcelocation1, jsonIn.source, exception1.getMessage()));
                  }
               }).toList()) {
                  BlockModelDefinition blockmodeldefinition = pair1.getSecond();
                  Map<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> map2 = Maps.newIdentityHashMap();
                  MultiPart multipart;
                  if (blockmodeldefinition.isMultiPart()) {
                     multipart = blockmodeldefinition.getMultiPart();
                     immutablelist.forEach((stateIn) -> {
                        map2.put(stateIn, Pair.of(multipart, () -> {
                           return ModelBakery.ModelGroupKey.create(stateIn, multipart, list);
                        }));
                     });
                  } else {
                     multipart = null;
                  }

                  blockmodeldefinition.getVariants().forEach((keyIn, variantIn) -> {
                     try {
                        immutablelist.stream().filter(predicate(statedefinition, keyIn)).forEach((stateIn) -> {
                           Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair2 = map2.put(stateIn, Pair.of(variantIn, () -> {
                              return ModelBakery.ModelGroupKey.create(stateIn, variantIn, list);
                           }));
                           if (pair2 != null && pair2.getFirst() != multipart) {
                              map2.put(stateIn, pair);
                              throw new RuntimeException("Overlapping definition with: " + (String)blockmodeldefinition.getVariants().entrySet().stream().filter((entityIn) -> {
                                 return entityIn.getValue() == pair2.getFirst();
                              }).findFirst().get().getKey());
                           }
                        });
                     } catch (Exception exception1) {
                        LOGGER.warn("Exception loading blockstate definition: '{}' in resourcepack: '{}' for variant: '{}': {}", resourcelocation1, pair1.getFirst(), keyIn, exception1.getMessage());
                     }

                  });
                  map1.putAll(map2);
               }
            } catch (ModelBakery.BlockStateDefinitionException modelbakery$blockstatedefinitionexception) {
               throw modelbakery$blockstatedefinitionexception;
            } catch (Exception exception) {
               throw new ModelBakery.BlockStateDefinitionException(String.format(Locale.ROOT, "Exception loading blockstate definition: '%s': %s", resourcelocation1, exception));
            } finally {
               Map<ModelBakery.ModelGroupKey, Set<BlockState>> map3 = Maps.newHashMap();
               map.forEach((locationIn, stateIn) -> {
                  Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair2 = map1.get(stateIn);
                  if (pair2 == null) {
                     LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", resourcelocation1, locationIn);
                     pair2 = pair;
                  }

                  this.cacheAndQueueDependencies(locationIn, pair2.getFirst());

                  try {
                     ModelBakery.ModelGroupKey modelbakery$modelgroupkey1 = pair2.getSecond().get();
                     map3.computeIfAbsent(modelbakery$modelgroupkey1, (keyIn) -> {
                        return Sets.newIdentityHashSet();
                     }).add(stateIn);
                  } catch (Exception exception11) {
                     LOGGER.warn("Exception evaluating model definition: '{}'", locationIn, exception11);
                  }

               });
               map3.forEach((keyIn, setIn) -> {
                  Iterator<BlockState> iterator = setIn.iterator();

                  while(iterator.hasNext()) {
                     BlockState blockstate = iterator.next();
                     if (blockstate.getRenderShape() != RenderShape.MODEL) {
                        iterator.remove();
                        this.modelGroups.put(blockstate, 0);
                     }
                  }

                  if (setIn.size() > 1) {
                     this.registerModelGroup(setIn);
                  }

               });
            }
         }
      } else {
         this.cacheAndQueueDependencies(pBlockstateLocation, this.loadBlockModel(pBlockstateLocation));
      }

   }

   private void cacheAndQueueDependencies(ResourceLocation pLocation, UnbakedModel pModel) {
      this.unbakedCache.put(pLocation, pModel);
      this.loadingStack.addAll(pModel.getDependencies());
   }

   public void loadTopLevel(ModelResourceLocation pLocation) {
      UnbakedModel unbakedmodel = this.getModel(pLocation);
      this.unbakedCache.put(pLocation, unbakedmodel);
      this.topLevelModels.put(pLocation, unbakedmodel);
   }

   private void registerModelGroup(Iterable<BlockState> pBlockStates) {
      int i = this.nextModelGroup++;
      pBlockStates.forEach((stateIn) -> {
         this.modelGroups.put(stateIn, i);
      });
   }

   private BlockModel loadBlockModel(ResourceLocation pLocation) throws IOException {
      String s = pLocation.getPath();
      if ("builtin/generated".equals(s)) {
         return GENERATION_MARKER;
      } else if ("builtin/entity".equals(s)) {
         return BLOCK_ENTITY_MARKER;
      } else if (s.startsWith("builtin/")) {
         String s2 = s.substring("builtin/".length());
         String s3 = BUILTIN_MODELS.get(s2);
         if (s3 == null) {
            throw new FileNotFoundException(pLocation.toString());
         } else {
            Reader reader = new StringReader(s3);
            BlockModel blockmodel1 = BlockModel.fromStream(reader);
            blockmodel1.name = pLocation.toString();
            return blockmodel1;
         }
      } else {
         ResourceLocation resourcelocation = this.getModelLocation(pLocation);
         BlockModel blockmodel = this.modelResources.get(resourcelocation);
         if (blockmodel == null) {
            blockmodel = this.loadBlockModel(resourcelocation);
            if (blockmodel != null) {
               this.modelResources.put(resourcelocation, blockmodel);
            }
         }

         if (blockmodel == null) {
            throw new FileNotFoundException(resourcelocation.toString());
         } else {
            blockmodel.name = pLocation.toString();
            String s1 = TextureUtils.getBasePath(resourcelocation.getPath());
            fixModelLocations(blockmodel, s1);
            return blockmodel;
         }
      }
   }

   public Map<ResourceLocation, BakedModel> getBakedTopLevelModels() {
      return this.bakedTopLevelModels;
   }

   public Object2IntMap<BlockState> getModelGroups() {
      return this.modelGroups;
   }

   private ResourceLocation getModelLocation(ResourceLocation location) {
      String s = location.getPath();
      if (s.startsWith("optifine/")) {
         if (!s.endsWith(".json")) {
            location = new ResourceLocation(location.getNamespace(), s + ".json");
         }

         return location;
      } else {
         return MODEL_LISTER.idToFile(location);
      }
   }

   public static void fixModelLocations(BlockModel modelBlock, String basePath) {
      ResourceLocation resourcelocation = fixModelLocation(FaceBakery.getParentLocation(modelBlock), basePath);
      if (resourcelocation != FaceBakery.getParentLocation(modelBlock)) {
         FaceBakery.setParentLocation(modelBlock, resourcelocation);
      }

      if (FaceBakery.getTextures(modelBlock) != null) {
         for(Map.Entry<String, Either<Material, String>> entry : FaceBakery.getTextures(modelBlock).entrySet()) {
            Either<Material, String> either = entry.getValue();
            Optional<Material> optional = either.left();
            if (optional.isPresent()) {
               Material material = optional.get();
               ResourceLocation resourcelocation1 = material.texture();
               String s = resourcelocation1.getPath();
               String s1 = fixResourcePath(s, basePath);
               if (!s1.equals(s)) {
                  ResourceLocation resourcelocation2 = new ResourceLocation(resourcelocation1.getNamespace(), s1);
                  Material material1 = new Material(material.atlasLocation(), resourcelocation2);
                  Either<Material, String> either1 = Either.left(material1);
                  entry.setValue(either1);
               }
            }
         }
      }

   }

   public static ResourceLocation fixModelLocation(ResourceLocation loc, String basePath) {
      if (loc != null && basePath != null) {
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
      } else {
         return loc;
      }
   }

   private static String fixResourcePath(String path, String basePath) {
      path = TextureUtils.fixResourcePath(path, basePath);
      path = StrUtils.removeSuffix(path, ".json");
      return StrUtils.removeSuffix(path, ".png");
   }
   //TODO: check it
//   public BlockModel loadBlockModel(ResourceLocation locJson) {
//      try {
//         Resource resource = Config.getResource(locJson);
//         Reader reader = resource.openAsReader();
//         return BlockModel.fromStream(reader);
//      } catch (Exception exception) {
//         Config.warn("Error loading model: " + locJson);
//         Config.warn(exception.getClass().getName() + ": " + exception.getMessage());
//         return null;
//      }
//   }

   static record BakedCacheKey(ResourceLocation id, Transformation transformation, boolean isUvLocked) {

      public ResourceLocation id() {
         return this.id;
      }

      public Transformation transformation() {
         return this.transformation;
      }

      public boolean isUvLocked() {
         return this.isUvLocked;
      }
   }

   static class BlockStateDefinitionException extends RuntimeException {
      public BlockStateDefinitionException(String pMessage) {
         super(pMessage);
      }
   }

   public static record LoadedJson(String source, JsonElement data) {

      public String source() {
         return this.source;
      }

      public JsonElement data() {
         return this.data;
      }
   }

   class ModelBakerImpl implements ModelBaker {
      private final Function<Material, TextureAtlasSprite> modelTextureGetter;

      ModelBakerImpl(BiFunction<ResourceLocation, Material, TextureAtlasSprite> p_249651_, ResourceLocation p_251408_) {
         this.modelTextureGetter = (p_246280_2_) -> {
            return p_249651_.apply(p_251408_, p_246280_2_);
         };
      }

      public UnbakedModel getModel(ResourceLocation p_248568_) {
         return ModelBakery.this.getModel(p_248568_);
      }

      public Function<Material, TextureAtlasSprite> getModelTextureGetter() {
         return this.modelTextureGetter;
      }

      public BakedModel bake(ResourceLocation p_252176_, ModelState p_249765_) {
         return this.bake(p_252176_, p_249765_, this.modelTextureGetter);
      }

      public BakedModel bake(ResourceLocation locIn, ModelState stateIn, Function<Material, TextureAtlasSprite> sprites) {
         ModelBakery.BakedCacheKey modelbakery$bakedcachekey = new ModelBakery.BakedCacheKey(locIn, stateIn.getRotation(), stateIn.isUvLocked());
         BakedModel bakedmodel = ModelBakery.this.bakedCache.get(modelbakery$bakedcachekey);
         if (bakedmodel != null) {
            return bakedmodel;
         } else {
            UnbakedModel unbakedmodel = this.getModel(locIn);
            if (unbakedmodel instanceof BlockModel) {
               BlockModel blockmodel = (BlockModel)unbakedmodel;
               if (blockmodel.getRootModel() == ModelBakery.GENERATION_MARKER) {
                  return ModelBakery.ITEM_MODEL_GENERATOR.generateBlockModel(sprites, blockmodel).bake(this, blockmodel, sprites, stateIn, locIn, false);
               }
            }

            BakedModel bakedmodel1 = unbakedmodel.bake(this, sprites, stateIn, locIn);
            ModelBakery.this.bakedCache.put(modelbakery$bakedcachekey, bakedmodel1);
            return bakedmodel1;
         }
      }
   }

   static class ModelGroupKey {
      private final List<UnbakedModel> models;
      private final List<Object> coloringValues;

      public ModelGroupKey(List<UnbakedModel> pModels, List<Object> pColoringValues) {
         this.models = pModels;
         this.coloringValues = pColoringValues;
      }

      public boolean equals(Object pOther) {
         if (this == pOther) {
            return true;
         } else if (!(pOther instanceof ModelBakery.ModelGroupKey)) {
            return false;
         } else {
            ModelBakery.ModelGroupKey modelbakery$modelgroupkey = (ModelBakery.ModelGroupKey)pOther;
            return Objects.equals(this.models, modelbakery$modelgroupkey.models) && Objects.equals(this.coloringValues, modelbakery$modelgroupkey.coloringValues);
         }
      }

      public int hashCode() {
         return 31 * this.models.hashCode() + this.coloringValues.hashCode();
      }

      public static ModelBakery.ModelGroupKey create(BlockState pBlockState, MultiPart pMultipart, Collection<Property<?>> pProperties) {
         StateDefinition<Block, BlockState> statedefinition = pBlockState.getBlock().getStateDefinition();
         List<UnbakedModel> list = pMultipart.getSelectors().stream().filter((selectorIn) -> {
            return selectorIn.getPredicate(statedefinition).test(pBlockState);
         }).map(Selector::getVariant).collect(ImmutableList.toImmutableList());
         List<Object> list1 = getColoringValues(pBlockState, pProperties);
         return new ModelBakery.ModelGroupKey(list, list1);
      }

      public static ModelBakery.ModelGroupKey create(BlockState pBlockState, UnbakedModel pModel, Collection<Property<?>> pProperties) {
         List<Object> list = getColoringValues(pBlockState, pProperties);
         return new ModelBakery.ModelGroupKey(ImmutableList.of(pModel), list);
      }

      private static List<Object> getColoringValues(BlockState pBlockState, Collection<Property<?>> pProperties) {
         return pProperties.stream().map(pBlockState::getValue).collect(ImmutableList.toImmutableList());
      }
   }
}
