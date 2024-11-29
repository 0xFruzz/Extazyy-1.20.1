package ru.fruzz.extazyy.misc.util.anim;

import net.minecraft.util.Mth;
import ru.fruzz.extazyy.misc.util.Mine;


public class Anim implements Mine {
    public static double deltaTime() {
        return mc.getFps() > 0 ? (1.0000 / mc.getFps()) : 1;
    }

    public static float fast(float end, float start, float multiple) {
        return (1 - Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1)) * end + Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1) * start;
    }

    public static float lerp(float end, float start, float multiple) {
        return (float) (end + (start - end) * Mth.clamp(Anim.deltaTime() * multiple, 0, 1));
    }

    public static double lerp(double end, double start, double multiple) {
        return (end + (start - end) * Mth.clamp(Anim.deltaTime() * multiple, 0, 1));
    }

    

    public static double bezier(double x, double controlPointX1, double controlPointX2, double endX, double t) {
        double oneMinusT = 1.0 - t;
        double tSquared = t * t;
        double oneMinusTSquared = oneMinusT * oneMinusT;
        double cubic = oneMinusT * oneMinusTSquared;
        double cubicTimesThree = 3.0 * cubic;
        double quadraticTimesThree = 3.0 * oneMinusT * tSquared;
        double linearTimesThree = t * tSquared;

        return cubic * x + cubicTimesThree * controlPointX1 + quadraticTimesThree * controlPointX2 + linearTimesThree * endX;
    }
    public static float easeIn(float end, float start, float multiple) {
        float t = Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1);
        return (end - start) * t * t * t + start;
    }

    public static float easeOut(float end, float start, float multiple) {
        float t = Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1);
        t = 1 - (--t) * t * t;
        return (end - start) * t + start;
    }

    public static float easeInOut(float end, float start, float multiple) {
        float t = Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1);
        t = t < 0.5 ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
        return (end - start) * t + start;
    }

    public static float elasticIn(float end, float start, float multiple) {
        float t = Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1);
        float p = 0.3f;
        return (end - start) * (float) Math.pow(2, 10 * (t - 1)) * (float) Math.sin((t - p / 4) * (2 * Math.PI) / p) + start;
    }

    public static float elasticOut(float end, float start, float multiple) {
        float t = Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1);
        float p = 0.3f;
        return (float) ((end - start) * ((float) Math.pow(2, -10 * t) * Math.sin((t - p / 4) * (2 * Math.PI) / p) + 1) + start);
    }

    public static float bounceIn(float end, float start, float multiple) {
        float t = Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1);
        return (end - start) - bounceOut(0, end - start, 1 - t) + start;
    }

    public static float bounceOut(float start, float end, float t) {
        if (t < (1 / 2.75)) {
            return (end - start) * (7.5625f * t * t) + start;
        } else if (t < (2 / 2.75)) {
            t -= (1.5 / 2.75);
            return (end - start) * (7.5625f * t * t + 0.75f) + start;
        } else if (t < (2.5 / 2.75)) {
            t -= (2.25 / 2.75);
            return (end - start) * (7.5625f * t * t + 0.9375f) + start;
        } else {
            t -= (2.625 / 2.75);
            return (end - start) * (7.5625f * t * t + 0.984375f) + start;
        }
    }    public static float smoothStep(float end, float start, float multiple) {
        float t = Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1);
        return (end - start) * (t * t * (3 - 2 * t)) + start;
    }

    public static float smootherStep(float end, float start, float multiple) {
        float t = Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1);
        return (end - start) * (t * t * t * (t * (t * 6 - 15) + 10)) + start;
    }

    public static float accelerateDecelerate(float end, float start, float multiple) {
        float t = Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1);
        return (float) ((end - start) * (Math.cos((t + 1) * Math.PI) / 2.0) + start);
    }

    public static float decelerateAccelerate(float end, float start, float multiple) {
        float t = Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1);
        return (float) ((end - start) * (Math.cos(t * Math.PI) / 2.0 + 0.5) + start);
    }

    public static float sineWave(float end, float start, float multiple) {
        float t = Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1);
        return (end - start) * (float) Math.sin(t * Math.PI) + start;
    }

    public static float cubicBezier(float end, float start, float controlPoint1, float controlPoint2, float multiple) {
        float t = Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1);
        float oneMinusT = 1 - t;
        return (end - start) * (3 * oneMinusT * oneMinusT * t * controlPoint1 +
                3 * oneMinusT * t * t * controlPoint2 + t * t * t) + start;
    }

    public static float quadraticBezier(float end, float start, float controlPoint, float multiple) {
        float t = Mth.clamp((float) (Anim.deltaTime() * multiple), 0, 1);
        float oneMinusT = 1 - t;
        return (end - start) * (2 * oneMinusT * t * controlPoint + t * t) + start;
    }
}

