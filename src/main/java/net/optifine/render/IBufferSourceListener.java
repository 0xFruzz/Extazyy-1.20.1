package net.optifine.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderType;

public interface IBufferSourceListener {
   void finish(RenderType var1, BufferBuilder var2);
}