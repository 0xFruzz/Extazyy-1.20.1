package ru.fruzz.extazyy.main.drag;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.platform.Window;
import lombok.Getter;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import net.minecraft.world.phys.Vec2;

import ru.fruzz.extazyy.misc.util.drag.ScaleMah;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

public class Dragging {
    @Expose
    @SerializedName("x")
    private float xPos;
    @Expose
    @SerializedName("y")
    private float yPos;

    public float initialXVal;
    public float initialYVal;

    private float startX, startY;
    private boolean dragging;

    private float width, height;

    @Expose
    @SerializedName("name")
    private String name;
    @Getter
    private final Module module;


    public Dragging(Module module, String name, float initialXVal, float initialYVal) {
        this.module = module;
        this.name = name;
        this.xPos = initialXVal;
        this.yPos = initialYVal;
        this.initialXVal = initialXVal;
        this.initialYVal = initialYVal;
    }

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getX() {
        return xPos;
    }

    public void setX(float x) {
        this.xPos = x;
    }

    public float getY() {
        return yPos;
    }

    public void setY(float y) {
        this.yPos = y;
    }

    public final void onDraw(int mouseX, int mouseY, Window res) {
        Vec2 fixed = ScaleMah.getMouse(mouseX,mouseY);
        mouseX = (int) fixed.x;
        mouseY = (int) fixed.y;

        if (dragging) {
            xPos = (mouseX - startX);
            yPos = (mouseY - startY);
            if (xPos + width > res.getGuiScaledWidth()) {
                xPos = res.getGuiScaledWidth() - width;
            }
            if (yPos + height > res.getGuiScaledHeight()) {
                yPos = res.getGuiScaledHeight() - height;
            }
            if (xPos < 0) {
                xPos = 0;
            }
            if (yPos < 0) {
                yPos = 0;
            }
        }
    }

    public final void onClick(double mouseX, double mouseY, int button) {
        Vec2 fixed = ScaleMah.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.x;
        mouseY = fixed.y;
        if (button == 0 && DrawHelper.isInRegion(mouseX, mouseY, xPos, yPos, width, height)) {
            dragging = true;
            startX = (int) (mouseX - xPos);
            startY = (int) (mouseY - yPos);
        }
    }

    public final void onClickGui(double mouseX, double mouseY, int button) {
        Vec2 fixed = ScaleMah.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.x;
        mouseY = fixed.y;
        if (button == 2 && DrawHelper.isInRegion(mouseX, mouseY, xPos, yPos, width, height)) {

            dragging = true;
            startX = (int) (mouseX - xPos);
            startY = (int) (mouseY - yPos);
        }
    }

    public final void onRelease(int button) {
        if (button == 0) dragging = false;
    }
    public final void onReleaseGui(int button) {
        if (button == 2) dragging = false;
    }


}
