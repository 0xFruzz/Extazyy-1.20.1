package ru.fruzz.extazyy.misc.event.events;

public abstract class Event {

    private boolean stopped;

    public void stop() {
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

}
