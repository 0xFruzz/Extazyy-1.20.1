package ru.fruzz.extazyy.misc.font;


import net.minecraft.resources.ResourceLocation;

public class Texture extends ResourceLocation {
    public Texture(String path) {
        super("Extazyy", validatePath(path));
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