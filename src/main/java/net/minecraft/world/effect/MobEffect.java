package net.minecraft.world.effect;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class MobEffect {
   private final Map<Attribute, AttributeModifier> attributeModifiers = Maps.newHashMap();
   private final MobEffectCategory category;
   private final int color;
   @Nullable
   private String descriptionId;
   private Supplier<MobEffectInstance.FactorData> factorDataFactory = () -> {
      return null;
   };

   @Nullable
   public static MobEffect byId(int pPotionID) {
      return BuiltInRegistries.MOB_EFFECT.byId(pPotionID);
   }

   public static int getId(MobEffect pPotion) {
      return BuiltInRegistries.MOB_EFFECT.getId(pPotion);
   }

   public static int getIdFromNullable(@Nullable MobEffect pPotion) {
      return BuiltInRegistries.MOB_EFFECT.getId(pPotion);
   }

   protected MobEffect(MobEffectCategory pCategory, int pColor) {
      this.category = pCategory;
      this.color = pColor;
   }

   public Optional<MobEffectInstance.FactorData> createFactorData() {
      return Optional.ofNullable(this.factorDataFactory.get());
   }

   public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
      if (this == MobEffects.REGENERATION) {
         if (pLivingEntity.getHealth() < pLivingEntity.getMaxHealth()) {
            pLivingEntity.heal(1.0F);
         }
      } else if (this == MobEffects.POISON) {
         if (pLivingEntity.getHealth() > 1.0F) {
            pLivingEntity.hurt(pLivingEntity.damageSources().magic(), 1.0F);
         }
      } else if (this == MobEffects.WITHER) {
         pLivingEntity.hurt(pLivingEntity.damageSources().wither(), 1.0F);
      } else if (this == MobEffects.HUNGER && pLivingEntity instanceof Player) {
         ((Player)pLivingEntity).causeFoodExhaustion(0.005F * (float)(pAmplifier + 1));
      } else if (this == MobEffects.SATURATION && pLivingEntity instanceof Player) {
         if (!pLivingEntity.level().isClientSide) {
            ((Player)pLivingEntity).getFoodData().eat(pAmplifier + 1, 1.0F);
         }
      } else if ((this != MobEffects.HEAL || pLivingEntity.isInvertedHealAndHarm()) && (this != MobEffects.HARM || !pLivingEntity.isInvertedHealAndHarm())) {
         if (this == MobEffects.HARM && !pLivingEntity.isInvertedHealAndHarm() || this == MobEffects.HEAL && pLivingEntity.isInvertedHealAndHarm()) {
            pLivingEntity.hurt(pLivingEntity.damageSources().magic(), (float)(6 << pAmplifier));
         }
      } else {
         pLivingEntity.heal((float)Math.max(4 << pAmplifier, 0));
      }

   }

   public void applyInstantenousEffect(@Nullable Entity pSource, @Nullable Entity pIndirectSource, LivingEntity pLivingEntity, int pAmplifier, double pHealth) {
      if ((this != MobEffects.HEAL || pLivingEntity.isInvertedHealAndHarm()) && (this != MobEffects.HARM || !pLivingEntity.isInvertedHealAndHarm())) {
         if (this == MobEffects.HARM && !pLivingEntity.isInvertedHealAndHarm() || this == MobEffects.HEAL && pLivingEntity.isInvertedHealAndHarm()) {
            int j = (int)(pHealth * (double)(6 << pAmplifier) + 0.5D);
            if (pSource == null) {
               pLivingEntity.hurt(pLivingEntity.damageSources().magic(), (float)j);
            } else {
               pLivingEntity.hurt(pLivingEntity.damageSources().indirectMagic(pSource, pIndirectSource), (float)j);
            }
         } else {
            this.applyEffectTick(pLivingEntity, pAmplifier);
         }
      } else {
         int i = (int)(pHealth * (double)(4 << pAmplifier) + 0.5D);
         pLivingEntity.heal((float)i);
      }

   }

   public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
      if (this == MobEffects.REGENERATION) {
         int k = 50 >> pAmplifier;
         if (k > 0) {
            return pDuration % k == 0;
         } else {
            return true;
         }
      } else if (this == MobEffects.POISON) {
         int j = 25 >> pAmplifier;
         if (j > 0) {
            return pDuration % j == 0;
         } else {
            return true;
         }
      } else if (this == MobEffects.WITHER) {
         int i = 40 >> pAmplifier;
         if (i > 0) {
            return pDuration % i == 0;
         } else {
            return true;
         }
      } else {
         return this == MobEffects.HUNGER;
      }
   }

   public boolean isInstantenous() {
      return false;
   }

   protected String getOrCreateDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("effect", BuiltInRegistries.MOB_EFFECT.getKey(this));
      }

      return this.descriptionId;
   }

   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   public Component getDisplayName() {
      return Component.translatable(this.getDescriptionId());
   }

   public MobEffectCategory getCategory() {
      return this.category;
   }

   public int getColor() {
      return this.color;
   }

   public MobEffect addAttributeModifier(Attribute pAttribute, String pUuid, double pAmount, AttributeModifier.Operation pOperation) {
      AttributeModifier attributemodifier = new AttributeModifier(UUID.fromString(pUuid), this::getDescriptionId, pAmount, pOperation);
      this.attributeModifiers.put(pAttribute, attributemodifier);
      return this;
   }

   public MobEffect setFactorDataFactory(Supplier<MobEffectInstance.FactorData> pFactorDataFactory) {
      this.factorDataFactory = pFactorDataFactory;
      return this;
   }

   public Map<Attribute, AttributeModifier> getAttributeModifiers() {
      return this.attributeModifiers;
   }

   public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
      for(Map.Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet()) {
         AttributeInstance attributeinstance = pAttributeMap.getInstance(entry.getKey());
         if (attributeinstance != null) {
            attributeinstance.removeModifier(entry.getValue());
         }
      }

   }

   public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
      for(Map.Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet()) {
         AttributeInstance attributeinstance = pAttributeMap.getInstance(entry.getKey());
         if (attributeinstance != null) {
            AttributeModifier attributemodifier = entry.getValue();
            attributeinstance.removeModifier(attributemodifier);
            attributeinstance.addPermanentModifier(new AttributeModifier(attributemodifier.getId(), this.getDescriptionId() + " " + pAmplifier, this.getAttributeModifierValue(pAmplifier, attributemodifier), attributemodifier.getOperation()));
         }
      }

   }

   public double getAttributeModifierValue(int pAmplifier, AttributeModifier pModifier) {
      return pModifier.getAmount() * (double)(pAmplifier + 1);
   }

   public boolean isBeneficial() {
      return this.category == MobEffectCategory.BENEFICIAL;
   }
}