package net.minecraft.network;

import net.minecraft.network.chat.Component;

public interface PacketListener {
   void onDisconnect(Component pReason);

   boolean isAcceptingMessages();

   default boolean shouldPropagateHandlingExceptions() {
      return true;
   }
}