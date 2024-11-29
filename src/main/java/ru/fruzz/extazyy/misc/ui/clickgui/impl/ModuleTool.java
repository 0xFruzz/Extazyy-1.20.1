package ru.fruzz.extazyy.misc.ui.clickgui.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.tools.Tools;
import ru.fruzz.extazyy.main.modules.tools.imp.*;
import ru.fruzz.extazyy.misc.ui.clickgui.tools.*;
import ru.fruzz.extazyy.misc.ui.clickgui.tools.NullTools;
import ru.fruzz.extazyy.misc.ui.themeui.impl.Tool;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.misc.util.ClientUtil;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;
import ru.fruzz.extazyy.misc.util.anim.AnimMath;
import ru.fruzz.extazyy.misc.util.render.lowrender.RenderMcd;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleTool extends Tool {

    public Module module;

    public List<Tool> components = new ArrayList<>();

    public ModuleTool(Module module) {
        this.module = module;
        for (Tools tools : module.getToolsList()) {
            switch (tools.getType()) {
                case BOOLEAN_OPTION -> components.add(new BooleanTool((BooleanOption) tools));
                case NUMBER_SETTING -> components.add(new SliderTool((NumberTools) tools));
                case MODE_SETTING -> components.add(new ModeTool((ModeTools) tools));
                case COLOR_SETTING -> components.add(new ColorTool((ColorTools) tools));
                case MULTI_BOX_SETTING -> components.add(new MultiBoxTool((MultiBoxTools) tools));
                case BIND_SETTING -> components.add(new BindTool((BindTools) tools));
                case TEXT_SETTING -> components.add(new TextTool((TextTools) tools));
                case NULL_OPTION -> components.add(new NullTools((NULka) tools));
            }
        }
    }
    public float animation = 0.0F;

    public float animationToggle;
    public static ModuleTool binding;


    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        float totalHeight = 1;
        for (Tool component : components) {
            if (component.s != null && component.s.visible()) {
                totalHeight += component.height;
            }
        }

        components.forEach(c -> {
            c.module = module;
            c.parent = parent;
        });
        String key = ClientUtil.getKey(module.bind);
        animationToggle = AnimMath.lerp(animationToggle, module.state ? 1 : 0, 10);

        DrawHelper.rectangle(matrixStack,x, y, width, height + totalHeight, 3.5f, new Color(23, 23, 23, 255).getRGB());
        DrawHelper.rectangle(matrixStack,x + 5, y + 20, width - 10, 0.8f, 0, new Color(59, 58, 58, 5).getRGB());

        //DrawHelper.drawSemiRoundRect(matrixStack,x, y, width, height -1, 0,3.5f,0,3.5f, new Color(65, 65, 65, 255).getRGB());

        FontRenderers.msSemi16.drawString(matrixStack, module.name, x + 6.0f, y + 7f, new Color(255, 255, 255).getRGB());

        if (binding == this && key != null) {
            FontRenderers.msSemi16.drawCenteredString(matrixStack, "Binding...", x + width - 66 -  FontRenderers.msSemi16.getStringWidth(key) + 5 + (10 +  FontRenderers.msSemi16.getStringWidth(key)) / 2, y + 7, Extazyy.themesUtil.getCurrentStyle().getColor(90));
        }
        if (binding == this && key == null) {
            FontRenderers.msSemi16.drawCenteredString(matrixStack, "Binding...", x + width - 66  + 5 + (10 + 5) / 2, y + 7, Extazyy.themesUtil.getCurrentStyle().getColor(90));
        }
        if (key != null) {
            String keybig = key.toUpperCase();
            FontRenderers.msSemi16.drawCenteredString(matrixStack, "[" +  keybig + "]", x + width - 41 -  FontRenderers.msSemi16.getStringWidth(keybig) + 5 + (10 +  FontRenderers.msSemi16.getStringWidth(keybig)) / 2, y + 7, Extazyy.themesUtil.getCurrentStyle().getColor(90));
        }
        int color = new Color(33, 33, 33, 255).getRGB();
         RenderMcd.drawBlurredShadow(matrixStack,this.x + this.width - 25.0F, y + 5, 20.0F, 10.0F, 10, new Color(12, 12, 12, 216).getRGB());
        DrawHelper.rectangle(matrixStack,this.x + this.width - 25.0F, y + 5, 20.0F, 10.0F, 4.0F, module.isEnabled() ? ColorUtil.getColorStyle(90) : color);
        DrawHelper.rectangle(matrixStack,this.x + this.width - 23.5F - this.animation * 10.0F, y + 5 + 1.5f, 7.0F, 7.0F, 3.0F, module.isEnabled() ? new Color(255,255,255).getRGB() : new Color(95, 95, 95, 255).getRGB());

        this.animation = AnimMath.fast(this.animation, module.isEnabled() ? -1.0F : 0.0F, 15.0F);


        float offsetY = 0;
        for (Tool component : components) {
            if (component.s != null && component.s.visible()) {
                component.setPosition(x, y + height + offsetY, width, 20);
                component.drawComponent(matrixStack, mouseX, mouseY);
                offsetY += component.height;
            }
        }

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (DrawHelper.isInRegion(mouseX, mouseY, x + 5, y + 5, width - 10, 10) && mouseButton <= 1) {
            module.toggle();
        }

        if (binding == this && mouseButton > 2) {
            if(!ClientUtil.getKey(-100 + mouseButton).contains("ESCAPE")) {
                module.bind = -100 + mouseButton;
                binding = null;
            }
        }

        if (DrawHelper.isInRegion(mouseX, mouseY, x + 5, y, width - 10, 20)) {
            if (mouseButton == 2) {
                binding = this;
            }
        }
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

