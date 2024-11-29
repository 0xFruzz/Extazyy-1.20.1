package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;
import net.optifine.config.FloatOptions;
import net.optifine.config.SliderPercentageOptionOF;
import net.optifine.config.SliderableValueSetInt;
import net.optifine.gui.IOptionControl;
import org.slf4j.Logger;

public class OptionInstance<T> {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final OptionInstance.Enum<Boolean> BOOLEAN_VALUES = new OptionInstance.Enum<>(ImmutableList.of(Boolean.TRUE, Boolean.FALSE), Codec.BOOL);
   public static final OptionInstance.CaptionBasedToString<Boolean> BOOLEAN_TO_STRING = (p_231543_0_, p_231543_1_) -> {
      return p_231543_1_ ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
   };
   private final OptionInstance.TooltipSupplier<T> tooltip;
   final Function<T, Component> toString;
   private final OptionInstance.ValueSet<T> values;
   private final Codec<T> codec;
   private final T initialValue;
   private final Consumer<T> onValueUpdate;
   final Component caption;
   T value;
   private String resourceKey;
   public static final Map<String, OptionInstance> OPTIONS_BY_KEY = new HashMap<>();

   public static OptionInstance<Boolean> createBoolean(String pKey, boolean pInitialValue, Consumer<Boolean> pOnValueUpdate) {
      return createBoolean(pKey, noTooltip(), pInitialValue, pOnValueUpdate);
   }

   public static OptionInstance<Boolean> createBoolean(String pKey, boolean pInitialValue) {
      return createBoolean(pKey, noTooltip(), pInitialValue, (p_231547_0_) -> {
      });
   }

   public static OptionInstance<Boolean> createBoolean(String pCaption, OptionInstance.TooltipSupplier<Boolean> pTooltip, boolean pInitialValue) {
      return createBoolean(pCaption, pTooltip, pInitialValue, (p_231512_0_) -> {
      });
   }

   public static OptionInstance<Boolean> createBoolean(String pCaption, OptionInstance.TooltipSupplier<Boolean> pTooltip, boolean pInitialValue, Consumer<Boolean> pOnValueUpdate) {
      return createBoolean(pCaption, pTooltip, BOOLEAN_TO_STRING, pInitialValue, pOnValueUpdate);
   }

   public static OptionInstance<Boolean> createBoolean(String pCaption, OptionInstance.TooltipSupplier<Boolean> pTooltip, OptionInstance.CaptionBasedToString<Boolean> pValueStringifier, boolean pInitialValue, Consumer<Boolean> pOnValueUpdate) {
      return new OptionInstance<>(pCaption, pTooltip, pValueStringifier, BOOLEAN_VALUES, pInitialValue, pOnValueUpdate);
   }

   public OptionInstance(String pCaption, OptionInstance.TooltipSupplier<T> pTooltip, OptionInstance.CaptionBasedToString<T> pValueStringifier, OptionInstance.ValueSet<T> pValues, T pInitialValue, Consumer<T> pOnValueUpdate) {
      this(pCaption, pTooltip, pValueStringifier, pValues, pValues.codec(), pInitialValue, pOnValueUpdate);
   }

   public OptionInstance(String pCaption, OptionInstance.TooltipSupplier<T> pTooltip, OptionInstance.CaptionBasedToString<T> pValueStringifier, OptionInstance.ValueSet<T> pValues, Codec<T> pCodec, T pInitialValue, Consumer<T> pOnValueUpdate) {
      this.caption = Component.translatable(pCaption);
      this.tooltip = pTooltip;
      this.toString = (p_231504_2_) -> {
         return pValueStringifier.toString(this.caption, p_231504_2_);
      };
      this.values = pValues;
      this.codec = pCodec;
      this.initialValue = pInitialValue;
      this.onValueUpdate = pOnValueUpdate;
      this.value = this.initialValue;
      this.resourceKey = pCaption;
      OPTIONS_BY_KEY.put(this.resourceKey, this);
   }

   public static <T> OptionInstance.TooltipSupplier<T> noTooltip() {
      return (p_257064_0_) -> {
         return null;
      };
   }

   public static <T> OptionInstance.TooltipSupplier<T> cachedConstantTooltip(Component pMessage) {
      return (p_257065_1_) -> {
         return Tooltip.create(pMessage);
      };
   }

