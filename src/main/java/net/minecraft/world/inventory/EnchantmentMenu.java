package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantmentTableBlock;

public class EnchantmentMenu extends AbstractContainerMenu {
   private final Container enchantSlots = new SimpleContainer(2) {
      public void setChanged() {
         super.setChanged();
         EnchantmentMenu.this.slotsChanged(this);
      }
   };
   private final ContainerLevelAccess access;
   private final RandomSource random = RandomSource.create();
   private final DataSlot enchantmentSeed = DataSlot.standalone();
   public final int[] costs = new int[3];
   public final int[] enchantClue = new int[]{-1, -1, -1};
   public final int[] levelClue = new int[]{-1, -1, -1};

   public EnchantmentMenu(int pContainerId, Inventory pPlayerInventory) {
      this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
   }

   public EnchantmentMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
      super(MenuType.ENCHANTMENT, pContainerId);
      this.access = pAccess;
      this.addSlot(new Slot(this.enchantSlots, 0, 15, 47) {
         public boolean mayPlace(ItemStack p_39508_) {
            return true;
         }

         public int getMaxStackSize() {
            return 1;
         }
      });
      this.addSlot(new Slot(this.enchantSlots, 1, 35, 47) {
         public boolean mayPlace(ItemStack p_39517_) {
            return p_39517_.is(Items.LAPIS_LAZULI);
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

      this.addDataSlot(DataSlot.shared(this.costs, 0));
      this.addDataSlot(DataSlot.shared(this.costs, 1));
      this.addDataSlot(DataSlot.shared(this.costs, 2));
      this.addDataSlot(this.enchantmentSeed).set(pPlayerInventory.player.getEnchantmentSeed());
      this.addDataSlot(DataSlot.shared(this.enchantClue, 0));
      this.addDataSlot(DataSlot.shared(this.enchantClue, 1));
      this.addDataSlot(DataSlot.shared(this.enchantClue, 2));
      this.addDataSlot(DataSlot.shared(this.levelClue, 0));
      this.addDataSlot(DataSlot.shared(this.levelClue, 1));
      this.addDataSlot(DataSlot.shared(this.levelClue, 2));
   }

   public void slotsChanged(Container pInventory) {
      if (pInventory == this.enchantSlots) {
         ItemStack itemstack = pInventory.getItem(0);
         if (!itemstack.isEmpty() && itemstack.isEnchantable()) {
            this.access.execute((p_39485_, p_39486_) -> {
               int j = 0;

               for(BlockPos blockpos : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
                  if (EnchantmentTableBlock.isValidBookShelf(p_39485_, p_39486_, blockpos)) {
                     ++j;
                  }
               }

               this.random.setSeed((long)this.enchantmentSeed.get());

               for(int k = 0; k < 3; ++k) {
                  this.costs[k] = EnchantmentHelper.getEnchantmentCost(this.random, k, j, itemstack);
                  this.enchantClue[k] = -1;
                  this.levelClue[k] = -1;
                  if (this.costs[k] < k + 1) {
                     this.costs[k] = 0;
                  }
               }

               for(int l = 0; l < 3; ++l) {
                  if (this.costs[l] > 0) {
                     List<EnchantmentInstance> list = this.getEnchantmentList(itemstack, l, this.costs[l]);
                     if (list != null && !list.isEmpty()) {
                        EnchantmentInstance enchantmentinstance = list.get(this.random.nextInt(list.size()));
                        this.enchantClue[l] = BuiltInRegistries.ENCHANTMENT.getId(enchantmentinstance.enchantment);
                        this.levelClue[l] = enchantmentinstance.level;
                     }
                  }
               }

               this.broadcastChanges();
            });
         } else {
            for(int i = 0; i < 3; ++i) {
               this.costs[i] = 0;
               this.enchantClue[i] = -1;
               this.levelClue[i] = -1;
            }
         }
      }

   }

   public boolean clickMenuButton(Player pPlayer, int pId) {
      if (pId >= 0 && pId < this.costs.length) {
         ItemStack itemstack = this.enchantSlots.getItem(0);
         ItemStack itemstack1 = this.enchantSlots.getItem(1);
         int i = pId + 1;
         if ((itemstack1.isEmpty() || itemstack1.getCount() < i) && !pPlayer.getAbilities().instabuild) {
            return false;
         } else if (this.costs[pId] <= 0 || itemstack.isEmpty() || (pPlayer.experienceLevel < i || pPlayer.experienceLevel < this.costs[pId]) && !pPlayer.getAbilities().instabuild) {
            return false;
         } else {
            this.access.execute((p_39481_, p_39482_) -> {
               ItemStack itemstack2 = itemstack;
               List<EnchantmentInstance> list = this.getEnchantmentList(itemstack, pId, this.costs[pId]);
               if (!list.isEmpty()) {
                  pPlayer.onEnchantmentPerformed(itemstack, i);
                  boolean flag = itemstack.is(Items.BOOK);
                  if (flag) {
                     itemstack2 = new ItemStack(Items.ENCHANTED_BOOK);
                     CompoundTag compoundtag = itemstack.getTag();
                     if (compoundtag != null) {
                        itemstack2.setTag(compoundtag.copy());
                     }

                     this.enchantSlots.setItem(0, itemstack2);
                  }

                  for(int j = 0; j < list.size(); ++j) {
                     EnchantmentInstance enchantmentinstance = list.get(j);
                     if (flag) {
                        EnchantedBookItem.addEnchantment(itemstack2, enchantmentinstance);
                     } else {
                        itemstack2.enchant(enchantmentinstance.enchantment, enchantmentinstance.level);
                     }
                  }

                  if (!pPlayer.getAbilities().instabuild) {
                     itemstack1.shrink(i);
                     if (itemstack1.isEmpty()) {
                        this.enchantSlots.setItem(1, ItemStack.EMPTY);
                     }
                  }

                  pPlayer.awardStat(Stats.ENCHANT_ITEM);
                  if (pPlayer instanceof ServerPlayer) {
                     CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer)pPlayer, itemstack2, i);
                  }

                  this.enchantSlots.setChanged();
                  this.enchantmentSeed.set(pPlayer.getEnchantmentSeed());
                  this.slotsChanged(this.enchantSlots);
                  p_39481_.playSound((Player)null, p_39482_, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, p_39481_.random.nextFloat() * 0.1F + 0.9F);
               }

            });
            return true;
         }
      } else {
         Util.logAndPauseIfInIde(pPlayer.getName() + " pressed invalid button id: " + pId);
         return false;
      }
   }

