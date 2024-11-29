package ru.fruzz.extazyy.main.modules.impl.combat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;

@ModuleAnnotation(name = "AntiBot", type = CategoryUtil.Combat)
public class AntiBot extends Module {

    public boolean isBot(LivingEntity player) {
        return false;
    }
}
