package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Function;

public interface ConditionUserBuilder<T extends ConditionUserBuilder<T>> {
   T when(LootItemCondition.Builder pConditionBuilder);

   default <E> T when(Iterable<E> pBuilderSources, Function<E, LootItemCondition.Builder> pToBuilderFunction) {
      T t = this.unwrap();

      for(E e : pBuilderSources) {
         t = t.when(pToBuilderFunction.apply(e));
      }

      return t;
   }

   T unwrap();
}