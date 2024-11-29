package ru.fruzz.extazyy.main.modules.impl.render;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.joml.Vector2f;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.commands.impl.GpsCommand;
import ru.fruzz.extazyy.main.drag.Dragging;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.ChatReceivedEvent;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent2D;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;
import ru.fruzz.extazyy.main.modules.tools.imp.MultiBoxTools;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;
import ru.fruzz.extazyy.misc.util.*;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;
import ru.fruzz.extazyy.misc.util.anim.AnimMath;
import ru.fruzz.extazyy.misc.util.math.TimerUtil;
import ru.fruzz.extazyy.misc.util.render.lowrender.RenderMcd;
import ru.fruzz.extazyy.misc.util.music.Music;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;
import ru.fruzz.extazyy.misc.util.render.TestRender;
import ru.fruzz.extazyy.misc.util.text.AnimationText;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
@ModuleAnnotation(name = "Hud", type = CategoryUtil.Render, risk = true, setting = true)
public class Hud extends Module implements Mine  {


    public final MultiBoxTools settings = new MultiBoxTools("Элементы",
            new BooleanOption("Watermark", true),
            new BooleanOption("ArrayList", true),
            new BooleanOption("Armor Hud", true),
            new BooleanOption("Hotbar", true),
            new BooleanOption("KeyBinds", true),
            new BooleanOption("Potions", true),
            new BooleanOption("Music", false),
            new BooleanOption("Scoreboard", true),
            new BooleanOption("GPS", true)
    );

    public NumberTools numberTools = new NumberTools("Громкость", 0.1f, 0, 2, 0.01f).setVisible(() -> settings.get("Music"));

    public void onEnable() {
        musichelper.init();
    }

    public Hud() {
        addSettings(settings, numberTools);
    }

