package net.minecraft.network.protocol.game;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundLevelParticlesPacket implements Packet<ClientGamePacketListener> {
   private final double x;
   private final double y;
   private final double z;
   private final float xDist;
   private final float yDist;
   private final float zDist;
   private final float maxSpeed;
   private final int count;
   private final boolean overrideLimiter;
   private final ParticleOptions particle;

   public <T extends ParticleOptions> ClientboundLevelParticlesPacket(T pParticle, boolean pOverrideLimiter, double pX, double pY, double pZ, float pXDist, float pYDist, float pZDist, float pMaxSpeed, int pCount) {
      this.particle = pParticle;
      this.overrideLimiter = pOverrideLimiter;
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.xDist = pXDist;
      this.yDist = pYDist;
      this.zDist = pZDist;
      this.maxSpeed = pMaxSpeed;
      this.count = pCount;
   }

   public ClientboundLevelParticlesPacket(FriendlyByteBuf pBuffer) {
      ParticleType<?> particletype = pBuffer.readById(BuiltInRegistries.PARTICLE_TYPE);
      this.overrideLimiter = pBuffer.readBoolean();
      this.x = pBuffer.readDouble();
      this.y = pBuffer.readDouble();
      this.z = pBuffer.readDouble();
      this.xDist = pBuffer.readFloat();
      this.yDist = pBuffer.readFloat();
      this.zDist = pBuffer.readFloat();
      this.maxSpeed = pBuffer.readFloat();
      this.count = pBuffer.readInt();
      this.particle = this.readParticle(pBuffer, particletype);
   }

   private <T extends ParticleOptions> T readParticle(FriendlyByteBuf pBuffer, ParticleType<T> pParticleType) {
      return pParticleType.getDeserializer().fromNetwork(pParticleType, pBuffer);
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeId(BuiltInRegistries.PARTICLE_TYPE, this.particle.getType());
      pBuffer.writeBoolean(this.overrideLimiter);
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeFloat(this.xDist);
      pBuffer.writeFloat(this.yDist);
      pBuffer.writeFloat(this.zDist);
      pBuffer.writeFloat(this.maxSpeed);
      pBuffer.writeInt(this.count);
      this.particle.writeToNetwork(pBuffer);
   }

   public boolean isOverrideLimiter() {
      return this.overrideLimiter;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public float getXDist() {
      return this.xDist;
   }

   public float getYDist() {
      return this.yDist;
   }

   public float getZDist() {
      return this.zDist;
   }

   public float getMaxSpeed() {
      return this.maxSpeed;
   }

   public int getCount() {
      return this.count;
   }

   public ParticleOptions getParticle() {
      return this.particle;
   }

   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleParticleEvent(this);
   }
}