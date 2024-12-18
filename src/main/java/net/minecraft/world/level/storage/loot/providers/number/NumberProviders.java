package net.minecraft.world.level.storage.loot.providers.number;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;

public class NumberProviders {
   public static final LootNumberProviderType CONSTANT = register("constant", new ConstantValue.Serializer());
   public static final LootNumberProviderType UNIFORM = register("uniform", new UniformGenerator.Serializer());
   public static final LootNumberProviderType BINOMIAL = register("binomial", new BinomialDistributionGenerator.Serializer());
   public static final LootNumberProviderType SCORE = register("score", new ScoreboardValue.Serializer());

   private static LootNumberProviderType register(String pName, Serializer<? extends NumberProvider> pSerializer) {
      return Registry.register(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, new ResourceLocation(pName), new LootNumberProviderType(pSerializer));
   }

   public static Object createGsonAdapter() {
      return GsonAdapterFactory.builder(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, "provider", "type", NumberProvider::getType).withInlineSerializer(CONSTANT, new ConstantValue.InlineSerializer()).withDefaultType(UNIFORM).build();
   }
}