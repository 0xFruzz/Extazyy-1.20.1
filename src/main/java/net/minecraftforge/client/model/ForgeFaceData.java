package net.minecraftforge.client.model;

public record ForgeFaceData(int color, int blockLight, int skyLight, boolean ambientOcclusion) {
   public static final ForgeFaceData DEFAULT = new ForgeFaceData(-1, 0, 0, true);
}