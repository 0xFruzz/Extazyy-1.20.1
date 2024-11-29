package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringDecomposer;
import net.minecraftforge.client.extensions.IForgeFont;
import net.optifine.util.MathUtils;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public class Font implements IForgeFont {
   private static final float EFFECT_DEPTH = 0.01F;
   private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);
   public static final int ALPHA_CUTOFF = 8;
   public final int lineHeight = 9;
   public final RandomSource random = RandomSource.create();
   private final Function<ResourceLocation, FontSet> fonts;
   final boolean filterFishyGlyphs;
   private final StringSplitter splitter;
   private Matrix4f matrixShadow = new Matrix4f();

   public Font(Function<ResourceLocation, FontSet> pFonts, boolean pFilterFishyGlyphs) {
      this.fonts = pFonts;
      this.filterFishyGlyphs = pFilterFishyGlyphs;
      this.splitter = new StringSplitter((p_243025_1_, p_243025_2_) -> {
         return this.getFontSet(p_243025_2_.getFont()).getGlyphInfo(p_243025_1_, this.filterFishyGlyphs).getAdvance(p_243025_2_.isBold());
      });
   }

   FontSet getFontSet(ResourceLocation pFontLocation) {
      return this.fonts.apply(pFontLocation);
   }

   public String bidirectionalShaping(String pText) {
      try {
         Bidi bidi = new Bidi((new ArabicShaping(8)).shape(pText), 127);
         bidi.setReorderingMode(0);
         return bidi.writeReordered(2);
      } catch (ArabicShapingException arabicshapingexception1) {
         return pText;
      }
   }

   public int drawInBatch(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, Font.DisplayMode pDisplayMode, int pBackgroundColor, int pPackedLightCoords) {
      return this.drawInBatch(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pDisplayMode, pBackgroundColor, pPackedLightCoords, this.isBidirectional());
   }

   public int drawInBatch(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, Font.DisplayMode pDisplayMode, int pBackgroundColor, int pPackedLightCoords, boolean pBidirectional) {
      return this.drawInternal(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pDisplayMode, pBackgroundColor, pPackedLightCoords, pBidirectional);
   }

   public int drawInBatch(Component pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, Font.DisplayMode pDisplayMode, int pBackgroundColor, int pPackedLightCoords) {
      return this.drawInBatch(pText.getVisualOrderText(), pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pDisplayMode, pBackgroundColor, pPackedLightCoords);
   }

   public int drawInBatch(FormattedCharSequence pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, Font.DisplayMode pDisplayMode, int pBackgroundColor, int pPackedLightCoords) {
      return this.drawInternal(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pDisplayMode, pBackgroundColor, pPackedLightCoords);
   }

   public void drawInBatch8xOutline(FormattedCharSequence pText, float pX, float pY, int pColor, int pBackgroundColor, Matrix4f pMatrix, MultiBufferSource pBufferSource, int pPackedLightCoords) {
      int i = adjustColor(pBackgroundColor);
      Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(pBufferSource, 0.0F, 0.0F, i, false, pMatrix, Font.DisplayMode.NORMAL, pPackedLightCoords);

      for(int j = -1; j <= 1; ++j) {
         for(int k = -1; k <= 1; ++k) {
            if (j != 0 || k != 0) {
               float[] afloat = new float[]{pX};
               int l = j;
               int i1 = k;
               pText.accept((p_168654_7_, p_168654_8_, p_168654_9_) -> {
                  boolean flag = p_168654_8_.isBold();
                  FontSet fontset = this.getFontSet(p_168654_8_.getFont());
                  GlyphInfo glyphinfo = fontset.getGlyphInfo(p_168654_9_, this.filterFishyGlyphs);
                  font$stringrenderoutput.x = afloat[0] + (float)l * glyphinfo.getShadowOffset();
                  font$stringrenderoutput.y = pY + (float)i1 * glyphinfo.getShadowOffset();
                  afloat[0] += glyphinfo.getAdvance(flag);
                  return font$stringrenderoutput.accept(p_168654_7_, p_168654_8_.withColor(i), p_168654_9_);
               });
            }
         }
      }

      Font.StringRenderOutput font$stringrenderoutput1 = new Font.StringRenderOutput(pBufferSource, pX, pY, adjustColor(pColor), false, pMatrix, Font.DisplayMode.POLYGON_OFFSET, pPackedLightCoords);
      pText.accept(font$stringrenderoutput1);
      font$stringrenderoutput1.finish(0, pX);
   }

   private static int adjustColor(int pColor) {
      return (pColor & -67108864) == 0 ? pColor | -16777216 : pColor;
   }

   private int drawInternal(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, Font.DisplayMode pDisplayMode, int pBackgroundColor, int pPackedLightCoords, boolean pBidirectional) {
      if (pBidirectional) {
         pText = this.bidirectionalShaping(pText);
      }

      pColor = adjustColor(pColor);
      Matrix4f matrix4f = this.matrixShadow.set((Matrix4fc)pMatrix);
      if (pDropShadow) {
         this.renderText(pText, pX, pY, pColor, true, pMatrix, pBuffer, pDisplayMode, pBackgroundColor, pPackedLightCoords);
         matrix4f.translate(SHADOW_OFFSET);
      }

      pX = this.renderText(pText, pX, pY, pColor, false, matrix4f, pBuffer, pDisplayMode, pBackgroundColor, pPackedLightCoords);
      return (int)pX + (pDropShadow ? 1 : 0);
   }

   private int drawInternal(FormattedCharSequence pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, Font.DisplayMode pDisplayMode, int pBackgroundColor, int pPackedLightCoords) {
      pColor = adjustColor(pColor);
      Matrix4f matrix4f = this.matrixShadow.set((Matrix4fc)pMatrix);
      if (pDropShadow) {
         this.renderText(pText, pX, pY, pColor, true, pMatrix, pBuffer, pDisplayMode, pBackgroundColor, pPackedLightCoords);
         matrix4f.translate(SHADOW_OFFSET);
      }

      pX = this.renderText(pText, pX, pY, pColor, false, matrix4f, pBuffer, pDisplayMode, pBackgroundColor, pPackedLightCoords);
      return (int)pX + (pDropShadow ? 1 : 0);
   }

   private float renderText(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, Font.DisplayMode pDisplayMode, int pBackgroundColor, int pPackedLightCoords) {
      Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(pBuffer, pX, pY, pColor, pDropShadow, pMatrix, pDisplayMode, pPackedLightCoords);
      StringDecomposer.iterateFormatted(pText, Style.EMPTY, font$stringrenderoutput);
      return font$stringrenderoutput.finish(pBackgroundColor, pX);
   }

   private float renderText(FormattedCharSequence pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, Font.DisplayMode pDisplayMode, int pBackgroundColor, int pPackedLightCoords) {
      Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(pBuffer, pX, pY, pColor, pDropShadow, pMatrix, pDisplayMode, pPackedLightCoords);
      pText.accept(font$stringrenderoutput);
      return font$stringrenderoutput.finish(pBackgroundColor, pX);
   }

   void renderChar(BakedGlyph pGlyph, boolean pBold, boolean pItalic, float pBoldOffset, float pX, float pY, Matrix4f pMatrix, VertexConsumer pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pPackedLight) {
      pGlyph.render(pItalic, pX, pY, pMatrix, pBuffer, pRed, pGreen, pBlue, pAlpha, pPackedLight);
      if (pBold) {
         pGlyph.render(pItalic, pX + pBoldOffset, pY, pMatrix, pBuffer, pRed, pGreen, pBlue, pAlpha, pPackedLight);
      }

   }

   public int width(String pText) {
      return Mth.ceil(this.splitter.stringWidth(pText));
   }

   public int width(FormattedText pText) {
      return Mth.ceil(this.splitter.stringWidth(pText));
   }

   public int width(FormattedCharSequence pText) {
      return Mth.ceil(this.splitter.stringWidth(pText));
   }

   public String plainSubstrByWidth(String pText, int pMaxWidth, boolean pTail) {
      return pTail ? this.splitter.plainTailByWidth(pText, pMaxWidth, Style.EMPTY) : this.splitter.plainHeadByWidth(pText, pMaxWidth, Style.EMPTY);
   }

   public String plainSubstrByWidth(String pText, int pMaxWidth) {
      return this.splitter.plainHeadByWidth(pText, pMaxWidth, Style.EMPTY);
   }

   public FormattedText substrByWidth(FormattedText pText, int pMaxWidth) {
      return this.splitter.headByWidth(pText, pMaxWidth, Style.EMPTY);
   }

   public int wordWrapHeight(String pText, int pMaxWidth) {
      return 9 * this.splitter.splitLines(pText, pMaxWidth, Style.EMPTY).size();
   }

   public int wordWrapHeight(FormattedText pText, int pMaxWidth) {
      return 9 * this.splitter.splitLines(pText, pMaxWidth, Style.EMPTY).size();
   }

   public List<FormattedCharSequence> split(FormattedText pText, int pMaxWidth) {
      return Language.getInstance().getVisualOrder(this.splitter.splitLines(pText, pMaxWidth, Style.EMPTY));
   }

   public boolean isBidirectional() {
      return Language.getInstance().isDefaultRightToLeft();
   }

   public StringSplitter getSplitter() {
      return this.splitter;
   }

   public Font self() {
      return this;
   }

   public static enum DisplayMode {
      NORMAL,
      SEE_THROUGH,
      POLYGON_OFFSET;
   }

   class StringRenderOutput implements FormattedCharSink {
      final MultiBufferSource bufferSource;
      private final boolean dropShadow;
      private final float dimFactor;
      private final float r;
      private final float g;
      private final float b;
      private final float a;
      private final Matrix4f pose;
      private final Font.DisplayMode mode;
      private final int packedLightCoords;
      float x;
      float y;
      @Nullable
      private List<BakedGlyph.Effect> effects;
      private Style lastStyle;
      private FontSet lastStyleFont;

      private void addEffect(BakedGlyph.Effect pEffect) {
         if (this.effects == null) {
            this.effects = Lists.newArrayList();
         }

         this.effects.add(pEffect);
      }

      public StringRenderOutput(MultiBufferSource pBufferSource, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pPose, Font.DisplayMode pMode, int pPackedLightCoords) {
         this.bufferSource = pBufferSource;
         this.x = pX;
         this.y = pY;
         this.dropShadow = pDropShadow;
         this.dimFactor = pDropShadow ? 0.25F : 1.0F;
         this.r = (float)(pColor >> 16 & 255) / 255.0F * this.dimFactor;
         this.g = (float)(pColor >> 8 & 255) / 255.0F * this.dimFactor;
         this.b = (float)(pColor & 255) / 255.0F * this.dimFactor;
         this.a = (float)(pColor >> 24 & 255) / 255.0F;
         this.pose = MathUtils.isIdentity(pPose) ? BakedGlyph.MATRIX_IDENTITY : pPose;
         this.mode = pMode;
         this.packedLightCoords = pPackedLightCoords;
      }

      public boolean accept(int pPositionInCurrentSequence, Style pStyle, int pCodePoint) {
         FontSet fontset = this.getFont(pStyle);
         GlyphInfo glyphinfo = fontset.getGlyphInfo(pCodePoint, Font.this.filterFishyGlyphs);
         BakedGlyph bakedglyph = pStyle.isObfuscated() && pCodePoint != 32 ? fontset.getRandomGlyph(glyphinfo) : fontset.getGlyph(pCodePoint);
         boolean flag = pStyle.isBold();
         float f = this.a;
         TextColor textcolor = pStyle.getColor();
         float f1;
         float f2;
         float f3;
         if (textcolor != null) {
            int i = textcolor.getValue();
            f1 = (float)(i >> 16 & 255) / 255.0F * this.dimFactor;
            f2 = (float)(i >> 8 & 255) / 255.0F * this.dimFactor;
            f3 = (float)(i & 255) / 255.0F * this.dimFactor;
         } else {
            f1 = this.r;
            f2 = this.g;
            f3 = this.b;
         }

         if (!(bakedglyph instanceof EmptyGlyph)) {
            float f5 = flag ? glyphinfo.getBoldOffset() : 0.0F;
            float f4 = this.dropShadow ? glyphinfo.getShadowOffset() : 0.0F;
            VertexConsumer vertexconsumer = this.bufferSource.getBuffer(bakedglyph.renderType(this.mode));
            Font.this.renderChar(bakedglyph, flag, pStyle.isItalic(), f5, this.x + f4, this.y + f4, this.pose, vertexconsumer, f1, f2, f3, f, this.packedLightCoords);
         }

         float f6 = glyphinfo.getAdvance(flag);
         float f7 = this.dropShadow ? 1.0F : 0.0F;
         if (pStyle.isStrikethrough()) {
            this.addEffect(new BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 4.5F, this.x + f7 + f6, this.y + f7 + 4.5F - 1.0F, 0.01F, f1, f2, f3, f));
         }

         if (pStyle.isUnderlined()) {
            this.addEffect(new BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 9.0F, this.x + f7 + f6, this.y + f7 + 9.0F - 1.0F, 0.01F, f1, f2, f3, f));
         }

         this.x += f6;
         return true;
      }

      public float finish(int pBackgroundColor, float pX) {
         if (pBackgroundColor != 0) {
            float f = (float)(pBackgroundColor >> 24 & 255) / 255.0F;
            float f1 = (float)(pBackgroundColor >> 16 & 255) / 255.0F;
            float f2 = (float)(pBackgroundColor >> 8 & 255) / 255.0F;
            float f3 = (float)(pBackgroundColor & 255) / 255.0F;
            this.addEffect(new BakedGlyph.Effect(pX - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, f1, f2, f3, f));
         }

         if (this.effects != null) {
            BakedGlyph bakedglyph = Font.this.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
            VertexConsumer vertexconsumer = this.bufferSource.getBuffer(bakedglyph.renderType(this.mode));

            for(BakedGlyph.Effect bakedglyph$effect : this.effects) {
               bakedglyph.renderEffect(bakedglyph$effect, this.pose, vertexconsumer, this.packedLightCoords);
            }
         }

         return this.x;
      }

      private FontSet getFont(Style styleIn) {
         if (styleIn == this.lastStyle) {
            return this.lastStyleFont;
         } else {
            this.lastStyle = styleIn;
            this.lastStyleFont = Font.this.getFontSet(styleIn.getFont());
            return this.lastStyleFont;
         }
      }
   }
}