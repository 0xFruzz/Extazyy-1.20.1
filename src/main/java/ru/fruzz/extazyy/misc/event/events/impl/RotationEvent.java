package ru.fruzz.extazyy.misc.event.events.impl;

import ru.fruzz.extazyy.misc.event.events.Event;

public class RotationEvent extends Event {
    private float xRot, yRot;
    private final float staticXrot, staticYrot;
    public RotationEvent(float xRot, float yRot) {
        this.staticXrot = xRot;
        this.staticYrot = yRot;
        this.xRot = xRot;
        this.yRot = yRot;
    }


    public float getXRot() {
        return xRot;
    }

    public float getYRot() {
        return yRot;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
    }

    public void setYRot(float yRot) {
        this.yRot = yRot;
    }


    public float getStaticXrot() {
        return staticXrot;
    }

    public float getStaticYrot() {
        return staticYrot;
    }

    public boolean isEquals() {
        return xRot == staticXrot && yRot == staticYrot;
    }
}
