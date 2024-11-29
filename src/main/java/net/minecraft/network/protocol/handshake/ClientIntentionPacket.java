package net.minecraft.network.protocol.handshake;

import net.minecraft.SharedConstants;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientIntentionPacket implements Packet<ServerHandshakePacketListener> {
   private static final int MAX_HOST_LENGTH = 255;
   private final int protocolVersion;
   private final String hostName;
   private final int port;
   private final ConnectionProtocol intention;

   public ClientIntentionPacket(String pHostName, int pPort, ConnectionProtocol pIntention) {
      this.protocolVersion = SharedConstants.getCurrentVersion().getProtocolVersion();
      this.hostName = pHostName;
      this.port = pPort;
      this.intention = pIntention;
   }

   public ClientIntentionPacket(FriendlyByteBuf pBuffer) {
      this.protocolVersion = pBuffer.readVarInt();
      this.hostName = pBuffer.readUtf(255);
      this.port = pBuffer.readUnsignedShort();
      this.intention = ConnectionProtocol.getById(pBuffer.readVarInt());
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.protocolVersion);
      pBuffer.writeUtf(this.hostName);
      pBuffer.writeShort(this.port);
      pBuffer.writeVarInt(this.intention.getId());
   }

   public void handle(ServerHandshakePacketListener pHandler) {
      pHandler.handleIntention(this);
   }

   public ConnectionProtocol getIntention() {
      return this.intention;
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }

   public String getHostName() {
      return this.hostName;
   }

   public int getPort() {
      return this.port;
   }
}