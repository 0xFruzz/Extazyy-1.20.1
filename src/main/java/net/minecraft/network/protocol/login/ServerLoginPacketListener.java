package net.minecraft.network.protocol.login;

import net.minecraft.network.protocol.game.ServerPacketListener;

public interface ServerLoginPacketListener extends ServerPacketListener {
   void handleHello(ServerboundHelloPacket pPacket);

   void handleKey(ServerboundKeyPacket pPacket);

   void handleCustomQueryPacket(ServerboundCustomQueryPacket pPacket);
}