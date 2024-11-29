package net.optifine.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineBuffer implements Iterable<String> {
   private ArrayList<String> lines = new ArrayList<>();

   public LineBuffer() {
   }

   public LineBuffer(String[] linesArr) {
      this.lines.addAll(Arrays.asList(linesArr));
   }

   public int size() {
      return this.lines.size();
   }

   public String get(int index) {
      return this.lines.get(index);
   }

   public String set(int index, String line) {
      return this.lines.set(index, line);
   }

   public void add(String line) {
      this.checkLine(line);
      this.lines.add(line);
   }

   public void add(String[] ls) {
      for(int i = 0; i < ls.length; ++i) {
         String s = ls[i];
         this.add(s);
      }

   }

   public void insert(int index, String line) {
      this.checkLine(line);
      this.lines.add(index, line);
   }

   public void insert(int index, String[] ls) {
      for(int i = 0; i < ls.length; ++i) {
         String s = ls[i];
         this.checkLine(s);
      }

      this.lines.addAll(index, Arrays.asList(ls));
   }

   private void checkLine(String line) {
      if (line == null) {
         throw new IllegalArgumentException("Line is null");
      }
   }

   public int indexMatch(Pattern regexp) {
      return this.indexMatch(regexp, 0, true);
   }

   public int indexMatch(Pattern regexp, int startIndex) {
      return this.indexMatch(regexp, startIndex, true);
   }

   public int indexNonMatch(Pattern regexp) {
      return this.indexMatch(regexp, 0, false);
   }

   public int indexNonMatch(Pattern regexp, int startIndex) {
      return this.indexMatch(regexp, startIndex, false);
   }

   public int indexMatch(Pattern regexp, int startIndex, boolean match) {
      if (startIndex < 0) {
         return -1;
      } else {
         for(int i = startIndex; i < this.lines.size(); ++i) {
            String s = this.lines.get(i);
            Matcher matcher = regexp.matcher(s);
            if (matcher.matches() == match) {
               return i;
            }
         }

         return -1;
      }
   }

   public int lastIndexMatch(Pattern regexp) {
      return this.lastIndexMatch(regexp, this.lines.size(), true);
   }

   public int lastIndexMatch(Pattern regexp, int startIndex) {
      return this.lastIndexMatch(regexp, startIndex, true);
   }

   public int lastIndexMatch(Pattern regexp, int startIndex, boolean match) {
      if (startIndex > this.lines.size()) {
         return -1;
      } else {
         for(int i = startIndex - 1; i >= 0; --i) {
            String s = this.lines.get(i);
            Matcher matcher = regexp.matcher(s);
            if (matcher.matches() == match) {
               return i;
            }
         }

         return -1;
      }
   }

   public static LineBuffer readAll(Reader reader) throws IOException {
      LineBuffer linebuffer = new LineBuffer();
      BufferedReader bufferedreader = new BufferedReader(reader);

      while(true) {
         String s = bufferedreader.readLine();
         if (s == null) {
            bufferedreader.close();
            return linebuffer;
         }

         linebuffer.add(s);
      }
   }

   public String[] getLines() {
      return this.lines.toArray(new String[this.lines.size()]);
   }

   public Iterator<String> iterator() {
      return new LineBuffer.Itr();
   }

   public boolean contains(String line) {
      return this.indexOf(line) >= 0;
   }

   private int indexOf(String lineFind) {
      for(int i = 0; i < this.lines.size(); ++i) {
         String s = this.lines.get(i);
         if (s.equals(lineFind)) {
            return i;
         }
      }

      return -1;
   }

   public boolean remove(String lineRemove) {
      return this.lines.remove(lineRemove);
   }

   public String remove(int index) {
      return this.lines.remove(index);
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder();

      for(int i = 0; i < this.lines.size(); ++i) {
         String s = this.lines.get(i);
         stringbuilder.append(s);
         stringbuilder.append("\n");
      }

      return stringbuilder.toString();
   }

   public class Itr implements Iterator<String> {
      private int position;

      public boolean hasNext() {
         return this.position < LineBuffer.this.lines.size();
      }

      public String next() {
         String s = LineBuffer.this.lines.get(this.position);
         ++this.position;
         return s;
      }
   }
}