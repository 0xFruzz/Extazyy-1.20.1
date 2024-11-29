package ru.fruzz.extazyy.main.modules.impl.render;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent2D;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent3D;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.misc.util.render.dangertech.ESPUtil;
import ru.fruzz.extazyy.misc.util.render.dangertech.EntityPos;
import ru.fruzz.extazyy.misc.util.funtime.FuntimeItems;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;
import ru.fruzz.extazyy.misc.util.render.TestRender;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ModuleAnnotation(name = "VulcanESP", type = CategoryUtil.Render)
public class VulcanESP extends Module {



    @EventHandler
    public void render2D(RenderEvent2D e) {
        for (Entity ent : mc.level.getEntities().getAll()) {
            if (ent instanceof ItemEntity) {
                for (FuntimeItems item : FuntimeItems.values()) {
                    ItemStack stack = ((ItemEntity) ent).getItem();
                    if(stack.getItem() == item.getItem()) {
                        Vector3d vec = ESPUtil.toScreen(EntityPos.get(ent, 0.9f, e.getPartialTicks()));

                        String full =  stack.getDisplayName().getString().replace("[", "").replace("]", "").replace("★", "") + ChatFormatting.RESET + " " + getItemLevel(stack)  + ChatFormatting.GRAY +  " [" + ChatFormatting.GREEN + (int) ent.distanceTo(mc.player) + "M" +ChatFormatting.GRAY + "] ";
                        float width = FontRenderers.umbrellatext16.getStringWidth(full);
                        float height = FontRenderers.umbrellatext16.getFontHeight("I");
                        DrawHelper.rectangle(e.getGuiGraphics().pose(), (float) vec.x - (width) / 2, (float) vec.y, width, height, 0.5f, new Color(30, 30, 30, 179).getRGB());
                        FontRenderers.umbrellatext16.drawString(e.getGuiGraphics().pose(), full, vec.x - (width) / 2, vec.y + 2f, new Color(255, 0, 0).getRGB());
                    }
                }
            }
        }
    }

    private String getItemLevel(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            CompoundTag tag = itemStack.getTag();
            if (tag.contains("display", 10)) {
                CompoundTag display = tag.getCompound("display");
                if (display.contains("Lore", 9)) {
                    ListTag lore = display.getList("Lore", 8);
                    for (int i = 0; i < lore.size(); i++) {
                        String line = lore.getString(i);
                        if (line.contains("Уровень")){
                            if (line.contains("MAX")) return "MAX";
                            Pattern pattern = Pattern.compile("(\\d+)/3");
                            Matcher matcher = pattern.matcher(line);
                            if (matcher.find()) return matcher.group(1) + "/3";
                        }
                    }
                }
            }
        }
        return "";
    }

    public static String extractTextFromJson(String jsonString) {
        StringBuilder resultText = new StringBuilder();
        int startIndex = jsonString.indexOf("Name:'");
        int endIndex = jsonString.indexOf("'}", startIndex);

        if (startIndex != -1 && endIndex != -1) {
            String nameData = jsonString.substring(startIndex + 6, endIndex);
            String[] parts = nameData.split("\\},\\{");

            for (String part : parts) {
                int textStart = part.indexOf("\"text\":\"");
                while (textStart != -1) {
                    int textEnd = part.indexOf("\"", textStart + 8);
                    if (textEnd != -1) {
                        String textValue = part.substring(textStart + 8, textEnd);
                        resultText.append(textValue);
                    }
                    textStart = part.indexOf("\"text\":\"", textEnd);
                }
            }
        }

        return resultText.toString();
    }


    @EventHandler
    public void render3D(RenderEvent3D e) {
        for (Entity ent : mc.level.getEntities().getAll()) {
            if (ent instanceof ItemEntity) {
                for (FuntimeItems item : FuntimeItems.values()) {
                    ItemStack stack = ((ItemEntity) ent).getItem();
                    if(stack.getItem() == item.getItem() || ent.getName().getString().equals(item.getName())) {
                        Vec3 vec = calculateFuturePosition((ItemEntity) ent, mc.level);
                        if (ent.onGround()) {
                            continue;
                        }
                        TestRender.drawLine(ent.position(), vec, new Color(255, 0, 0).getRGB());
                    }
                }
            }
        }
    }

    public static Vec3 calculateFuturePosition(ItemEntity item, Level level) {
        Vec3 position = item.position();
        Vec3 velocity = item.getDeltaMovement();

        final double gravity = -0.04;
        final double airResistance = 0.98;

        while (position.y > 0) {
            velocity = new Vec3(velocity.x * airResistance, velocity.y + gravity, velocity.z * airResistance);
            position = position.add(velocity);

            BlockPos blockPos = new BlockPos((int) position.x, (int) position.y, (int) position.z);
            if (!level.getBlockState(blockPos).isAir()) {
                return new Vec3(position.x, position.y, position.z);
            }
        }

        return new Vec3(position.x, 0, position.z);
    }




}
