package net.minecraft;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import net.minecraft.util.MemoryReserve;
import net.optifine.CrashReporter;
import net.optifine.reflect.Reflector;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

public class CrashReport {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
   private final String title;
   private final Throwable exception;
   private final List<CrashReportCategory> details = Lists.newArrayList();
   private File saveFile;
   private boolean trackingStackTrace = true;
   private StackTraceElement[] uncategorizedStackTrace = new StackTraceElement[0];
   private final SystemReport systemReport = new SystemReport();
   private boolean reported = false;

   public CrashReport(String pTitle, Throwable pException) {
      this.title = pTitle;
      this.exception = pException;
   }

   public String getTitle() {
      return this.title;
   }

   public Throwable getException() {
      return this.exception;
   }

   public String getDetails() {
      StringBuilder stringbuilder = new StringBuilder();
      this.getDetails(stringbuilder);
      return stringbuilder.toString();
   }

   public void getDetails(StringBuilder pBuilder) {
      if ((this.uncategorizedStackTrace == null || this.uncategorizedStackTrace.length <= 0) && !this.details.isEmpty()) {
         this.uncategorizedStackTrace = ArrayUtils.subarray((StackTraceElement[])this.details.get(0).getStacktrace(), 0, 1);
      }

      if (this.uncategorizedStackTrace != null && this.uncategorizedStackTrace.length > 0) {
         pBuilder.append("-- Head --\n");
         pBuilder.append("Thread: ").append(Thread.currentThread().getName()).append("\n");
         if (Reflector.CrashReportExtender_generateEnhancedStackTraceSTE.exists()) {
            pBuilder.append(Reflector.CrashReportAnalyser_appendSuspectedMods.callString(this.exception, this.uncategorizedStackTrace));
            pBuilder.append("Stacktrace:");
            pBuilder.append(Reflector.CrashReportExtender_generateEnhancedStackTraceSTE.callString1(this.uncategorizedStackTrace));
         } else {
            pBuilder.append("Stacktrace:\n");

            for(StackTraceElement stacktraceelement : this.uncategorizedStackTrace) {
               pBuilder.append("\t").append("at ").append((Object)stacktraceelement);
               pBuilder.append("\n");
            }

            pBuilder.append("\n");
         }
      }

      for(CrashReportCategory crashreportcategory : this.details) {
         crashreportcategory.getDetails(pBuilder);
         pBuilder.append("\n\n");
      }

      Reflector.CrashReportExtender_extendSystemReport.call((Object)this.systemReport);
      this.systemReport.appendToCrashReportString(pBuilder);
   }

   public String getExceptionMessage() {
      StringWriter stringwriter = null;
      PrintWriter printwriter = null;
      Throwable throwable = this.exception;
      if (throwable.getMessage() == null) {
         if (throwable instanceof NullPointerException) {
            throwable = new NullPointerException(this.title);
         } else if (throwable instanceof StackOverflowError) {
            throwable = new StackOverflowError(this.title);
         } else if (throwable instanceof OutOfMemoryError) {
            throwable = new OutOfMemoryError(this.title);
         }

         throwable.setStackTrace(this.exception.getStackTrace());
      }

      try {
         if (Reflector.CrashReportExtender_generateEnhancedStackTraceT.exists()) {
            return Reflector.CrashReportExtender_generateEnhancedStackTraceT.callString(throwable);
         }
      } catch (Throwable throwable1) {
         throwable1.printStackTrace();
      }

      String s;
      try {
         stringwriter = new StringWriter();
         printwriter = new PrintWriter(stringwriter);
         throwable.printStackTrace(printwriter);
         s = stringwriter.toString();
      } finally {
         IOUtils.closeQuietly((Writer)stringwriter);
         IOUtils.closeQuietly((Writer)printwriter);
      }

      return s;
   }

   public String getFriendlyReport() {
      if (!this.reported) {
         this.reported = true;
         CrashReporter.onCrashReport(this, this.systemReport);
      }

      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append("---- Minecraft Crash Report ----\n");
      if (Reflector.CrashReportExtender_addCrashReportHeader != null && Reflector.CrashReportExtender_addCrashReportHeader.exists()) {
         Reflector.CrashReportExtender_addCrashReportHeader.call(stringbuilder, this);
      }

      stringbuilder.append("// ");
      stringbuilder.append(getErrorComment());
      stringbuilder.append("\n\n");
      stringbuilder.append("Time: ");
      stringbuilder.append(DATE_TIME_FORMATTER.format(ZonedDateTime.now()));
      stringbuilder.append("\n");
      stringbuilder.append("Description: ");
      stringbuilder.append(this.title);
      stringbuilder.append("\n\n");
      stringbuilder.append(this.getExceptionMessage());
      stringbuilder.append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");

      for(int i = 0; i < 87; ++i) {
         stringbuilder.append("-");
      }

      stringbuilder.append("\n\n");
      this.getDetails(stringbuilder);
      return stringbuilder.toString();
   }

