package net.minecraft.world.item;

import net.minecraft.tags.BlockTags;

public class PickaxeItem extends DiggerItem {
   protected PickaxeItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Item.Properties pProperties) {
      super((float)pAttackDamageModifier, pAttackSpeedModifier, pTier, BlockTags.MINEABLE_WITH_PICKAXE, pProperties);
   }
}