   private List<EnchantmentInstance> getEnchantmentList(ItemStack pStack, int pEnchantSlot, int pLevel) {
      this.random.setSeed((long)(this.enchantmentSeed.get() + pEnchantSlot));
      List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(this.random, pStack, pLevel, false);
      if (pStack.is(Items.BOOK) && list.size() > 1) {
         list.remove(this.random.nextInt(list.size()));
      }

      return list;
   }

   public int getGoldCount() {
      ItemStack itemstack = this.enchantSlots.getItem(1);
      return itemstack.isEmpty() ? 0 : itemstack.getCount();
   }

   public int getEnchantmentSeed() {
      return this.enchantmentSeed.get();
   }

   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.access.execute((p_39469_, p_39470_) -> {
         this.clearContainer(pPlayer, this.enchantSlots);
      });
   }

   public boolean stillValid(Player pPlayer) {
      return stillValid(this.access, pPlayer, Blocks.ENCHANTING_TABLE);
   }

   public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(pIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (pIndex == 0) {
            if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex == 1) {
            if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
               return ItemStack.EMPTY;
            }
         } else if (itemstack1.is(Items.LAPIS_LAZULI)) {
            if (!this.moveItemStackTo(itemstack1, 1, 2, true)) {
               return ItemStack.EMPTY;
            }
         } else {
            if (this.slots.get(0).hasItem() || !this.slots.get(0).mayPlace(itemstack1)) {
               return ItemStack.EMPTY;
            }

            ItemStack itemstack2 = itemstack1.copyWithCount(1);
            itemstack1.shrink(1);
            this.slots.get(0).setByPlayer(itemstack2);
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