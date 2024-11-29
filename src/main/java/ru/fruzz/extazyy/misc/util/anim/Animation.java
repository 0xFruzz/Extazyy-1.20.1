package ru.fruzz.extazyy.misc.util.anim;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Animation {
    public static Animation ZERO() {
        return new Animation(Ease.LINEAR, 0, 0, 100);
    }
    private long time;
    private float start, target, speed;
    private Ease type;

    public Animation(Ease type, float start, float target, float speed) {
        this.type = type;
        this.start = start;
        this.target = target;
        this.speed = speed;
        this.time = System.currentTimeMillis();
    }

    public Animation(float start, float target, float speed) {
        this(Ease.LINEAR, start, target, speed);
    }

    public void setType(Ease type) {
        this.type = type;
    }

    public float getValue() {
        return interpolate(start, target, getProgress());
    }

    public void setTarget(float target) {
        if (target != this.target) {
            reset(getValue());
            this.target = target;
        }
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getTarget() {
        return target;
    }

    public void reset(float val) {
        this.start = val;
        time = System.currentTimeMillis();
    }

    public float getProgress() {
        return type.get(max(min((System.currentTimeMillis() - time) / speed, 1), 0));
    }

    private float interpolate(float a, float b, float p) {
        return a + (b - a) * p;
    }

    public interface Ease {
        Ease LINEAR = x -> x;
        Ease EASE_OUT_EXPO = x -> x == 1 ? 1 : (float) (1 - Math.pow(2f, -10 * x));
        float get(float x);
    }
}
