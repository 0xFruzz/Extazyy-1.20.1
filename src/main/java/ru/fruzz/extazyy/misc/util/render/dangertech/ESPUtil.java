package ru.fruzz.extazyy.misc.util.render.dangertech;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.optifine.Config;
import net.optifine.util.BufferUtil;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import ru.fruzz.extazyy.misc.util.Mine;

import java.lang.Math;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class ESPUtil implements Mine {
    private static Matrix4f projectionMatrix = new Matrix4f();
    private static Matrix4f viewMatrix = new Matrix4f();
    private static Matrix4f projectionViewMatrix = new Matrix4f();

    public static void setProjectionViewMatrix(Matrix4f projection, Matrix4f view) {
        projectionMatrix.set(projection);
        viewMatrix.set(view);

        projectionMatrix.mul(view, projectionViewMatrix);
    }

    public static Vector3d toScreen(double x, double y, double z) {
        final float NEAR_PLANE = 0.05f;
        final double screenWidth = mc.getWindow().getGuiScaledWidth();
        final double screenHeight = mc.getWindow().getGuiScaledHeight();

        Vector3d camera = Vec2Vector.convert(mc.getEntityRenderDispatcher().camera.getPosition());
        Vector3d dir = new Vector3d(camera).sub(x, y, z);

        Vector4f pos = new Vector4f((float) dir.x, (float) dir.y, (float) dir.z, 1.f);
        pos.mul(projectionViewMatrix);

        float w = pos.w;
        if (w < NEAR_PLANE && w != 0) {
            pos.div(w);
        } else {
            float scale = (float) Math.max(screenWidth, screenHeight);
            pos.set(pos.x * -1 * scale, pos.y * -1 * scale, pos.z, pos.w);
        }

        double hw = screenWidth / 2.d;
        double hh = screenHeight / 2.d;
        double pointX = (hw * pos.x) + (pos.x + hw);
        double pointY = -(hh * pos.y) + (pos.y + hh);

        return new Vector3d(pointX, pointY,
                (pointX >= 0
                        && pointX < screenWidth
                        && pointY >= 0
                        && pointY < screenHeight ? 1d : 0d));
    }


    public static Vector4d Vec3toVec4d(Entity entity) {
        AABB aabb = entity.getBoundingBox();
        Vector3d projection = Vec2Vector.convert(mc.getEntityRenderDispatcher().camera.getPosition());
        Vector4d position = null;

        for (int i = 0; i < 8; i++) {
            Vector3d vector = new Vector3d(
                    i % 2 == 0 ? aabb.minX : aabb.maxX,
                    (i / 2) % 2 == 0 ? aabb.minY : aabb.maxY,
                    (i / 4) % 2 == 0 ? aabb.minZ : aabb.maxZ);

            vector = project2D((float) (vector.x - projection.x), (float) (vector.y - projection.y), (float) (vector.z - projection.z));

            if (vector != null && vector.z >= 0.0 && vector.z < 1.0) {
                if (position == null) {
                    position = new Vector4d(vector.x, vector.y, vector.z, 1.0f);
                } else {
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                    position.w = Math.max(vector.y, position.w);
                }
            }
        }

        return position;
    }

    private static final IntBuffer VIEWPORT_BUFFER = Config.createDirectIntBuffer(16);
    private static final FloatBuffer MODELVIEW_BUFFER = BufferUtil.createDirectFloatBuffer(16);
    private static final FloatBuffer PROJECTION_BUFFER = BufferUtil.createDirectFloatBuffer(16);
    private static final FloatBuffer VECTOR_BUFFER = BufferUtil.createDirectFloatBuffer(4);


    private static Vector3d project2D(float x, float y, float z) {
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_BUFFER);
        GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, PROJECTION_BUFFER);
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

        return new Vector3d(VECTOR_BUFFER.get(0) / 2, (mc.getWindow().getHeight() - VECTOR_BUFFER.get(1)) / 2, VECTOR_BUFFER.get(2));

    }

    public static Vector3d toScreen(Vector3d vec) {
        return toScreen(vec.x, vec.y, vec.z);
    }

    public static Vector3d toScreen(Vector3i vec) {
        return toScreen(vec.x, vec.y, vec.z);
    }

    public static Vector3d multiplyBy(Vector3d vec1, Vector3d vec2) {
        return new Vector3d(vec1).mul(vec2);
    }

    public static Vector3d copy(Vector3d toCopy) {
        return new Vector3d(toCopy);
    }

    public static double getCrosshairDistance(Vector3d eyes, Vector3d directionVec, Vector3d pos) {
        return new Vector3d(pos).sub(eyes).normalize().sub(directionVec).length();
    }

    public static Vector3d toFPIVector(Vector3i vec) {
        return new Vector3d(vec.x, vec.y, vec.z);
    }
}
