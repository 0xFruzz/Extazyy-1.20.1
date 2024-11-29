package net.optifine.entity.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.Config;
import net.optifine.IRandomEntity;
import net.optifine.Log;
import net.optifine.RandomEntities;
import net.optifine.RandomEntityContext;
import net.optifine.RandomEntityProperties;
import net.optifine.entity.model.anim.ModelResolver;
import net.optifine.entity.model.anim.ModelUpdater;
import net.optifine.reflect.Reflector;
import net.optifine.util.Either;
import net.optifine.util.StrUtils;

public class CustomEntityModels {
   private static boolean active = false;
   private static Map<EntityType, RandomEntityProperties<IEntityRenderer>> mapEntityProperties = new HashMap<>();
   private static Map<BlockEntityType, RandomEntityProperties<IEntityRenderer>> mapBlockEntityProperties = new HashMap<>();
   private static int matchingRuleIndex;
   private static Map<EntityType, EntityRenderer> originalEntityRenderMap = null;
   private static Map<BlockEntityType, BlockEntityRenderer> originalTileEntityRenderMap = null;
   private static Map<SkullBlock.Type, SkullModelBase> originalSkullModelMap = null;
   private static List<BlockEntityType> customTileEntityTypes = new ArrayList<>();
   private static BookModel customBookModel;
   private static boolean debugModels = Boolean.getBoolean("cem.debug.models");
   public static final String PREFIX_OPTIFINE_CEM = "optifine/cem/";
   public static final String SUFFIX_JEM = ".jem";
   public static final String SUFFIX_PROPERTIES = ".properties";

   public static void update() {
      Map<EntityType, EntityRenderer> map = getEntityRenderMap();
      Map<BlockEntityType, BlockEntityRenderer> map1 = getTileEntityRenderMap();
      Map<SkullBlock.Type, SkullModelBase> map2 = getSkullModelMap();
      if (map == null) {
         Config.warn("Entity render map not found, custom entity models are DISABLED.");
      } else if (map1 == null) {
         Config.warn("Tile entity render map not found, custom entity models are DISABLED.");
      } else {
         active = false;
         map.clear();
         map1.clear();
         map2.clear();
         customTileEntityTypes.clear();
         map.putAll(originalEntityRenderMap);
         map1.putAll(originalTileEntityRenderMap);
         map2.putAll(originalSkullModelMap);
         BlockEntityWithoutLevelRenderer blockentitywithoutlevelrenderer = Minecraft.getInstance().getItemRenderer().getBlockEntityRenderer();
         blockentitywithoutlevelrenderer.tridentModel = new TridentModel(ModelAdapter.bakeModelLayer(ModelLayers.TRIDENT));
         ParrotOnShoulderLayer.customParrotModel = null;
         customBookModel = null;
         BlockEntityRenderer.CACHED_TYPES.clear();
         if (Minecraft.getInstance().level != null) {
            for(Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
               Map map3 = entity.getEntityData().modelVariables;
               if (map3 != null) {
                  map3.clear();
               }
            }
         }

         mapEntityProperties.clear();
         mapBlockEntityProperties.clear();
         if (Config.isCustomEntityModels()) {
            RandomEntityContext.Models randomentitycontext$models = new RandomEntityContext.Models();
            RendererCache renderercache = randomentitycontext$models.getRendererCache();
            ResourceLocation[] aresourcelocation = getModelLocations();

            for(int i = 0; i < aresourcelocation.length; ++i) {
               ResourceLocation resourcelocation = aresourcelocation[i];
               Config.dbg("CustomEntityModel: " + resourcelocation.getPath());
               IEntityRenderer ientityrenderer = parseEntityRender(resourcelocation, renderercache, 0);
               if (ientityrenderer != null) {
                  Either<EntityType, BlockEntityType> either = ientityrenderer.getType();
                  if (ientityrenderer instanceof EntityRenderer) {
                     map.put(either.getLeft().get(), (EntityRenderer)ientityrenderer);
                     renderercache.put(either.getLeft().get(), 0, (EntityRenderer)ientityrenderer);
                     if (ientityrenderer instanceof ThrownTridentRenderer) {
                        ThrownTridentRenderer throwntridentrenderer = (ThrownTridentRenderer)ientityrenderer;
                        TridentModel tridentmodel = (TridentModel)Reflector.getFieldValue(throwntridentrenderer, Reflector.RenderTrident_modelTrident);
                        if (tridentmodel != null) {
                           blockentitywithoutlevelrenderer.tridentModel = tridentmodel;
                        }
                     }

                     if (ientityrenderer instanceof ParrotRenderer) {
                        ParrotRenderer parrotrenderer = (ParrotRenderer)ientityrenderer;
                        ParrotModel parrotmodel = parrotrenderer.getModel();
                        if (parrotmodel != null) {
                           ParrotOnShoulderLayer.customParrotModel = parrotmodel;
                        }
                     }
                  } else if (ientityrenderer instanceof BlockEntityRenderer) {
                     map1.put(either.getRight().get(), (BlockEntityRenderer)ientityrenderer);
                     renderercache.put(either.getRight().get(), 0, (BlockEntityRenderer)ientityrenderer);
                     if (ientityrenderer instanceof EnchantTableRenderer) {
                        EnchantTableRenderer enchanttablerenderer = (EnchantTableRenderer)ientityrenderer;
                        BookModel bookmodel = (BookModel)Reflector.getFieldValue(enchanttablerenderer, Reflector.TileEntityEnchantmentTableRenderer_modelBook);
                        setEnchantmentScreenBookModel(bookmodel);
                     }

                     customTileEntityTypes.add(either.getRight().get());
                  } else {
                     Config.warn("Unknown renderer type: " + ientityrenderer.getClass().getName());
                  }

                  active = true;
               }
            }

            updateRandomProperties(randomentitycontext$models);
         }
      }
   }

