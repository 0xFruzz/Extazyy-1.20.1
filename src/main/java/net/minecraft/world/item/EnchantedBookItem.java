package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;

public class EnchantedBookItem extends Item {
   public static final String TAG_STORED_ENCHANTMENTS = "StoredEnchantments";

   public EnchantedBookItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public boolean isFoil(ItemStack pStack) {
      return true;
   }

   public boolean isEnchantable(ItemStack pStack) {
      return false;
   }

   public static ListTag getEnchantments(ItemStack pEnchantedBookStack) {
      CompoundTag compoundtag = pEnchantedBookStack.getTag();
      return compoundtag != null ? compoundtag.getList("StoredEnchantments", 10) : new ListTag();
   }

   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
      ItemStack.appendEnchantmentNames(pTooltip, getEnchantments(pStack));
   }

   public static void addEnchantment(ItemStack pStack, EnchantmentInstance pInstance) {
      ListTag listtag = getEnchantments(pStack);
      boolean flag = true;
      ResourceLocation resourcelocation = EnchantmentHelper.getEnchantmentId(pInstance.enchantment);

      for(int i = 0; i < listtag.size(); ++i) {
         CompoundTag compoundtag = listtag.getCompound(i);
         ResourceLocation resourcelocation1 = EnchantmentHelper.getEnchantmentId(compoundtag);
         if (resourcelocation1 != null && resourcelocation1.equals(resourcelocation)) {
            if (EnchantmentHelper.getEnchantmentLevel(compoundtag) < pInstance.level) {
               EnchantmentHelper.setEnchantmentLevel(compoundtag, pInstance.level);
            }

            flag = false;
            break;
         }
      }

      if (flag) {
         listtag.add(EnchantmentHelper.storeEnchantment(resourcelocation, pInstance.level));
      }

      pStack.getOrCreateTag().put("StoredEnchantments", listtag);
   }

   public static ItemStack createForEnchantment(EnchantmentInstance pInstance) {
      ItemStack itemstack = new ItemStack(Items.ENCHANTED_BOOK);
      addEnchantment(itemstack, pInstance);
      return itemstack;
   }
}