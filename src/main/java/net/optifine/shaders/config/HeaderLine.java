package net.optifine.shaders.config;

public abstract class HeaderLine {
   public abstract String getText();

   public abstract boolean matches(String var1);

   public abstract String removeFrom(String var1);

   public boolean equals(Object obj) {
      if (!(obj instanceof HeaderLine)) {
         return false;
      } else {
         String s = ((HeaderLine)obj).getText();
         return this.getText().equals(s);
      }
   }

   public int hashCode() {
      return this.getText().hashCode();
   }

   public String toString() {
      return this.getText();
   }
}