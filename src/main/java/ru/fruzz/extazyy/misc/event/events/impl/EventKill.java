package ru.fruzz.extazyy.misc.event.events.impl;

import lombok.Getter;
import net.minecraft.world.entity.Entity;
import ru.fruzz.extazyy.misc.event.events.Event;

public class EventKill extends Event {

    @Getter
    private Entity killer;
    @Getter
    private Entity killed;

    public EventKill(Entity killer, Entity killed) {
        this.killed = killed;
        this.killer = killer;
    }

}
