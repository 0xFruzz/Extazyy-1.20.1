package ru.fruzz.extazyy.misc.configs;

import ru.fruzz.extazyy.Extazyy;
import com.google.gson.*;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class ConfigManager {

    public static final File CONFIG_DIR = new File("C:\\Extazyy\\configurations");
    private final File autoCfgDir = new File("C:\\Extazyy\\configurations");
    private static final JsonParser jsonParser = new JsonParser();

    public void init(){
        try {
            if (!CONFIG_DIR.exists()) {
                CONFIG_DIR.mkdirs();
            } else if (autoCfgDir.exists()) {
                loadConfiguration("autoconf", true);
            } else {
                //File autoconfiguration not found, created.
                autoCfgDir.createNewFile();
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Ошибка при инициализации конфигураций. Возможное решение: удалите папку Extazyy на диске C.");
        }
    }

    public List<String> getAllConfigurations() {
        List<String> configurations = new ArrayList<>();
        File[] files = CONFIG_DIR.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".cfg")) {
                    String configName = file.getName().substring(0, file.getName().lastIndexOf(".cfg"));
                    configurations.add(configName);
                }
            }
        }
        return configurations;
    }

    public void loadConfiguration(String configuration, boolean start) {
        Config config = findConfig(configuration);
        if (config == null) {
            System.out.println("CFG NOT FOUND");
            return;
        }

        try {
            byte[] byArray = java.nio.file.Files.readAllBytes(config.getFile().toPath());
            String string = new String(byArray, 0, byArray.length, StandardCharsets.UTF_8);

            JsonElement element = jsonParser.parse(string);
            if (element != null) {
                JsonObject object = element.getAsJsonObject();
                config.load(object, configuration, start);
            } else {
                saveConfiguration(configuration);
            }
        } catch (Exception e) {
            System.out.println("Error on uploading configuration");
            //Error on uploading configuration
        }
    }


    public void saveConfiguration(String configuration) {
        Config config = findConfig(configuration);
        if (config == null) {
            config = new Config(configuration);
        }

        try {
            String string = new GsonBuilder().setPrettyPrinting().create().toJson(config.save());
            byte[] byArray = string.getBytes(StandardCharsets.UTF_8);
            java.nio.file.Files.write(config.getFile().toPath(), byArray, new OpenOption[0]);
        } catch (IOException e) {
        }
    }

    public Config findConfig(String configName) {
        if (configName == null) return null;
        if (new File(CONFIG_DIR, configName + ".cfg").exists())
            return new Config(configName);

        return null;
    }

    public void deleteConfig(String configName) {
        if (configName == null)
            return;
        Config config = findConfig(configName);
        if (config != null) {
            File file = config.getFile();
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    //Deleted config
                }
            }
        }
    }
}