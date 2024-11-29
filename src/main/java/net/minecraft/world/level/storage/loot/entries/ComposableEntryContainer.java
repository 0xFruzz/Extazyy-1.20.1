package net.minecraft.world.level.storage.loot.entries;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.LootContext;

@FunctionalInterface
interface ComposableEntryContainer {
   ComposableEntryContainer ALWAYS_FALSE = (p_79418_, p_79419_) -> {
      return false;
   };
   ComposableEntryContainer ALWAYS_TRUE = (p_79409_, p_79410_) -> {
      return true;
   };

   boolean expand(LootContext pLootContext, Consumer<LootPoolEntry> pEntryConsumer);

   default ComposableEntryContainer and(ComposableEntryContainer pEntry) {
      Objects.requireNonNull(pEntry);
      return (p_79424_, p_79425_) -> {
         return this.expand(p_79424_, p_79425_) && pEntry.expand(p_79424_, p_79425_);
      };
   }

   default ComposableEntryContainer or(ComposableEntryContainer pEntry) {
      Objects.requireNonNull(pEntry);
      return (p_79415_, p_79416_) -> {
         return this.expand(p_79415_, p_79416_) || pEntry.expand(p_79415_, p_79416_);
      };
   }
}