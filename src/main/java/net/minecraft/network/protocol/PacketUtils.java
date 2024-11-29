package net.minecraft.network.protocol;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.Level;
import net.optifine.util.PacketRunnable;
import org.slf4j.Logger;

public class PacketUtils {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static ResourceKey<Level> lastDimensionType = null;

   public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> pPacket, T pProcessor, ServerLevel pLevel) throws RunningOnDifferentThreadException {
      ensureRunningOnSameThread(pPacket, pProcessor, pLevel.getServer());
   }

   public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> pPacket, T pProcessor, BlockableEventLoop<?> pExecutor) throws RunningOnDifferentThreadException {
      if (!pExecutor.isSameThread()) {
         pExecutor.executeIfPossible(new PacketRunnable(pPacket, () -> {
            clientPreProcessPacket(pPacket);
            if (pProcessor.isAcceptingMessages()) {
               try {
                  pPacket.handle(pProcessor);
               } catch (Exception exception) {
                  if (pProcessor.shouldPropagateHandlingExceptions()) {
                     throw exception;
                  }

                  LOGGER.error("Failed to handle packet {}, suppressing error", pPacket, exception);
               }
            } else {
               LOGGER.debug("Ignoring packet due to disconnection: {}", (Object)pPacket);
            }

         }));
         throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
      } else {
         clientPreProcessPacket(pPacket);
      }
   }

   protected static void clientPreProcessPacket(Packet packetIn) {
      if (packetIn instanceof ClientboundPlayerPositionPacket) {
         Minecraft.getInstance().levelRenderer.onPlayerPositionSet();
      }

      if (packetIn instanceof ClientboundRespawnPacket clientboundrespawnpacket) {
         lastDimensionType = clientboundrespawnpacket.getDimension();
      } else if (packetIn instanceof ClientboundLoginPacket clientboundloginpacket) {
         lastDimensionType = clientboundloginpacket.dimension();
      } else {
         lastDimensionType = null;
      }

   }
}