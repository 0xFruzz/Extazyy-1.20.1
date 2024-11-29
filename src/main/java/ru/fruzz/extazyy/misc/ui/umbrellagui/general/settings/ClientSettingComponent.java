package ru.fruzz.extazyy.misc.ui.umbrellagui.general.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.Component;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.settings.impl.CloudSettings;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.settings.impl.GeneralSettings;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.settings.impl.MusicSettings;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.settings.impl.ThemeSettings;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClientSettingComponent extends Component {


    public List<Component> generalWindow = new ArrayList<>();

    public ClientSettingComponent() {
        generalWindow.add(new GeneralSettings());
        generalWindow.add(new CloudSettings());
        generalWindow.add(new MusicSettings());
        generalWindow.add(new ThemeSettings());
    }

    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        width = 167;
        height = 344;

        DrawHelper.rectangle(matrixStack, x - 0.5f, y -0.5f, 166,343, 2, new Color(51, 51, 51).getRGB());
        DrawHelper.rectangle(matrixStack, x, y, 165,342, 2, new Color(16,17,21,255).getRGB());

        DrawHelper.rectangle(matrixStack, x,y + 9, 165, 1f,0, new Color(25, 25, 30).getRGB());


        FontRenderers.glyphter.drawString(matrixStack, "R",x + 2f , y  + 3f, new Color(157, 158,161).getRGB());
        FontRenderers.umbrellatext14.drawString(matrixStack, "Settings",x + 11f , y  + 2f, new Color(157, 158,161).getRGB());
        FontRenderers.music10.drawString(matrixStack, "r",x + 157, y + 4f, new Color(157, 158,161).getRGB());


        float yoffset = 15;
        for (Component component : generalWindow) {
            component.setPosition(x + 5, y + yoffset, width - 12, 10);
            component.drawComponent(matrixStack, mouseX, mouseY);

            yoffset += component.height + 5;
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
