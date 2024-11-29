package net.minecraft.network.protocol.game;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public record ClientboundUpdateEnabledFeaturesPacket(Set<ResourceLocation> features) implements Packet<ClientGamePacketListener> {
   public ClientboundUpdateEnabledFeaturesPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.<ResourceLocation, Set<ResourceLocation>>readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeCollection(this.features, FriendlyByteBuf::writeResourceLocation);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleEnabledFeatures(this);
   }
}