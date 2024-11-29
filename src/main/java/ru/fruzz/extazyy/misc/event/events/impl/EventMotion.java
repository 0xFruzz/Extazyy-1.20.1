package ru.fruzz.extazyy.misc.event.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.fruzz.extazyy.misc.event.events.Event;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class EventMotion extends Event {
    private boolean onGround;
}