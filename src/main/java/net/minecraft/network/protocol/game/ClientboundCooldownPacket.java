package net.minecraft.network.protocol.game;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.Item;

public class ClientboundCooldownPacket implements Packet<ClientGamePacketListener> {
   private final Item item;
   private final int duration;

   public ClientboundCooldownPacket(Item pItem, int pDuration) {
      this.item = pItem;
      this.duration = pDuration;
   }

   public ClientboundCooldownPacket(FriendlyByteBuf pBuffer) {
      this.item = pBuffer.readById(BuiltInRegistries.ITEM);
      this.duration = pBuffer.readVarInt();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeId(BuiltInRegistries.ITEM, this.item);
      pBuffer.writeVarInt(this.duration);
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleItemCooldown(this);
   }

   public Item getItem() {
      return this.item;
   }

   public int getDuration() {
      return this.duration;
   }
}