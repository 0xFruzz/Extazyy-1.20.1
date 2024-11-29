package net.minecraft.util;

import net.minecraft.network.chat.Component;

public interface ProgressListener {
   void progressStartNoAbort(Component pComponent);

   void progressStart(Component pHeader);

   void progressStage(Component pStage);

   void progressStagePercentage(int pProgress);

   void stop();
}