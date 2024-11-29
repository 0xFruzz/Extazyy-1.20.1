package ru.fruzz.extazyy.misc.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.misc.font.FontRenderer;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static ru.fruzz.extazyy.Extazyy.mc;
@UtilityClass
public class Utils {

    public static String trimBeforeArrow(String input) {
        int index = input.indexOf("⇨");
        if (index != -1) {
            return input.substring(index + 1).trim();
        }
        return input;
    }

    public static double[] forward(final double d) {
        float f = mc.player.input.forwardImpulse;
        float f2 = mc.player.input.leftImpulse;
        float f3 = mc.player.getYRot();
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += ((f > 0.0f) ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += ((f > 0.0f) ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        final double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        final double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        final double d4 = f * d * d3 + f2 * d * d2;
        final double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }
    public void disableSprint() {
        mc.player.setSprinting(false);
        mc.options.keySprint.setDown(false);
        mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
    }

    public void enableSprint() {
        mc.player.setSprinting(true);
        mc.options.keySprint.setDown(true);
        mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
    }

    public static int getLevel(MobEffectInstance potion) {
        return potion.getAmplifier() + 1;
    }

    public  int getAxe() {
        for (int i = 0; i <= 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }

    public static String getPotionDurationString(MobEffectInstance effect, float durationFactor) {
        if (effect.isInfiniteDuration() || Mth.floor((float)effect.getDuration() * durationFactor) > 9600) {
            return "**:**";
        } else {
            int i = Mth.floor((float)effect.getDuration() * durationFactor);
            return ticksToElapsedTime(i);
        }
    }

    public static boolean isInWeb() {
        AABB pBox = mc.player.getBoundingBox();
        BlockPos pBlockPos = BlockPos.containing(mc.player.position());

        for (int x = pBlockPos.getX() - 2; x <= pBlockPos.getX() + 2; x++) {
            for (int y = pBlockPos.getY() - 1; y <= pBlockPos.getY() + 4; y++) {
                for (int z = pBlockPos.getZ() - 2; z <= pBlockPos.getZ() + 2; z++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    if (pBox.intersects(new AABB(bp)) && mc.level.getBlockState(bp).getBlock() == Blocks.COBWEB)
                        return true;
                }
            }
        }
        return false;
    }

    public static int findItemSlot(Item item, boolean armor) {
        if (armor) {
            for (ItemStack stack : mc.player.getInventory().armor) {
                if (stack.getItem() == item) {
                    return -2;
                }
            }
        }
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getItem(i);
            if (s.getItem() == item) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }

    public static void renderEntityInInventoryFollowsMouse(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float pMouseX, float pMouseY, LivingEntity pEntity) {
        float f = (float)Math.atan((double)(pMouseX / 40.0F));
        float f1 = (float)Math.atan((double)(pMouseY / 40.0F));
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(f1 * 20.0F * ((float)Math.PI / 180F));
        quaternionf.mul(quaternionf1);
        float f2 = pEntity.yBodyRot;
        float f3 = pEntity.getYRot();
        float f4 = pEntity.getXRot();
        float f5 = pEntity.yHeadRotO;
        float f6 = pEntity.yHeadRot;
        pEntity.yBodyRot = 180.0F + f * 20.0F;
        pEntity.setYRot(180.0F + f * 40.0F);
        pEntity.setXRot(-f1 * 20.0F);
        pEntity.yHeadRot = pEntity.getYRot();
        pEntity.yHeadRotO = pEntity.getYRot();
        renderEntityInInventory(pGuiGraphics, pX, pY, pScale, quaternionf, quaternionf1, pEntity);
        pEntity.yBodyRot = f2;
        pEntity.setYRot(f3);
        pEntity.setXRot(f4);
        pEntity.yHeadRotO = f5;
        pEntity.yHeadRot = f6;
    }

    public static int mirror(int number) {
        return -number;
    }

    public static void renderEntityInInventory(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, Quaternionf pPose, @Nullable Quaternionf pCameraOrientation, LivingEntity pEntity) {
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate((double)pX, (double)pY, 50.0D);
        pGuiGraphics.pose().mulPoseMatrix((new Matrix4f()).scaling((float)pScale, (float)pScale, (float)(-pScale)));
        pGuiGraphics.pose().mulPose(pPose);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (pCameraOrientation != null) {
            pCameraOrientation.conjugate();
            entityrenderdispatcher.overrideCameraOrientation(pCameraOrientation);
        }

        entityrenderdispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> {
            entityrenderdispatcher.render(pEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, pGuiGraphics.pose(), pGuiGraphics.bufferSource(), 15728880);
        });
        pGuiGraphics.flush();
        entityrenderdispatcher.setRenderShadow(true);
        pGuiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    public static String ticksToElapsedTime(int ticks) {
        int i = ticks / 20;
        int j = i / 60;
        i = i % 60;
        return i < 10 ? j + ":0" + i : j + ":" + i;
    }


    public static String calculateBPS() {
        return String.format("%.2f", Math.hypot(mc.player.getX() - mc.player.xo, mc.player.getZ() - mc.player.zo) * (double) mc.timer.timerSpeed * 20.0D);
    }


    public static byte[] readAllBytes(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        try {
            for (int len = is.read(buffer); len != -1; len = is.read(buffer))
                baos.write(buffer, 0, len);
        } catch (Exception ignored) {
        }

        return baos.toByteArray();
    }

    public static InputStream getResourceByURL(String str) {
        try {
            return downloadStream(str);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isArmor() {
        boolean armor = false;
        for (ItemStack itemStack : mc.player.getArmorSlots()) {
            if(!itemStack.isEmpty()) {
                armor = true;
                break;
            }
        }
        return armor;
    }


    public static InputStream downloadStream(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        URLConnection uc = url.openConnection();
        uc.addRequestProperty("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
        uc.connect();
        return uc.getInputStream();
    }

    public static ResourceLocation getCResource(String path) {
        if (path.charAt(0) == '/') {
            return new ResourceLocation("/extazyy" + path);
        } else {
            return new ResourceLocation("/extazyy/" + path);
        }
    }

    public static InputStream getCRStream(String string) {
        if (string.charAt(0) == '/') {
            return Minecraft.class.getResourceAsStream("/assets/minecraft/extazyy" + string);
        } else {
            return Minecraft.class.getResourceAsStream("/assets/minecraft/extazyy/" + string);
        }
    }

    public static List<Module> getSorted(FontRenderer font) {
        List<Module> modules = Extazyy.moduleManager.getFunctions();
        modules.sort(Comparator.comparing(module -> {
            float width = font.getStringWidth(module.name) + 4;
            return -width;
        }));
        return modules;
    }

    public static String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line).append('\n');

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static int getTextureId(ResourceLocation identifier) {
        AbstractTexture abstractTexture = mc.getTextureManager().getTexture(identifier);
        if (abstractTexture == null) {
            abstractTexture = new SimpleTexture(identifier);
            mc.getTextureManager().register(identifier, abstractTexture);
        }
        return abstractTexture.getId();
    }

    public static String currentAnarchy() {
        String[] split = mc.gui.getTabList().header.getString().split("Режим: ");
        return split.length < 2 ? "none" : split[1].replaceAll("Анархия-", "");
    }

    public static void copyToClipboard(String text) {
        try {
            String[] cmd = new String[]{"cmd", "/c", "echo", text, "|", "clip"};
            Runtime.getRuntime().exec(cmd);
        } catch (IOException var2) {
            throw new RuntimeException(var2);
        }
    }

    public static void sendChat(String text) {
        mc.player.connection.sendChat(text);
    }

    public static String trimString(String input) {
        return input.length() > 7 ? input.substring(0, 12) : input;
    }

    public static String getFuntimeHealth(Entity entity) {


        return "0";
    }


    public static String remainingVulcan;
    public static String checkAndStartCountdown(String input) {
        if (input.contains("Еще не активирован, до извержения") && remainingVulcan == null) {
            String secondsString = input.replaceAll("[^0-9]", "");
            int seconds = Integer.parseInt(secondsString);
            System.out.println(seconds);
            if(remainingVulcan == null) {
                startCountdown(seconds);
            }
            return "До извержения: " + remainingVulcan;
        }
        return null;
    }

    private static Timer timer;
    private static void startCountdown(int seconds) {
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            int remainingSeconds = seconds;
            @Override
            public void run() {
                if (remainingSeconds > 0) {
                    remainingVulcan = "До извержения: " + remainingSeconds;
                    remainingSeconds--;
                } else {
                    remainingVulcan = null;
                    timer.cancel();
                }
            }
        }, 0, 1000);
    }


}
