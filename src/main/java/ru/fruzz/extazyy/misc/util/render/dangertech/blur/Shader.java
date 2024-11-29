package ru.fruzz.extazyy.misc.util.render.dangertech.blur;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import ru.fruzz.extazyy.misc.util.Mine;

import java.io.IOException;


public class Shader implements Mine {
    protected ShaderInstance program;

    protected Shader(String name, VertexFormat vertexFormat) {
        try {
            this.program = new ShaderInstance(mc.getResourceManager(), new ResourceLocation(name), vertexFormat);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Shader create(String name, VertexFormat vertexFormat) {
        return new Shader(name, vertexFormat);
    }

    public Uniform uniform(String name) {
        return this.program.getUniform(name);
    }

    public void setSample(String name, int id) {
        this.program.setSampler(name, id);
    }

    public void bind() {
        RenderSystem.setShader(() -> this.program);
    }

    public void unbind() {
        RenderSystem.setShader(() -> null);
    }

    public static void drawQuads(Matrix4f matrix4f, float x, float y, float width, float height) {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(matrix4f, x, y, 0).endVertex();
        bufferBuilder.vertex(matrix4f, x, y + height, 0).endVertex();
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0).endVertex();
        bufferBuilder.vertex(matrix4f, x + width, y, 0).endVertex();

        tesselator.end();
    }

    public static void drawQuadsTex(Matrix4f matrix, float x, float y, float width, float height) {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, x, y, 0).uv(0.0F, 0.0F).endVertex();
        bufferBuilder.vertex(matrix, x, y + height, 0).uv(0.0F, 1.0F).endVertex();
        bufferBuilder.vertex(matrix, x + width, y + height, 0).uv(1.0F, 1.0F).endVertex();
        bufferBuilder.vertex(matrix, x + width, y, 0).uv(1.0F, 0.0F).endVertex();
        tesselator.end();

    }
}
