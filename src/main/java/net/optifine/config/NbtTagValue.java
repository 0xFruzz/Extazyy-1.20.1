package net.optifine.config;

import java.util.Arrays;
import java.util.regex.Pattern;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.optifine.Config;
import net.optifine.util.StrUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class NbtTagValue {
   private String[] parents = null;
   private String name = null;
   private boolean negative = false;
   private boolean raw = false;
   private int type = 0;
   private String value = null;
   private Pattern valueRegex = null;
   private RangeListInt valueRange = null;
   private int valueFormat = 0;
   private static final int TYPE_INVALID = -1;
   private static final int TYPE_TEXT = 0;
   private static final int TYPE_PATTERN = 1;
   private static final int TYPE_IPATTERN = 2;
   private static final int TYPE_REGEX = 3;
   private static final int TYPE_IREGEX = 4;
   private static final int TYPE_RANGE = 5;
   private static final int TYPE_EXISTS = 6;
   private static final String PREFIX_PATTERN = "pattern:";
   private static final String PREFIX_IPATTERN = "ipattern:";
   private static final String PREFIX_REGEX = "regex:";
   private static final String PREFIX_IREGEX = "iregex:";
   private static final String PREFIX_RAW = "raw:";
   private static final String PREFIX_RANGE = "range:";
   private static final String PREFIX_EXISTS = "exists:";
   private static final int FORMAT_DEFAULT = 0;
   private static final int FORMAT_HEX_COLOR = 1;
   private static final String PREFIX_HEX_COLOR = "#";
   private static final Pattern PATTERN_HEX_COLOR = Pattern.compile("^#[0-9a-f]{6}+$");

   public NbtTagValue(String tag, String value) {
      String[] astring = Config.tokenize(tag, ".");
      this.parents = Arrays.copyOfRange(astring, 0, astring.length - 1);
      this.name = astring[astring.length - 1];
      if (value.startsWith("!")) {
         this.negative = true;
         value = value.substring(1);
      }

      if (value.startsWith("raw:")) {
         this.raw = true;
         value = value.substring("raw:".length());
      }

      if (value.startsWith("pattern:")) {
         this.type = 1;
         value = value.substring("pattern:".length());
         if (value.equals("*")) {
            this.type = 6;
         }
      } else if (value.startsWith("ipattern:")) {
         this.type = 2;
         value = value.substring("ipattern:".length()).toLowerCase();
         if (value.equals("*")) {
            this.type = 6;
         }
      } else if (value.startsWith("regex:")) {
         this.type = 3;
         value = value.substring("regex:".length());
         this.valueRegex = Pattern.compile(value);
         if (value.equals(".*")) {
            this.type = 6;
         }
      } else if (value.startsWith("iregex:")) {
         this.type = 4;
         value = value.substring("iregex:".length());
         this.valueRegex = Pattern.compile(value, 2);
         if (value.equals(".*")) {
            this.type = 6;
         }
      } else if (value.startsWith("range:")) {
         this.type = 5;
         value = value.substring("range:".length());
         ConnectedParser connectedparser = new ConnectedParser("NbtTag");
         this.valueRange = connectedparser.parseRangeListIntNeg(value);
         if (this.valueRange == null) {
            Config.warn("Invalid range: " + value);
            this.type = -1;
            this.negative = false;
         }
      } else if (value.startsWith("exists:")) {
         this.type = 6;
         value = value.substring("exists:".length());
         Boolean obool = Config.parseBoolean(value, (Boolean)null);
         if (Config.isFalse(obool)) {
            this.negative = !this.negative;
         }

         if (obool == null) {
            Config.warn("Invalid exists: " + value);
            this.type = -1;
            this.negative = false;
         }
      } else {
         this.type = 0;
      }

      value = StringEscapeUtils.unescapeJava(value);
      if (this.type == 0 && PATTERN_HEX_COLOR.matcher(value).matches()) {
         this.valueFormat = 1;
      }

      this.value = value;
   }

   public boolean matches(CompoundTag nbt) {
      if (this.negative) {
         return !this.matchesCompound(nbt);
      } else {
         return this.matchesCompound(nbt);
      }
   }

   public boolean matchesCompound(CompoundTag nbt) {
      if (nbt == null) {
         return false;
      } else {
         Tag tag = nbt;

         for(int i = 0; i < this.parents.length; ++i) {
            String s = this.parents[i];
            tag = getChildTag(tag, s);
            if (tag == null) {
               return false;
            }
         }

         if (this.name.equals("*")) {
            return this.matchesAnyChild(tag);
         } else {
            Tag tag1 = getChildTag(tag, this.name);
            if (tag1 == null) {
               return false;
            } else {
               return this.matchesBase(tag1);
            }
         }
      }
   }

   private boolean matchesAnyChild(Tag tagBase) {
      if (tagBase instanceof CompoundTag compoundtag) {
         for(String s : compoundtag.getAllKeys()) {
            Tag tag = compoundtag.get(s);
            if (this.matchesBase(tag)) {
               return true;
            }
         }
      }

      if (tagBase instanceof ListTag listtag) {
         int i = listtag.size();

         for(int j = 0; j < i; ++j) {
            Tag tag1 = listtag.get(j);
            if (this.matchesBase(tag1)) {
               return true;
            }
         }
      }

      return false;
   }

   private static Tag getChildTag(Tag tagBase, String tag) {
      if (tagBase instanceof CompoundTag compoundtag) {
         return compoundtag.get(tag);
      } else if (tagBase instanceof ListTag listtag) {
         if (tag.equals("count")) {
            return IntTag.valueOf(listtag.size());
         } else {
            int i = Config.parseInt(tag, -1);
            return i >= 0 && i < listtag.size() ? listtag.get(i) : null;
         }
      } else {
         return null;
      }
   }

   public boolean matchesBase(Tag nbtBase) {
      if (nbtBase == null) {
         return false;
      } else {
         switch (this.type) {
            case -1:
               return false;
            case 5:
               int i = getNbtInt(nbtBase, Integer.MIN_VALUE);
               if (i != Integer.MIN_VALUE) {
                  return matchesRange(i, this.valueRange);
               }
            case 6:
               return true;
            default:
               String s = this.raw ? String.valueOf((Object)nbtBase) : getNbtString(nbtBase, this.valueFormat);
               return this.matchesValue(s);
         }
      }
   }

   public boolean matchesValue(String nbtValue) {
      if (nbtValue == null) {
         return false;
      } else {
         switch (this.type) {
            case -1:
               return false;
            case 0:
               return nbtValue.equals(this.value);
            case 1:
               return matchesPattern(nbtValue, this.value);
            case 2:
               return matchesPattern(nbtValue.toLowerCase(), this.value);
            case 3:
            case 4:
               return matchesRegex(nbtValue, this.valueRegex);
            case 5:
               return matchesRange(nbtValue, this.valueRange);
            case 6:
               return true;
            default:
               throw new IllegalArgumentException("Unknown NbtTagValue type: " + this.type);
         }
      }
   }

   private static boolean matchesPattern(String str, String pattern) {
      return StrUtils.equalsMask(str, pattern, '*', '?');
   }

   private static boolean matchesRegex(String str, Pattern regex) {
      return regex.matcher(str).matches();
   }

   private static boolean matchesRange(String str, RangeListInt range) {
      if (range == null) {
         return false;
      } else {
         int i = Config.parseInt(str, Integer.MIN_VALUE);
         return i == Integer.MIN_VALUE ? false : matchesRange(i, range);
      }
   }

   private static boolean matchesRange(int valInt, RangeListInt range) {
      return range == null ? false : range.isInRange(valInt);
   }

   private static String getNbtString(Tag nbtBase, int format) {
      if (nbtBase == null) {
         return null;
      } else if (!(nbtBase instanceof StringTag)) {
         if (nbtBase instanceof IntTag) {
            IntTag inttag = (IntTag)nbtBase;
            return format == 1 ? "#" + StrUtils.fillLeft(Integer.toHexString(inttag.getAsInt()), 6, '0') : Integer.toString(inttag.getAsInt());
         } else if (nbtBase instanceof ByteTag) {
            ByteTag bytetag = (ByteTag)nbtBase;
            return Byte.toString(bytetag.getAsByte());
         } else if (nbtBase instanceof ShortTag) {
            ShortTag shorttag = (ShortTag)nbtBase;
            return Short.toString(shorttag.getAsShort());
         } else if (nbtBase instanceof LongTag) {
            LongTag longtag = (LongTag)nbtBase;
            return Long.toString(longtag.getAsLong());
         } else if (nbtBase instanceof FloatTag) {
            FloatTag floattag = (FloatTag)nbtBase;
            return Float.toString(floattag.getAsFloat());
         } else if (nbtBase instanceof DoubleTag) {
            DoubleTag doubletag = (DoubleTag)nbtBase;
            return Double.toString(doubletag.getAsDouble());
         } else {
            return nbtBase.toString();
         }
      } else {
         StringTag stringtag = (StringTag)nbtBase;
         String s = stringtag.getAsString();
         if (s.startsWith("{") && s.endsWith("}")) {
            s = getMergedJsonText(s);
         } else if (s.startsWith("[{") && s.endsWith("}]")) {
            s = getMergedJsonText(s);
         }

         return s;
      }
   }

   private static int getNbtInt(Tag nbtBase, int defVal) {
      if (nbtBase == null) {
         return defVal;
      } else if (nbtBase instanceof IntTag) {
         IntTag inttag = (IntTag)nbtBase;
         return inttag.getAsInt();
      } else if (nbtBase instanceof ByteTag) {
         ByteTag bytetag = (ByteTag)nbtBase;
         return bytetag.getAsByte();
      } else if (nbtBase instanceof ShortTag) {
         ShortTag shorttag = (ShortTag)nbtBase;
         return shorttag.getAsShort();
      } else if (nbtBase instanceof LongTag) {
         LongTag longtag = (LongTag)nbtBase;
         return (int)longtag.getAsLong();
      } else if (nbtBase instanceof FloatTag) {
         FloatTag floattag = (FloatTag)nbtBase;
         return (int)floattag.getAsFloat();
      } else if (nbtBase instanceof DoubleTag) {
         DoubleTag doubletag = (DoubleTag)nbtBase;
         return (int)doubletag.getAsDouble();
      } else {
         return defVal;
      }
   }

   private static String getMergedJsonText(String text) {
      StringBuilder stringbuilder = new StringBuilder();
      String s = "\"text\":\"";
      int i = -1;

      while(true) {
         i = text.indexOf(s, i + 1);
         if (i < 0) {
            return stringbuilder.toString();
         }

         String s1 = parseString(text, i + s.length());
         if (s1 != null) {
            stringbuilder.append(s1);
         }
      }
   }

   private static String parseString(String text, int pos) {
      StringBuilder stringbuilder = new StringBuilder();
      boolean flag = false;

      for(int i = pos; i < text.length(); ++i) {
         char c0 = text.charAt(i);
         if (flag) {
            if (c0 == 'b') {
               stringbuilder.append('\b');
            } else if (c0 == 'f') {
               stringbuilder.append('\f');
            } else if (c0 == 'n') {
               stringbuilder.append('\n');
            } else if (c0 == 'r') {
               stringbuilder.append('\r');
            } else if (c0 == 't') {
               stringbuilder.append('\t');
            } else {
               stringbuilder.append(c0);
            }

            flag = false;
         } else if (c0 == '\\') {
            flag = true;
         } else {
            if (c0 == '"') {
               break;
            }

            stringbuilder.append(c0);
         }
      }

      return stringbuilder.toString();
   }

   public String toString() {
      StringBuffer stringbuffer = new StringBuffer();

      for(int i = 0; i < this.parents.length; ++i) {
         String s = this.parents[i];
         if (i > 0) {
            stringbuffer.append(".");
         }

         stringbuffer.append(s);
      }

      if (stringbuffer.length() > 0) {
         stringbuffer.append(".");
      }

      stringbuffer.append(this.name);
      stringbuffer.append(" = ");
      stringbuffer.append(this.value);
      return stringbuffer.toString();
   }
}