   public static <T extends OptionEnum> OptionInstance.CaptionBasedToString<T> forOptionEnum() {
      return (p_231537_0_, p_231537_1_) -> {
         return p_231537_1_.getCaption();
      };
   }

   public AbstractWidget createButton(Options pOptions, int pX, int pY, int pWidth) {
      return this.createButton(pOptions, pX, pY, pWidth, (p_260737_0_) -> {
      });
   }

   public AbstractWidget createButton(Options pOptions, int pX, int pY, int pWidth, Consumer<T> pOnValueChanged) {
      return this.values.createButton(this.tooltip, pOptions, pX, pY, pWidth, pOnValueChanged).apply(this);
   }

   public T get() {
      if (this instanceof SliderPercentageOptionOF sliderpercentageoptionof) {
         if (this.value instanceof Integer) {
            return (T)(Integer)(int)sliderpercentageoptionof.getOptionValue();
         }

         if (this.value instanceof Double) {
            return (T)(Double)sliderpercentageoptionof.getOptionValue();
         }
      }

      return this.value;
   }

   public Codec<T> codec() {
      return this.codec;
   }

   public String toString() {
      return this.caption.getString();
   }

   public void set(T pValue) {
      T t = this.values.validateValue(pValue).orElseGet(() -> {
         LOGGER.error("Illegal option value " + pValue + " for " + this.caption);
         return this.initialValue;
      });
      if (!Minecraft.getInstance().isRunning()) {
         this.value = t;
      } else if (!Objects.equals(this.value, t)) {
         this.value = t;
         this.onValueUpdate.accept(this.value);
      }

   }

   public OptionInstance.ValueSet<T> values() {
      return this.values;
   }

   public String getResourceKey() {
      return this.resourceKey;
   }

   public Component getCaption() {
      return this.caption;
   }

   public T getMinValue() {
      OptionInstance.IntRangeBase optioninstance$intrangebase = this.getIntRangeBase();
      if (optioninstance$intrangebase != null) {
         return (T)(Integer)optioninstance$intrangebase.minInclusive();
      } else {
         throw new IllegalArgumentException("Min value not supported: " + this.getResourceKey());
      }
   }

   public T getMaxValue() {
      OptionInstance.IntRangeBase optioninstance$intrangebase = this.getIntRangeBase();
      if (optioninstance$intrangebase != null) {
         return (T)(Integer)optioninstance$intrangebase.maxInclusive();
      } else {
         throw new IllegalArgumentException("Max value not supported: " + this.getResourceKey());
      }
   }

   public OptionInstance.IntRangeBase getIntRangeBase() {
      if (this.values instanceof OptionInstance.IntRangeBase) {
         OptionInstance.IntRangeBase optioninstance$intrangebase = (OptionInstance.IntRangeBase)this.values;
         return optioninstance$intrangebase;
      } else {
         return this.values instanceof SliderableValueSetInt ? ((SliderableValueSetInt)this.values).getIntRange() : null;
      }
   }

   public boolean isProgressOption() {
      return this.values instanceof OptionInstance.SliderableValueSet;
   }

   public static record AltEnum<T>(List<T> values, List<T> altValues, BooleanSupplier altCondition, OptionInstance.CycleableValueSet.ValueSetter<T> valueSetter, Codec<T> codec) implements OptionInstance.CycleableValueSet<T> {

      public CycleButton.ValueListSupplier<T> valueListSupplier() {
         return CycleButton.ValueListSupplier.create(this.altCondition, this.values, this.altValues);
      }

      public Optional<T> validateValue(T p_231570_) {
         return (this.altCondition.getAsBoolean() ? this.altValues : this.values).contains(p_231570_) ? Optional.of(p_231570_) : Optional.empty();
      }

      public OptionInstance.CycleableValueSet.ValueSetter<T> valueSetter() {
         return this.valueSetter;
      }

      public Codec<T> codec() {
         return this.codec;
      }

      public List<T> values() {
         return this.values;
      }

      public List<T> altValues() {
         return this.altValues;
      }

      public BooleanSupplier altCondition() {
         return this.altCondition;
      }

   }

