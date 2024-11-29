package ru.fruzz.extazyy.misc.util.render.dangertech;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class Vec2Vector extends Vector3d {

    protected Vec2Vector(Vec3 vec3) {
        super(vec3.x, vec3.y, vec3.z);
    }


    public static Vector3d convert(Vec3 vec3) {
        return new Vec2Vector(vec3);
    }
}
