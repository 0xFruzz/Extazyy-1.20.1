package ru.fruzz.extazyy.misc.event.events.impl;

import net.minecraft.world.entity.LivingEntity;
import ru.fruzz.extazyy.misc.event.events.Event;

public class EventDamageEntity extends Event {
    private LivingEntity attacker;
    private LivingEntity target;

    public LivingEntity getAttacker() {
        return attacker;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public EventDamageEntity(LivingEntity attacker, LivingEntity target) {
        this.attacker = attacker;
        this.target = target;
    }
}