   public interface CaptionBasedToString<T> {
      Component toString(Component pCaption, T pValue);
   }

   public static record ClampingLazyMaxIntRange(int minInclusive, IntSupplier maxSupplier, int encodableMaxInclusive) implements OptionInstance.IntRangeBase, OptionInstance.SliderableOrCyclableValueSet<Integer> {

      public Optional<Integer> validateValue(Integer p_231590_) {
         return Optional.of(Mth.clamp(p_231590_, this.minInclusive(), this.maxInclusive()));
      }

      public int maxInclusive() {
         return this.maxSupplier.getAsInt();
      }

      public Codec<Integer> codec() {
         return ExtraCodecs.validate(Codec.INT, (p_276075_1_) -> {
            int i = this.encodableMaxInclusive + 1;
            return p_276075_1_.compareTo(this.minInclusive) >= 0 && p_276075_1_.compareTo(i) <= 0 ? DataResult.success(p_276075_1_) : DataResult.error(() -> {
               return "Value " + p_276075_1_ + " outside of range [" + this.minInclusive + ":" + i + "]";
            }, p_276075_1_);
         });
      }

      public boolean createCycleButton() {
         return true;
      }

      public CycleButton.ValueListSupplier<Integer> valueListSupplier() {
         return CycleButton.ValueListSupplier.create(IntStream.range(this.minInclusive, this.maxInclusive() + 1).boxed().toList());
      }

      public int minInclusive() {
         return this.minInclusive;
      }

      public IntSupplier maxSupplier() {
         return this.maxSupplier;
      }

      public int encodableMaxInclusive() {
         return this.encodableMaxInclusive;
      }
   }

   interface CycleableValueSet<T> extends OptionInstance.ValueSet<T> {
      CycleButton.ValueListSupplier<T> valueListSupplier();

      default OptionInstance.CycleableValueSet.ValueSetter<T> valueSetter() {
         return OptionInstance::set;
      }

      default Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> pTooltipSupplier, Options pOptions, int pX, int pY, int pWidth, Consumer<T> pOnValueChanged) {
         return (p_260738_7_) -> {
            return CycleButton.builder(p_260738_7_.toString).withValues(this.valueListSupplier()).withTooltip(pTooltipSupplier).withInitialValue(p_260738_7_.value).create(pX, pY, pWidth, 20, p_260738_7_.caption, (p_260739_4_, p_260739_5_) -> {
               this.valueSetter().set(p_260738_7_, p_260739_5_);
               pOptions.save();
               pOnValueChanged.accept(p_260739_5_);
            });
         };
      }

