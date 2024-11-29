package net.optifine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final boolean logDetail = System.getProperty("log.detail", "false").equals("true");

   public static void detail(String s) {
      if (logDetail) {
         LOGGER.info("[OptiFine] " + s);
      }

   }

   public static void dbg(String s) {
      LOGGER.info("[OptiFine] " + s);
   }

   public static void warn(String s) {
      LOGGER.warn("[OptiFine] " + s);
   }

   public static void warn(String s, Throwable t) {
      limitStackTrace(t);
      LOGGER.warn("[OptiFine] " + s, t);
   }

   public static void error(String s) {
      LOGGER.error("[OptiFine] " + s);
   }

   public static void error(String s, Throwable t) {
      limitStackTrace(t);
      LOGGER.error("[OptiFine] " + s, t);
   }

   public static void log(String s) {
      dbg(s);
   }

   private static void limitStackTrace(Throwable t) {
      StackTraceElement[] astacktraceelement = t.getStackTrace();
      if (astacktraceelement != null) {
         if (astacktraceelement.length > 35) {
            List<StackTraceElement> list = new ArrayList<>(Arrays.asList(astacktraceelement));
            List<StackTraceElement> list1 = new ArrayList<>();
            list1.addAll(list.subList(0, 30));
            list1.add(new StackTraceElement("..", "", (String)null, 0));
            list1.addAll(list.subList(list.size() - 5, list.size()));
            StackTraceElement[] astacktraceelement1 = list1.toArray(new StackTraceElement[list1.size()]);
            t.setStackTrace(astacktraceelement1);
         }
      }
   }
}