   private static void updateRandomProperties(RandomEntityContext.Models context) {
      String[] astring1 = new String[]{"optifine/cem/"};
      astring1 = new String[]{".jem", ".properties"};
      String[] astring = CustomModelRegistry.getModelNames();

      for(int i = 0; i < astring.length; ++i) {
         String s = astring[i];
         ModelAdapter modeladapter = CustomModelRegistry.getModelAdapter(s);
         Either<EntityType, BlockEntityType> either = modeladapter.getType();
         RandomEntityProperties randomentityproperties = makeProperties(s, context);
         if (randomentityproperties == null) {
            randomentityproperties = makeProperties(s + "/" + s, context);
         }

         if (randomentityproperties != null) {
            if (either.getLeft().isPresent()) {
               mapEntityProperties.put(either.getLeft().get(), randomentityproperties);
            } else {
               mapBlockEntityProperties.put(either.getRight().get(), randomentityproperties);
            }
         }
      }

   }

   private static RandomEntityProperties makeProperties(String name, RandomEntityContext.Models context) {
      ResourceLocation resourcelocation = new ResourceLocation("optifine/cem/" + name + ".jem");
      ResourceLocation resourcelocation1 = new ResourceLocation("optifine/cem/" + name + ".properties");
      if (Config.hasResource(resourcelocation1)) {
         RandomEntityProperties randomentityproperties = RandomEntityProperties.parse(resourcelocation1, resourcelocation, context);
         if (randomentityproperties != null) {
            return randomentityproperties;
         }
      }

      if (!Config.hasResource(resourcelocation)) {
         return null;
      } else {
         int[] aint = RandomEntities.getLocationsVariants(resourcelocation, false, context);
         if (aint == null) {
            return null;
         } else {
            RandomEntityProperties<IEntityRenderer> randomentityproperties1 = new RandomEntityProperties<>(resourcelocation.getPath(), resourcelocation, aint, context);
            return !randomentityproperties1.isValid(resourcelocation.getPath()) ? null : randomentityproperties1;
         }
      }
   }

   private static void setEnchantmentScreenBookModel(BookModel bookModel) {
      customBookModel = bookModel;
   }

