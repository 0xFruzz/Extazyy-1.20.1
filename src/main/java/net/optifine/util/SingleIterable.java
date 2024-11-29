package net.optifine.util;

import java.util.Iterator;

public class SingleIterable<T> implements Iterable<T>, Iterator<T> {
   private T value;

   public SingleIterable() {
   }

   public SingleIterable(T value) {
      this.value = value;
   }

   public Iterator<T> iterator() {
      return this;
   }

   public boolean hasNext() {
      return this.value != null;
   }

   public T next() {
      T t = this.value;
      this.value = null;
      return t;
   }

   public void setValue(T value) {
      this.value = value;
   }
}