package net.minecraft.network;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import ru.fruzz.extazyy.misc.event.EventManager;
import ru.fruzz.extazyy.misc.event.events.impl.EventPacket;

public class Connection extends SimpleChannelInboundHandler<Packet<?>> {
   private static final float AVERAGE_PACKETS_SMOOTHING = 0.75F;
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Marker ROOT_MARKER = MarkerFactory.getMarker("NETWORK");
   public static final Marker PACKET_MARKER = Util.make(MarkerFactory.getMarker("NETWORK_PACKETS"), (p_202569_) -> {
      p_202569_.add(ROOT_MARKER);
   });
   public static final Marker PACKET_RECEIVED_MARKER = Util.make(MarkerFactory.getMarker("PACKET_RECEIVED"), (p_202562_) -> {
      p_202562_.add(PACKET_MARKER);
   });
   public static final Marker PACKET_SENT_MARKER = Util.make(MarkerFactory.getMarker("PACKET_SENT"), (p_202557_) -> {
      p_202557_.add(PACKET_MARKER);
   });
   public static final AttributeKey<ConnectionProtocol> ATTRIBUTE_PROTOCOL = AttributeKey.valueOf("protocol");
   public static final LazyLoadedValue<NioEventLoopGroup> NETWORK_WORKER_GROUP = new LazyLoadedValue<>(() -> {
      return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build());
   });
   public static final LazyLoadedValue<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = new LazyLoadedValue<>(() -> {
      return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build());
   });
   public static final LazyLoadedValue<DefaultEventLoopGroup> LOCAL_WORKER_GROUP = new LazyLoadedValue<>(() -> {
      return new DefaultEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Client IO #%d").setDaemon(true).build());
   });
   private final PacketFlow receiving;
   private final Queue<Connection.PacketHolder> queue = Queues.newConcurrentLinkedQueue();
   private Channel channel;
   private SocketAddress address;
   private PacketListener packetListener;
   private Component disconnectedReason;
   private boolean encrypted;
   private boolean disconnectionHandled;
   private int receivedPackets;
   private int sentPackets;
   private float averageReceivedPackets;
   private float averageSentPackets;
   private int tickCount;
   private boolean handlingFault;
   @Nullable
   private volatile Component delayedDisconnect;

   public Connection(PacketFlow pReceiving) {
      this.receiving = pReceiving;
   }

   public void channelActive(ChannelHandlerContext pContext) throws Exception {
      super.channelActive(pContext);
      this.channel = pContext.channel();
      this.address = this.channel.remoteAddress();

      try {
         this.setProtocol(ConnectionProtocol.HANDSHAKING);
      } catch (Throwable throwable) {
         LOGGER.error(LogUtils.FATAL_MARKER, "Failed to change protocol to handshake", throwable);
      }

      if (this.delayedDisconnect != null) {
         this.disconnect(this.delayedDisconnect);
      }

   }

   public void setProtocol(ConnectionProtocol pNewState) {
      this.channel.attr(ATTRIBUTE_PROTOCOL).set(pNewState);
      this.channel.attr(BundlerInfo.BUNDLER_PROVIDER).set(pNewState);
      this.channel.config().setAutoRead(true);
      LOGGER.debug("Enabled auto read");
   }

   public void channelInactive(ChannelHandlerContext pContext) {
      this.disconnect(Component.translatable("disconnect.endOfStream"));
   }

   public void exceptionCaught(ChannelHandlerContext pContext, Throwable pException) {
      if (pException instanceof SkipPacketException) {
         LOGGER.debug("Skipping packet due to errors", pException.getCause());
      } else {
         boolean flag = !this.handlingFault;
         this.handlingFault = true;
         if (this.channel.isOpen()) {
            if (pException instanceof TimeoutException) {
               LOGGER.debug("Timeout", pException);
               this.disconnect(Component.translatable("disconnect.timeout"));
            } else {
               Component component = Component.translatable("disconnect.genericReason", "Internal Exception: " + pException);
               if (flag) {
                  LOGGER.debug("Failed to sent packet", pException);
                  ConnectionProtocol connectionprotocol = this.getCurrentProtocol();
                  Packet<?> packet = (Packet<?>)(connectionprotocol == ConnectionProtocol.LOGIN ? new ClientboundLoginDisconnectPacket(component) : new ClientboundDisconnectPacket(component));
                  this.send(packet, PacketSendListener.thenRun(() -> {
                     this.disconnect(component);
                  }));
                  this.setReadOnly();
               } else {
                  LOGGER.debug("Double fault", pException);
                  this.disconnect(component);
               }
            }

         }
      }
   }

   protected void channelRead0(ChannelHandlerContext pContext, Packet<?> pPacket) {
      if (this.channel.isOpen()) {
         EventPacket e = new EventPacket(pPacket, EventPacket.PacketType.RECEIVE);
         EventManager.call(e);
         if(e.isStopped()) return;
         try {
            genericsFtw(pPacket, this.packetListener);
         } catch (RunningOnDifferentThreadException runningondifferentthreadexception) {
         } catch (RejectedExecutionException rejectedexecutionexception) {
            this.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
         } catch (ClassCastException classcastexception) {
            LOGGER.error("Received {} that couldn't be processed", pPacket.getClass(), classcastexception);
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_packet"));
         }

         ++this.receivedPackets;
      }

   }

   private static <T extends PacketListener> void genericsFtw(Packet<T> pPacket, PacketListener pListener) {
      pPacket.handle((T)pListener);
   }

   public void setListener(PacketListener pHandler) {
      Validate.notNull(pHandler, "packetListener");
      this.packetListener = pHandler;
   }

   public void send(Packet<?> pPacket) {
      this.send(pPacket, (PacketSendListener)null);
   }

   public void send(Packet<?> pPacket, @Nullable PacketSendListener pSendListener) {

      EventPacket event = new EventPacket(pPacket, EventPacket.PacketType.SEND);
      EventManager.call(event);

      if (event.isStopped()) return;

      if (this.isConnected()) {
         this.flushQueue();
         this.sendPacket(pPacket, pSendListener);
      } else {
         this.queue.add(new Connection.PacketHolder(pPacket, pSendListener));
      }

   }

   private void sendPacket(Packet<?> pPacket, @Nullable PacketSendListener pSendListener) {
      ConnectionProtocol connectionprotocol = ConnectionProtocol.getProtocolForPacket(pPacket);
      ConnectionProtocol connectionprotocol1 = this.getCurrentProtocol();
      ++this.sentPackets;
      if (connectionprotocol1 != connectionprotocol) {
         if (connectionprotocol == null) {
            throw new IllegalStateException("Encountered packet without set protocol: " + pPacket);
         }

         LOGGER.debug("Disabled auto read");
         this.channel.config().setAutoRead(false);
      }

      if (this.channel.eventLoop().inEventLoop()) {
         this.doSendPacket(pPacket, pSendListener, connectionprotocol, connectionprotocol1);
      } else {
         this.channel.eventLoop().execute(() -> {
            this.doSendPacket(pPacket, pSendListener, connectionprotocol, connectionprotocol1);
         });
      }

   }

   private void doSendPacket(Packet<?> pPacket, @Nullable PacketSendListener pSendListener, ConnectionProtocol pNewProtocol, ConnectionProtocol pCurrentProtocol) {
      if (pNewProtocol != pCurrentProtocol) {
         this.setProtocol(pNewProtocol);
      }

      ChannelFuture channelfuture = this.channel.writeAndFlush(pPacket);
      if (pSendListener != null) {
         channelfuture.addListener((p_243167_) -> {
            if (p_243167_.isSuccess()) {
               pSendListener.onSuccess();
            } else {
               Packet<?> packet = pSendListener.onFailure();
               if (packet != null) {
                  ChannelFuture channelfuture1 = this.channel.writeAndFlush(packet);
                  channelfuture1.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
               }
            }

         });
      }

      channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
   }

   private ConnectionProtocol getCurrentProtocol() {
      return this.channel.attr(ATTRIBUTE_PROTOCOL).get();
   }

   private void flushQueue() {
      if (this.channel != null && this.channel.isOpen()) {
         synchronized(this.queue) {
            Connection.PacketHolder connection$packetholder;
            while((connection$packetholder = this.queue.poll()) != null) {
               this.sendPacket(connection$packetholder.packet, connection$packetholder.listener);
            }

         }
      }
   }

   public void tick() {
      this.flushQueue();
      PacketListener packetlistener = this.packetListener;
      if (packetlistener instanceof TickablePacketListener tickablepacketlistener) {
         tickablepacketlistener.tick();
      }

      if (!this.isConnected() && !this.disconnectionHandled) {
         this.handleDisconnection();
      }

      if (this.channel != null) {
         this.channel.flush();
      }

      if (this.tickCount++ % 20 == 0) {
         this.tickSecond();
      }

   }

   protected void tickSecond() {
      this.averageSentPackets = Mth.lerp(0.75F, (float)this.sentPackets, this.averageSentPackets);
      this.averageReceivedPackets = Mth.lerp(0.75F, (float)this.receivedPackets, this.averageReceivedPackets);
      this.sentPackets = 0;
      this.receivedPackets = 0;
   }

   public SocketAddress getRemoteAddress() {
      return this.address;
   }

   public void disconnect(Component pMessage) {
      if (this.channel == null) {
         this.delayedDisconnect = pMessage;
      }

      if (this.isConnected()) {
         this.channel.close().awaitUninterruptibly();
         this.disconnectedReason = pMessage;
      }

   }

   public boolean isMemoryConnection() {
      return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
   }

   public PacketFlow getReceiving() {
      return this.receiving;
   }

   public PacketFlow getSending() {
      return this.receiving.getOpposite();
   }

   public static Connection connectToServer(InetSocketAddress pAddress, boolean pUseEpollIfAvailable) {
      Connection connection = new Connection(PacketFlow.CLIENTBOUND);
      ChannelFuture channelfuture = connect(pAddress, pUseEpollIfAvailable, connection);
      channelfuture.syncUninterruptibly();
      return connection;
   }

   public static ChannelFuture connect(InetSocketAddress pAddress, boolean pUseEpollIfAvailable, final Connection pConnection) {
      Class<? extends SocketChannel> oclass;
      LazyLoadedValue<? extends EventLoopGroup> lazyloadedvalue;
      if (Epoll.isAvailable() && pUseEpollIfAvailable) {
         oclass = EpollSocketChannel.class;
         lazyloadedvalue = NETWORK_EPOLL_WORKER_GROUP;
      } else {
         oclass = NioSocketChannel.class;
         lazyloadedvalue = NETWORK_WORKER_GROUP;
      }

      return (new Bootstrap()).group(lazyloadedvalue.get()).handler(new ChannelInitializer<Channel>() {
         protected void initChannel(Channel p_129552_) {
            try {
               p_129552_.config().setOption(ChannelOption.TCP_NODELAY, true);
            } catch (ChannelException channelexception) {
            }

            ChannelPipeline channelpipeline = p_129552_.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
            Connection.configureSerialization(channelpipeline, PacketFlow.CLIENTBOUND);
            channelpipeline.addLast("packet_handler", pConnection);
         }
      }).channel(oclass).connect(pAddress.getAddress(), pAddress.getPort());
   }

   public static void configureSerialization(ChannelPipeline pPipeline, PacketFlow pFlow) {
      PacketFlow packetflow = pFlow.getOpposite();
      pPipeline.addLast("splitter", new Varint21FrameDecoder()).addLast("decoder", new PacketDecoder(pFlow)).addLast("prepender", new Varint21LengthFieldPrepender()).addLast("encoder", new PacketEncoder(packetflow)).addLast("unbundler", new PacketBundleUnpacker(packetflow)).addLast("bundler", new PacketBundlePacker(pFlow));
   }

   public static Connection connectToLocalServer(SocketAddress pAddress) {
      final Connection connection = new Connection(PacketFlow.CLIENTBOUND);
      (new Bootstrap()).group(LOCAL_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
         protected void initChannel(Channel p_129557_) {
            ChannelPipeline channelpipeline = p_129557_.pipeline();
            channelpipeline.addLast("packet_handler", connection);
         }
      }).channel(LocalChannel.class).connect(pAddress).syncUninterruptibly();
      return connection;
   }

   public void setEncryptionKey(Cipher pDecryptingCipher, Cipher pEncryptingCipher) {
      this.encrypted = true;
      this.channel.pipeline().addBefore("splitter", "decrypt", new CipherDecoder(pDecryptingCipher));
      this.channel.pipeline().addBefore("prepender", "encrypt", new CipherEncoder(pEncryptingCipher));
   }

   public boolean isEncrypted() {
      return this.encrypted;
   }

   public boolean isConnected() {
      return this.channel != null && this.channel.isOpen();
   }

   public boolean isConnecting() {
      return this.channel == null;
   }

   public PacketListener getPacketListener() {
      return this.packetListener;
   }

   @Nullable
   public Component getDisconnectedReason() {
      return this.disconnectedReason;
   }

   public void setReadOnly() {
      if (this.channel != null) {
         this.channel.config().setAutoRead(false);
      }

   }

   public void setupCompression(int pThreshold, boolean pValidateDecompressed) {
      if (pThreshold >= 0) {
         if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder) {
            ((CompressionDecoder)this.channel.pipeline().get("decompress")).setThreshold(pThreshold, pValidateDecompressed);
         } else {
            this.channel.pipeline().addBefore("decoder", "decompress", new CompressionDecoder(pThreshold, pValidateDecompressed));
         }

         if (this.channel.pipeline().get("compress") instanceof CompressionEncoder) {
            ((CompressionEncoder)this.channel.pipeline().get("compress")).setThreshold(pThreshold);
         } else {
            this.channel.pipeline().addBefore("encoder", "compress", new CompressionEncoder(pThreshold));
         }
      } else {
         if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder) {
            this.channel.pipeline().remove("decompress");
         }

         if (this.channel.pipeline().get("compress") instanceof CompressionEncoder) {
            this.channel.pipeline().remove("compress");
         }
      }

   }

   public void handleDisconnection() {
      if (this.channel != null && !this.channel.isOpen()) {
         if (this.disconnectionHandled) {
            LOGGER.warn("handleDisconnection() called twice");
         } else {
            this.disconnectionHandled = true;
            if (this.getDisconnectedReason() != null) {
               this.getPacketListener().onDisconnect(this.getDisconnectedReason());
            } else if (this.getPacketListener() != null) {
               this.getPacketListener().onDisconnect(Component.translatable("multiplayer.disconnect.generic"));
            }
         }

      }
   }

   public float getAverageReceivedPackets() {
      return this.averageReceivedPackets;
   }

   public float getAverageSentPackets() {
      return this.averageSentPackets;
   }

   static class PacketHolder {
      final Packet<?> packet;
      @Nullable
      final PacketSendListener listener;

      public PacketHolder(Packet<?> pPacket, @Nullable PacketSendListener pListener) {
         this.packet = pPacket;
         this.listener = pListener;
      }
   }
}