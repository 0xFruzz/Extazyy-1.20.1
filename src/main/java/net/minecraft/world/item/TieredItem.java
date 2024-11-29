package net.minecraft.world.item;

public class TieredItem extends Item {
   private final Tier tier;

   public TieredItem(Tier pTier, Item.Properties pProperties) {
      super(pProperties.defaultDurability(pTier.getUses()));
      this.tier = pTier;
   }

   public Tier getTier() {
      return this.tier;
   }

   public int getEnchantmentValue() {
      return this.tier.getEnchantmentValue();
   }

   public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
      return this.tier.getRepairIngredient().test(pRepair) || super.isValidRepairItem(pToRepair, pRepair);
   }
}