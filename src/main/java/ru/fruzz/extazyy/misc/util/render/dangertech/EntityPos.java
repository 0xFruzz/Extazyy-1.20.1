package ru.fruzz.extazyy.misc.util.render.dangertech;

import net.minecraft.world.entity.Entity;
import org.joml.Vector3d;

public class EntityPos extends Vector3d {
    protected EntityPos(Entity entity, float height, float pt) {
        super(interpolate(entity.getX(), entity.xOld, pt), interpolate(entity.getY() , entity.yOld, pt)+ height, interpolate(entity.getZ(), entity.zOld, pt));
    }


    public static double interpolate(double current, double old, double scale) {
        return old + (current - old) * scale;
    }

    public static Vector3d get(Entity entity, float height, float pt) {
        return new EntityPos(entity, height, pt);
    }

    public static Vector3d get(Entity entity, float pt) {
        return get(entity, 0, pt);
    }


}
