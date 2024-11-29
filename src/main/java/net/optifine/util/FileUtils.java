package net.optifine.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
   public static List<String> collectFiles(File folder, boolean recursive) {
      List<String> list = new ArrayList<>();
      collectFiles(folder, "", list, recursive);
      return list;
   }

   public static void collectFiles(File folder, String basePath, List<String> list, boolean recursive) {
      File[] afile = folder.listFiles();
      if (afile != null) {
         for(int i = 0; i < afile.length; ++i) {
            File file1 = afile[i];
            if (file1.isFile()) {
               String s = basePath + file1.getName();
               list.add(s);
            } else if (recursive && file1.isDirectory()) {
               String s1 = basePath + file1.getName() + "/";
               collectFiles(file1, s1, list, recursive);
            }
         }

      }
   }
}