package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.commands.Command;
import ru.fruzz.extazyy.main.drag.DragManager;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.misc.util.Mine;

@OnlyIn(Dist.CLIENT)
public class ChatScreen extends Screen {
   public static final double MOUSE_SCROLL_SPEED = 7.0D;
   private static final Component USAGE_TEXT = Component.translatable("chat_screen.usage");
   private static final int TOOLTIP_MAX_WIDTH = 210;
   private String historyBuffer = "";
   private int historyPos = -1;
   protected EditBox input;
   private String initial;
   CommandSuggestions commandSuggestions;

   public ChatScreen(String pInitial) {
      super(Component.translatable("chat_screen.title"));
      this.initial = pInitial;
   }

   protected void init() {
      this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
      this.input = new EditBox(this.minecraft.fontFilterFishy, 4, this.height - 12, this.width - 4, 12, Component.translatable("chat.editBox")) {
         protected MutableComponent createNarrationMessage() {
            return super.createNarrationMessage().append(ChatScreen.this.commandSuggestions.getNarrationMessage());
         }
      };
      this.input.setMaxLength(256);
      this.input.setBordered(false);
      this.input.setValue(this.initial);
      this.input.setResponder(this::onEdited);
      this.input.setCanLoseFocus(false);
      this.addWidget(this.input);
      this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
      this.commandSuggestions.updateCommandInfo();
      this.setInitialFocus(this.input);
   }