    public static AnimationText wtA = new AnimationText(1500, Arrays.asList("", Extazyy.userInfo.getName() + " [UID: " + Extazyy.getUserInfo().getUid() + "]", "Ваше время: " + (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(new Date()), "https://dsc.gg/extz", "https://extazyy.xyz/", Extazyy.getUserInfo().getName() + " [" + Extazyy.getUserInfo().getRole() + "]"));
    private int syncedColor;

    @EventHandler
    public void render2d(RenderEvent2D event2D) {

        PoseStack stack = event2D.getGuiGraphics().pose();

        if (settings.get("Watermark")) {
            renderWT(stack);
        }
        if(settings.get("Armor Hud")) {
            armorv2(stack, event2D);
        }
        if(settings.get("ArrayList")) {
            renderArrayList(stack);
        }
        if(settings.get("Potions")) {
            onPotionElementsRender(stack, event2D);
        }
        if(settings.get("KeyBinds")) {
            onKeyBindsRender(stack);
        }
        if(settings.get("Music")) {
            music(stack, event2D);
        }

        renderEventTime(stack,event2D);
        //last render, lomaet nahyi
        if(settings.get("GPS")) {
                drawGps(event2D.getGuiGraphics().pose());
        }
    }

    public static void drawGps(PoseStack e) {
        float xOffset = mc.getWindow().getGuiScaledWidth() / 2f;
        float yOffset = mc.getWindow().getGuiScaledHeight() / 2f;
        float yaw = getRotations(new Vector2f((float) GpsCommand.vector3d.x, (float) GpsCommand.vector3d.z)) - mc.player.getYRot();
        e.last().pose().translate(xOffset, yOffset, 0.0F);
        e.mulPose(Axis.ZP.rotationDegrees(yaw));
        e.last().pose().translate(-xOffset, -yOffset, 0.0F);
        DrawHelper.drawTextureRotate(new ResourceLocation("minecraft", "extazyy/images/arrow.png"), e.last().pose(), xOffset - 3, yOffset - 50, 12.5f + 4, 12.5f + 4, 190, ColorUtil.getColorStyle(0));
        e.last().pose().translate(xOffset, yOffset, 0.0F);
        e.mulPose(Axis.ZP.rotationDegrees(-yaw));
        e.last().pose().translate(-xOffset, -yOffset, 0.0F);


            int text = getDistance(new BlockPos((int) GpsCommand.vector3d.x, 0, (int) GpsCommand.vector3d.z));
            float widthoff = FontRenderers.umbrellatext16.getStringWidth(text + "m");
            FontRenderers.umbrellatext16.drawString(e, text + "m", xOffset - widthoff /2, yOffset + 10, -1);

            if(text < 10) {
                GpsCommand.vector3d = null;
            }
    }

    public static int getDistance(BlockPos bp) {
        double d0 = mc.player.getX() - bp.getX();
        double d2 = mc.player.getZ() - bp.getZ();
        return (int) (Mth.sqrt((float) (d0 * d0 + d2 * d2)));
    }

    public static float getRotations(Vector2f vec) {
        if (mc.player == null) return 0;
        double x = vec.x - mc.player.getX();
        double z = vec.y - mc.player.getZ();
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }




    public Dragging sco = Extazyy.createDrag(this, "scorebb", 300, 300);

    public Dragging timeHud = Extazyy.createDrag(this, "TimeHud", 180, 80);
    public Dragging music = Extazyy.createDrag(this, "MusicHud", 100, 80);

    public void displayScoreboardSidebar(GuiGraphics pGuiGraphics, Objective pObjective) {
        if(!settings.get("Scoreboard")) return;
        float x = Extazyy.getModuleManager().hud.sco.getX();
        float y = Extazyy.getModuleManager().hud.sco.getY() + 90;

        Scoreboard scoreboard = pObjective.getScoreboard();
        Collection<Score> collection = scoreboard.getPlayerScores(pObjective);
        List<Score> list = collection.stream().filter((p_93026_0_) -> {
            return p_93026_0_.getOwner() != null && !p_93026_0_.getOwner().startsWith("#");
        }).collect(Collectors.toList());
        if (list.size() > 15) {
            collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
        } else {
            collection = list;
        }

        List<Pair<Score, net.minecraft.network.chat.Component>> list1 = Lists.newArrayListWithCapacity(collection.size());
        net.minecraft.network.chat.Component component = pObjective.getDisplayName();

        int i = Gui.getFont().width(component);
        int j = i;
        int k = Gui.getFont().width(": ");

        for (Score score : collection) {
            PlayerTeam playerteam = scoreboard.getPlayersTeam(score.getOwner());
            net.minecraft.network.chat.Component component1 = PlayerTeam.formatNameForTeam(playerteam, net.minecraft.network.chat.Component.literal(score.getOwner()));
            list1.add(Pair.of(score, component1));
            j = Math.max(j, Gui.getFont().width(component1) + k + Gui.getFont().width(Integer.toString(score.getScore())));
        }
        //i = j;
        int scoreBoardHeight = collection.size() * 9;
        int j2 = (int) y + scoreBoardHeight / 3;
        int l2 = (int) x;
        int l = 0;

        RenderMcd.drawBlurredShadow(pGuiGraphics.pose(), x, Extazyy.getModuleManager().hud.sco.getY(), j, scoreBoardHeight + 12, 9, syncedColor);
        DrawHelper.rectangle(pGuiGraphics.pose(), x, Extazyy.getModuleManager().hud.sco.getY(), j, scoreBoardHeight + 12, 2,new Color(20, 20, 20, 217).getRGB());

        sco.setHeight(scoreBoardHeight + 10);
        sco.setWidth(j);
        for (Pair<Score, net.minecraft.network.chat.Component> pair : list1) {
            ++l;
            Score score1 = pair.getFirst();
            Component component2 = pair.getSecond();

            String s = "" + ChatFormatting.RED + score1.getScore();
            int k1 = j2 - l * 9;
            int l1 = (int) x + j + 2;

            pGuiGraphics.drawString(Gui.getFont(), component2, l2, k1, -1, false);
            if (l == collection.size()) {
                pGuiGraphics.drawString(Gui.getFont(), component, l2 + j / 2 - i / 2, k1 - 9, -1, false);
            }
        }
    }

    @EventHandler
    public void onChat(ChatReceivedEvent e) {
        Utils.checkAndStartCountdown(Utils.trimBeforeArrow(e.getComponent().getString()));
    }



    public void renderEventTime(PoseStack stack, RenderEvent2D event2D) {
        if(Utils.remainingVulcan == null) return;
        float x = timeHud.getX();
        float y = timeHud.getY();
        float width;
        float height;
        width = 93;
        height = 30;

        String textName = "Время до ивента";
        String info = Utils.remainingVulcan;
        float endwith = FontRenderers.msSemi16.getStringWidth(info);
        float textwidth = FontRenderers.msSemi16.getStringWidth(textName);
        width = Math.max(textwidth, endwith);

        RenderMcd.drawBlurredShadow(stack, x, y, width, height, 9, syncedColor);
        DrawHelper.rectangle(stack ,x, y, width, height, 2, new Color(20, 20, 20, 217).getRGB());

        FontRenderers.msSemi16.drawString(stack, textName, (x + width / 2) - textwidth / 2, y + 6, syncedColor);

        FontRenderers.msSemi14.drawString(stack, info, (x + width / 2) - textwidth / 2f, y + 19, -1);
        timeHud.setHeight(height);
        timeHud.setWidth(width);
    }

    public static AnimationText musictext;
    public static boolean musicstate = false;
    public String time = "00:00";
    Music musichelper = new Music();

    public void music(PoseStack stack, RenderEvent2D event2D) {
        musichelper.update();
        float x = music.getX();
        float y = music.getY();
        float width;
        float height;
        width = 83;
        height = 45;


        float endwith = FontRenderers.msSemi16.getStringWidth(time);
        float textwidth = FontRenderers.msSemi16.getStringWidth("Музыка");
        float msheight = FontRenderers.msSemi16.getFontHeight("1");

        RenderMcd.drawBlurredShadow(stack, x, y, width, height, 9, syncedColor);
        DrawHelper.rectangle(stack ,x, y, width, height, 2, new Color(20, 20, 20, 217).getRGB());

        FontRenderers.msSemi16.drawString(stack, "Музыка", (x + width / 2) - textwidth / 2, y + 6, syncedColor);

        //Scissor
        TestRender.addWindow(stack ,x + 5,  y +18, x + 83 - 10 - endwith, y + 18 + msheight, 1);
        //
        FontRenderers.msSemi16.drawString(stack, musictext.done, x + 5, y + 18, new Color(255,255,255,255).getRGB());
        //
        TestRender.popWindow();
        //Scissor end

        FontRenderers.msSemi16.drawString(stack, time, x + width - 5 - endwith, y + 18, new Color(255,255,255,255).getRGB());


        float offset = 15;
        float start = 7;
        DrawHelper.rectangle(stack ,x + start + (offset * 1), y + 30, 11, 11, 2, new Color(255, 255, 255, 11).getRGB());
        DrawHelper.rectangle(stack ,x + start + (offset * 2), y + 30, 11, 11, 2, new Color(255, 255, 255, 11).getRGB());
        DrawHelper.rectangle(stack ,x + start + (offset * 3), y + 30, 11, 11, 2, new Color(255, 255, 255, 11).getRGB());

        FontRenderers.music14.drawString(stack, "s", x + start + (offset * 1) + 2f, y + 34f, ColorUtil.toRGBA(135, 136, 148, 255));
        FontRenderers.music14.drawString(stack, musicstate ? "u" : "v", x + start + (offset * 2) + 2f + (musicstate ? 0 : 0.5f), y + 34f, ColorUtil.toRGBA(135, 136, 148, 255));
        FontRenderers.music14.drawString(stack, "t", x + start + (offset * 3) + 2f, y + 34f, ColorUtil.toRGBA(135, 136, 148, 255));

        music.setWidth(width);
        music.setHeight(height);
    }

    public void onClicked(double pMouseX, double pMouseY, int pButton) {
        float x = music.getX();
        float y = music.getY();

        float offset = 15;
        float start = 7;
        if(DrawHelper.isInRegion(pMouseX, pMouseY, x + start + (offset * 1), y + 30, 11, 11)) {
            musichelper.previousCallback();
        }
        if(DrawHelper.isInRegion(pMouseX, pMouseY, x + start + (offset * 2), y + 30, 11, 11)) {
            musichelper.playCallback();
        }
        if(DrawHelper.isInRegion(pMouseX, pMouseY, x + start + (offset * 3), y + 30, 11, 11)) {
            musichelper.nextCallback();
        }
    }

    public Dragging armorhud = Extazyy.createDrag(this, "ArmorHud", 500, 80);

    public void armorv2(PoseStack stack, RenderEvent2D event2D) {
        GuiGraphics guiGraphics = event2D.getGuiGraphics();
        if(Utils.isArmor()) {
            float x = armorhud.getX();
            float y = armorhud.getY();
            float width;
            float height;
            width = 83;
            height = 79;
            RenderMcd.drawBlurredShadow(stack, x, y, width, height, 9, syncedColor);
            DrawHelper.rectangle(stack ,x, y, width, height, 2, new Color(20, 20, 20, 217).getRGB());
            float textwidth = FontRenderers.msSemi16.getStringWidth("Броня");
            FontRenderers.msSemi16.drawString(stack, "Броня", (x + width / 2) - textwidth / 2, y + 6, syncedColor);

            int posX = (int) x + 3;
            int posY = (int) y + 63;

            for (ItemStack itemStack : mc.player.getArmorSlots()) {
                if (!itemStack.isEmpty()) {
                    guiGraphics.renderItem(itemStack, posX, posY);
                    float damagePercentage = (itemStack.getDamageValue() * 100.0f) / itemStack.getMaxDamage();
                    int red = (int) (255 * (damagePercentage / 100));
                    int green = 255 - red;
                    int barHeight2 = Math.round((28 * (100 - damagePercentage)) / 100);
                    DrawHelper.rectangle(stack,posX + 20, posY + 6, 28, 3, 1, ColorUtil.toRGBA(15, 15, 15, 255));
                    DrawHelper.rectangle(stack,posX + 20, posY + 6, barHeight2, 3, 1, ColorUtil.toRGBA(red, green, 0, 255));
                    float damage = itemStack.getMaxDamage();
                    if(damage > 0) {
                        FontRenderers.msSemi16.drawCenteredString(stack, (100 - (itemStack.getDamageValue() * 100) / itemStack.getMaxDamage()) + "%", posX + width - 20, posY + 4, -1);
                    }
                } else {
                    FontRenderers.msSemi16.drawCenteredString(stack, "-", posX + 8, posY + 4.5f, ColorUtil.toRGBA(135, 136, 148, 255));
                    DrawHelper.rectangle(stack,posX + 20, posY + 6, 28, 3, 1, ColorUtil.toRGBA(135, 136, 148, 255));
                }

                posY -= 16;
            }

            armorhud.setWidth(width);
            armorhud.setHeight(height);
        }
    }

    private void renderWT(PoseStack poseStack) {
        String textA = wtA.done;
        final String title = "Extazyy | " + mc.getFps() + "fps | " + "0" + "ms";

        // Используем сохраненный цвет
        int firstColor = syncedColor;  // Синхронизированный цвет из renderArrayList
        int secondColor = Extazyy.themesUtil.getCurrentStyle().getColorLowSpeed(0);

        int width = (int) (Math.max(FontRenderers.msSemi16.getStringWidth(title) + 6, FontRenderers.msSemi16.getStringWidth(textA)) + 14.0);
        final float x = 5;
        final float y = 5;
        final float titleHeight = 23;

        RenderMcd.drawBlurredShadow(poseStack, x, y, width, titleHeight, 9, firstColor);
        DrawHelper.rectangle(poseStack, x, y, width, titleHeight, 2, ColorUtil.toRGBA(20, 20, 20, 217));
        DrawHelper.rectRGB(poseStack, 6.4f, 6.5f, 8, 20f, 1.3f ,secondColor,firstColor, secondColor, firstColor);
        FontRenderers.msSemi16.drawString(poseStack, wtA.done, x + 11, y + 14 - 0.35, -1);
        FontRenderers.msSemi16.drawString(poseStack, title, x + 11, y + 4 - 0.35, new Color(255, 255, 255, 255).getRGB());
    }


    public Dragging keyBinds = Extazyy.createDrag(this, "KeyBinds", 200, 50);

    public void onKeyBindsRender(PoseStack stack) {
        float posX = keyBinds.getX();
        float posY = keyBinds.getY();

        int headerHeight = 13;
        int width = 93;
        int padding = 5;
        int offset = 13;
        float height = activeModules * offset;
        this.heightDynamic = AnimMath.fast(this.heightDynamic, height, 15);
        for (Module f : Extazyy.moduleManager.getFunctions()) {
            if (f.bind != 0 && f.state) {
                RenderMcd.drawBlurredShadow(stack,posX - 2.5f, posY, width + 5, heightDynamic + headerHeight + 2.5f, 9,syncedColor);
                DrawHelper.rectangle(stack,posX - 2.5f, posY, width + 5, heightDynamic + headerHeight + 2.5f, 2, new Color(20, 20, 20, 217).getRGB());
                FontRenderers.msSemi16.drawCenteredString(stack, "Клавиши", keyBinds.getX() + width / 2f, posY + 5f, syncedColor);

                break;
            }
        }
        TestRender.addWindow(stack, posX, posY + headerHeight, posX + width, posY + headerHeight + heightDynamic + padding / 2f, 1);
        int index = 0;
        for (Module f : Extazyy.moduleManager.getFunctions()) {
            if (f.bind != 0 && f.state) {
                String text = ClientUtil.getKey(f.bind);
                if (text == null) {
                    continue;
                }
                String bindText = "[" + text.toUpperCase() + "]";
                float bindWidth = FontRenderers.msSemi16.getStringWidth(bindText);
                FontRenderers.msSemi14.drawString(stack, f.name, posX + padding - 3 , posY + headerHeight + padding + (index * offset), -1);
                FontRenderers.msSemi14.drawString(stack, bindText, posX + width - bindWidth - padding + 4.5, posY + headerHeight + padding + (index * offset), -1);
                index++;
            }
        }
        TestRender.popWindow();
        activeModules = index;
        keyBinds.setWidth(width);
        keyBinds.setHeight(activeModules * offset + headerHeight);
    }

    public Dragging potionStatus = Extazyy.createDrag(this, "PotionStatus", 300, 50);
    private float hDynamic = 0;
    private int activePotions = 0;

    private void onPotionElementsRender(final PoseStack matrixStack, final RenderEvent2D renderEvent) {
        float posX = potionStatus.getX();
        float posY = potionStatus.getY();
        int headerHeight = 14;
        int width = 93;
        int padding = 5;
        float offset = 14.5f;
        float height = activePotions * offset;
        this.hDynamic = AnimMath.fast(this.hDynamic, height, 10);
        if(!mc.player.getActiveEffects().isEmpty()) {
            RenderMcd.drawBlurredShadow(matrixStack, posX - 2.5f, posY, width, hDynamic + headerHeight, 9, syncedColor);
            DrawHelper.rectangle(matrixStack,posX - 2.5f, posY, width, hDynamic + headerHeight, 2, new Color(20, 20, 20, 217).getRGB());
            FontRenderers.msSemi16.drawCenteredString(matrixStack, "Зелья", potionStatus.getX() + width / 2f - 3, posY + 5.5f, syncedColor);
        }
        int index = 0;
        for (MobEffectInstance p : mc.player.getActiveEffects()) {
            if (p.showIcon()) {
                String durationText = Utils.getPotionDurationString(p, 1);
                float durationWidth = FontRenderers.msSemi14.getStringWidth(durationText);
                FontRenderers.msSemi14.drawString(matrixStack, I18n.get(p.getEffect().getDisplayName().getString()) + " " + Utils.getLevel(p), posX + padding - 3, posY + headerHeight + padding + (index * offset), -1);
                FontRenderers.msSemi14.drawString(matrixStack, durationText, posX + width - durationWidth - padding, posY + headerHeight + padding + (index * offset), new Color(255,255,255,255).getRGB());
                index++;
            }
        }
        activePotions = index;
        potionStatus.setWidth(width);
        potionStatus.setHeight(activePotions * offset + headerHeight);
    }

    private float heightDynamic = 0;
    private int activeModules = 0;
    public List<Module> sortedModules = new ArrayList<>();
    TimerUtil delay = new TimerUtil();

    public void renderArrayList(PoseStack stack) {
        float x = 5;
        float y = 24;
        float height = 10;
        float yOffset = 6;

        if (delay.hasTimeElapsed(10000)) {
            sortedModules = Utils.getSorted(FontRenderers.msSemi16);
            delay.reset();
        }

        int fontOffset = 0;
        int secondColor;

        for (Module module : sortedModules) {
            module.animation = AnimMath.lerp(module.animation, module.state ? 1 : 0, 15);
            if (module.animation >= 0.01) {
                float width = FontRenderers.msSemi14.getStringWidth(module.name) + 5;
                secondColor = Extazyy.themesUtil.getCurrentStyle().getColorLowSpeed((int) -(yOffset * 1));

                syncedColor = secondColor;
                RenderMcd.drawBlurredShadow(stack,x - 2, y + yOffset, width + 3, height ,7,secondColor);
                DrawHelper.rectangle(stack, x, y + yOffset, width, height, 1.5f, ColorUtil.toRGBA(20, 20, 20, 217));
                FontRenderers.msSemi14.drawString(stack, module.name, x + 2.3, y - 2.25 + yOffset + FontRenderers.msSemi14.getFontHeight("1") / 2f - fontOffset, secondColor);yOffset += height * module.animation + 2;
            }
        }

        yOffset = 0;
        for (Module module : sortedModules) {
            module.animation = AnimMath.lerp(module.animation, module.state ? 1 : 0, 15);
            if (module.animation >= 0.01) {
                yOffset += height * module.animation;
            }
        }
    }


}
