package ru.fruzz.extazyy.misc.ui.umbrellagui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.drag.DragManager;
import ru.fruzz.extazyy.main.drag.Dragging;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.EventManager;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent2D;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent3D;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.RenderModule;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.RenderModuleCategory;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.settings.ClientSettingComponent;
import ru.fruzz.extazyy.misc.util.Mine;
import ru.fruzz.extazyy.misc.util.render.dangertech.blur.DrawShader;
import ru.fruzz.extazyy.misc.util.drag.ScaleMah;
import ru.fruzz.extazyy.misc.util.anim.AnimMath;
import ru.fruzz.extazyy.misc.util.render.lowrender.RenderMcd;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;
import java.util.ArrayList;

/*
    UmbrellaGui Skeed (Dota2)
    Created 16.09.2024
 */

public class UmbrellaGui extends Screen {

    public Dragging dragGui = Extazyy.createDrag(Extazyy.moduleManager.hud, "UmbrellaGui", Mine.mc.getWindow().getGuiScaledWidth() / 2 - 210, Mine.mc.getWindow().getGuiScaledHeight() / 2 - 135);
    public Dragging settings = Extazyy.createDrag(Extazyy.moduleManager.hud, "UmbrellaGuiSettings", Mine.mc.getWindow().getGuiScaledWidth() / 2 - 380, Mine.mc.getWindow().getGuiScaledHeight() / 2 - 207);


    public float x, y;
    public PoseStack poseStack;
    public Matrix4f matrix4f;
    public CategoryUtil current = CategoryUtil.Combat;
    int mouseX;
    int mouseY;
    public ArrayList<RenderModuleCategory> modulesCategory = new ArrayList<>();
    public ArrayList<RenderModule> modules = new ArrayList<>();
    private ClientSettingComponent setting;

    public float scroll2 = 0;
    public float animateScroll2 = 0;
    public float categoryoff = 0;

    public float scroll = -90;
    public float animateScroll = 0;

    private boolean isSettingOpened = false;


