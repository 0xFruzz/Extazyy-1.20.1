package net.minecraft.world.level.storage.loot.providers.number;

import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.SerializerType;

public class LootNumberProviderType extends SerializerType<NumberProvider> {
   public LootNumberProviderType(Serializer<? extends NumberProvider> pSerializer) {
      super(pSerializer);
   }
}