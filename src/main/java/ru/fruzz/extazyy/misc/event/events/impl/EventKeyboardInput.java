package ru.fruzz.extazyy.misc.event.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.fruzz.extazyy.misc.event.events.Event;
@Getter
@Setter
@AllArgsConstructor
public class EventKeyboardInput extends Event {
    private float forward, strafe;
    private boolean jump, sneak;
    private double sneakSlowDownMultiplier;
}
