package ru.fruzz.extazyy.misc.event.events.impl;

import lombok.Getter;
import net.minecraft.world.phys.Vec3;
import ru.fruzz.extazyy.misc.event.events.Event;

@Getter
public class EventPlayerTravel extends Event {
    private final Vec3 mVec;
    private final boolean pre;

    public EventPlayerTravel(Vec3 mVec,boolean pre) {
        this.mVec = mVec;
        this.pre = pre;
    }

}
