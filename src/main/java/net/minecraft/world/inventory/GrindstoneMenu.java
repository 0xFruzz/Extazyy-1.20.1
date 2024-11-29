package net.minecraft.world.inventory;

import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class GrindstoneMenu extends AbstractContainerMenu {
   public static final int MAX_NAME_LENGTH = 35;
   public static final int INPUT_SLOT = 0;
   public static final int ADDITIONAL_SLOT = 1;
   public static final int RESULT_SLOT = 2;
   private static final int INV_SLOT_START = 3;
   private static final int INV_SLOT_END = 30;
   private static final int USE_ROW_SLOT_START = 30;
   private static final int USE_ROW_SLOT_END = 39;
   private final Container resultSlots = new ResultContainer();
   final Container repairSlots = new SimpleContainer(2) {
      public void setChanged() {
         super.setChanged();
         GrindstoneMenu.this.slotsChanged(this);
      }
   };
   private final ContainerLevelAccess access;

   public GrindstoneMenu(int pContainerId, Inventory pPlayerInventory) {
      this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
   }

   public GrindstoneMenu(int pContainerId, Inventory pPlayerInventory, final ContainerLevelAccess pAccess) {
      super(MenuType.GRINDSTONE, pContainerId);
      this.access = pAccess;
      this.addSlot(new Slot(this.repairSlots, 0, 49, 19) {
         public boolean mayPlace(ItemStack p_39607_) {
            return p_39607_.isDamageableItem() || p_39607_.is(Items.ENCHANTED_BOOK) || p_39607_.isEnchanted();
         }
      });
      this.addSlot(new Slot(this.repairSlots, 1, 49, 40) {
         public boolean mayPlace(ItemStack p_39616_) {
            return p_39616_.isDamageableItem() || p_39616_.is(Items.ENCHANTED_BOOK) || p_39616_.isEnchanted();
         }
      });
      this.addSlot(new Slot(this.resultSlots, 2, 129, 34) {
         public boolean mayPlace(ItemStack p_39630_) {
            return false;
         }

         public void onTake(Player p_150574_, ItemStack p_150575_) {
            pAccess.execute((p_39634_, p_39635_) -> {
               if (p_39634_ instanceof ServerLevel) {
                  ExperienceOrb.award((ServerLevel)p_39634_, Vec3.atCenterOf(p_39635_), this.getExperienceAmount(p_39634_));
               }

               p_39634_.levelEvent(1042, p_39635_, 0);
            });
            GrindstoneMenu.this.repairSlots.setItem(0, ItemStack.EMPTY);
            GrindstoneMenu.this.repairSlots.setItem(1, ItemStack.EMPTY);
         }

         private int getExperienceAmount(Level p_39632_) {
            int l = 0;
            l += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(0));
            l += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(1));
            if (l > 0) {
               int i1 = (int)Math.ceil((double)l / 2.0D);
               return i1 + p_39632_.random.nextInt(i1);
            } else {
               return 0;
            }
         }

         private int getExperienceFromItem(ItemStack p_39637_) {
            int l = 0;
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(p_39637_);

            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
               Enchantment enchantment = entry.getKey();
               Integer integer = entry.getValue();
               if (!enchantment.isCurse()) {
                  l += enchantment.getMinCost(integer);
               }
            }

            return l;
         }
      });

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 142));
      }

   }

   public void slotsChanged(Container pInventory) {
      super.slotsChanged(pInventory);
      if (pInventory == this.repairSlots) {
         this.createResult();
      }

   }

   private void createResult() {
      ItemStack itemstack = this.repairSlots.getItem(0);
      ItemStack itemstack1 = this.repairSlots.getItem(1);
      boolean flag = !itemstack.isEmpty() || !itemstack1.isEmpty();
      boolean flag1 = !itemstack.isEmpty() && !itemstack1.isEmpty();
      if (!flag) {
         this.resultSlots.setItem(0, ItemStack.EMPTY);
      } else {
         boolean flag2 = !itemstack.isEmpty() && !itemstack.is(Items.ENCHANTED_BOOK) && !itemstack.isEnchanted() || !itemstack1.isEmpty() && !itemstack1.is(Items.ENCHANTED_BOOK) && !itemstack1.isEnchanted();
         if (itemstack.getCount() > 1 || itemstack1.getCount() > 1 || !flag1 && flag2) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.broadcastChanges();
            return;
         }

         int j = 1;
         int i;
         ItemStack itemstack2;
         if (flag1) {
            if (!itemstack.is(itemstack1.getItem())) {
               this.resultSlots.setItem(0, ItemStack.EMPTY);
               this.broadcastChanges();
               return;
            }

            Item item = itemstack.getItem();
            int k = item.getMaxDamage() - itemstack.getDamageValue();
            int l = item.getMaxDamage() - itemstack1.getDamageValue();
            int i1 = k + l + item.getMaxDamage() * 5 / 100;
            i = Math.max(item.getMaxDamage() - i1, 0);
            itemstack2 = this.mergeEnchants(itemstack, itemstack1);
            if (!itemstack2.isDamageableItem()) {
               if (!ItemStack.matches(itemstack, itemstack1)) {
                  this.resultSlots.setItem(0, ItemStack.EMPTY);
                  this.broadcastChanges();
                  return;
               }

               j = 2;
            }
         } else {
            boolean flag3 = !itemstack.isEmpty();
            i = flag3 ? itemstack.getDamageValue() : itemstack1.getDamageValue();
            itemstack2 = flag3 ? itemstack : itemstack1;
         }

         this.resultSlots.setItem(0, this.removeNonCurses(itemstack2, i, j));
      }

      this.broadcastChanges();
   }

   private ItemStack mergeEnchants(ItemStack pCopyTo, ItemStack pCopyFrom) {
      ItemStack itemstack = pCopyTo.copy();
      Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(pCopyFrom);

      for(Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
         Enchantment enchantment = entry.getKey();
         if (!enchantment.isCurse() || EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemstack) == 0) {
            itemstack.enchant(enchantment, entry.getValue());
         }
      }

      return itemstack;
   }

   private ItemStack removeNonCurses(ItemStack pStack, int pDamage, int pCount) {
      ItemStack itemstack = pStack.copyWithCount(pCount);
      itemstack.removeTagKey("Enchantments");
      itemstack.removeTagKey("StoredEnchantments");
      if (pDamage > 0) {
         itemstack.setDamageValue(pDamage);
      } else {
         itemstack.removeTagKey("Damage");
      }

      Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(pStack).entrySet().stream().filter((p_39584_) -> {
         return p_39584_.getKey().isCurse();
      }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      EnchantmentHelper.setEnchantments(map, itemstack);
      itemstack.setRepairCost(0);
      if (itemstack.is(Items.ENCHANTED_BOOK) && map.size() == 0) {
         itemstack = new ItemStack(Items.BOOK);
         if (pStack.hasCustomHoverName()) {
            itemstack.setHoverName(pStack.getHoverName());
         }
      }

      for(int i = 0; i < map.size(); ++i) {
         itemstack.setRepairCost(AnvilMenu.calculateIncreasedRepairCost(itemstack.getBaseRepairCost()));
      }

      return itemstack;
   }

   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.access.execute((p_39575_, p_39576_) -> {
         this.clearContainer(pPlayer, this.repairSlots);
      });
   }

   public boolean stillValid(Player pPlayer) {
      return stillValid(this.access, pPlayer, Blocks.GRINDSTONE);
   }

   public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(pIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         ItemStack itemstack2 = this.repairSlots.getItem(0);
         ItemStack itemstack3 = this.repairSlots.getItem(1);
         if (pIndex == 2) {
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (pIndex != 0 && pIndex != 1) {
            if (!itemstack2.isEmpty() && !itemstack3.isEmpty()) {
               if (pIndex >= 3 && pIndex < 30) {
                  if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (pIndex >= 30 && pIndex < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(itemstack1, 0, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(pPlayer, itemstack1);
      }

      return itemstack;
   }
}