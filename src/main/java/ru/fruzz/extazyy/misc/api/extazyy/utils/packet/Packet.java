package ru.fruzz.extazyy.misc.api.extazyy.utils.packet;

import java.net.HttpURLConnection;
import java.net.URL;

public class Packet {
   private final String host;
   private final String[] args;
   private final Returnable thr;
   public static long lastPacketTime;
   private boolean post;

   private Packet(String host, Returnable thr, String... args) {
      this.host = host;
      this.thr = thr;
      this.args = args;
   }

   public static Packet newInstance(String host, Returnable returnable, String... args) {
      return new Packet(host, returnable, args);
   }

   public void send() {
      try {
         lastPacketTime = System.currentTimeMillis();
         StringBuilder args = new StringBuilder();

         for(int i = 0; i < this.args.length; ++i) {
            args.append(this.args[i]);
            if (i < this.args.length - 1) {
               args.append("/");
            }
         }

         String fullString = host + "/" + args;
         URL url = new URL(fullString);
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         if (post) {
            thr.returnSome(connection);
         }

         connection.getResponseCode();
         if (!post) {
            thr.returnSome(connection);
         }

         connection.disconnect();
      } catch (Exception e) {
      }

   }

   public Packet setPost(boolean b) {
      post = b;
      return this;
   }

}
