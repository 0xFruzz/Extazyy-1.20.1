package net.minecraft.util;

import java.util.function.IntConsumer;

public interface BitStorage {
   int getAndSet(int pIndex, int pValue);

   void set(int pIndex, int pValue);

   int get(int pIndex);

   long[] getRaw();

   int getSize();

   int getBits();

   void getAll(IntConsumer pConsumer);

   void unpack(int[] pArray);

   BitStorage copy();

   default void finish() {
   }
}