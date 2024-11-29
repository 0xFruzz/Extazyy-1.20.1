package ru.fruzz.extazyy.misc.util.drag;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;

public class ScaleMah {

    public static Vec2 getMouse(int mouseX, int mouseY){
        return new Vec2((int) (mouseX * Minecraft.getInstance().getWindow().getGuiScale() / 2), (int) (mouseY * Minecraft.getInstance().getWindow().getGuiScale() / 2));
    }


    public static Vec2 scaleVec2i(Vec2 vector, float scaleFactor) {
        return new Vec2((int) (vector.x * scaleFactor), (int) (vector.y * scaleFactor));
    }

    public static float calculateDistance(Vec2 point1, Vec2 point2) {
        return (float) Math.sqrt(Math.pow(point2.x - point1.x, 2) + Math.pow(point2.y - point1.y, 2));
    }

    public static Vec2 addVectors(Vec2 vector1, Vec2 vector2) {
        return new Vec2(vector1.x + vector2.x, vector1.y + vector2.y);
    }
}