   private static Map<EntityType, EntityRenderer> getEntityRenderMap() {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      Map map = entityrenderdispatcher.getEntityRenderMap();
      if (map == null) {
         return null;
      } else {
         if (originalEntityRenderMap == null) {
            originalEntityRenderMap = new HashMap<>(map);
         }

         return map;
      }
   }

   private static Map<BlockEntityType, BlockEntityRenderer> getTileEntityRenderMap() {
      BlockEntityRenderDispatcher blockentityrenderdispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
      Map<BlockEntityType, BlockEntityRenderer> map = blockentityrenderdispatcher.getBlockEntityRenderMap();
      if (originalTileEntityRenderMap == null) {
         originalTileEntityRenderMap = new HashMap<>(map);
      }

      return map;
   }

   private static Map<SkullBlock.Type, SkullModelBase> getSkullModelMap() {
      Map<SkullBlock.Type, SkullModelBase> map = SkullBlockRenderer.models;
      if (map == null) {
         Config.warn("Field not found: SkullBlockRenderer.MODELS");
         map = new HashMap<>();
      }

      if (originalSkullModelMap == null) {
         originalSkullModelMap = new HashMap<>(map);
      }

      return map;
   }

   private static ResourceLocation[] getModelLocations() {
      String s = "optifine/cem/";
      String s1 = ".jem";
      List<ResourceLocation> list = new ArrayList<>();
      String[] astring = CustomModelRegistry.getModelNames();

      for(int i = 0; i < astring.length; ++i) {
         String s2 = astring[i];
         String s3 = s + s2 + s1;
         ResourceLocation resourcelocation = new ResourceLocation(s3);
         if (Config.hasResource(resourcelocation) || debugModels) {
            list.add(resourcelocation);
         }
      }

      return list.toArray(new ResourceLocation[list.size()]);
   }

   public static IEntityRenderer parseEntityRender(ResourceLocation location, RendererCache rendererCache, int index) {
      try {
         if (debugModels && index == 0) {
            return makeDebugEntityRenderer(location, rendererCache, index);
         } else {
            JsonObject jsonobject = CustomEntityModelParser.loadJson(location);
            return parseEntityRender(jsonobject, location.getPath(), rendererCache, index);
         }
      } catch (IOException ioexception) {
         Config.error(ioexception.getClass().getName() + ": " + ioexception.getMessage());
         return null;
      } catch (JsonParseException jsonparseexception) {
         Config.error(jsonparseexception.getClass().getName() + ": " + jsonparseexception.getMessage());
         return null;
      } catch (Exception exception) {
         Log.warn("Error loading CEM: " + location, exception);
         return null;
      }
   }

   private static IEntityRenderer makeDebugEntityRenderer(ResourceLocation loc, RendererCache rendererCache, int index) {
      String s = loc.getPath();
      String s1 = StrUtils.removePrefix(s, "optifine/cem/");
      String s2 = StrUtils.removeSuffix(s1, ".jem");
      ModelAdapter modeladapter = CustomModelRegistry.getModelAdapter(s2);
      Model model = modeladapter.makeModel();
      DyeColor[] adyecolor = DyeColor.values();
      int i = Math.abs(loc.hashCode()) % 256;
      String[] astring = modeladapter.getModelRendererNames();

      for(int j = 0; j < astring.length; ++j) {
         String s3 = astring[j];
         ModelPart modelpart = modeladapter.getModelRenderer(model, s3);
         if (modelpart != null) {
            DyeColor dyecolor = adyecolor[(j + i) % adyecolor.length];
            ResourceLocation resourcelocation = new ResourceLocation("textures/block/" + dyecolor.getSerializedName() + "_stained_glass.png");
            modelpart.setTextureLocation(resourcelocation);
            Config.dbg("  " + s3 + ": " + dyecolor.getSerializedName());
         }
      }

      IEntityRenderer ientityrenderer = modeladapter.makeEntityRender(model, modeladapter.getShadowSize(), rendererCache, index);
      if (ientityrenderer == null) {
         return null;
      } else {
         ientityrenderer.setType(modeladapter.getType());
         return ientityrenderer;
      }
   }

