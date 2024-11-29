package net.minecraft.world.level.storage.loot;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class ValidationContext {
   private final Multimap<String, String> problems;
   private final Supplier<String> context;
   private final LootContextParamSet params;
   private final LootDataResolver resolver;
   private final Set<LootDataId<?>> visitedElements;
   @Nullable
   private String contextCache;

   public ValidationContext(LootContextParamSet pParams, LootDataResolver pResolver) {
      this(HashMultimap.create(), () -> {
         return "";
      }, pParams, pResolver, ImmutableSet.of());
   }

   public ValidationContext(Multimap<String, String> pProblems, Supplier<String> pContext, LootContextParamSet pParams, LootDataResolver pResolver, Set<LootDataId<?>> pVisitedElements) {
      this.problems = pProblems;
      this.context = pContext;
      this.params = pParams;
      this.resolver = pResolver;
      this.visitedElements = pVisitedElements;
   }

   private String getContext() {
      if (this.contextCache == null) {
         this.contextCache = this.context.get();
      }

      return this.contextCache;
   }

   public void reportProblem(String pProblem) {
      this.problems.put(this.getContext(), pProblem);
   }

   public ValidationContext forChild(String pChildName) {
      return new ValidationContext(this.problems, () -> {
         return this.getContext() + pChildName;
      }, this.params, this.resolver, this.visitedElements);
   }

   public ValidationContext enterElement(String pChildName, LootDataId<?> pElement) {
      ImmutableSet<LootDataId<?>> immutableset = ImmutableSet.<LootDataId<?>>builder().addAll(this.visitedElements).add(pElement).build();
      return new ValidationContext(this.problems, () -> {
         return this.getContext() + pChildName;
      }, this.params, this.resolver, immutableset);
   }

   public boolean hasVisitedElement(LootDataId<?> pElement) {
      return this.visitedElements.contains(pElement);
   }

   public Multimap<String, String> getProblems() {
      return ImmutableMultimap.copyOf(this.problems);
   }

   public void validateUser(LootContextUser pLootContextUser) {
      this.params.validateUser(this, pLootContextUser);
   }

   public LootDataResolver resolver() {
      return this.resolver;
   }

   public ValidationContext setParams(LootContextParamSet pParams) {
      return new ValidationContext(this.problems, this.context, pParams, this.resolver, this.visitedElements);
   }
}