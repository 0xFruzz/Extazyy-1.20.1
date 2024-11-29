package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.slf4j.Logger;
import ru.fruzz.extazyy.misc.event.EventManager;
import ru.fruzz.extazyy.misc.event.events.impl.ChatReceivedEvent;
import ru.fruzz.extazyy.misc.util.Utils;

public class ChatComponent {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_CHAT_HISTORY = 100;
   private static final int MESSAGE_NOT_FOUND = -1;
   private static final int MESSAGE_INDENT = 4;
   private static final int MESSAGE_TAG_MARGIN_LEFT = 4;
   private static final int BOTTOM_MARGIN = 40;
   private static final int TIME_BEFORE_MESSAGE_DELETION = 60;
   private static final Component DELETED_CHAT_MESSAGE = Component.translatable("chat.deleted_marker").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
   private final Minecraft minecraft;
   private final List<String> recentChat = Lists.newArrayList();
   private final List<GuiMessage> allMessages = Lists.newArrayList();
   private final List<GuiMessage.Line> trimmedMessages = Lists.newArrayList();
   private int chatScrollbarPos;
   private boolean newMessageSinceScroll;
   private final List<ChatComponent.DelayedMessageDeletion> messageDeletionQueue = new ArrayList<>();
   private int lastChatWidth = 0;

