package ru.fruzz.extazyy.misc.ui.clickgui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.drag.DragManager;
import ru.fruzz.extazyy.main.drag.Dragging;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.misc.ui.clickgui.impl.ModuleTool;
import ru.fruzz.extazyy.misc.ui.clickgui.tools.ColorTool;
import ru.fruzz.extazyy.misc.ui.themeui.impl.Tool;
import ru.fruzz.extazyy.misc.util.*;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;
import ru.fruzz.extazyy.misc.util.color.ColorUtils;
import ru.fruzz.extazyy.misc.util.drag.ScaleMah;
import ru.fruzz.extazyy.misc.util.anim.AnimMath;
import ru.fruzz.extazyy.misc.util.render.lowrender.RenderMcd;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;
import ru.fruzz.extazyy.misc.util.render.TestRender;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClickGui extends Screen {

    public ClickGui(Component pTitle) {
        super(pTitle);
        for (Module module : Extazyy.moduleManager.getFunctions()) {
            objects.add(new ModuleTool(module));
        }
    }

    public ArrayList<ModuleTool> objects = new ArrayList<>();


    public float scroll = -50;
    public float animateScroll = 0;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll += delta * 10;
        ColorTool.opened = null;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public boolean isMain;

    @Override
    public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialticks) {
        float x = gui.getX();
        float y = gui.getY();


        super.render(matrixStack, mouseX, mouseY, partialticks);
        if(!isMain) {
           // drawGuiWatermark(matrixStack, matrixStack.pose(), mouseX, mouseY);
            TestRender.addWindow(matrixStack.pose(), x - 13, y + 1, x + 407.5f, y + 248, 1);
            drawComponents(matrixStack, matrixStack.pose(), mouseX, mouseY);
            drawHyi(matrixStack, matrixStack.pose(), mouseX, mouseY);
            TestRender.popWindow();
        } else {
            DrawHelper.beginScissor(x - 13, y + 1, x + 407.5f, y + 248);
            main(matrixStack, matrixStack.pose(), mouseX, mouseY);
            DrawHelper.endScissor();
        }

        if (ColorTool.opened != null) {
            ColorTool.opened.draw(matrixStack.pose(), mouseX, mouseY);
        }
        Window window = Mine.mc.getWindow();
        DragManager.draggables.values().forEach(dragging -> {
                dragging.onDraw(mouseX, mouseY, window);
        });
    }


    CategoryUtil current = CategoryUtil.Render;

    public Dragging gui = Extazyy.createDrag(Extazyy.moduleManager.hud, "ClickGui", (Mine.mc.getWindow().getGuiScaledWidth()) / 2 - 203.75f , Mine.mc.getWindow().getGuiScaledHeight() / 2 - 124);

    void drawComponents(GuiGraphics guiGraphics,PoseStack stack, int mouseX, int mouseY) {
        // Координаты в зависимости от установленного интерфейса
        float x = gui.getX();
        float y = gui.getY();
        //float xpanel = x / 3 - 90;
        //float yPanel = y / 3 - 123;

        //Деление модулей на три столбца для их отображения
        List<ModuleTool> filteredObjects = objects.stream()
                .filter(moduleObject -> moduleObject.module.category == current)
                .collect(Collectors.toList());

        List<ModuleTool> first = new ArrayList<>();
        List<ModuleTool> second = new ArrayList<>();


        for (int i = 0; i < filteredObjects.size(); i++) {
            if (i % 2 == 0) {
                first.add(filteredObjects.get(i));
            } else if (i % 2 == 1) {
                second.add(filteredObjects.get(i));
            }
        }


        float scale = 2f;
        animateScroll = AnimMath.lerp(animateScroll, scroll, 10);
        float height = 248;
        DrawHelper.drawSemiRoundRect(stack,x, y, 407.5f, 248, 12, 12, 12,12,new Color(16, 16, 16, 255).getRGB());
        // DrawHelper.drawSemiRoundRect(stack,x + 68, y + 218, 269.5f, 30, 0, 12,0,12, new Color(27, 27, 27, 255).getRGB());

          //Список модулей слева
        float offset = y + (64 / 2f) + 9 + animateScroll;
        float size1 = 0;
        for (ModuleTool component : first) {
            //ВРОДЕ проверка на категорию, чтобы типа модуль
            component.guiparent = this;
            //Установка координат залупы
            component.setPosition((float) x + 13 ,20 + offset, 324 / 2f + 25, 20);
            //Рендер сеттингов
            component.drawComponent(stack, mouseX, mouseY);
            //Если установленны хоть какие то сеттинги
            if (!component.components.isEmpty()) {
                for (Tool settingComp : component.components) {
                    if (settingComp.s != null && settingComp.s.visible()) {
                        //я не помню
                        offset += settingComp.height;
                        size1 += settingComp.height;
                    }
                }
            }
            //я не помню
            offset += component.height + 4;
            size1 += component.height + 4;
        }

        //Список модулей справа
        float offset2 = (y + (64 / 2f) + 9) + animateScroll;
        float size2 = 0;
        for (ModuleTool component : second) {
            //ВРОДЕ проверка на категорию, чтобы типа модуль
            if (component.module.category != current) continue;

            component.guiparent = this;
            //Установка координат залупы
            component.setPosition(x + 208.5f, 20 + offset2, 324 / 2f + 25, 20);

            //Рендер сеттингов
            component.drawComponent(stack, mouseX, mouseY);
            if (!component.components.isEmpty()) {
                for (Tool settingComp : component.components) {
                    if (settingComp.s != null && settingComp.s.visible()) {
                        //я не помню
                        offset2 += settingComp.height;
                        size2 += settingComp.height;
                    }
                }
            }
            //я не помню
            offset2 += component.height + 4;
            size2 += component.height + 4;
        }

        //Скролл
        float max = Math.max(size1, size2);
        if (max < height) {
            scroll = -50;
        } else {
            scroll = Mth.clamp(scroll, -(max - height + 90),-50);
        }

        gui.setWidth(407.5f);
        gui.setHeight(248);
    }


    void drawHyi(GuiGraphics guiGraphics,PoseStack stack, int mouseX, int mouseY) {
        float x = gui.getX();
        float y = gui.getY();
        float heightCategory = 55;
        DrawHelper.drawSemiRoundRect(stack,x + 68, y + 218, 269.5f, 30, 0, 12,0,12, new Color(27, 27, 27, 255).getRGB());
        for (CategoryUtil t : CategoryUtil.values()) {
            boolean hovered = DrawHelper.isInRegion(mouseX, mouseY, x, y + 32.5f + t.ordinal() * heightCategory, 25, heightCategory);
            t.animf = AnimMath.lerp(t.animf, (hovered ? 5 : 0), 10);
            char iconChar = '\0';
            switch (t) {
                case Movement:
                    iconChar = 'x';
                    break;
                case Combat:
                    iconChar = 'y';
                    break;
                case Render:
                    iconChar = 'z';
                    break;
                case Player:
                    iconChar = 'w';
                    break;
                case Misc:
                    iconChar = 'v';
                    break;

            }
                if (iconChar != '\0') {
                    //RenderMcd.drawBlurredShadow(stack,x + 83 + t.ordinal() * heightCategory, y + 226f, 12,12, 6,new Color(40,40,40, 213).getRGB() );
                    FontRenderers.extazyy24.drawString(stack, String.valueOf(iconChar), x + 85 + t.ordinal() * heightCategory, y + 229f, t == current ? ColorUtil.getColorStyle(90) : new Color(50, 50, 50).getRGB());
                }
        }
    }

    // Переменные для анимации
    private float width = 60.0f;
    private float textOffsetX = 9.0f;
    private float letterTAlpha = 0.0f;
    private boolean isHovered = false;

    void drawGuiWatermark(GuiGraphics guiGraphics, PoseStack stack, int mouseX, int mouseY) {
        float x = gui.getX();
        float y = gui.getY();

        boolean inRegion = DrawHelper.isInRegion(mouseX, mouseY, x, y - 19, 70, 20);

        if (inRegion && !isHovered) {
            isHovered = true;
        } else if (!inRegion && isHovered) {
            isHovered = false;
        }

        if (isHovered) {
            width = Math.min(70.0f, width + 2.0f); // Увеличиваем ширину
            textOffsetX = Math.min(21.0f, textOffsetX + 1.0f); // Сдвигаем текст вправо
            letterTAlpha = Math.min(1.0f, letterTAlpha + 0.1f); // Плавно проявляем букву "t"
        } else {
            width = Math.max(60.0f, width - 2.0f);
            textOffsetX = Math.max(9.0f, textOffsetX - 1.0f);
            letterTAlpha = Math.max(0.0f, letterTAlpha - 0.1f);
        }

        DrawHelper.drawSemiRoundRect(stack, x, y - 19, width, 28, 0, 6, 0, 6, new Color(16, 16, 16, 255).getRGB());
        if (inRegion) {
            RenderMcd.drawBlurredShadow(stack, x + 6.23f, y - 15f, 12, 12, 7, new Color(40, 40, 40, 255).getRGB());
            FontRenderers.extazyy18.drawString(stack, "t", x + 8, y - 11.5f, new Color(255, 255, 255, (int) (255 * letterTAlpha)).getRGB());
        }
        FontRenderers.msSemi24.drawString(stack, "Extazy", x + textOffsetX, y - 16, ColorUtils.getColorStyleLowSpeed(90));
    }




    protected void init() {
        super.init();
        ColorTool.opened = null;
    }

    void main(GuiGraphics guiGraphics, PoseStack stack, int mouseX, int mouseY) {
        float x = gui.getX();
        float y = gui.getY();

        DrawHelper.drawSemiRoundRect(stack,x, y, 407.5f, 248, 12, 12, 12,12,new Color(16, 16, 16, 255).getRGB());

    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vec2 fixed = ScaleMah.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.x;
        mouseY = fixed.y;
        for (ModuleTool m : objects) {
            if (m.module.category != current) continue;
            m.mouseReleased((int) mouseX, (int) mouseY, button);
        }
        if (ColorTool.opened != null) {
            ColorTool.opened.unclick((int) mouseX, (int) mouseY);
        }
        if(button == 2) {
            DragManager.draggables.values().forEach(dragging -> {
                dragging.onReleaseGui(button);
            });
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ModuleTool m : objects) {
            if (m.module.category != current) continue;
            m.keyTyped(keyCode, scanCode, modifiers);
        }
        //Бля, вроде бинды
        if (ModuleTool.binding != null) {
            //Delete \ Escape для отмены бинда
            if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                ModuleTool.binding.module.bind = 0;
            } else {
              ModuleTool.binding.module.bind = keyCode;
            }
            ModuleTool.binding = null;
        }


        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (ModuleTool m : objects) {
            if (m.module.category != current) continue;
            m.charTyped(codePoint,modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }
    float animation;



    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        Vec2 fixed = ScaleMah.getMouse((int) mouseX, (int) mouseY);
        mouseX = fixed.x;
        mouseY = fixed.y;
        float scale = 2f;
        float width = 900 / scale;
        float height = 650 / scale;
        float x = gui.getX();
        float y = gui.getY();

        double finalMouseY = mouseY;
        double finalMouseX = mouseX;
            DragManager.draggables.values().forEach(dragging -> {
                dragging.onClickGui(finalMouseX, finalMouseY, button);
            });

        if (ColorTool.opened != null) {
            if (!ColorTool.opened.click((int) mouseX, (int) mouseY))
                return super.mouseClicked(mouseX, mouseY, button);
        }
        boolean inRegion = DrawHelper.isInRegion(mouseX, mouseY, x, y - 19, 70, 20);
        if(inRegion) {
            //isMain = true; // При добавлении домашнего меню
        }
        float heightCategory = 55;
        for (CategoryUtil t : CategoryUtil.values()) {
            if (DrawHelper.isInRegion(mouseX, mouseY, x + 81 + t.ordinal() * heightCategory, y + 223f, 18, 16)) {
                if (current == t) continue;
                current = t;
                animation = 1;
                scroll = 0;
                ColorTool.opened = null;
            }
        }
        if (!DrawHelper.isInRegion(mouseX, mouseY, x, y, 407.5f, 248)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (DrawHelper.isInRegion(mouseX, mouseY, x, y - 30, width, height - 64 / 2f)) {
            for (ModuleTool m : objects) {
                if (m.module.category != current) continue;
                m.mouseClicked((int) mouseX, (int) mouseY, button);
            }
        }


        return super.mouseClicked(mouseX, mouseY, button);
    }

}
