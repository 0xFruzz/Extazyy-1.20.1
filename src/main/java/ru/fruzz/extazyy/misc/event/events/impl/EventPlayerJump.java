package ru.fruzz.extazyy.misc.event.events.impl;

import lombok.Getter;
import ru.fruzz.extazyy.misc.event.events.Event;

@Getter
public class EventPlayerJump extends Event {
    private final boolean pre;

    public EventPlayerJump(boolean pre) {
        this.pre = pre;
    }

}