      public interface ValueSetter<T> {
         void set(OptionInstance<T> pInstance, T pValue);
      }
   }

   public static record Enum<T>(List<T> values, Codec<T> codec) implements OptionInstance.CycleableValueSet<T> {

      public Optional<T> validateValue(T p_231632_) {
         return this.values.contains(p_231632_) ? Optional.of(p_231632_) : Optional.empty();
      }

      public CycleButton.ValueListSupplier<T> valueListSupplier() {
         return CycleButton.ValueListSupplier.create(this.values);
      }

      public Codec<T> codec() {
         return this.codec;
      }

      public List<T> values() {
         return this.values;
      }
   }

   public static record IntRange(int minInclusive, int maxInclusive) implements OptionInstance.IntRangeBase {

      public Optional<Integer> validateValue(Integer p_231645_) {
         return p_231645_.compareTo(this.minInclusive()) >= 0 && p_231645_.compareTo(this.maxInclusive()) <= 0 ? Optional.of(p_231645_) : Optional.empty();
      }

      public Codec<Integer> codec() {
         return Codec.intRange(this.minInclusive, this.maxInclusive + 1);
      }

      public int minInclusive() {
         return this.minInclusive;
      }

      public int maxInclusive() {
         return this.maxInclusive;
      }
   }

   public interface IntRangeBase extends OptionInstance.SliderableValueSet<Integer> {
      int minInclusive();

      int maxInclusive();

      default double toSliderValue(Integer p_231663_) {
         return (double)Mth.map((float)p_231663_.intValue(), (float)this.minInclusive(), (float)this.maxInclusive(), 0.0F, 1.0F);
      }

      default Integer fromSliderValue(double p_231656_) {
         return Mth.floor(Mth.map(p_231656_, 0.0D, 1.0D, (double)this.minInclusive(), (double)this.maxInclusive()));
      }

      default <R> OptionInstance.SliderableValueSet<R> xmap(final IntFunction<? extends R> p_231658_, final ToIntFunction<? super R> p_231659_) {
         return new SliderableValueSetInt<R>() {
            public Optional<R> validateValue(R p_231674_) {
               return IntRangeBase.this.validateValue(Integer.valueOf(p_231659_.applyAsInt(p_231674_))).map(p_231658_::apply);
            }

            public double toSliderValue(R p_231678_) {
               return IntRangeBase.this.toSliderValue(p_231659_.applyAsInt(p_231678_));
            }

            public R fromSliderValue(double p_231676_) {
               return p_231658_.apply(IntRangeBase.this.fromSliderValue(p_231676_));
            }

            public Codec<R> codec() {
               return IntRangeBase.this.codec().xmap(p_231658_::apply, p_231659_::applyAsInt);
            }

            public OptionInstance.IntRangeBase getIntRange() {
               return IntRangeBase.this;
            }
         };
      }
   }

   public static record LazyEnum<T>(Supplier<List<T>> values, Function<T, Optional<T>> validateValue, Codec<T> codec) implements OptionInstance.CycleableValueSet<T> {

      public Optional<T> validateValue(T p_231689_) {
         return this.validateValue.apply(p_231689_);
      }

      public CycleButton.ValueListSupplier<T> valueListSupplier() {
         return CycleButton.ValueListSupplier.create(this.values.get());
      }

      public Codec<T> codec() {
         return this.codec;
      }

      public Supplier<List<T>> values() {
         return this.values;
      }

      public Function<T, Optional<T>> validateValue() {
         return this.validateValue;
      }

   }

   static final class OptionInstanceSliderButton<N> extends AbstractOptionSliderButton implements IOptionControl {
      private final OptionInstance<N> instance;
      private final OptionInstance.SliderableValueSet<N> values;
      private final OptionInstance.TooltipSupplier<N> tooltipSupplier;
      private final Consumer<N> onValueChanged;
      private boolean supportAdjusting;
      private boolean adjusting;

      OptionInstanceSliderButton(Options pOptions, int pX, int pY, int pWidth, int pHeight, OptionInstance<N> pInstance, OptionInstance.SliderableValueSet<N> pValues, OptionInstance.TooltipSupplier<N> pTooltipSupplier, Consumer<N> pOnValueChanged) {
         super(pOptions, pX, pY, pWidth, pHeight, pValues.toSliderValue(pInstance.get()));
         this.instance = pInstance;
         this.values = pValues;
         this.tooltipSupplier = pTooltipSupplier;
         this.onValueChanged = pOnValueChanged;
         this.updateMessage();
         this.supportAdjusting = FloatOptions.supportAdjusting(this.instance);
         this.adjusting = false;
      }

      protected void updateMessage() {
         if (this.adjusting) {
            double d0 = ((Number)this.values.fromSliderValue(this.value)).doubleValue();
            Component component1 = FloatOptions.getTextComponent(this.instance, d0);
            if (component1 != null) {
               this.setMessage(component1);
            }

         } else {
            if (this.instance instanceof SliderPercentageOptionOF) {
               SliderPercentageOptionOF sliderpercentageoptionof = (SliderPercentageOptionOF)this.instance;
               Component component = sliderpercentageoptionof.getOptionText();
               if (component != null) {
                  this.setMessage(component);
               }
            } else {
               this.setMessage(this.instance.toString.apply(this.instance.get()));
            }

            this.setTooltip(this.tooltipSupplier.apply(this.values.fromSliderValue(this.value)));
         }
      }

      protected void applyValue() {
         if (!this.adjusting) {
            N n = this.instance.get();
            N n1 = this.values.fromSliderValue(this.value);
            if (!n1.equals(n)) {
               if (this.instance instanceof SliderPercentageOptionOF) {
                  SliderPercentageOptionOF sliderpercentageoptionof = (SliderPercentageOptionOF)this.instance;
                  sliderpercentageoptionof.setOptionValue(((Number)n1).doubleValue());
               }

               this.instance.set(this.values.fromSliderValue(this.value));
               this.options.save();
               this.onValueChanged.accept(this.instance.get());
            }
         }
      }

      public void onClick(double mouseX, double mouseY) {
         if (this.supportAdjusting) {
            this.adjusting = true;
         }

         super.onClick(mouseX, mouseY);
      }

      protected void onDrag(double mouseX, double mouseY, double mouseDX, double mouseDY) {
         if (this.supportAdjusting) {
            this.adjusting = true;
         }

         super.onDrag(mouseX, mouseY, mouseDX, mouseDY);
      }

      public void onRelease(double mouseX, double mouseY) {
         if (this.adjusting) {
            this.adjusting = false;
            this.applyValue();
            this.updateMessage();
         }

         super.onRelease(mouseX, mouseY);
      }

      public OptionInstance getControlOption() {
         return this.instance;
      }
   }

   interface SliderableOrCyclableValueSet<T> extends OptionInstance.CycleableValueSet<T>, OptionInstance.SliderableValueSet<T> {
      boolean createCycleButton();

      default Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> p_261786_, Options p_262030_, int p_261940_, int p_262149_, int p_261495_, Consumer<T> p_261881_) {
         return this.createCycleButton() ? OptionInstance.CycleableValueSet.super.createButton(p_261786_, p_262030_, p_261940_, p_262149_, p_261495_, p_261881_) : OptionInstance.SliderableValueSet.super.createButton(p_261786_, p_262030_, p_261940_, p_262149_, p_261495_, p_261881_);
      }
   }

   public interface SliderableValueSet<T> extends OptionInstance.ValueSet<T> {
      double toSliderValue(T pValue);

      T fromSliderValue(double pValue);

      default Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> pTooltipSupplier, Options pOptions, int pX, int pY, int pWidth, Consumer<T> pOnValueChanged) {
         return (p_260740_7_) -> {
            return new OptionInstance.OptionInstanceSliderButton<>(pOptions, pX, pY, pWidth, 20, p_260740_7_, this, pTooltipSupplier, pOnValueChanged);
         };
      }
   }

   @FunctionalInterface
   public interface TooltipSupplier<T> {
      @Nullable
      Tooltip apply(T p_259319_);
   }

   public static enum UnitDouble implements OptionInstance.SliderableValueSet<Double> {
      INSTANCE;

      public Optional<Double> validateValue(Double p_231747_) {
         return p_231747_ >= 0.0D && p_231747_ <= 1.0D ? Optional.of(p_231747_) : Optional.empty();
      }

      public double toSliderValue(Double p_231756_) {
         return p_231756_;
      }

      public Double fromSliderValue(double p_231741_) {
         return p_231741_;
      }

      public <R> OptionInstance.SliderableValueSet<R> xmap(final DoubleFunction<? extends R> p_231751_, final ToDoubleFunction<? super R> p_231752_) {
         return new OptionInstance.SliderableValueSet<R>() {
            public Optional<R> validateValue(R p_231773_) {
               return UnitDouble.this.validateValue(p_231752_.applyAsDouble(p_231773_)).map(p_231751_::apply);
            }

            public double toSliderValue(R p_231777_) {
               return UnitDouble.this.toSliderValue(p_231752_.applyAsDouble(p_231777_));
            }

            public R fromSliderValue(double p_231775_) {
               return p_231751_.apply(UnitDouble.this.fromSliderValue(p_231775_));
            }

            public Codec<R> codec() {
               return UnitDouble.this.codec().xmap(p_231751_::apply, p_231752_::applyAsDouble);
            }
         };
      }

      public Codec<Double> codec() {
         return Codec.either(Codec.doubleRange(0.0D, 1.0D), Codec.BOOL).xmap((p_231742_0_) -> {
            return p_231742_0_.map((p_231759_0_) -> {
               return p_231759_0_;
            }, (p_231744_0_) -> {
               return p_231744_0_ ? 1.0D : 0.0D;
            });
         }, Either::left);
      }
   }

   public interface ValueSet<T> {
      Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> pTooltipSupplier, Options pOptions, int pX, int pY, int pWidth, Consumer<T> pOnValueChanged);

      Optional<T> validateValue(T pValue);

      Codec<T> codec();
   }
}
