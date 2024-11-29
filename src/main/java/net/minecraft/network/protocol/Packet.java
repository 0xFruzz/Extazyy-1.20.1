package net.minecraft.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;

public interface Packet<T extends PacketListener> {
   void write(FriendlyByteBuf pBuffer);

   void handle(T pHandler);

   default boolean isSkippable() {
      return false;
   }
}