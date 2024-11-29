package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootContext {
   private final LootParams params;
   private final RandomSource random;
   private final LootDataResolver lootDataResolver;
   private final Set<LootContext.VisitedEntry<?>> visitedElements = Sets.newLinkedHashSet();

   LootContext(LootParams pParams, RandomSource pRandom, LootDataResolver pLootDataResolver) {
      this.params = pParams;
      this.random = pRandom;
      this.lootDataResolver = pLootDataResolver;
   }

   public boolean hasParam(LootContextParam<?> pParameter) {
      return this.params.hasParam(pParameter);
   }

   public <T> T getParam(LootContextParam<T> pParam) {
      return this.params.getParameter(pParam);
   }

   public void addDynamicDrops(ResourceLocation pName, Consumer<ItemStack> pConsumer) {
      this.params.addDynamicDrops(pName, pConsumer);
   }

   @Nullable
   public <T> T getParamOrNull(LootContextParam<T> pParameter) {
      return this.params.getParamOrNull(pParameter);
   }

   public boolean hasVisitedElement(LootContext.VisitedEntry<?> pElement) {
      return this.visitedElements.contains(pElement);
   }

   public boolean pushVisitedElement(LootContext.VisitedEntry<?> pElement) {
      return this.visitedElements.add(pElement);
   }

   public void popVisitedElement(LootContext.VisitedEntry<?> pElement) {
      this.visitedElements.remove(pElement);
   }

   public LootDataResolver getResolver() {
      return this.lootDataResolver;
   }

   public RandomSource getRandom() {
      return this.random;
   }

   public float getLuck() {
      return this.params.getLuck();
   }

   public ServerLevel getLevel() {
      return this.params.getLevel();
   }

   public static LootContext.VisitedEntry<LootTable> createVisitedEntry(LootTable pLootTable) {
      return new LootContext.VisitedEntry<>(LootDataType.TABLE, pLootTable);
   }

   public static LootContext.VisitedEntry<LootItemCondition> createVisitedEntry(LootItemCondition pPredicate) {
      return new LootContext.VisitedEntry<>(LootDataType.PREDICATE, pPredicate);
   }

   public static LootContext.VisitedEntry<LootItemFunction> createVisitedEntry(LootItemFunction pModifier) {
      return new LootContext.VisitedEntry<>(LootDataType.MODIFIER, pModifier);
   }

   public static class Builder {
      private final LootParams params;
      @Nullable
      private RandomSource random;

      public Builder(LootParams pParams) {
         this.params = pParams;
      }

      public LootContext.Builder withOptionalRandomSeed(long pSeed) {
         if (pSeed != 0L) {
            this.random = RandomSource.create(pSeed);
         }

         return this;
      }

      public ServerLevel getLevel() {
         return this.params.getLevel();
      }

      public LootContext create(@Nullable ResourceLocation pLocation) {
         ServerLevel serverlevel = this.getLevel();
         MinecraftServer minecraftserver = serverlevel.getServer();
         RandomSource randomsource;
         if (this.random != null) {
            randomsource = this.random;
         } else if (pLocation != null) {
            randomsource = serverlevel.getRandomSequence(pLocation);
         } else {
            randomsource = serverlevel.getRandom();
         }

         return new LootContext(this.params, randomsource, minecraftserver.getLootData());
      }
   }

   public static enum EntityTarget {
      THIS("this", LootContextParams.THIS_ENTITY),
      KILLER("killer", LootContextParams.KILLER_ENTITY),
      DIRECT_KILLER("direct_killer", LootContextParams.DIRECT_KILLER_ENTITY),
      KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER);

      final String name;
      private final LootContextParam<? extends Entity> param;

      private EntityTarget(String pName, LootContextParam<? extends Entity> pParam) {
         this.name = pName;
         this.param = pParam;
      }

      public LootContextParam<? extends Entity> getParam() {
         return this.param;
      }

      public static LootContext.EntityTarget getByName(String pName) {
         for(LootContext.EntityTarget lootcontext$entitytarget : values()) {
            if (lootcontext$entitytarget.name.equals(pName)) {
               return lootcontext$entitytarget;
            }
         }

         throw new IllegalArgumentException("Invalid entity target " + pName);
      }

      public static class Serializer extends TypeAdapter<LootContext.EntityTarget> {
         public void write(JsonWriter pWriter, LootContext.EntityTarget pEntityTarget) throws IOException {
            pWriter.value(pEntityTarget.name);
         }

         public LootContext.EntityTarget read(JsonReader pReader) throws IOException {
            return LootContext.EntityTarget.getByName(pReader.nextString());
         }
      }
   }

   public static record VisitedEntry<T>(LootDataType<T> type, T value) {
   }
}