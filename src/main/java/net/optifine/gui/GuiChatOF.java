package net.optifine.gui;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.optifine.Config;
import net.optifine.shaders.Shaders;

public class GuiChatOF extends ChatScreen {
   private static final String CMD_RELOAD_SHADERS = "/reloadShaders";
   private static final String CMD_RELOAD_CHUNKS = "/reloadChunks";

   public GuiChatOF(ChatScreen guiChat) {
      super(VideoSettingsScreen.getGuiChatText(guiChat));
   }

   public boolean handleChatInput(String msg, boolean add) {
      if (this.checkCustomCommand(msg)) {
         this.minecraft.gui.getChat().addRecentChat(msg);
         return false;
      } else {
         return super.handleChatInput(msg, add);
      }
   }

   private boolean checkCustomCommand(String msg) {
      if (msg == null) {
         return false;
      } else {
         msg = msg.trim();
         if (msg.equals("/reloadShaders")) {
            if (Config.isShaders()) {
               Shaders.uninit();
               Shaders.loadShaderPack();
            }

            return true;
         } else if (msg.equals("/reloadChunks")) {
            this.minecraft.levelRenderer.allChanged();
            return true;
         } else {
            return false;
         }
      }
   }
}