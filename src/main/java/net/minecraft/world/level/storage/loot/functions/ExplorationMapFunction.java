package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ExplorationMapFunction extends LootItemConditionalFunction {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final TagKey<Structure> DEFAULT_DESTINATION = StructureTags.ON_TREASURE_MAPS;
   public static final String DEFAULT_DECORATION_NAME = "mansion";
   public static final MapDecoration.Type DEFAULT_DECORATION = MapDecoration.Type.MANSION;
   public static final byte DEFAULT_ZOOM = 2;
   public static final int DEFAULT_SEARCH_RADIUS = 50;
   public static final boolean DEFAULT_SKIP_EXISTING = true;
   final TagKey<Structure> destination;
   final MapDecoration.Type mapDecoration;
   final byte zoom;
   final int searchRadius;
   final boolean skipKnownStructures;

   ExplorationMapFunction(LootItemCondition[] pConditions, TagKey<Structure> pDestination, MapDecoration.Type pMapDecoration, byte pZoom, int pSearchRadius, boolean pSkipKnownStructures) {
      super(pConditions);
      this.destination = pDestination;
      this.mapDecoration = pMapDecoration;
      this.zoom = pZoom;
      this.searchRadius = pSearchRadius;
      this.skipKnownStructures = pSkipKnownStructures;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.EXPLORATION_MAP;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.ORIGIN);
   }

   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (!pStack.is(Items.MAP)) {
         return pStack;
      } else {
         Vec3 vec3 = pContext.getParamOrNull(LootContextParams.ORIGIN);
         if (vec3 != null) {
            ServerLevel serverlevel = pContext.getLevel();
            BlockPos blockpos = serverlevel.findNearestMapStructure(this.destination, BlockPos.containing(vec3), this.searchRadius, this.skipKnownStructures);
            if (blockpos != null) {
               ItemStack itemstack = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), this.zoom, true, true);
               MapItem.renderBiomePreviewMap(serverlevel, itemstack);
               MapItemSavedData.addTargetDecoration(itemstack, blockpos, "+", this.mapDecoration);
               return itemstack;
            }
         }

         return pStack;
      }
   }

   public static ExplorationMapFunction.Builder makeExplorationMap() {
      return new ExplorationMapFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<ExplorationMapFunction.Builder> {
      private TagKey<Structure> destination = ExplorationMapFunction.DEFAULT_DESTINATION;
      private MapDecoration.Type mapDecoration = ExplorationMapFunction.DEFAULT_DECORATION;
      private byte zoom = 2;
      private int searchRadius = 50;
      private boolean skipKnownStructures = true;

      protected ExplorationMapFunction.Builder getThis() {
         return this;
      }

      public ExplorationMapFunction.Builder setDestination(TagKey<Structure> pDestination) {
         this.destination = pDestination;
         return this;
      }

      public ExplorationMapFunction.Builder setMapDecoration(MapDecoration.Type pMapDecoration) {
         this.mapDecoration = pMapDecoration;
         return this;
      }

      public ExplorationMapFunction.Builder setZoom(byte pZoom) {
         this.zoom = pZoom;
         return this;
      }

      public ExplorationMapFunction.Builder setSearchRadius(int pSearchRadius) {
         this.searchRadius = pSearchRadius;
         return this;
      }

      public ExplorationMapFunction.Builder setSkipKnownStructures(boolean pSkipKnownStructures) {
         this.skipKnownStructures = pSkipKnownStructures;
         return this;
      }

      public LootItemFunction build() {
         return new ExplorationMapFunction(this.getConditions(), this.destination, this.mapDecoration, this.zoom, this.searchRadius, this.skipKnownStructures);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<ExplorationMapFunction> {
      public void serialize(JsonObject pJson, ExplorationMapFunction pExplorationMapFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pExplorationMapFunction, pSerializationContext);
         if (!pExplorationMapFunction.destination.equals(ExplorationMapFunction.DEFAULT_DESTINATION)) {
            pJson.addProperty("destination", pExplorationMapFunction.destination.location().toString());
         }

         if (pExplorationMapFunction.mapDecoration != ExplorationMapFunction.DEFAULT_DECORATION) {
            pJson.add("decoration", pSerializationContext.serialize(pExplorationMapFunction.mapDecoration.toString().toLowerCase(Locale.ROOT)));
         }

         if (pExplorationMapFunction.zoom != 2) {
            pJson.addProperty("zoom", pExplorationMapFunction.zoom);
         }

         if (pExplorationMapFunction.searchRadius != 50) {
            pJson.addProperty("search_radius", pExplorationMapFunction.searchRadius);
         }

         if (!pExplorationMapFunction.skipKnownStructures) {
            pJson.addProperty("skip_existing_chunks", pExplorationMapFunction.skipKnownStructures);
         }

      }

      public ExplorationMapFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         TagKey<Structure> tagkey = readStructure(pObject);
         String s = pObject.has("decoration") ? GsonHelper.getAsString(pObject, "decoration") : "mansion";
         MapDecoration.Type mapdecoration$type = ExplorationMapFunction.DEFAULT_DECORATION;

         try {
            mapdecoration$type = MapDecoration.Type.valueOf(s.toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException illegalargumentexception) {
            ExplorationMapFunction.LOGGER.error("Error while parsing loot table decoration entry. Found {}. Defaulting to {}", s, ExplorationMapFunction.DEFAULT_DECORATION);
         }

         byte b0 = GsonHelper.getAsByte(pObject, "zoom", (byte)2);
         int i = GsonHelper.getAsInt(pObject, "search_radius", 50);
         boolean flag = GsonHelper.getAsBoolean(pObject, "skip_existing_chunks", true);
         return new ExplorationMapFunction(pConditions, tagkey, mapdecoration$type, b0, i, flag);
      }

      private static TagKey<Structure> readStructure(JsonObject pJson) {
         if (pJson.has("destination")) {
            String s = GsonHelper.getAsString(pJson, "destination");
            return TagKey.create(Registries.STRUCTURE, new ResourceLocation(s));
         } else {
            return ExplorationMapFunction.DEFAULT_DESTINATION;
         }
      }
   }
}