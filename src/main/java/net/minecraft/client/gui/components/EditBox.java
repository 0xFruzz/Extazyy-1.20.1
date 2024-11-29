package net.minecraft.client.gui.components;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EditBox extends AbstractWidget implements Renderable {
   public static final int BACKWARDS = -1;
   public static final int FORWARDS = 1;
   private static final int CURSOR_INSERT_WIDTH = 1;
   private static final int CURSOR_INSERT_COLOR = -3092272;
   private static final String CURSOR_APPEND_CHARACTER = "_";
   public static final int DEFAULT_TEXT_COLOR = 14737632;
   private static final int BORDER_COLOR_FOCUSED = -1;
   private static final int BORDER_COLOR = -6250336;
   private static final int BACKGROUND_COLOR = -16777216;
   private final Font font;
   private String value = "";
   private int maxLength = 32;
   private int frame;
   private boolean bordered = true;
   private boolean canLoseFocus = true;
   private boolean isEditable = true;
   private boolean shiftPressed;
   private int displayPos;
   private int cursorPos;
   private int highlightPos;
   private int textColor = 14737632;
   private int textColorUneditable = 7368816;
   @Nullable
   private String suggestion;
   @Nullable
   private Consumer<String> responder;
   private Predicate<String> filter = Objects::nonNull;
   private BiFunction<String, Integer, FormattedCharSequence> formatter = (p_94147_, p_94148_) -> {
      return FormattedCharSequence.forward(p_94147_, Style.EMPTY);
   };
   @Nullable
   private Component hint;

   public EditBox(Font pFont, int pX, int pY, int pWidth, int pHeight, Component pMessage) {
      this(pFont, pX, pY, pWidth, pHeight, (EditBox)null, pMessage);
   }

   public EditBox(Font pFont, int pX, int pY, int pWidth, int pHeight, @Nullable EditBox pEditBox, Component pMessage) {
      super(pX, pY, pWidth, pHeight, pMessage);
      this.font = pFont;
      if (pEditBox != null) {
         this.setValue(pEditBox.getValue());
      }

   }

   public void setResponder(Consumer<String> pResponder) {
      this.responder = pResponder;
   }

   public void setFormatter(BiFunction<String, Integer, FormattedCharSequence> pTextFormatter) {
      this.formatter = pTextFormatter;
   }

   public void tick() {
      ++this.frame;
   }

   protected MutableComponent createNarrationMessage() {
      Component component = this.getMessage();
      return Component.translatable("gui.narrate.editBox", component, this.value);
   }

   public void setValue(String pText) {
      if (this.filter.test(pText)) {
         if (pText.length() > this.maxLength) {
            this.value = pText.substring(0, this.maxLength);
         } else {
            this.value = pText;
         }

         this.moveCursorToEnd();
         this.setHighlightPos(this.cursorPos);
         this.onValueChange(pText);
      }
   }

   public String getValue() {
      return this.value;
   }

   public String getHighlighted() {
      int i = Math.min(this.cursorPos, this.highlightPos);
      int j = Math.max(this.cursorPos, this.highlightPos);
      return this.value.substring(i, j);
   }

   public void setFilter(Predicate<String> pValidator) {
      this.filter = pValidator;
   }

   public void insertText(String pTextToWrite) {
      int i = Math.min(this.cursorPos, this.highlightPos);
      int j = Math.max(this.cursorPos, this.highlightPos);
      int k = this.maxLength - this.value.length() - (i - j);
      String s = SharedConstants.filterText(pTextToWrite);
      int l = s.length();
      if (k < l) {
         s = s.substring(0, k);
         l = k;
      }

      String s1 = (new StringBuilder(this.value)).replace(i, j, s).toString();
      if (this.filter.test(s1)) {
         this.value = s1;
         this.setCursorPosition(i + l);
         this.setHighlightPos(this.cursorPos);
         this.onValueChange(this.value);
      }
   }

   private void onValueChange(String pNewText) {
      if (this.responder != null) {
         this.responder.accept(pNewText);
      }

   }

   private void deleteText(int pCount) {
      if (Screen.hasControlDown()) {
         this.deleteWords(pCount);
      } else {
         this.deleteChars(pCount);
      }

   }

   public void deleteWords(int pNum) {
      if (!this.value.isEmpty()) {
         if (this.highlightPos != this.cursorPos) {
            this.insertText("");
         } else {
            this.deleteChars(this.getWordPosition(pNum) - this.cursorPos);
         }
      }
   }

   public void deleteChars(int pNum) {
      if (!this.value.isEmpty()) {
         if (this.highlightPos != this.cursorPos) {
            this.insertText("");
         } else {
            int i = this.getCursorPos(pNum);
            int j = Math.min(i, this.cursorPos);
            int k = Math.max(i, this.cursorPos);
            if (j != k) {
               String s = (new StringBuilder(this.value)).delete(j, k).toString();
               if (this.filter.test(s)) {
                  this.value = s;
                  this.moveCursorTo(j);
               }
            }
         }
      }
   }

   public int getWordPosition(int pNumWords) {
      return this.getWordPosition(pNumWords, this.getCursorPosition());
   }

   private int getWordPosition(int pN, int pPos) {
      return this.getWordPosition(pN, pPos, true);
   }

   private int getWordPosition(int pN, int pPos, boolean pSkipWs) {
      int i = pPos;
      boolean flag = pN < 0;
      int j = Math.abs(pN);

      for(int k = 0; k < j; ++k) {
         if (!flag) {
            int l = this.value.length();
            i = this.value.indexOf(32, i);
            if (i == -1) {
               i = l;
            } else {
               while(pSkipWs && i < l && this.value.charAt(i) == ' ') {
                  ++i;
               }
            }
         } else {
            while(pSkipWs && i > 0 && this.value.charAt(i - 1) == ' ') {
               --i;
            }

            while(i > 0 && this.value.charAt(i - 1) != ' ') {
               --i;
            }
         }
      }

      return i;
   }

   public void moveCursor(int pDelta) {
      this.moveCursorTo(this.getCursorPos(pDelta));
   }

   private int getCursorPos(int pDelta) {
      return Util.offsetByCodepoints(this.value, this.cursorPos, pDelta);
   }

   public void moveCursorTo(int pPos) {
      this.setCursorPosition(pPos);
      if (!this.shiftPressed) {
         this.setHighlightPos(this.cursorPos);
      }

      this.onValueChange(this.value);
   }

   public void setCursorPosition(int pPos) {
      this.cursorPos = Mth.clamp(pPos, 0, this.value.length());
   }

   public void moveCursorToStart() {
      this.moveCursorTo(0);
   }

   public void moveCursorToEnd() {
      this.moveCursorTo(this.value.length());
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (!this.canConsumeInput()) {
         return false;
      } else {
         this.shiftPressed = Screen.hasShiftDown();
         if (Screen.isSelectAll(pKeyCode)) {
            this.moveCursorToEnd();
            this.setHighlightPos(0);
            return true;
         } else if (Screen.isCopy(pKeyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            return true;
         } else if (Screen.isPaste(pKeyCode)) {
            if (this.isEditable) {
               this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            }

            return true;
         } else if (Screen.isCut(pKeyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            if (this.isEditable) {
               this.insertText("");
            }

            return true;
         } else {
            switch (pKeyCode) {
               case 259:
                  if (this.isEditable) {
                     this.shiftPressed = false;
                     this.deleteText(-1);
                     this.shiftPressed = Screen.hasShiftDown();
                  }

                  return true;
               case 260:
               case 264:
               case 265:
               case 266:
               case 267:
               default:
                  return false;
               case 261:
                  if (this.isEditable) {
                     this.shiftPressed = false;
                     this.deleteText(1);
                     this.shiftPressed = Screen.hasShiftDown();
                  }

                  return true;
               case 262:
                  if (Screen.hasControlDown()) {
                     this.moveCursorTo(this.getWordPosition(1));
                  } else {
                     this.moveCursor(1);
                  }

                  return true;
               case 263:
                  if (Screen.hasControlDown()) {
                     this.moveCursorTo(this.getWordPosition(-1));
                  } else {
                     this.moveCursor(-1);
                  }

                  return true;
               case 268:
                  this.moveCursorToStart();
                  return true;
               case 269:
                  this.moveCursorToEnd();
                  return true;
            }
         }
      }
   }

   public boolean canConsumeInput() {
      return this.isVisible() && this.isFocused() && this.isEditable();
   }

   public boolean charTyped(char pCodePoint, int pModifiers) {
      if (!this.canConsumeInput()) {
         return false;
      } else if (SharedConstants.isAllowedChatCharacter(pCodePoint)) {
         if (this.isEditable) {
            this.insertText(Character.toString(pCodePoint));
         }

         return true;
      } else {
         return false;
      }
   }

   public void onClick(double pMouseX, double pMouseY) {
      int i = Mth.floor(pMouseX) - this.getX();
      if (this.bordered) {
         i -= 4;
      }

      String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
      this.moveCursorTo(this.font.plainSubstrByWidth(s, i).length() + this.displayPos);
   }

   public void playDownSound(SoundManager pHandler) {
   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      if (this.isVisible()) {
         if (this.isBordered()) {
            int i = this.isFocused() ? -1 : -6250336;
            pGuiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, i);
            pGuiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -16777216);
         }

         int i2 = this.isEditable ? this.textColor : this.textColorUneditable;
         int j = this.cursorPos - this.displayPos;
         int k = this.highlightPos - this.displayPos;
         String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
         boolean flag = j >= 0 && j <= s.length();
         boolean flag1 = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
         int l = this.bordered ? this.getX() + 4 : this.getX();
         int i1 = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
         int j1 = l;
         if (k > s.length()) {
            k = s.length();
         }

         if (!s.isEmpty()) {
            String s1 = flag ? s.substring(0, j) : s;
            j1 = pGuiGraphics.drawString(this.font, this.formatter.apply(s1, this.displayPos), l, i1, i2);
         }

         boolean flag2 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
         int k1 = j1;
         if (!flag) {
            k1 = j > 0 ? l + this.width : l;
         } else if (flag2) {
            k1 = j1 - 1;
            --j1;
         }

         if (!s.isEmpty() && flag && j < s.length()) {
            pGuiGraphics.drawString(this.font, this.formatter.apply(s.substring(j), this.cursorPos), j1, i1, i2);
         }

         if (this.hint != null && s.isEmpty() && !this.isFocused()) {
            pGuiGraphics.drawString(this.font, this.hint, j1, i1, i2);
         }

         if (!flag2 && this.suggestion != null) {
            pGuiGraphics.drawString(this.font, this.suggestion, k1 - 1, i1, -8355712);
         }

         if (flag1) {
            if (flag2) {
               pGuiGraphics.fill(RenderType.guiOverlay(), k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
            } else {
               pGuiGraphics.drawString(this.font, "_", k1, i1, i2);
            }
         }

         if (k != j) {
            int l1 = l + this.font.width(s.substring(0, k));
            this.renderHighlight(pGuiGraphics, k1, i1 - 1, l1 - 1, i1 + 1 + 9);
         }

      }
   }

   private void renderHighlight(GuiGraphics pGuiGraphics, int pMinX, int pMinY, int pMaxX, int pMaxY) {
      if (pMinX < pMaxX) {
         int i = pMinX;
         pMinX = pMaxX;
         pMaxX = i;
      }

      if (pMinY < pMaxY) {
         int j = pMinY;
         pMinY = pMaxY;
         pMaxY = j;
      }

      if (pMaxX > this.getX() + this.width) {
         pMaxX = this.getX() + this.width;
      }

      if (pMinX > this.getX() + this.width) {
         pMinX = this.getX() + this.width;
      }

      pGuiGraphics.fill(RenderType.guiTextHighlight(), pMinX, pMinY, pMaxX, pMaxY, -16776961);
   }

   public void setMaxLength(int pLength) {
      this.maxLength = pLength;
      if (this.value.length() > pLength) {
         this.value = this.value.substring(0, pLength);
         this.onValueChange(this.value);
      }

   }

   private int getMaxLength() {
      return this.maxLength;
   }

   public int getCursorPosition() {
      return this.cursorPos;
   }

   private boolean isBordered() {
      return this.bordered;
   }

   public void setBordered(boolean pEnableBackgroundDrawing) {
      this.bordered = pEnableBackgroundDrawing;
   }

   public void setTextColor(int pColor) {
      this.textColor = pColor;
   }

   public void setTextColorUneditable(int pColor) {
      this.textColorUneditable = pColor;
   }

   @Nullable
   public ComponentPath nextFocusPath(FocusNavigationEvent pEvent) {
      return this.visible && this.isEditable ? super.nextFocusPath(pEvent) : null;
   }

   public boolean isMouseOver(double pMouseX, double pMouseY) {
      return this.visible && pMouseX >= (double)this.getX() && pMouseX < (double)(this.getX() + this.width) && pMouseY >= (double)this.getY() && pMouseY < (double)(this.getY() + this.height);
   }

   public void setFocused(boolean pFocused) {
      if (this.canLoseFocus || pFocused) {
         super.setFocused(pFocused);
         if (pFocused) {
            this.frame = 0;
         }

      }
   }

   private boolean isEditable() {
      return this.isEditable;
   }

   public void setEditable(boolean pEnabled) {
      this.isEditable = pEnabled;
   }

   public int getInnerWidth() {
      return this.isBordered() ? this.width - 8 : this.width;
   }

   public void setHighlightPos(int pPosition) {
      int i = this.value.length();
      this.highlightPos = Mth.clamp(pPosition, 0, i);
      if (this.font != null) {
         if (this.displayPos > i) {
            this.displayPos = i;
         }

         int j = this.getInnerWidth();
         String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), j);
         int k = s.length() + this.displayPos;
         if (this.highlightPos == this.displayPos) {
            this.displayPos -= this.font.plainSubstrByWidth(this.value, j, true).length();
         }

         if (this.highlightPos > k) {
            this.displayPos += this.highlightPos - k;
         } else if (this.highlightPos <= this.displayPos) {
            this.displayPos -= this.displayPos - this.highlightPos;
         }

         this.displayPos = Mth.clamp(this.displayPos, 0, i);
      }

   }

   public void setCanLoseFocus(boolean pCanLoseFocus) {
      this.canLoseFocus = pCanLoseFocus;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean pIsVisible) {
      this.visible = pIsVisible;
   }

   public void setSuggestion(@Nullable String pSuggestion) {
      this.suggestion = pSuggestion;
   }

   public int getScreenX(int pCharNum) {
      return pCharNum > this.value.length() ? this.getX() : this.getX() + this.font.width(this.value.substring(0, pCharNum));
   }

   public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
      pNarrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
   }

   public void setHint(Component pHint) {
      this.hint = pHint;
   }
}