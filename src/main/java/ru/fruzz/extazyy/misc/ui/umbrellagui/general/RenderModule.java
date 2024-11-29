package ru.fruzz.extazyy.misc.ui.umbrellagui.general;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.module.GeneralModule;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.module.SettingModule;
import ru.fruzz.extazyy.misc.util.render.TestRender;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RenderModule extends Component {

    public Module module;
    public List<Component> components = new ArrayList<>();

    public RenderModule(Module module) {
        this.module = module;
        components.add(new GeneralModule());
        components.add(new SettingModule(module, this.guiparent));
    }


    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        if(!this.module.equals(this.guiparent.currentModule)) return;


        components.forEach(c -> {
            c.module = module;
            c.guiparent = guiparent;
        });

        TestRender.addWindow(matrixStack, guiparent.x + 105, guiparent.y + 0.5f, guiparent.x +105 + 315, guiparent.y + 270, 1);

        if(guiparent.currentModule != null) {
            float width = FontRenderers.umbrellatext16.getStringWidth(guiparent.currentModule.category.name() + " / ");
            FontRenderers.umbrellatext16.drawString(matrixStack, guiparent.currentModule.category.name() + " / ", x + 110, y + 16.5f + guiparent.categoryoff, new Color(66, 72, 82).getRGB());
            FontRenderers.umbrellatext16.drawString(matrixStack, guiparent.currentModule.name, x + 110 + width, y + 16.5f + guiparent.categoryoff, new Color(207, 54, 54).getRGB());
        }
        for (Component component : components) {
                component.module = guiparent.currentModule;
                component.setPosition(x, y, width, 30);
                component.drawComponent(matrixStack, mouseX, mouseY);
        }
        TestRender.popWindow();
    }



    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
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