    public Module currentModule = Extazyy.moduleManager.aura;
    public UmbrellaGui() {
        super(Component.literal("UmbrellaGui"));
        for (Module module : Extazyy.moduleManager.getFunctions()) {
            modules.add(new RenderModule(module));
            modulesCategory.add(new RenderModuleCategory(module));
        }
    }



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialticks) {
        x = dragGui.getX();
        y = dragGui.getY();

        init(guiGraphics, mouseX, mouseY);
        mainWindow();
        drawModulesCategory();
        drawModules();
        renderSettings(guiGraphics, mouseX, mouseY);
        DragManager.draggables.values().forEach(dragging -> {
            dragging.onDraw(mouseX, mouseY, Mine.mc.getWindow());
        });
        dragGui.setHeight(270);
        dragGui.setWidth(420);
    }

    public void init(GuiGraphics stack, int mouseX, int mouseY) {
        EventManager.unregister(this);
        poseStack = stack.pose();
        matrix4f = stack.pose().last().pose();
        this.mouseX = mouseX;
        this.mouseY = mouseY;


    }



    public void renderSettings(GuiGraphics stack, int mouseX, int mouseY) {
        float settingsX = settings.getX();
        float settingsY = settings.getY();

        setting = new ClientSettingComponent();


        setting.guiparent = this;
        setting.setPosition(settingsX, settingsY,10,10);
        setting.drawComponent(stack.pose(), mouseX, mouseY);


        settings.setWidth(setting.width);
        settings.setHeight(setting.height);
    }

    public void drawModulesCategory() {
        float offset = 0 + animateScroll;
        float size1 = 0;
        float height = 250;
        FontRenderers.umbrellatext16.drawString(poseStack, current.description, x + 33, y + 15.5f, new Color(176, 176, 176).getRGB());
        animateScroll = AnimMath.lerp(animateScroll, scroll, 10);

        for (RenderModuleCategory component : modulesCategory) {
            if (component.module.category != current) continue;
            component.guiparent = this;
            component.setPosition(x + 8, y + 35 + offset, 100, 15);
            component.drawComponent(poseStack, mouseX, mouseY);

            offset += component.height + 2;
            size1 += component.height + 2;
        }

        if (size1 < height) {
            scroll = 0;
        } else {
            scroll = Mth.clamp(scroll, -(size1 - height + 20), 0);
        }
    }


    public void drawModules() {
        float offset = -1 + animateScroll2;
        float size1 = 870;
        float height = 850;
        FontRenderers.umbrellatext16.drawString(poseStack, current.description, x + 33, y + 15.5f, new Color(176, 176, 176).getRGB());
        animateScroll2 = AnimMath.lerp(animateScroll2, scroll2, 10);

        for (RenderModule component : modules) {
            if (component.module.category != current) continue;
            component.guiparent = this;
            component.setPosition(x, y + offset, 100, 15);
            component.drawComponent(poseStack, mouseX, mouseY);


        }
        if (size1 < height) {
            scroll2 = 0;
        } else {
            scroll2 = Mth.clamp(scroll2, -(size1 - height + 20), 0);
        }
        categoryoff = offset;
    }

    private void mainWindow() {
        DrawHelper.rectangle(poseStack, x - 0.5f, y - 0.5f, 420 + 1.1f, 270 + 1.1f, 2f, new Color(51, 51, 51).getRGB());
        DrawShader.drawRoundBlur(poseStack, x, y, 420, 270, 4f, new Color(12, 11, 10, 255).getRGB(), 10, 0.25f);
        DrawHelper.drawSemiRoundRect(poseStack, x + 104.8f, y, 315, 270, 0, 0, 2, 2, new Color(16, 17, 21, 255).getRGB());
        DrawHelper.drawSemiRoundRect(poseStack, x, y, 105, 270, 2, 2, 0, 0, new Color(19, 21, 25, 255).getRGB());
        DrawHelper.rectangle(poseStack, x + 104.8f, y, 0.3f, 270, 0, new Color(26, 26, 31, 255).getRGB());
        DrawHelper.rectangle(poseStack, x + 25f, y, 0.5f, 270, 0, new Color(26, 26, 31, 255).getRGB());
        //new Color(207, 54, 54, 255).getRGB()
        FontRenderers.umbrellagui22.drawString(poseStack, String.valueOf('J'), x + 7f, y + 10, new Color(207, 54, 54, 255).getRGB());

        renderCategories();

        FontRenderers.umbrellagui15.drawString(poseStack, String.valueOf('A'), x + 9f, y + 251, isSettingOpened ? new Color(207, 54, 54, 255).getRGB() : new Color(176, 176, 176).getRGB());


    }

    private void renderCategories() {
        float height = 23;
        for (CategoryUtil t : CategoryUtil.values()) {
            String iconChar = t.image;
            t.anim.setTarget(t == current ? 1 : 0);
            t.anim.setSpeed(200);
            if (t == current) {
                RenderMcd.drawBlurredShadow(poseStack, x + 3.5f, y + 27.5f + t.ordinal() * height, 19, 19, 6, new Color(6 /255f, 6 / 255f, 6 /255f,  t.anim.getValue() / 2).getRGB());
                DrawHelper.rectangle(poseStack, x + 3f, y + 27.5f + t.ordinal() * height, 19.5f, 19, 2.5f, new Color(23 / 255f, 28 / 255f, 31 / 255f, t.anim.getValue()).getRGB());
                DrawHelper.rectangle(poseStack, x + 1.25f, y + 28f + t.ordinal() * height + 5, 1f, 8, 0, new Color(207 / 255f, 54 / 255f, 54 / 255f, t.anim.getValue()).getRGB());
            }
            FontRenderers.umbrellagui17.drawString(poseStack, iconChar, x + t.offset, y + 35f + t.ordinal() * height, t == current ? new Color(207, 54, 54).getRGB() : new Color(176, 176, 176).getRGB());
        }
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if(DrawHelper.isInRegion(mouseX, mouseY, x + 25, y + 30, 80, 240)) {
            scroll += delta * 10;
        }
        if(DrawHelper.isInRegion(mouseX, mouseY, x + 105, y, 315, 270)) {
            scroll2 += delta * 10;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vec2 fixed = ScaleMah.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.x;
        mouseY = fixed.y;
        if (button == 2) {
            DragManager.draggables.values().forEach(dragging -> {
                dragging.onReleaseGui(button);
            });
        }
        for (RenderModule m : modules) {

            if (DrawHelper.isInRegion(mouseX, mouseY, x + 110, y + 30, 305, 240)) {
                if (m.module != currentModule) continue;
                m.mouseReleased((int) mouseX, (int) mouseY, button);
            }
        }
        setting.mouseReleased((int) mouseX, (int) mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2 fixed = ScaleMah.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.x;
        mouseY = fixed.y;
        double finalMouseX = mouseX;
        double finalMouseY = mouseY;
        DragManager.draggables.values().forEach(dragging -> {
           dragging.onClickGui(finalMouseX, finalMouseY, button);
        });

        for (CategoryUtil t : CategoryUtil.values()) {
            if (DrawHelper.isInRegion(mouseX, mouseY, x + 3, y + 27.5f + t.ordinal() * 23, 19.5f, 19)) {
                if (current == t) continue;
                current = t;
            }
        }

        for (RenderModule m : modules) {
                if (DrawHelper.isInRegion(mouseX, mouseY, x + 110, y + 30 + categoryoff, 305, 240)) {
                    if (m.module != currentModule) continue;
                    m.mouseClicked((int) mouseX, (int) mouseY, button);
                }
        }
        for (RenderModuleCategory m : modulesCategory) {
            if (DrawHelper.isInRegion(mouseX, mouseY, x + 25, y + 30, 80, 240)) {

                if (m.module.category != current) continue;
                m.mouseClicked((int) mouseX, (int) mouseY, button);
            }
        }
        setting.mouseClicked((int) mouseX, (int) mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }




    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (RenderModule m : modules) {
            if (m.module.category != current) continue;
            m.keyTyped(keyCode, scanCode, modifiers);
        }
        setting.keyTyped(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    //Чтоб у тя еблана майнкрафт не умирал когда в одиночке в гуи заходишь
    @Override
    public boolean isPauseScreen() {
        return false;
    }



}
