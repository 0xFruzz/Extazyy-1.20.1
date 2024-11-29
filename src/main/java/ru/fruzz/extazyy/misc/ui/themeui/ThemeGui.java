package ru.fruzz.extazyy.misc.ui.themeui;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.themes.Theme;
import ru.fruzz.extazyy.misc.ui.themeui.impl.ThemeTool;
import ru.fruzz.extazyy.misc.util.Mine;
import ru.fruzz.extazyy.misc.util.drag.ScaleMah;
import ru.fruzz.extazyy.misc.util.anim.AnimMath;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ThemeGui extends Screen {
    public ThemeGui(Component pTitle) {
        super(pTitle);
    }

    protected void init() {
        super.init();
        for (Theme theme : Extazyy.themesUtil.themes) {
            this.theme.add(new ThemeTool(theme));
        }
    }

    public float scroll = -100;
    public float animateScroll = -80;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll += delta * 15;
        ThemeTool.selected = null;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public List<ThemeTool> theme = new ArrayList<>();

    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        float x =  Mine.mc.getWindow().getGuiScaledWidth();
        float y = Mine.mc.getWindow().getGuiScaledHeight();
        float xpanel = x / 3;
        float yPanel = y /3 - 100;
        DrawHelper.beginScissor(xpanel - 13, y / 5 - 9, xpanel - 13 + 350, y / 5 - 9 + 313);
        draw(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        DrawHelper.endScissor();
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    void draw(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        float x =  Mine.mc.getWindow().getGuiScaledWidth();
        float y = Mine.mc.getWindow().getGuiScaledHeight();
        float xpanel = x / 3;
        float yPanel = y /3;
        DrawHelper.rectangle(pGuiGraphics.pose(),xpanel - 13, y / 5 - 9, 350, 313, 3, new Color(30, 30, 30, 240).getRGB());
        animateScroll = AnimMath.lerp(animateScroll, scroll, 15);
        float offset2 = 0;
        offset2 = (yPanel + (64 / 2f) - 24) + animateScroll;
        for (ThemeTool component : theme) {
            component.parent = this;
            component.setPosition((float) xpanel, offset2 + 29, 314 + 12, 20);
            component.drawComponent(pGuiGraphics.pose(), pMouseX, pMouseY);
            offset2 += component.height + 2;
        }
        scroll = -100;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vec2 fixed = ScaleMah.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.x;
        mouseY = fixed.y;
        for (ThemeTool component : theme) {
            component.parent = this;
            component.mouseReleased((int) mouseX, (int) mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2 fixed = ScaleMah.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.x;
        mouseY = fixed.y;
        for (ThemeTool component : theme) {
            component.parent = this;
            component.mouseClicked((int) mouseX, (int) mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
