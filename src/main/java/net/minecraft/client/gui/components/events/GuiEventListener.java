package net.minecraft.client.gui.components.events;

import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface GuiEventListener extends TabOrderedElement {
   long DOUBLE_CLICK_THRESHOLD_MS = 250L;

   default void mouseMoved(double pMouseX, double pMouseY) {
   }

   default boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      return false;
   }

   default boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
      return false;
   }

   default boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
      return false;
   }

   default boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
      return false;
   }

   default boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      return false;
   }

   default boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
      return false;
   }

   default boolean charTyped(char pCodePoint, int pModifiers) {
      return false;
   }

   @Nullable
   default ComponentPath nextFocusPath(FocusNavigationEvent pEvent) {
      return null;
   }

   default boolean isMouseOver(double pMouseX, double pMouseY) {
      return false;
   }

   void setFocused(boolean pFocused);

   boolean isFocused();

   @Nullable
   default ComponentPath getCurrentFocusPath() {
      return this.isFocused() ? ComponentPath.leaf(this) : null;
   }

   default ScreenRectangle getRectangle() {
      return ScreenRectangle.empty();
   }
}