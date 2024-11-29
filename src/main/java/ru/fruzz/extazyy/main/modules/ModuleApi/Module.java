package ru.fruzz.extazyy.main.modules.ModuleApi;



import com.google.gson.JsonObject;
import lombok.Getter;
import ru.fruzz.extazyy.misc.event.EventManager;
import ru.fruzz.extazyy.main.modules.tools.Tools;
import ru.fruzz.extazyy.main.modules.tools.Usable;
import ru.fruzz.extazyy.main.modules.tools.imp.*;
import ru.fruzz.extazyy.misc.util.Mine;


public abstract class Module extends Usable implements Mine {
    public float degree = 0;

    private final ModuleAnnotation info = this.getClass().getAnnotation(ModuleAnnotation.class);

    public String name;
    public CategoryUtil category;
    public int bind;
    public float animation;
    @Getter
    public boolean state, util;
    public boolean canrisk;
    public boolean settings;


    public Module() {
        initializeProperties();
    }

    public Module(String name, CategoryUtil category, boolean risk, boolean settings) {
        this.name = name;
        this.category = category;
        state = false;
        canrisk = risk;
        this.settings = settings;
        bind = 0;
    }



    private void initializeProperties() {
        name = info.name();
        category = info.type();
        canrisk = info.risk();
        settings = info.setting();
        state = false;
        bind = info.key();
    }



    public void setState(final boolean enabled) {
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (!enabled)
            this.onDisable();
        else
            this.onEnable();

        state = enabled;
    }

    public void toggle() {
        this.state = !state;
        if (!state) {
            onDisable();
            EventManager.unregister(this);
        } else {
            onEnable();
            EventManager.register(this);
        }
        //Notify
    }

    protected void onDisable() {
    }


    protected void onEnable() {
    }


    public JsonObject save() {
        JsonObject object = new JsonObject();

        object.addProperty("bind", bind);
        object.addProperty("state", state);
        for (Tools tools : getToolsList()) {
            String name = tools.getName();
            switch (tools.getType()) {
                case BOOLEAN_OPTION -> object.addProperty(name, ((BooleanOption) tools).get());
                case NUMBER_SETTING -> object.addProperty(name, ((NumberTools) tools).getValue().floatValue());
                case MODE_SETTING -> object.addProperty(name, ((ModeTools) tools).getIndex());
                case COLOR_SETTING -> object.addProperty(name, ((ColorTools) tools).get());
                case MULTI_BOX_SETTING -> {
                    ((MultiBoxTools) tools).options.forEach(option -> object.addProperty(option.getName(), option.get()));
                }
                case BIND_SETTING -> object.addProperty(name, ((BindTools) tools).getKey());
                case TEXT_SETTING -> object.addProperty(name, ((TextTools) tools).text);
            }
        }
        return object;
    }

    public void load(JsonObject object, boolean start) {
        if (object != null) {
            if (object.has("bind"))
                bind = object.get("bind").getAsInt();
            if (object.has("state")) {
                if(object.get("state").getAsBoolean()) {
                    toggle();
                }
                setState(object.get("state").getAsBoolean());
            }

            for (Tools tools : getToolsList()) {
                String name = tools.getName();
                if (!object.has(name) && !(tools instanceof MultiBoxTools)) {
                    continue;
                }

                switch (tools.getType()) {

                    case BOOLEAN_OPTION -> ((BooleanOption) tools).set(object.get(name).getAsBoolean());
                    case NUMBER_SETTING -> ((NumberTools) tools).setValue((float) object.get(name).getAsDouble());
                    case MODE_SETTING -> ((ModeTools) tools).setIndex(object.get(name).getAsInt());
                    case BIND_SETTING -> ((BindTools) tools).setKey(object.get(name).getAsInt());
                    case COLOR_SETTING -> ((ColorTools) tools).color = object.get(name).getAsInt();
                    case MULTI_BOX_SETTING -> {
                        ((MultiBoxTools) tools).options.forEach(option -> option.set(object.get(option.getName()) != null && object.get(option.getName()).getAsBoolean()));
                    }
                    case TEXT_SETTING -> ((TextTools) tools).text = object.get(name).getAsString();
                }
            }
        }
    }

    public boolean isEnabled() {
        return this.state;
    }

}
