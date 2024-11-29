package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeEntryBase extends LootPoolEntryContainer {
   protected final LootPoolEntryContainer[] children;
   private final ComposableEntryContainer composedChildren;

   protected CompositeEntryBase(LootPoolEntryContainer[] pChildren, LootItemCondition[] pConditions) {
      super(pConditions);
      this.children = pChildren;
      this.composedChildren = this.compose(pChildren);
   }

   public void validate(ValidationContext pValidationContext) {
      super.validate(pValidationContext);
      if (this.children.length == 0) {
         pValidationContext.reportProblem("Empty children list");
      }

      for(int i = 0; i < this.children.length; ++i) {
         this.children[i].validate(pValidationContext.forChild(".entry[" + i + "]"));
      }

   }

   protected abstract ComposableEntryContainer compose(ComposableEntryContainer[] pEntries);

   public final boolean expand(LootContext pLootContext, Consumer<LootPoolEntry> pEntryConsumer) {
      return !this.canRun(pLootContext) ? false : this.composedChildren.expand(pLootContext, pEntryConsumer);
   }

   public static <T extends CompositeEntryBase> LootPoolEntryContainer.Serializer<T> createSerializer(final CompositeEntryBase.CompositeEntryConstructor<T> pFactory) {
      return new LootPoolEntryContainer.Serializer<T>() {
         public void serializeCustom(JsonObject p_79449_, T p_79450_, JsonSerializationContext p_79451_) {
            p_79449_.add("children", p_79451_.serialize(p_79450_.children));
         }

         public final T deserializeCustom(JsonObject p_79445_, JsonDeserializationContext p_79446_, LootItemCondition[] p_79447_) {
            LootPoolEntryContainer[] alootpoolentrycontainer = GsonHelper.getAsObject(p_79445_, "children", p_79446_, LootPoolEntryContainer[].class);
            return pFactory.create(alootpoolentrycontainer, p_79447_);
         }
      };
   }

   @FunctionalInterface
   public interface CompositeEntryConstructor<T extends CompositeEntryBase> {
      T create(LootPoolEntryContainer[] pChildren, LootItemCondition[] pConditions);
   }
}