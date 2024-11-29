package ru.fruzz.extazyy.misc.util.render;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import ru.fruzz.extazyy.misc.util.Mine;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.renderer.ShaderInstance;

import java.io.IOException;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Shader implements Mine {
    ShaderInstance program;

    public Shader(String name, VertexFormat vertexFormat) {
        try {
            this.program = new ShaderInstance(mc.getResourceManager(), name, vertexFormat);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }



    public static Shader create(String name, VertexFormat vertexFormat) {
        return new Shader(name, vertexFormat);
    }

    public void setSample(String name, int id) {
        this.program.setSampler(name, id);
    }

    public Uniform uniform(String name) {
        return this.program.getUniform(name);
    }

    public void bind() {
        RenderSystem.setShader(() -> this.program);
    }

    public void unbind() {
        RenderSystem.setShader(() -> null);
    }
}
