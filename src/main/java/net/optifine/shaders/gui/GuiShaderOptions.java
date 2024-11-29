package net.optifine.shaders.gui;

import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.optifine.Config;
import net.optifine.Lang;
import net.optifine.gui.GuiButtonOF;
import net.optifine.gui.GuiScreenOF;
import net.optifine.gui.TooltipManager;
import net.optifine.gui.TooltipProviderShaderOptions;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.config.ShaderOption;
import net.optifine.shaders.config.ShaderOptionProfile;
import net.optifine.shaders.config.ShaderOptionScreen;

import java.util.List;

public class GuiShaderOptions extends GuiScreenOF {
    private Screen prevScreen;
    private Options settings;
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderShaderOptions());
    private String screenName = null;
    private String screenText = null;
    private boolean changed = false;
    public static final String OPTION_PROFILE = "<profile>";
    public static final String OPTION_EMPTY = "<empty>";
    public static final String OPTION_REST = "*";

    public GuiShaderOptions(Screen guiscreen, Options gamesettings) {
        super(Component.literal(I18n.get("of.options.shaderOptionsTitle")));
        this.prevScreen = guiscreen;
        this.settings = gamesettings;
    }

    public GuiShaderOptions(Screen guiscreen, Options gamesettings, String screenName) {
        this(guiscreen, gamesettings);
        this.screenName = screenName;
        if (screenName != null) {
            this.screenText = Shaders.translate("screen." + screenName, screenName);
        }

    }

    public void init() {
        int i = 100;
        int j = 0;
        int k = 30;
        int l = 20;
        int i1 = 120;
        int j1 = 20;
        int k1 = Shaders.getShaderPackColumns(this.screenName, 2);
        ShaderOption[] ashaderoption = Shaders.getShaderPackOptions(this.screenName);
        if (ashaderoption != null) {
            int l1 = Mth.ceil((double) ashaderoption.length / 9.0D);
            if (k1 < l1) {
                k1 = l1;
            }

            for (int i2 = 0; i2 < ashaderoption.length; ++i2) {
                ShaderOption shaderoption = ashaderoption[i2];
                if (shaderoption != null && shaderoption.isVisible()) {
                    int j2 = i2 % k1;
                    int k2 = i2 / k1;
                    int l2 = Math.min(this.width / k1, 200);
                    j = (this.width - l2 * k1) / 2;
                    int i3 = j2 * l2 + 5 + j;
                    int j3 = k + k2 * l;
                    int k3 = l2 - 10;
                    String s = getButtonText(shaderoption, k3);
                    GuiButtonShaderOption guibuttonshaderoption;
                    if (Shaders.isShaderPackOptionSlider(shaderoption.getName())) {
                        guibuttonshaderoption = new GuiSliderShaderOption(i + i2, i3, j3, k3, j1, shaderoption, s);
                    } else {
                        guibuttonshaderoption = new GuiButtonShaderOption(i + i2, i3, j3, k3, j1, shaderoption, s);
                    }

                    guibuttonshaderoption.active = shaderoption.isEnabled();
                    this.addRenderableWidget(guibuttonshaderoption);
                }
            }
        }

        this.addRenderableWidget(new GuiButtonOF(201, this.width / 2 - i1 - 20, this.height / 6 + 168 + 11, i1, j1, I18n.get("controls.reset")));
        this.addRenderableWidget(new GuiButtonOF(200, this.width / 2 + 20, this.height / 6 + 168 + 11, i1, j1, I18n.get("gui.done")));
    }

    public static String getButtonText(ShaderOption so, int btnWidth) {
        String s = so.getNameText();
        if (so instanceof ShaderOptionScreen shaderoptionscreen) {
            return s + "...";
        } else {
            Font font = Config.getMinecraft().font;

            for (int i = font.width(": " + Lang.getOff()) + 5; font.width(s) + i >= btnWidth && s.length() > 0; s = s.substring(0, s.length() - 1)) {
            }

            String s1 = so.isChanged() ? so.getValueColor(so.getValue()) : "";
            String s2 = so.getValueText(so.getValue());
            return s + ": " + s1 + s2;
        }
    }

    protected void actionPerformed(AbstractWidget guiElement) {
        if (guiElement instanceof GuiButtonOF guibuttonof) {
            if (guibuttonof.active) {
                if (guibuttonof.id < 200 && guibuttonof instanceof GuiButtonShaderOption) {
                    GuiButtonShaderOption guibuttonshaderoption = (GuiButtonShaderOption) guibuttonof;
                    ShaderOption shaderoption = guibuttonshaderoption.getShaderOption();

                    if (shaderoption instanceof ShaderOptionScreen) {
                        String s = shaderoption.getName();
                        GuiShaderOptions guishaderoptions = new GuiShaderOptions(this, this.settings, s);
                        this.minecraft.setScreen(guishaderoptions);
                        return;
                    }

                    if (hasShiftDown()) {
                        shaderoption.resetValue();
                    } else if (guibuttonshaderoption.isSwitchable()) {
                        shaderoption.nextValue();
                    }

                    this.updateAllButtons();
                    this.changed = true;
                }

                if (guibuttonof.id == 201) {
                    ShaderOption[] ashaderoption = Shaders.getChangedOptions(Shaders.getShaderPackOptions());

                    for (int i = 0; i < ashaderoption.length; ++i) {
                        ShaderOption shaderoption1 = ashaderoption[i];
                        shaderoption1.resetValue();
                        this.changed = true;
                    }

                    this.updateAllButtons();
                }

                if (guibuttonof.id == 200) {
                    if (this.changed) {
                        Shaders.saveShaderPackOptions();
                        this.changed = false;
                        Shaders.uninit();
                    }

                    this.minecraft.setScreen(this.prevScreen);
                }
            }
        }
    }


    public void removed() {
        if (this.changed) {
            Shaders.saveShaderPackOptions();
            this.changed = false;
            Shaders.uninit();
        }

        super.removed();
    }

    protected void actionPerformedRightClick(AbstractWidget guiElement) {
        if (guiElement instanceof GuiButtonShaderOption guibuttonshaderoption) {
            ShaderOption shaderoption = guibuttonshaderoption.getShaderOption();

            if (hasShiftDown()) {
                shaderoption.resetValue();
            } else if (guibuttonshaderoption.isSwitchable()) {
                shaderoption.prevValue();
            }

            this.updateAllButtons();
            this.changed = true;
        }
    }


    private void updateAllButtons() {
        for (Button button : (List<Button>) (List<?>) this.getButtonList()) {
            if (button instanceof GuiButtonShaderOption guibuttonshaderoption) {
                ShaderOption shaderoption = guibuttonshaderoption.getShaderOption();

                if (shaderoption instanceof ShaderOptionProfile shaderoptionprofile) {
                    shaderoptionprofile.updateProfile();
                }

                guibuttonshaderoption.setMessage(getButtonText(shaderoption, guibuttonshaderoption.getWidth()));
                guibuttonshaderoption.valueChanged();
            }
        }
    }


    public void render(GuiGraphics graphicsIn, int x, int y, float partialTicks) {
        this.renderBackground(graphicsIn);
        if (this.screenText != null) {
            drawCenteredString(graphicsIn, this.fontRenderer, this.screenText, this.width / 2, 15, 16777215);
        } else {
            drawCenteredString(graphicsIn, this.fontRenderer, this.title, this.width / 2, 15, 16777215);
        }

        super.render(graphicsIn, x, y, partialTicks);
        this.tooltipManager.drawTooltips(graphicsIn, x, y, this.getButtonList());
    }
}
