package net.minecraft.world.entity.ai.attributes;

public class Attribute {
   public static final int MAX_NAME_LENGTH = 64;
   private final double defaultValue;
   private boolean syncable;
   private final String descriptionId;

   protected Attribute(String pDescriptionId, double pDefaultValue) {
      this.defaultValue = pDefaultValue;
      this.descriptionId = pDescriptionId;
   }

   public double getDefaultValue() {
      return this.defaultValue;
   }

   public boolean isClientSyncable() {
      return this.syncable;
   }

   public Attribute setSyncable(boolean pWatch) {
      this.syncable = pWatch;
      return this;
   }

   public double sanitizeValue(double pValue) {
      return pValue;
   }

   public String getDescriptionId() {
      return this.descriptionId;
   }
}