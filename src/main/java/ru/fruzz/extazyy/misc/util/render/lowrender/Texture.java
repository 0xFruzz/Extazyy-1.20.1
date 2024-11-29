package ru.fruzz.extazyy.misc.util.render.lowrender;


import net.minecraft.resources.ResourceLocation;

public class Texture extends ResourceLocation {
    public Texture(String path) {
        super("minced", validatePath(path));
    }

    public Texture(ResourceLocation i) {
        super(i.getNamespace(), i.getPath());
    }

    static String validatePath(String path) {
        if (ResourceLocation.isValidPath(path)) {
            return path;
        }
        StringBuilder ret = new StringBuilder();
        for (char c : path.toLowerCase().toCharArray()) {
            if (ResourceLocation.validPathChar(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }
}