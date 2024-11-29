package ru.fruzz.extazyy.misc.ui.clickgui.tools;



import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;
import ru.fruzz.extazyy.main.modules.tools.imp.MultiBoxTools;
import ru.fruzz.extazyy.misc.ui.themeui.impl.Tool;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;
import ru.fruzz.extazyy.misc.util.render.TestRender;
import ru.fruzz.extazyy.misc.util.anim.AnimMath;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

public class MultiBoxTool extends Tool {

    public MultiBoxTools option;

    public boolean opened;

    public MultiBoxTool(MultiBoxTools option) {
        this.option = option;
        this.s = option;
    }

    // ?????????? ??? ????????
    private float currentHeight = 0.0f;
    private float targetHeight = 0.0f;

    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        float off = 4;
        float baseOffset = 17 - 8;
        float expandedOffset = baseOffset;

        // ???????????? ?????? ?????? ??? ????????? ?????????
        for (BooleanOption s : option.options) {
            expandedOffset += 9;
        }

        // ???? ???? ???????, ????????????? ??????? ?????? ??? ?????? ??????
        // ????? ??????? ?????? ????? ??????? (???????? ?????????)
        if (opened) {
            targetHeight = expandedOffset;
        } else {
            targetHeight = 0;
        }

        // ?????? ????????????? ??????? ?????? ? ???????
        currentHeight = AnimMath.lerp(currentHeight, targetHeight, 10);

        // ?????? ???????? ???????
        FontRenderers.msSemi16.drawString(matrixStack, option.getName(), x + 6, y + 3, -1);
        off += FontRenderers.msSemi16.getFontHeight("1") / 2f + 2;

        // ???????????? ????? ?????? ??????????
        height = off + currentHeight + 15;

        // ?????? ???????? ???
        DrawHelper.rectangle(matrixStack, x + 5, y + off, width - 10, 20 - 6, 2, new Color(74, 74, 74, 26).getRGB());

        // ?????? ??? ??? ?????? ?????
        DrawHelper.rectangle(matrixStack, x + 5, y + off + 17, width - 10, currentHeight - 3.5f, 2, new Color(74, 74, 74, 26).getRGB());

        // ????????? ???? (????????)
        TestRender.addWindow(matrixStack, x + 5, y + off, x + 5 + width - 10, y + off + 20 - 6, 1);

        // ?????? ????????? ?????
        FontRenderers.msSemi16.drawString(matrixStack, option.get(), x + 8, y + 20 - 4, new Color(214, 214, 214, 255).getRGB());

        // ??????? ???? (????? ?????????)
        TestRender.popWindow();

        // ???? ???? ???????, ?????? ?????? ?????
        if (opened) {
            int i = 1;
            for (BooleanOption s : option.options) {
                boolean hovered = DrawHelper.isInRegion(mouseX, mouseY, x, y + off + 20 + i, width, 8);
                s.anim = AnimMath.lerp(s.anim, (hovered ? 2 : 0), 10);
                FontRenderers.msSemi16.drawString(matrixStack, s.getName(), x + 8 + s.anim, y + off + 20F + i, option.get(s.getName()) ? ColorUtil.getColorStyle(90) : new Color(132, 132, 132, 255).getRGB());
                i += 9;
            }
            height += 1;
        }
    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        float off = 3;
        off += FontRenderers.msSemi16.getFontHeight("1") / 2f + 2;
        if (DrawHelper.isInRegion(mouseX, mouseY, x + 5, y + off, width - 10, 20 - 5)) {
            opened = !opened;
        }


        if (!opened) return;
        int i = 1;
        for (BooleanOption s : option.options) {
            if (DrawHelper.isInRegion(mouseX, mouseY, x, y + off + 20F + i, width, 8))
                option.set((i - 1) / 9, !option.get(s.getName()));
            i += 9;
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
