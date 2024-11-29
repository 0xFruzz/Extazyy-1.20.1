package net.minecraft.world.item.enchantment;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;

public abstract class Enchantment {
   private final EquipmentSlot[] slots;
   private final Enchantment.Rarity rarity;
   public final EnchantmentCategory category;
   @Nullable
   protected String descriptionId;

   @Nullable
   public static Enchantment byId(int pId) {
      return BuiltInRegistries.ENCHANTMENT.byId(pId);
   }

   protected Enchantment(Enchantment.Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot[] pApplicableSlots) {
      this.rarity = pRarity;
      this.category = pCategory;
      this.slots = pApplicableSlots;
   }

   public Map<EquipmentSlot, ItemStack> getSlotItems(LivingEntity pEntity) {
      Map<EquipmentSlot, ItemStack> map = Maps.newEnumMap(EquipmentSlot.class);

      for(EquipmentSlot equipmentslot : this.slots) {
         ItemStack itemstack = pEntity.getItemBySlot(equipmentslot);
         if (!itemstack.isEmpty()) {
            map.put(equipmentslot, itemstack);
         }
      }

      return map;
   }

   public Enchantment.Rarity getRarity() {
      return this.rarity;
   }

   public int getMinLevel() {
      return 1;
   }

   public int getMaxLevel() {
      return 1;
   }

   public int getMinCost(int pLevel) {
      return 1 + pLevel * 10;
   }

   public int getMaxCost(int pLevel) {
      return this.getMinCost(pLevel) + 5;
   }

   public int getDamageProtection(int pLevel, DamageSource pSource) {
      return 0;
   }

   public float getDamageBonus(int pLevel, MobType pType) {
      return 0.0F;
   }

   public final boolean isCompatibleWith(Enchantment pOther) {
      return this.checkCompatibility(pOther) && pOther.checkCompatibility(this);
   }

   protected boolean checkCompatibility(Enchantment pOther) {
      return this != pOther;
   }

   protected String getOrCreateDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("enchantment", BuiltInRegistries.ENCHANTMENT.getKey(this));
      }

      return this.descriptionId;
   }

   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   public Component getFullname(int pLevel) {
      MutableComponent mutablecomponent = Component.translatable(this.getDescriptionId());
      if (this.isCurse()) {
         mutablecomponent.withStyle(ChatFormatting.RED);
      } else {
         mutablecomponent.withStyle(ChatFormatting.GRAY);
      }

      if (pLevel != 1 || this.getMaxLevel() != 1) {
         mutablecomponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + pLevel));
      }

      return mutablecomponent;
   }

   public boolean canEnchant(ItemStack pStack) {
      return this.category.canEnchant(pStack.getItem());
   }

   public void doPostAttack(LivingEntity pAttacker, Entity pTarget, int pLevel) {
   }

   public void doPostHurt(LivingEntity pTarget, Entity pAttacker, int pLevel) {
   }

   public boolean isTreasureOnly() {
      return false;
   }

   public boolean isCurse() {
      return false;
   }

   public boolean isTradeable() {
      return true;
   }

   public boolean isDiscoverable() {
      return true;
   }

   public static enum Rarity {
      COMMON(10),
      UNCOMMON(5),
      RARE(2),
      VERY_RARE(1);

      private final int weight;

      private Rarity(int pWeight) {
         this.weight = pWeight;
      }

      public int getWeight() {
         return this.weight;
      }
   }
}