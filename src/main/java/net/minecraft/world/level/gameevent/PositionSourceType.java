package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface PositionSourceType<T extends PositionSource> {
   PositionSourceType<BlockPositionSource> BLOCK = register("block", new BlockPositionSource.Type());
   PositionSourceType<EntityPositionSource> ENTITY = register("entity", new EntityPositionSource.Type());

   T read(FriendlyByteBuf pByteBuf);

   void write(FriendlyByteBuf pByteBuf, T pSource);

   Codec<T> codec();

   static <S extends PositionSourceType<T>, T extends PositionSource> S register(String pId, S pType) {
      return Registry.register(BuiltInRegistries.POSITION_SOURCE_TYPE, pId, pType);
   }

   static PositionSource fromNetwork(FriendlyByteBuf pByteBuf) {
      ResourceLocation resourcelocation = pByteBuf.readResourceLocation();
      return BuiltInRegistries.POSITION_SOURCE_TYPE.getOptional(resourcelocation).orElseThrow(() -> {
         return new IllegalArgumentException("Unknown position source type " + resourcelocation);
      }).read(pByteBuf);
   }

   static <T extends PositionSource> void toNetwork(T pSource, FriendlyByteBuf pByteBuf) {
      pByteBuf.writeResourceLocation(BuiltInRegistries.POSITION_SOURCE_TYPE.getKey(pSource.getType()));
      ((PositionSourceType<T>)pSource.getType()).write(pByteBuf, pSource);
   }
}