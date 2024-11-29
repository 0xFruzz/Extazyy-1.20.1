package ru.fruzz.extazyy.main.modules.impl.render;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3d;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent2D;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;
import ru.fruzz.extazyy.main.modules.tools.imp.MultiBoxTools;
import ru.fruzz.extazyy.misc.util.render.dangertech.ESPUtil;
import ru.fruzz.extazyy.misc.util.render.dangertech.EntityPos;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ModuleAnnotation(name = "NameTags", type = CategoryUtil.Render)
public class NameTags extends Module {

    MultiBoxTools render = new MultiBoxTools("Подсвечивать",
            new BooleanOption("Игроков",true),
            new BooleanOption("Предметы",false)
    );

    public BooleanOption booleanOption = new BooleanOption("Отключить оригинальное имя", true).setVisible(() -> render.get("Игроков"));



    public NameTags() {
        addSettings(render, booleanOption);
    }
    @EventHandler
    public void d2(RenderEvent2D e) {
        if(render.get("Игроков")) {
            renderplayers(e);
        }
        if(render.get("Предметы")) {
            renderitems(e);
        }
    }
    public void renderplayers(RenderEvent2D e) {
        for (Player ent : mc.level.players()) {
            if (mc.player.equals(ent)) continue;
            Vector3d vec = getBabyState() ? ESPUtil.toScreen(EntityPos.get(ent, 1.2f, e.getPartialTicks())) : ESPUtil.toScreen(EntityPos.get(ent, 2.1f, e.getPartialTicks()));
            String name = ent.getDisplayName().getString().replace("⚡", "") + ChatFormatting.RESET;
            String hp = ChatFormatting.GRAY + " [" + getHPFormat(ent) + (int) ent.getHealth()  + "HP" +ChatFormatting.RESET + ChatFormatting.GRAY + "] " + ChatFormatting.RESET;
            String full = " " + name + hp;
            float width = FontRenderers.umbrellatext16.getStringWidth(full);
            float height = FontRenderers.umbrellatext16.getFontHeight("I");
            DrawHelper.rectangle(e.getGuiGraphics().pose(),(float) vec.x - (width) / 2, (float) vec.y, width, height, 0.5f, new Color(30,30,30, 179).getRGB());
            FontRenderers.umbrellatext16.drawString(e.getGuiGraphics().pose(), full, vec.x - (width) / 2 , vec.y + 2f, new Color(255,255,255).getRGB());
            renderPlayerItems(e, (float) vec.x - width /2, (float) vec.y, ent);
        }
    }

    public boolean getBabyState() {
        return Extazyy.moduleManager.babyMode.state && !Extazyy.getModuleManager().babyMode.here.get();
    }

    public void renderPlayerItems(RenderEvent2D e, float x, float y, Player player) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(player.getMainHandItem());
        player.getArmorSlots().forEach(stacks::add);
        stacks.add(player.getOffhandItem());

        stacks.removeIf(i -> i.getItem() instanceof AirItem);

        float renderOffset = 0;
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }

            DrawHelper.rectangle(e.getGuiGraphics().pose(), x + renderOffset, y - 11, 10, 10, 1, new Color(30,30,30, 179).getRGB());

            e.getGuiGraphics().renderItemScale(stack,  (x + renderOffset) - 3f, (y - 11) - 3, 7);
            renderOffset += 13;
        }
    }

    public ChatFormatting getHPFormat(Player entity) {
        return entity.getHealth() > 5 ? ChatFormatting.GREEN : ChatFormatting.RED;
    }

    public void renderitems(RenderEvent2D e) {
        for (Entity ent : mc.level.getEntities().getAll()) {
            if (ent instanceof ItemEntity) {
                Vector3d vec = ESPUtil.toScreen(EntityPos.get(ent, 0.9f, e.getPartialTicks()));
                String name = ent.getName().getString();
                String full = " " + name + " [" + (int) ent.distanceTo(mc.player) + "M] ";
                float width = FontRenderers.umbrellatext16.getStringWidth(full);
                float height = FontRenderers.umbrellatext16.getFontHeight("I");
                DrawHelper.rectangle(e.getGuiGraphics().pose(), (float) vec.x - (width) / 2, (float) vec.y, width, height, 0.5f, new Color(30, 30, 30, 150).getRGB());
                FontRenderers.umbrellatext16.drawString(e.getGuiGraphics().pose(), full, vec.x - (width) / 2, vec.y + 2f, new Color(143, 83, 83).getRGB());
            }
        }
    }
}
