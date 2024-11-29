package net.minecraft.world.item.alchemy;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

public class Potion {
   @Nullable
   private final String name;
   private final ImmutableList<MobEffectInstance> effects;

   public static Potion byName(String pName) {
      return BuiltInRegistries.POTION.get(ResourceLocation.tryParse(pName));
   }

   public Potion(MobEffectInstance... pEffects) {
      this((String)null, pEffects);
   }

   public Potion(@Nullable String pName, MobEffectInstance... pEffects) {
      this.name = pName;
      this.effects = ImmutableList.copyOf(pEffects);
   }

   public String getName(String pPrefix) {
      return pPrefix + (this.name == null ? BuiltInRegistries.POTION.getKey(this).getPath() : this.name);
   }

   public List<MobEffectInstance> getEffects() {
      return this.effects;
   }

   public boolean hasInstantEffects() {
      if (!this.effects.isEmpty()) {
         for(MobEffectInstance mobeffectinstance : this.effects) {
            if (mobeffectinstance.getEffect().isInstantenous()) {
               return true;
            }
         }
      }

      return false;
   }
}