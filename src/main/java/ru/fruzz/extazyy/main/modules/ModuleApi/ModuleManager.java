package ru.fruzz.extazyy.main.modules.ModuleApi;


import lombok.Getter;
import ru.fruzz.extazyy.main.modules.impl.combat.AntiBot;
import ru.fruzz.extazyy.main.modules.impl.combat.Aura;
import ru.fruzz.extazyy.main.modules.impl.combat.AutoTotem;
import ru.fruzz.extazyy.main.modules.impl.combat.HitBox;
import ru.fruzz.extazyy.main.modules.impl.misc.*;
import ru.fruzz.extazyy.main.modules.impl.movement.*;
import ru.fruzz.extazyy.main.modules.impl.player.ChestStealer;
import ru.fruzz.extazyy.main.modules.impl.player.NoDelay;
import ru.fruzz.extazyy.main.modules.impl.render.*;
import ru.fruzz.extazyy.main.modules.impl.unused.TestModule;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class ModuleManager {

    private static final List<Module> modules = new CopyOnWriteArrayList<>();

    public TestModule testModule;
    public NoRender noRender;
    public HitBox hitBox;
    public Resolution resolution;
    public CustomWorld customWorld;
    public JumpCircle jumper;
    public Aura aura;
    public Hud hud;
    public SRPSpoofer srpSpoofer;
    public BabyMode babyMode;
    public CameraOffset cameraOffset;
    public NameTags nameTags;
    public AntiBot antiBot;
    public ButtonHighJump zigFly;
    public ElytraBoost elytraBoost;


    public ModuleManager() {
        modules.add(new NewHud());
        modules.add(new AntiBot());
        modules.add(new FunTest());
        modules.add(new ShulkerBoost());
        modules.add(new HitParticlesV2());
        modules.add(new StrafeSpeed());
        modules.add(new NoDelay());
        modules.add(new WorldParticles());
        modules.add(new BedrockOverlay());
        modules.add(new StreamerMode());
        modules.add(new VulcanESP());
        modules.add(new Xray());
        modules.add(new ESP());
        modules.add(new FunTimeClans());
        modules.add(new AutoTotem());
        modules.add(new Triangles());
        modules.add(new AutoLeave());
        modules.add(new NoWeb());
        modules.add(new ChestStealer());
        modules.addAll(Arrays.asList(
                elytraBoost = new ElytraBoost(),
                nameTags = new NameTags(),
                zigFly = new ButtonHighJump(),
                babyMode = new BabyMode(),
                cameraOffset = new CameraOffset(),
                srpSpoofer = new SRPSpoofer(),
                customWorld = new CustomWorld(),
                jumper = new JumpCircle(),
                aura = new Aura(),
                testModule = new TestModule(),
                noRender = new NoRender(),
                hitBox = new HitBox(),
                hud = new Hud(),
                resolution = new Resolution()
        ));



    }

    public List<Module> getFunctions() {
        return modules;
    }


    public static Module get(String name) {
        for (Module module : modules) {
            if (module != null && module.name.equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public static Module getModule(Class<? extends Module> zClass) {
        for (Module module : modules) {
            if(module.getClass() == zClass) {
                return module;
            }
        }
        return null;
    }

}
