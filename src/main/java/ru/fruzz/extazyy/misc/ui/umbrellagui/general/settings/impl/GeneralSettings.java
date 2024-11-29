package ru.fruzz.extazyy.misc.ui.umbrellagui.general.settings.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.main.modules.tools.imp.BindTools;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.Component;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.settings.impl.settingscomponents.BindSettingsComponent;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.settings.impl.settingscomponents.BooleanSettingsComponent;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GeneralSettings extends Component {

    BindTools menuBind = new BindTools("Menu Bind", 0);

    BindTools reloadBind = new BindTools("Reload Bind", 0);

    BooleanOption logWindow = new BooleanOption("Log Window", false);

    BooleanOption adminMode = new BooleanOption("Admin Mode", false);

    BooleanOption potateMode = new BooleanOption("Potate Mode", false);

    BooleanOption roflsMode = new BooleanOption("Rofls Mode", false);

    List<Component> generalWindow = new ArrayList<>();

    public GeneralSettings() {
        generalWindow.add(new BindSettingsComponent(menuBind));
        generalWindow.add(new BindSettingsComponent(reloadBind));
        generalWindow.add(new BooleanSettingsComponent(logWindow));
        generalWindow.add(new BooleanSettingsComponent(adminMode));
        generalWindow.add(new BooleanSettingsComponent(potateMode));
        generalWindow.add(new BooleanSettingsComponent(roflsMode));

    }

    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        height += 80;
        DrawHelper.rectangle(matrixStack, x,y, width, height, 5, new Color(19, 21, 25).getRGB());
        FontRenderers.umbrellatext17.drawString(matrixStack,"General Settings", x + 4,y +4, new Color(88, 90, 100).getRGB());
        float yoffset = 18;
        for (Component component : generalWindow) {
            component.setPosition(x + 4, y + yoffset, width , 10);
            component.drawComponent(matrixStack, mouseX, mouseY);

            yoffset += component.height + 2.6f;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (Component component : generalWindow) {
            component.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (Component component : generalWindow) {
            component.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        for (Component component : generalWindow) {
            component.keyTyped(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}
