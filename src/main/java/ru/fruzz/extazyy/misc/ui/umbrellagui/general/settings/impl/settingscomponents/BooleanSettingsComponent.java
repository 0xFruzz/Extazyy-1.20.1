package ru.fruzz.extazyy.misc.ui.umbrellagui.general.settings.impl.settingscomponents;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.Component;
import ru.fruzz.extazyy.misc.util.anim.AnimMath;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;
import ru.fruzz.extazyy.misc.util.render.lowrender.RenderMcd;

import java.awt.*;

public class BooleanSettingsComponent extends Component {

    public BooleanOption option;

    public BooleanSettingsComponent(BooleanOption component) {
        this.option = component;
    }


    public float animation = 0.0F;

    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {

        y -= 1f;
        //height -= 1f;

        this.animation = AnimMath.fast(this.animation, option.get() ? -1.0F : 0.0F, 15.0F);
        boolean enabled = option.get();
        boolean enabledragged = DrawHelper.isInRegion(mouseX, mouseY, x + 116.5f, y - 4.5f, 290f,8.5f);
        //FontRenderers.generalgui14.drawString(matrixStack, "A", x + 118.5f, y, enabled ? new Color(207, 54, 54).getRGB() : new Color(102, 102, 102, 255).getRGB());

        FontRenderers.umbrellatext16.drawString(matrixStack, option.getName(),x , y - 2.5f, new Color(176, 176, 176,255).getRGB());

        DrawHelper.rectangle(matrixStack, x + 132, y - 2.5f, 16, 9, 4.3f, enabled ? new Color(57, 28, 31).getRGB() : (enabledragged ?  new Color(50, 29, 33).getRGB() :  new Color(26, 28, 33).getRGB()));

        RenderMcd.drawBlurredShadow(matrixStack , x + 132 - this.animation * 7.90f, y - 2.5f, 8.5f, 8.5f, 6, new Color(17, 19, 23).getRGB());
        DrawHelper.rectangle(matrixStack, x + 132 - this.animation * 7.90f, y - 2.5f, 8.5f, 8.5f, 4f, enabled ? new Color(207, 54, 54).getRGB() : ( enabledragged ?  new Color(154, 78, 78).getRGB() :  new Color(102, 102, 102).getRGB()));

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (DrawHelper.isInRegion(mouseX, mouseY, x + 116.5f, y - 4.5f, 290f,8.5f)) {
            option.toggle();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}
