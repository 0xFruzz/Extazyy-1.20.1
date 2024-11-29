package net.minecraft.server.rcon;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NetworkDataOutputStream {
   private final ByteArrayOutputStream outputStream;
   private final DataOutputStream dataOutputStream;

   public NetworkDataOutputStream(int pCapacity) {
      this.outputStream = new ByteArrayOutputStream(pCapacity);
      this.dataOutputStream = new DataOutputStream(this.outputStream);
   }

   public void writeBytes(byte[] pData) throws IOException {
      this.dataOutputStream.write(pData, 0, pData.length);
   }

   public void writeString(String pData) throws IOException {
      this.dataOutputStream.writeBytes(pData);
      this.dataOutputStream.write(0);
   }

   public void write(int pData) throws IOException {
      this.dataOutputStream.write(pData);
   }

   public void writeShort(short pData) throws IOException {
      this.dataOutputStream.writeShort(Short.reverseBytes(pData));
   }

   public void writeInt(int pData) throws IOException {
      this.dataOutputStream.writeInt(Integer.reverseBytes(pData));
   }

   public void writeFloat(float pData) throws IOException {
      this.dataOutputStream.writeInt(Integer.reverseBytes(Float.floatToIntBits(pData)));
   }

   public byte[] toByteArray() {
      return this.outputStream.toByteArray();
   }

   public void reset() {
      this.outputStream.reset();
   }
}