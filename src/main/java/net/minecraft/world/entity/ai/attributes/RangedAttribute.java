package net.minecraft.world.entity.ai.attributes;

import net.minecraft.util.Mth;

public class RangedAttribute extends Attribute {
   private final double minValue;
   private final double maxValue;

   public RangedAttribute(String pDescriptionId, double pDefaultValue, double pMin, double pMax) {
      super(pDescriptionId, pDefaultValue);
      this.minValue = pMin;
      this.maxValue = pMax;
      if (pMin > pMax) {
         throw new IllegalArgumentException("Minimum value cannot be bigger than maximum value!");
      } else if (pDefaultValue < pMin) {
         throw new IllegalArgumentException("Default value cannot be lower than minimum value!");
      } else if (pDefaultValue > pMax) {
         throw new IllegalArgumentException("Default value cannot be bigger than maximum value!");
      }
   }

   public double getMinValue() {
      return this.minValue;
   }

   public double getMaxValue() {
      return this.maxValue;
   }

   public double sanitizeValue(double pValue) {
      return Double.isNaN(pValue) ? this.minValue : Mth.clamp(pValue, this.minValue, this.maxValue);
   }
}