package ru.fruzz.extazyy.misc.ui.umbrellagui.general;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.misc.util.anim.Animation;
import ru.fruzz.extazyy.misc.util.render.TestRender;
import ru.fruzz.extazyy.misc.util.render.lowrender.RenderMcd;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RenderModuleCategory extends Component {

    public Module module;

    public List<Component> components = new ArrayList<>();

    public RenderModuleCategory(Module module) {
        this.module = module;
    }

    Animation anim = new Animation(Animation.Ease.EASE_OUT_EXPO, 0, 1, 200);

    public static RenderModuleCategory binding;


    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        anim.setTarget(openedsettings ? 1 : 0);
        anim.setSpeed(200);

        components.forEach(c -> {
            c.module = module;
            c.guiparent = guiparent;
        });
        if(this.guiparent.currentModule.equals(module)) {
            openedsettings = true;
        } else {
            openedsettings = false;
        }
        float x2 = this.guiparent.x;
        float y2 = this.guiparent.y;

        TestRender.addWindow(matrixStack, x2 + 25, y2 + 30, x2 + 25 + 80, y2 + 30 + 240, 1);
        RenderMcd.drawBlurredShadow(matrixStack, x2 + 25, y2 + 27f,  80,  1, 6, new Color(19, 21, 25).getRGB());
        if(openedsettings) {
            RenderMcd.drawBlurredShadow(matrixStack, x + 22.8f, y - 3, 70f, 13, 4, new Color(6 /255f, 6/255f, 6/255f, anim.getValue() / 2.4f).getRGB());
            DrawHelper.rectangle(matrixStack, x + 22.8f, y - 3, 70f, 13, 1.5f, new Color(23/255f, 28/255f, 31 /255f, anim.getValue()).getRGB());
            DrawHelper.rectangle(matrixStack, x + 19.5f, y - 0.5f, 1f, 7, 0, new Color(207/255f, 54/255f, 54/255f, anim.getValue()).getRGB());
        }
        FontRenderers.umbrellatext15.drawString(matrixStack, module.name,x + 25, y + 0.5f, openedsettings ? new Color(207, 54, 54, 255).getRGB() : new Color(150, 153, 163).getRGB());

        TestRender.popWindow();





    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        if (DrawHelper.isInRegion(mouseX, mouseY, x + 22.8f, y - 3, 70f, 13) && mouseButton <= 1) {
            this.guiparent.currentModule = module;
        }
        if (!module.equals(this.guiparent.currentModule)) return;

        components.forEach(component -> component.mouseClicked(mouseX, mouseY, mouseButton));

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        components.forEach(component -> component.mouseReleased(mouseX, mouseY, mouseButton));
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        components.forEach(component -> component.keyTyped(keyCode, scanCode, modifiers));
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        components.forEach(component -> component.charTyped(codePoint, modifiers));
    }
}


