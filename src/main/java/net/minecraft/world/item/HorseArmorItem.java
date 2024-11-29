package net.minecraft.world.item;

import net.minecraft.resources.ResourceLocation;

public class HorseArmorItem extends Item {
   private static final String TEX_FOLDER = "textures/entity/horse/";
   private final int protection;
   private final String texture;

   public HorseArmorItem(int pProtection, String pIdentifier, Item.Properties pProperties) {
      super(pProperties);
      this.protection = pProtection;
      this.texture = "textures/entity/horse/armor/horse_armor_" + pIdentifier + ".png";
   }

   public ResourceLocation getTexture() {
      return new ResourceLocation(this.texture);
   }

   public int getProtection() {
      return this.protection;
   }
}