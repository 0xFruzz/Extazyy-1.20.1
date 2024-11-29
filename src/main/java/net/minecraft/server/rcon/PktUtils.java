package net.minecraft.server.rcon;

import java.nio.charset.StandardCharsets;

public class PktUtils {
   public static final int MAX_PACKET_SIZE = 1460;
   public static final char[] HEX_CHAR = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

   public static String stringFromByteArray(byte[] pInput, int pOffset, int pLength) {
      int i = pLength - 1;

      int j;
      for(j = pOffset > i ? i : pOffset; 0 != pInput[j] && j < i; ++j) {
      }

      return new String(pInput, pOffset, j - pOffset, StandardCharsets.UTF_8);
   }

   public static int intFromByteArray(byte[] pInput, int pOffset) {
      return intFromByteArray(pInput, pOffset, pInput.length);
   }

   public static int intFromByteArray(byte[] pInput, int pOffset, int pLength) {
      return 0 > pLength - pOffset - 4 ? 0 : pInput[pOffset + 3] << 24 | (pInput[pOffset + 2] & 255) << 16 | (pInput[pOffset + 1] & 255) << 8 | pInput[pOffset] & 255;
   }

   public static int intFromNetworkByteArray(byte[] pInput, int pOffset, int pLength) {
      return 0 > pLength - pOffset - 4 ? 0 : pInput[pOffset] << 24 | (pInput[pOffset + 1] & 255) << 16 | (pInput[pOffset + 2] & 255) << 8 | pInput[pOffset + 3] & 255;
   }

   public static String toHexString(byte pInput) {
      return "" + HEX_CHAR[(pInput & 240) >>> 4] + HEX_CHAR[pInput & 15];
   }
}