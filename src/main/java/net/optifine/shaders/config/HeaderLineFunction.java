package net.optifine.shaders.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderLineFunction extends HeaderLine {
   private String name;
   private String text;
   private Pattern patternLine;

   public HeaderLineFunction(String name, String text) {
      this.name = name;
      this.text = text;
      this.patternLine = Pattern.compile("^\\s*\\w+\\s+" + name + "\\s*\\(.*\\).*$", 32);
   }

   public String getText() {
      return this.text;
   }

   public boolean matches(String line) {
      if (!line.contains(this.name)) {
         return false;
      } else {
         Matcher matcher = this.patternLine.matcher(line);
         return matcher.matches();
      }
   }

   public String removeFrom(String line) {
      return null;
   }
}