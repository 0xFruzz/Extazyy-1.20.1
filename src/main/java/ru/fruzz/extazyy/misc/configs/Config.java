package ru.fruzz.extazyy.misc.configs;

import ru.fruzz.extazyy.Extazyy;

import com.google.gson.JsonObject;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class Config {

    private final File file;
    public String author;

    public Config(String name) {
        this.file = new File(ConfigManager.CONFIG_DIR, name + ".cfg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public JsonObject save() {
        JsonObject jsonObject = new JsonObject();

        JsonObject modulesObject = new JsonObject();
        Extazyy.moduleManager.getFunctions().forEach(module -> modulesObject.add(module.name, module.save()));
        jsonObject.add("Modules", modulesObject);
        JsonObject otherObject = new JsonObject();
        if (!otherObject.has("time"))
            otherObject.addProperty("time", System.currentTimeMillis());
        if(!otherObject.has("theme")) {
            otherObject.addProperty("theme", Extazyy.getThemesUtil().getCurrentStyle().name);
        }
        jsonObject.add("Others", otherObject);
        return jsonObject;
    }

    public void load(JsonObject object, String configuration, boolean start) {
        if (object.has("Modules")) {
            JsonObject modulesObject = object.getAsJsonObject("Modules");
            Extazyy.moduleManager.getFunctions().forEach(module -> {
                if (!start && module.isState()) {
                    module.setState(false);
                }
                module.load(modulesObject.getAsJsonObject(module.name), start);
            });
        }
        if(object.has("Others")) {
            JsonObject themeObject = object.getAsJsonObject("Others");
            String themeName = themeObject.get("theme").getAsString();
            Extazyy.getThemesUtil().setCurrentThemeByName(themeName);
        }
    }

    public File getFile() {
        return file;
    }
}