package ru.fruzz.extazyy.misc.event.events.impl;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;
import ru.fruzz.extazyy.misc.event.events.Event;

public class EventFixVelocity extends Event {
    @Getter
    final Vec3 movementInput;
    @Getter
    final float speed;
    final float yaw;
    @Getter
    @Setter
    Vec3 velocity;

    public EventFixVelocity(Vec3 movementInput, float speed, float yaw, Vec3 velocity) {
        this.movementInput = movementInput;
        this.speed = speed;
        this.yaw = yaw;
        this.velocity = velocity;
    }

}
