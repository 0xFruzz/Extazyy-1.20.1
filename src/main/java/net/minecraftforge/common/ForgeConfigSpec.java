package net.minecraftforge.common;

public class ForgeConfigSpec {
   public static class BooleanValue extends ForgeConfigSpec.ConfigValue<Boolean> {
   }

   public static class ConfigValue<T> {
   }

   public static class DoubleValue extends ForgeConfigSpec.ConfigValue<Double> {
   }

   public static class EnumValue<T extends Enum<T>> extends ForgeConfigSpec.ConfigValue<T> {
   }

   public static class IntValue extends ForgeConfigSpec.ConfigValue<Integer> {
   }

   public static class LongValue extends ForgeConfigSpec.ConfigValue<Long> {
   }
}