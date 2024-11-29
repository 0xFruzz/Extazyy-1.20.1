package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ElytraItem extends Item implements Equipable {
   public ElytraItem(Item.Properties pProperties) {
      super(pProperties);
      DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
   }

   public static boolean isFlyEnabled(ItemStack pElytraStack) {
      return pElytraStack.getDamageValue() < pElytraStack.getMaxDamage() - 1;
   }

   public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
      return pRepair.is(Items.PHANTOM_MEMBRANE);
   }

   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      return this.swapWithEquipmentSlot(this, pLevel, pPlayer, pHand);
   }

   public SoundEvent getEquipSound() {
      return SoundEvents.ARMOR_EQUIP_ELYTRA;
   }

   public EquipmentSlot getEquipmentSlot() {
      return EquipmentSlot.CHEST;
   }
}