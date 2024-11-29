package net.optifine;

import java.util.concurrent.ExecutorService;
import net.minecraft.client.Minecraft;
import net.optifine.util.ExecutorProxy;

public class SmartExecutorService extends ExecutorProxy {
   private ExecutorService executor;

   public SmartExecutorService(ExecutorService executor) {
      this.executor = executor;
   }

   protected ExecutorService delegate() {
      return this.executor;
   }

   public void execute(final Runnable command) {
      Runnable runnable = new Runnable() {
         public void run() {
            long i = System.currentTimeMillis();
            command.run();
            long j = System.currentTimeMillis();
            long k = j - i;
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null && (minecraft.isLocalServer() || minecraft.level != null)) {
               Config.sleep(10L * k);
            }

         }
      };
      super.execute(runnable);
   }
}