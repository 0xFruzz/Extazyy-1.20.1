package net.optifine.shaders.config;

public class HeaderLineText extends HeaderLine {
   private String text;

   public HeaderLineText(String text) {
      this.text = text;
   }

   public String getText() {
      return this.text;
   }

   public boolean matches(String line) {
      return line.equals(this.text);
   }

   public String removeFrom(String line) {
      return null;
   }
}