   public ChatComponent(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void tick() {
      if (!this.messageDeletionQueue.isEmpty()) {
         this.processMessageDeletionQueue();
      }

   }

   public void render(GuiGraphics pGuiGraphics, int pTickCount, int pMouseX, int pMouseY) {
      int i = this.getWidth();
      if (this.lastChatWidth != i) {
         this.lastChatWidth = i;
         this.rescaleChat();
      }

      if (!this.isChatHidden()) {
         int j = this.getLinesPerPage();
         int k = this.trimmedMessages.size();
         if (k > 0) {
            boolean flag = this.isChatFocused();
            float f = (float)this.getScale();
            int l = Mth.ceil((float)this.getWidth() / f);
            int i1 = pGuiGraphics.guiHeight();
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().scale(f, f, 1.0F);
            pGuiGraphics.pose().translate(4.0F, 0.0F, 0.0F);
            int j1 = Mth.floor((float)(i1 - 40) / f);
            int k1 = this.getMessageEndIndexAt(this.screenToChatX((double)pMouseX), this.screenToChatY((double)pMouseY));
            double d0 = this.minecraft.options.chatOpacity().get() * (double)0.9F + (double)0.1F;
            double d1 = this.minecraft.options.textBackgroundOpacity().get();
            double d2 = this.minecraft.options.chatLineSpacing().get();
            int l1 = this.getLineHeight();
            int i2 = (int)Math.round(-8.0D * (d2 + 1.0D) + 4.0D * d2);
            int j2 = 0;

            for(int k2 = 0; k2 + this.chatScrollbarPos < this.trimmedMessages.size() && k2 < j; ++k2) {
               int l2 = k2 + this.chatScrollbarPos;
               GuiMessage.Line guimessage$line = this.trimmedMessages.get(l2);
               if (guimessage$line != null) {
                  int i3 = pTickCount - guimessage$line.addedTime();
                  if (i3 < 200 || flag) {
                     double d3 = flag ? 1.0D : getTimeFactor(i3);
                     int k3 = (int)(255.0D * d3 * d0);
                     int l3 = (int)(255.0D * d3 * d1);
                     ++j2;
                     if (k3 > 3) {
                        int i4 = 0;
                        int j4 = j1 - k2 * l1;
                        int k4 = j4 + i2;
                        pGuiGraphics.pose().pushPose();
                        pGuiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
                        if (this.minecraft.options.ofChatBackground == 5) {
                           l = this.minecraft.font.width(guimessage$line.content()) - 2;
                        }

                        if (this.minecraft.options.ofChatBackground != 3) {
                           pGuiGraphics.fill(-4, j4 - l1, 0 + l + 4 + 4, j4, l3 << 24);
                        }

                        GuiMessageTag guimessagetag = guimessage$line.tag();
                        if (guimessagetag != null) {
                           int l4 = guimessagetag.indicatorColor() | k3 << 24;
                           pGuiGraphics.fill(-4, j4 - l1, -2, j4, l4);
                           if (l2 == k1 && guimessagetag.icon() != null) {
                              int i5 = this.getTagIconLeft(guimessage$line);
                              int j5 = k4 + 9;
                              this.drawTagIcon(pGuiGraphics, i5, j5, guimessagetag.icon());
                           }
                        }

                        pGuiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
                        if (!this.minecraft.options.ofChatShadow) {
                           pGuiGraphics.drawString(this.minecraft.font, guimessage$line.content(), 0, k4, 16777215 + (k3 << 24));
                        } else {
                           pGuiGraphics.drawString(this.minecraft.font, guimessage$line.content(), 0, k4, 16777215 + (k3 << 24));
                        }

                        pGuiGraphics.pose().popPose();
                     }
                  }
               }
            }

            long k5 = this.minecraft.getChatListener().queueSize();
            if (k5 > 0L) {
               int l5 = (int)(128.0D * d0);
               int j6 = (int)(255.0D * d1);
               pGuiGraphics.pose().pushPose();
               pGuiGraphics.pose().translate(0.0F, (float)j1, 50.0F);
               pGuiGraphics.fill(-2, 0, l + 4, 9, j6 << 24);
               pGuiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
               pGuiGraphics.drawString(this.minecraft.font, Component.translatable("chat.queue", k5), 0, 1, 16777215 + (l5 << 24));
               pGuiGraphics.pose().popPose();
            }

            if (flag) {
               int i6 = this.getLineHeight();
               int k6 = k * i6;
               int l6 = j2 * i6;
               int j3 = this.chatScrollbarPos * l6 / k - j1;
               int i7 = l6 * l6 / k6;
               if (k6 != l6) {
                  int j7 = j3 > 0 ? 170 : 96;
                  int k7 = this.newMessageSinceScroll ? 13382451 : 3355562;
                  int l7 = l + 4;
                  pGuiGraphics.fill(l7, -j3, l7 + 2, -j3 - i7, k7 + (j7 << 24));
                  pGuiGraphics.fill(l7 + 2, -j3, l7 + 1, -j3 - i7, 13421772 + (j7 << 24));
               }
            }

            pGuiGraphics.pose().popPose();
         }
      }

   }

   private void drawTagIcon(GuiGraphics pGuiGraphics, int pLeft, int pBottom, GuiMessageTag.Icon pTagIcon) {
      int i = pBottom - pTagIcon.height - 1;
      pTagIcon.draw(pGuiGraphics, pLeft, i);
   }

   private int getTagIconLeft(GuiMessage.Line pLine) {
      return this.minecraft.font.width(pLine.content()) + 4;
   }

   private boolean isChatHidden() {
      return this.minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN;
   }

   private static double getTimeFactor(int pCounter) {
      double d0 = (double)pCounter / 200.0D;
      d0 = 1.0D - d0;
      d0 *= 10.0D;
      d0 = Mth.clamp(d0, 0.0D, 1.0D);
      return d0 * d0;
   }

   public void clearMessages(boolean pClearSentMsgHistory) {
      this.minecraft.getChatListener().clearQueue();
      this.messageDeletionQueue.clear();
      this.trimmedMessages.clear();
      this.allMessages.clear();
      if (pClearSentMsgHistory) {
         this.recentChat.clear();
      }

   }

   public void addMessage(Component pChatComponent) {
      this.addMessage(pChatComponent, (MessageSignature)null, this.minecraft.isSingleplayer() ? GuiMessageTag.systemSinglePlayer() : GuiMessageTag.system());
   }

   public void addMessage(Component pChatComponent, @Nullable MessageSignature pHeaderSignature, @Nullable GuiMessageTag pTag) {
      EventManager.call(new ChatReceivedEvent(pChatComponent));
      this.logChatMessage(pChatComponent, pTag);
      this.addMessage(pChatComponent, pHeaderSignature, this.minecraft.gui.getGuiTicks(), pTag, false);
   }

   private void logChatMessage(Component pChatComponent, @Nullable GuiMessageTag pTag) {
      String s = pChatComponent.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
      String s1 = Optionull.map(pTag, GuiMessageTag::logTag);
      if (s1 != null) {
         LOGGER.info("[{}] [CHAT] {}", s1, s);
      } else {
         LOGGER.info("[CHAT] {}", (Object)s);
      }

   }

   private void addMessage(Component pChatComponent, @Nullable MessageSignature pHeaderSignature, int pAddedTime, @Nullable GuiMessageTag pTag, boolean pOnlyTrim) {
      int i = Mth.floor((double)this.getWidth() / this.getScale());
      if (pTag != null && pTag.icon() != null) {
         i -= pTag.icon().width + 4 + 2;
      }

      List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(pChatComponent, i, this.minecraft.font);
      boolean flag = this.isChatFocused();

      for(int j = 0; j < list.size(); ++j) {
         FormattedCharSequence formattedcharsequence = list.get(j);
         if (flag && this.chatScrollbarPos > 0) {
            this.newMessageSinceScroll = true;
            this.scrollChat(1);
         }

         boolean flag1 = j == list.size() - 1;
         this.trimmedMessages.add(0, new GuiMessage.Line(pAddedTime, formattedcharsequence, pTag, flag1));
      }

      while(this.trimmedMessages.size() > 100) {
         this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
      }

      if (!pOnlyTrim) {
         this.allMessages.add(0, new GuiMessage(pAddedTime, pChatComponent, pHeaderSignature, pTag));

         while(this.allMessages.size() > 100) {
            this.allMessages.remove(this.allMessages.size() - 1);
         }
      }

   }

   private void processMessageDeletionQueue() {
      int i = this.minecraft.gui.getGuiTicks();
      this.messageDeletionQueue.removeIf((p_245406_2_) -> {
         if (i >= p_245406_2_.deletableAfter()) {
            return this.deleteMessageOrDelay(p_245406_2_.signature()) == null;
         } else {
            return false;
         }
      });
   }

   public void deleteMessage(MessageSignature pMessageSignature) {
      ChatComponent.DelayedMessageDeletion chatcomponent$delayedmessagedeletion = this.deleteMessageOrDelay(pMessageSignature);
      if (chatcomponent$delayedmessagedeletion != null) {
         this.messageDeletionQueue.add(chatcomponent$delayedmessagedeletion);
      }

   }

   @Nullable
   private ChatComponent.DelayedMessageDeletion deleteMessageOrDelay(MessageSignature pMessageSignature) {
      int i = this.minecraft.gui.getGuiTicks();
      ListIterator<GuiMessage> listiterator = this.allMessages.listIterator();

      while(listiterator.hasNext()) {
         GuiMessage guimessage = listiterator.next();
         if (pMessageSignature.equals(guimessage.signature())) {
            if (pMessageSignature.equals(LevelRenderer.loadVisibleChunksMessageId)) {
               listiterator.remove();
               this.refreshTrimmedMessage();
               return null;
            }

            int j = guimessage.addedTime() + 60;
            if (i >= j) {
               listiterator.set(this.createDeletedMarker(guimessage));
               this.refreshTrimmedMessage();
               return null;
            }

            return new ChatComponent.DelayedMessageDeletion(pMessageSignature, j);
         }
      }

      return null;
   }

   private GuiMessage createDeletedMarker(GuiMessage pMessage) {
      return new GuiMessage(pMessage.addedTime(), DELETED_CHAT_MESSAGE, (MessageSignature)null, GuiMessageTag.system());
   }

   public void rescaleChat() {
      this.resetChatScroll();
      this.refreshTrimmedMessage();
   }

   private void refreshTrimmedMessage() {
      this.trimmedMessages.clear();

      for(int i = this.allMessages.size() - 1; i >= 0; --i) {
         GuiMessage guimessage = this.allMessages.get(i);
         this.addMessage(guimessage.content(), guimessage.signature(), guimessage.addedTime(), guimessage.tag(), true);
      }

   }

   public List<String> getRecentChat() {
      return this.recentChat;
   }

   public void addRecentChat(String pMessage) {
      if (this.recentChat.isEmpty() || !this.recentChat.get(this.recentChat.size() - 1).equals(pMessage)) {
         this.recentChat.add(pMessage);
      }

   }

   public void resetChatScroll() {
      this.chatScrollbarPos = 0;
      this.newMessageSinceScroll = false;
   }

   public void scrollChat(int pPosInc) {
      this.chatScrollbarPos += pPosInc;
      int i = this.trimmedMessages.size();
      if (this.chatScrollbarPos > i - this.getLinesPerPage()) {
         this.chatScrollbarPos = i - this.getLinesPerPage();
      }

      if (this.chatScrollbarPos <= 0) {
         this.chatScrollbarPos = 0;
         this.newMessageSinceScroll = false;
      }

   }

   public boolean handleChatQueueClicked(double pMouseX, double pMouseY) {
      if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
         ChatListener chatlistener = this.minecraft.getChatListener();
         if (chatlistener.queueSize() == 0L) {
            return false;
         } else {
            double d0 = pMouseX - 2.0D;
            double d1 = (double)this.minecraft.getWindow().getGuiScaledHeight() - pMouseY - 40.0D;
            if (d0 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && d1 < 0.0D && d1 > (double)Mth.floor(-9.0D * this.getScale())) {
               chatlistener.acceptNextDelayedMessage();
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   @Nullable
   public Style getClickedComponentStyleAt(double pMouseX, double pMouseY) {
      double d0 = this.screenToChatX(pMouseX);
      double d1 = this.screenToChatY(pMouseY);
      int i = this.getMessageLineIndexAt(d0, d1);
      if (i >= 0 && i < this.trimmedMessages.size()) {
         GuiMessage.Line guimessage$line = this.trimmedMessages.get(i);
         return this.minecraft.font.getSplitter().componentStyleAtWidth(guimessage$line.content(), Mth.floor(d0));
      } else {
         return null;
      }
   }

   @Nullable
   public GuiMessageTag getMessageTagAt(double pMouseX, double pMouseY) {
      double d0 = this.screenToChatX(pMouseX);
      double d1 = this.screenToChatY(pMouseY);
      int i = this.getMessageEndIndexAt(d0, d1);
      if (i >= 0 && i < this.trimmedMessages.size()) {
         GuiMessage.Line guimessage$line = this.trimmedMessages.get(i);
         GuiMessageTag guimessagetag = guimessage$line.tag();
         if (guimessagetag != null && this.hasSelectedMessageTag(d0, guimessage$line, guimessagetag)) {
            return guimessagetag;
         }
      }

      return null;
   }

   private boolean hasSelectedMessageTag(double pX, GuiMessage.Line pLine, GuiMessageTag pTag) {
      if (pX < 0.0D) {
         return true;
      } else {
         GuiMessageTag.Icon guimessagetag$icon = pTag.icon();
         if (guimessagetag$icon == null) {
            return false;
         } else {
            int i = this.getTagIconLeft(pLine);
            int j = i + guimessagetag$icon.width;
            return pX >= (double)i && pX <= (double)j;
         }
      }
   }

   private double screenToChatX(double pX) {
      return pX / this.getScale() - 4.0D;
   }

   private double screenToChatY(double pY) {
      double d0 = (double)this.minecraft.getWindow().getGuiScaledHeight() - pY - 40.0D;
      return d0 / (this.getScale() * (double)this.getLineHeight());
   }

   private int getMessageEndIndexAt(double pMouseX, double pMouseY) {
      int i = this.getMessageLineIndexAt(pMouseX, pMouseY);
      if (i == -1) {
         return -1;
      } else {
         while(i >= 0) {
            if (this.trimmedMessages.get(i).endOfEntry()) {
               return i;
            }

            --i;
         }

         return i;
      }
   }

   private int getMessageLineIndexAt(double pMouseX, double pMouseY) {
      if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden()) {
         if (!(pMouseX < -4.0D) && !(pMouseX > (double)Mth.floor((double)this.getWidth() / this.getScale()))) {
            int i = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());
            if (pMouseY >= 0.0D && pMouseY < (double)i) {
               int j = Mth.floor(pMouseY + (double)this.chatScrollbarPos);
               if (j >= 0 && j < this.trimmedMessages.size()) {
                  return j;
               }
            }

            return -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private boolean isChatFocused() {
      return this.minecraft.screen instanceof ChatScreen;
   }

   public int getWidth() {
      int i = getWidth(this.minecraft.options.chatWidth().get());
      Window window = Minecraft.getInstance().getWindow();
      int j = (int)((double)(window.getWidth() - 3) / window.getGuiScale());
      return Mth.clamp(i, 0, j);
   }

   public int getHeight() {
      return getHeight(this.isChatFocused() ? this.minecraft.options.chatHeightFocused().get() : this.minecraft.options.chatHeightUnfocused().get());
   }

   public double getScale() {
      return this.minecraft.options.chatScale().get();
   }

   public static int getWidth(double pWidth) {
      int i = 320;
      int j = 40;
      return Mth.floor(pWidth * 280.0D + 40.0D);
   }

   public static int getHeight(double pHeight) {
      int i = 180;
      int j = 20;
      return Mth.floor(pHeight * 160.0D + 20.0D);
   }

   public static double defaultUnfocusedPct() {
      int i = 180;
      int j = 20;
      return 70.0D / (double)(getHeight(1.0D) - 20);
   }

   public int getLinesPerPage() {
      return this.getHeight() / this.getLineHeight();
   }

   private int getLineHeight() {
      return (int)(9.0D * (this.minecraft.options.chatLineSpacing().get() + 1.0D));
   }

   static record DelayedMessageDeletion(MessageSignature signature, int deletableAfter) {

      public MessageSignature signature() {
         return this.signature;
      }

      public int deletableAfter() {
         return this.deletableAfter;
      }
   }
}
