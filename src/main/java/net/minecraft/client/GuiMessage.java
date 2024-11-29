package net.minecraft.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.util.FormattedCharSequence;

public record GuiMessage(int addedTime, Component content, MessageSignature signature, GuiMessageTag tag) {

   public int addedTime() {
      return this.addedTime;
   }

   public Component content() {
      return this.content;
   }

   public MessageSignature signature() {
      return this.signature;
   }

   public GuiMessageTag tag() {
      return this.tag;
   }

   public static record Line(int addedTime, FormattedCharSequence content, GuiMessageTag tag, boolean endOfEntry) {

      public int addedTime() {
         return this.addedTime;
      }

      public FormattedCharSequence content() {
         return this.content;
      }

      public GuiMessageTag tag() {
         return this.tag;
      }

      public boolean endOfEntry() {
         return this.endOfEntry;
      }
   }
}
