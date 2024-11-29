package net.optifine;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.CrashReport;
import net.minecraft.SystemReport;
import net.optifine.http.FileUploadThread;
import net.optifine.http.IFileUploadListener;
import net.optifine.shaders.Shaders;

public class CrashReporter {
   public static void onCrashReport(CrashReport crashReport, SystemReport category) {
      try {
         Throwable throwable = crashReport.getException();
         if (throwable == null) {
            return;
         }

         if (throwable.getClass().getName().contains(".fml.client.SplashProgress")) {
            return;
         }

         if (throwable.getClass() == Throwable.class) {
            return;
         }

         if (throwable.getClass() == Exception.class && Config.equals(throwable.getMessage(), "dummy")) {
            return;
         }

         extendCrashReport(category);
         if (!Config.isTelemetryOn()) {
            return;
         }

         String s = "http://optifine.net/crashReport";
         String s1 = makeReport(crashReport);
         byte[] abyte = s1.getBytes("ASCII");
         IFileUploadListener ifileuploadlistener = new IFileUploadListener() {
            public void fileUploadFinished(String url, byte[] content, Throwable exception) {
            }
         };
         Map map = new HashMap();
         map.put("OF-Version", Config.getVersion());
         map.put("OF-Summary", makeSummary(crashReport));
         FileUploadThread fileuploadthread = new FileUploadThread(s, map, abyte, ifileuploadlistener);
         fileuploadthread.setPriority(10);
         fileuploadthread.start();
         Thread.sleep(1000L);
      } catch (Exception exception) {
         Config.dbg(exception.getClass().getName() + ": " + exception.getMessage());
      }

   }

   private static String makeReport(CrashReport crashReport) {
      StringBuffer stringbuffer = new StringBuffer();
      stringbuffer.append("OptiFineVersion: " + Config.getVersion() + "\n");
      stringbuffer.append("Summary: " + makeSummary(crashReport) + "\n");
      stringbuffer.append("\n");
      stringbuffer.append(crashReport.getFriendlyReport());
      stringbuffer.append("\n");
      return stringbuffer.toString();
   }

   private static String makeSummary(CrashReport crashReport) {
      Throwable throwable = crashReport.getException();
      if (throwable == null) {
         return "Unknown";
      } else {
         StackTraceElement[] astacktraceelement = throwable.getStackTrace();
         String s = "unknown";
         if (astacktraceelement.length > 0) {
            s = astacktraceelement[0].toString().trim();
         }

         return throwable.getClass().getName() + ": " + throwable.getMessage() + " (" + crashReport.getTitle() + ") [" + s + "]";
      }
   }

   public static void extendCrashReport(SystemReport cat) {
      cat.setDetail("OptiFine Version", Config.getVersion());
      cat.setDetail("OptiFine Build", Config.getBuild());
      if (Config.getGameSettings() != null) {
         cat.setDetail("Render Distance Chunks", "" + Config.getChunkViewDistance());
         cat.setDetail("Mipmaps", "" + Config.getMipmapLevels());
         cat.setDetail("Anisotropic Filtering", "" + Config.getAnisotropicFilterLevel());
         cat.setDetail("Antialiasing", "" + Config.getAntialiasingLevel());
         cat.setDetail("Multitexture", "" + Config.isMultiTexture());
      }

      cat.setDetail("Shaders", "" + Shaders.getShaderPackName());
      cat.setDetail("OpenGlVersion", "" + Config.openGlVersion);
      cat.setDetail("OpenGlRenderer", "" + Config.openGlRenderer);
      cat.setDetail("OpenGlVendor", "" + Config.openGlVendor);
      cat.setDetail("CpuCount", "" + Config.getAvailableProcessors());
   }
}