   private static IEntityRenderer parseEntityRender(JsonObject obj, String path, RendererCache rendererCache, int index) {
      CustomEntityRenderer customentityrenderer = CustomEntityModelParser.parseEntityRender(obj, path);
      String s = customentityrenderer.getName();
      s = StrUtils.trimTrailing(s, "0123456789");
      ModelAdapter modeladapter = CustomModelRegistry.getModelAdapter(s);
      checkNull(modeladapter, "Entity not found: " + s);
      Either<EntityType, BlockEntityType> either = modeladapter.getType();
      checkNull(either, "Entity type not found: " + s);
      IEntityRenderer ientityrenderer = makeEntityRender(modeladapter, customentityrenderer, rendererCache, index);
      if (ientityrenderer == null) {
         return null;
      } else {
         ientityrenderer.setType(either);
         return ientityrenderer;
      }
   }

   private static IEntityRenderer makeEntityRender(ModelAdapter modelAdapter, CustomEntityRenderer cer, RendererCache rendererCache, int index) {
      ResourceLocation resourcelocation = cer.getTextureLocation();
      CustomModelRenderer[] acustommodelrenderer = cer.getCustomModelRenderers();
      float f = cer.getShadowSize();
      if (f < 0.0F) {
         f = modelAdapter.getShadowSize();
      }

      Model model = modelAdapter.makeModel();
      if (model == null) {
         return null;
      } else {
         ModelResolver modelresolver = new ModelResolver(modelAdapter, model, acustommodelrenderer);
         if (!modifyModel(modelAdapter, model, acustommodelrenderer, modelresolver)) {
            return null;
         } else {
            IEntityRenderer ientityrenderer = modelAdapter.makeEntityRender(model, f, rendererCache, index);
            if (ientityrenderer == null) {
               throw new JsonParseException("Entity renderer is null, model: " + modelAdapter.getName() + ", adapter: " + modelAdapter.getClass().getName());
            } else {
               if (resourcelocation != null) {
                  setTextureLocation(modelAdapter, model, ientityrenderer, resourcelocation);
               }

               return ientityrenderer;
            }
         }
      }
   }

   private static void setTextureLocation(ModelAdapter modelAdapter, Model model, IEntityRenderer er, ResourceLocation textureLocation) {
      if (!modelAdapter.setTextureLocation(er, textureLocation)) {
         if (er instanceof LivingEntityRenderer) {
            er.setLocationTextureCustom(textureLocation);
         } else {
            setTextureTopModelRenderers(modelAdapter, model, textureLocation);
         }
      }
   }

   public static void setTextureTopModelRenderers(ModelAdapter modelAdapter, Model model, ResourceLocation textureLocation) {
      String[] astring = modelAdapter.getModelRendererNames();

      for(int i = 0; i < astring.length; ++i) {
         String s = astring[i];
         ModelPart modelpart = modelAdapter.getModelRenderer(model, s);
         if (modelpart != null && modelpart.getTextureLocation() == null) {
            modelpart.setTextureLocation(textureLocation);
         }
      }

   }

   private static boolean modifyModel(ModelAdapter modelAdapter, Model model, CustomModelRenderer[] modelRenderers, ModelResolver mr) {
      for(int i = 0; i < modelRenderers.length; ++i) {
         CustomModelRenderer custommodelrenderer = modelRenderers[i];
         if (!modifyModel(modelAdapter, model, custommodelrenderer, mr)) {
            return false;
         }
      }

      return true;
   }