   public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
      String s = this.input.getValue();
      this.init(pMinecraft, pWidth, pHeight);
      this.setChatLine(s);
      this.commandSuggestions.updateCommandInfo();
   }

   public void removed() {
      this.minecraft.gui.getChat().resetChatScroll();
   }

   public void tick() {
      this.input.tick();
   }

   private void onEdited(String p_95611_) {
      String s = this.input.getValue();
      this.commandSuggestions.setAllowSuggestions(!s.equals(this.initial));
      this.commandSuggestions.updateCommandInfo();
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (this.commandSuggestions.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else if (super.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else if (pKeyCode == 256) {
         this.minecraft.setScreen((Screen)null);
         return true;
      } else if (pKeyCode != 257 && pKeyCode != 335) {
         if (pKeyCode == 265) {
            this.moveInHistory(-1);
            return true;
         } else if (pKeyCode == 264) {
            this.moveInHistory(1);
            return true;
         } else if (pKeyCode == 266) {
            this.minecraft.gui.getChat().scrollChat(this.minecraft.gui.getChat().getLinesPerPage() - 1);
            return true;
         } else if (pKeyCode == 267) {
            this.minecraft.gui.getChat().scrollChat(-this.minecraft.gui.getChat().getLinesPerPage() + 1);
            return true;
         } else {
            return false;
         }
      } else {
         if (this.handleChatInput(this.input.getValue(), true)) {
            this.minecraft.setScreen((Screen)null);
         }

         return true;
      }
   }

   public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
      pDelta = Mth.clamp(pDelta, -1.0D, 1.0D);
      if (this.commandSuggestions.mouseScrolled(pDelta)) {
         return true;
      } else {
         if (!hasShiftDown()) {
            pDelta *= 7.0D;
         }

         this.minecraft.gui.getChat().scrollChat((int)pDelta);
         return true;
      }
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if(Extazyy.getModuleManager().hud.state) {
         Extazyy.getModuleManager().hud.onClicked(pMouseX, pMouseY, pButton);
      }
      if (this.commandSuggestions.mouseClicked((double)((int)pMouseX), (double)((int)pMouseY), pButton)) {
         return true;
      } else {
         if (pButton == 0) {
            ChatComponent chatcomponent = this.minecraft.gui.getChat();
            if (chatcomponent.handleChatQueueClicked(pMouseX, pMouseY)) {
               return true;
            }

            Style style = this.getComponentStyleAt(pMouseX, pMouseY);
            if (style != null && this.handleComponentClicked(style)) {
               this.initial = this.input.getValue();
               return true;
            }
         }
         DragManager.draggables.values().forEach(dragging -> {
            if (dragging.getModule().state ) {

               dragging.onClick(pMouseX, pMouseY, pButton);
            }
         });
         return this.input.mouseClicked(pMouseX, pMouseY, pButton) ? true : super.mouseClicked(pMouseX, pMouseY, pButton);
      }

   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      DragManager.draggables.values().forEach(dragging -> {
         if (dragging.getModule().state) {
            dragging.onRelease(button);
         }
      });
      return super.mouseReleased(mouseX, mouseY, button);
   }

   protected void insertText(String pText, boolean pOverwrite) {
      if (pOverwrite) {
         this.input.setValue(pText);
      } else {
         this.input.insertText(pText);
      }

   }

   public void moveInHistory(int pMsgPos) {
      int i = this.historyPos + pMsgPos;
      int j = this.minecraft.gui.getChat().getRecentChat().size();
      i = Mth.clamp(i, 0, j);
      if (i != this.historyPos) {
         if (i == j) {
            this.historyPos = j;
            this.input.setValue(this.historyBuffer);
         } else {
            if (this.historyPos == j) {
               this.historyBuffer = this.input.getValue();
            }

            this.input.setValue(this.minecraft.gui.getChat().getRecentChat().get(i));
            this.commandSuggestions.setAllowSuggestions(false);
            this.historyPos = i;
         }
      }
   }



   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      if(Extazyy.getModuleManager().hud.state) {
         drawDrag(pGuiGraphics);
      }
      pGuiGraphics.fill(2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
      this.input.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.commandSuggestions.render(pGuiGraphics, pMouseX, pMouseY);
      GuiMessageTag guimessagetag = this.minecraft.gui.getChat().getMessageTagAt((double)pMouseX, (double)pMouseY);
      if (guimessagetag != null && guimessagetag.text() != null) {
         pGuiGraphics.renderTooltip(this.font, this.font.split(guimessagetag.text(), 210), pMouseX, pMouseY);
      } else {
         Style style = this.getComponentStyleAt((double)pMouseX, (double)pMouseY);
         if (style != null && style.getHoverEvent() != null) {
            pGuiGraphics.renderComponentHoverEffect(this.font, style, pMouseX, pMouseY);
         }
      }

      Window window = Mine.mc.getWindow();
      DragManager.draggables.values().forEach(dragging -> {
         if (dragging.getModule().state) {
            dragging.onDraw(pMouseX, pMouseY, window);
         }
      });

   }

   public void drawDrag(GuiGraphics guiGraphics) {
      float width = Mine.mc.getWindow().getGuiScaledWidth();
      float height = Mine.mc.getWindow().getGuiScaledHeight();

      FontRenderers.msSemi12.drawCenteredString(guiGraphics.pose(), "Зажать ЛКМ - Изменение расположения модуля." ,width / 2, 5, -1);
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void setChatLine(String pChatLine) {
      this.input.setValue(pChatLine);
   }

   protected void updateNarrationState(NarrationElementOutput pOutput) {
      pOutput.add(NarratedElementType.TITLE, this.getTitle());
      pOutput.add(NarratedElementType.USAGE, USAGE_TEXT);
      String s = this.input.getValue();
      if (!s.isEmpty()) {
         pOutput.nest().add(NarratedElementType.TITLE, Component.translatable("chat_screen.message", s));
      }

   }

   @Nullable
   private Style getComponentStyleAt(double pMouseX, double pMouseY) {
      return this.minecraft.gui.getChat().getClickedComponentStyleAt(pMouseX, pMouseY);
   }

   public boolean handleChatInput(String pInput, boolean pAddToRecentChat) {
      pInput = this.normalizeChatMessage(pInput);
      if (pInput.isEmpty()) {
         return true;
      } else {
         if (pAddToRecentChat) {
            this.minecraft.gui.getChat().addRecentChat(pInput);
         }

         if (pInput.startsWith("/")) {
            this.minecraft.player.connection.sendCommand(pInput.substring(1));
         } else if (pInput.startsWith(".")) {
            for (Command command : Extazyy.commandmgr.getCommands()) {
               if (!pInput.startsWith("." + command.command)) continue;
               try {
                  command.run(pInput.split(" "));
               }
               catch (Exception ex) {
                  command.error();
                  ex.printStackTrace();
               }
            }
         } else {
            this.minecraft.player.connection.sendChat(pInput);
         }

         return true;
      }
   }

   public String normalizeChatMessage(String pMessage) {
      return StringUtil.trimChatMessage(StringUtils.normalizeSpace(pMessage.trim()));
   }
}