   public File getSaveFile() {
      return this.saveFile;
   }

   public boolean saveToFile(File pToFile) {
      if (this.saveFile != null) {
         return false;
      } else {
         if (pToFile.getParentFile() != null) {
            pToFile.getParentFile().mkdirs();
         }

         Writer writer = null;

         boolean flag;
         try {
            writer = new OutputStreamWriter(new FileOutputStream(pToFile), StandardCharsets.UTF_8);
            writer.write(this.getFriendlyReport());
            this.saveFile = pToFile;
            return true;
         } catch (Throwable throwable) {
            LOGGER.error("Could not save crash report to {}", pToFile, throwable);
            flag = false;
         } finally {
            IOUtils.closeQuietly(writer);
         }

         return flag;
      }
   }

   public SystemReport getSystemReport() {
      return this.systemReport;
   }

   public CrashReportCategory addCategory(String pName) {
      return this.addCategory(pName, 1);
   }

   public CrashReportCategory addCategory(String pCategoryName, int pStacktraceLength) {
      CrashReportCategory crashreportcategory = new CrashReportCategory(pCategoryName);

      try {
         if (this.trackingStackTrace) {
            int i = crashreportcategory.fillInStackTrace(pStacktraceLength);
            StackTraceElement[] astacktraceelement = this.exception.getStackTrace();
            StackTraceElement stacktraceelement = null;
            StackTraceElement stacktraceelement1 = null;
            int j = astacktraceelement.length - i;
            if (j < 0) {
               System.out.println("Negative index in crash report handler (" + astacktraceelement.length + "/" + i + ")");
            }

            if (astacktraceelement != null && 0 <= j && j < astacktraceelement.length) {
               stacktraceelement = astacktraceelement[j];
               if (astacktraceelement.length + 1 - i < astacktraceelement.length) {
                  stacktraceelement1 = astacktraceelement[astacktraceelement.length + 1 - i];
               }
            }

            this.trackingStackTrace = crashreportcategory.validateStackTrace(stacktraceelement, stacktraceelement1);
            if (astacktraceelement != null && astacktraceelement.length >= i && 0 <= j && j < astacktraceelement.length) {
               this.uncategorizedStackTrace = new StackTraceElement[j];
               System.arraycopy(astacktraceelement, 0, this.uncategorizedStackTrace, 0, this.uncategorizedStackTrace.length);
            } else {
               this.trackingStackTrace = false;
            }
         }
      } catch (Throwable throwable) {
         throwable.printStackTrace();
      }

      this.details.add(crashreportcategory);
      return crashreportcategory;
   }

   private static String getErrorComment() {
      String[] astring = new String[]{"Who set us up the TNT?", "Everything's going to plan. No, really, that was supposed to happen.", "Uh... Did I do that?", "Oops.", "Why did you do that?", "I feel sad now :(", "My bad.", "I'm sorry, Dave.", "I let you down. Sorry :(", "On the bright side, I bought you a teddy bear!", "Daisy, daisy...", "Oh - I know what I did wrong!", "Hey, that tickles! Hehehe!", "I blame Dinnerbone.", "You should try our sister game, Minceraft!", "Don't be sad. I'll do better next time, I promise!", "Don't be sad, have a hug! <3", "I just don't know what went wrong :(", "Shall we play a game?", "Quite honestly, I wouldn't worry myself about that.", "I bet Cylons wouldn't have this problem.", "Sorry :(", "Surprise! Haha. Well, this is awkward.", "Would you like a cupcake?", "Hi. I'm Minecraft, and I'm a crashaholic.", "Ooh. Shiny.", "This doesn't make any sense!", "Why is it breaking :(", "Don't do that.", "Ouch. That hurt :(", "You're mean.", "This is a token for 1 free hug. Redeem at your nearest Mojangsta: [~~HUG~~]", "There are four lights!", "But it works on my machine."};

      try {
         return astring[(int)(Util.getNanos() % (long)astring.length)];
      } catch (Throwable throwable) {
         return "Witty comment unavailable :(";
      }
   }

   public static CrashReport forThrowable(Throwable pCause, String pDescription) {
      while(pCause instanceof CompletionException && pCause.getCause() != null) {
         pCause = pCause.getCause();
      }

      CrashReport crashreport;
      if (pCause instanceof ReportedException) {
         crashreport = ((ReportedException)pCause).getReport();
      } else {
         crashreport = new CrashReport(pDescription, pCause);
      }

      return crashreport;
   }

   public static void preload() {
      MemoryReserve.allocate();
      (new CrashReport("Don't panic!", new Throwable())).getFriendlyReport();
   }
}