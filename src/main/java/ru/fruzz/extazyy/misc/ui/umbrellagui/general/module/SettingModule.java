package ru.fruzz.extazyy.misc.ui.umbrellagui.general.module;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.tools.Tools;
import ru.fruzz.extazyy.main.modules.tools.imp.*;
import ru.fruzz.extazyy.misc.ui.umbrellagui.UmbrellaGui;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.components.*;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.Component;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SettingModule extends Component {

    public Module module;
    public UmbrellaGui parentGui;
    public List<Component> components = new ArrayList<>();

    public SettingModule(Module module, UmbrellaGui parentGui) {
        this.module = module;
        this.parentGui = parentGui;
        List<Tools> toolsList = module.getToolsList();

        toolsList.sort(Comparator.comparingInt(tool -> {
            switch (tool.getType()) {
                case NUMBER_SETTING: return 1;
                case MODE_SETTING: return 2;
                case MULTI_BOX_SETTING: return 3;
                case BIND_SETTING: return 4;
                case BOOLEAN_OPTION: return 5;
                default: return 6;
            }
        }));

        for (int i = 0; i < toolsList.size(); i++) {
            Tools tools = toolsList.get(i);

            switch (tools.getType()) {
                case BOOLEAN_OPTION -> components.add(new BooleanComponent((BooleanOption) tools));
                case BIND_SETTING -> components.add(new BindComponent((BindTools) tools));
                case NUMBER_SETTING -> components.add(new SliderComponent((NumberTools) tools));
                case MODE_SETTING -> components.add(new ModeComponent((ModeTools) tools));
                case MULTI_BOX_SETTING -> components.add(new MultiBoxComponent((MultiBoxTools) tools));
            }

             if (i < toolsList.size() - 1) {
                 components.add(new LineComponent());
             }
        }
    }

    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        if(components.isEmpty()) return;

        float totalHeight = -4;
        for (Component component : components) {
            if(component instanceof BooleanComponent) {
                totalHeight += component.height - 1;
                continue;
            }
            totalHeight += component.height;
        }

        height += totalHeight;

        DrawHelper.rectangle(matrixStack, x + 110, y + 93, 304, height, 2f, new Color(19, 21, 25).getRGB());
        FontRenderers.umbrellatext15.drawString(matrixStack, "Settings",x + 115.5f, y + 100, new Color(115, 117, 130,255).getRGB());

        float offsetY = 0;
        float perf = 119;
        for (Component component : components) {
            component.module = guiparent.currentModule;
            component.setPosition(x, y + offsetY + perf, width, 16);
            component.drawComponent(matrixStack, mouseX, mouseY);

            offsetY += component.height;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
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