   private static boolean modifyModel(ModelAdapter modelAdapter, Model model, CustomModelRenderer customModelRenderer, ModelResolver modelResolver) {
      String s = customModelRenderer.getModelPart();
      ModelPart modelpart = modelAdapter.getModelRenderer(model, s);
      if (modelpart == null) {
         Config.warn("Model part not found: " + s + ", model: " + model);
         return false;
      } else {
         if (!customModelRenderer.isAttach()) {
            if (modelpart.cubes != null) {
               modelpart.cubes.clear();
            }

            if (modelpart.spriteList != null) {
               modelpart.spriteList.clear();
            }

            if (modelpart.children != null) {
               ModelPart[] amodelpart = modelAdapter.getModelRenderers(model);
               Set<ModelPart> set = Collections.newSetFromMap(new IdentityHashMap<>());
               set.addAll(Arrays.asList(amodelpart));

               for(String s1 : new HashSet<>(modelpart.children.keySet())) {
                  ModelPart modelpart1 = modelpart.children.get(s1);
                  if (!set.contains(modelpart1)) {
                     modelpart.children.remove(s1);
                  }
               }
            }
         }

         String s2 = modelpart.getUniqueChildModelName("CEM-" + s);
         modelpart.addChildModel(s2, customModelRenderer.getModelRenderer());
         ModelUpdater modelupdater = customModelRenderer.getModelUpdater();
         if (modelupdater != null) {
            modelResolver.setThisModelRenderer(customModelRenderer.getModelRenderer());
            modelResolver.setPartModelRenderer(modelpart);
            if (!modelupdater.initialize(modelResolver)) {
               return false;
            }

            customModelRenderer.getModelRenderer().setModelUpdater(modelupdater);
         }

         return true;
      }
   }

   private static void checkNull(Object obj, String msg) {
      if (obj == null) {
         throw new JsonParseException(msg);
      }
   }

   public static boolean isActive() {
      return active;
   }

   public static boolean isCustomModel(BlockState blockStateIn) {
      for(int i = 0; i < customTileEntityTypes.size(); ++i) {
         BlockEntityType blockentitytype = customTileEntityTypes.get(i);
         if (blockentitytype.isValid(blockStateIn)) {
            return true;
         }
      }

      return false;
   }

   public static void onRenderScreen(Screen screen) {
      if (customBookModel != null && screen instanceof EnchantmentScreen enchantmentscreen) {
         Reflector.GuiEnchantment_bookModel.setValue(enchantmentscreen, customBookModel);
      }

   }

   public static EntityRenderer getEntityRenderer(Entity entityIn, EntityRenderer renderer) {
      if (mapEntityProperties.isEmpty()) {
         return renderer;
      } else {
         IRandomEntity irandomentity = RandomEntities.getRandomEntity(entityIn);
         if (irandomentity == null) {
            return renderer;
         } else {
            RandomEntityProperties<IEntityRenderer> randomentityproperties = mapEntityProperties.get(entityIn.getType());
            if (randomentityproperties == null) {
               return renderer;
            } else {
               IEntityRenderer ientityrenderer = randomentityproperties.getResource(irandomentity, renderer);
               if (!(ientityrenderer instanceof EntityRenderer)) {
                  return null;
               } else {
                  matchingRuleIndex = randomentityproperties.getMatchingRuleIndex();
                  return (EntityRenderer)ientityrenderer;
               }
            }
         }
      }
   }

   public static BlockEntityRenderer getBlockEntityRenderer(BlockEntity entityIn, BlockEntityRenderer renderer) {
      if (mapBlockEntityProperties.isEmpty()) {
         return renderer;
      } else {
         IRandomEntity irandomentity = RandomEntities.getRandomBlockEntity(entityIn);
         if (irandomentity == null) {
            return renderer;
         } else {
            RandomEntityProperties<IEntityRenderer> randomentityproperties = mapBlockEntityProperties.get(entityIn.getType());
            if (randomentityproperties == null) {
               return renderer;
            } else {
               IEntityRenderer ientityrenderer = randomentityproperties.getResource(irandomentity, renderer);
               if (!(ientityrenderer instanceof BlockEntityRenderer)) {
                  return null;
               } else {
                  matchingRuleIndex = randomentityproperties.getMatchingRuleIndex();
                  return (BlockEntityRenderer)ientityrenderer;
               }
            }
         }
      }
   }

   public static int getMatchingRuleIndex() {
      return matchingRuleIndex;
   }
}
