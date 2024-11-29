package ru.fruzz.extazyy.main.modules.impl.combat;

import lombok.Generated;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.math.MathHelpper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.time.StopWatch;
import org.joml.Vector2f;
import org.joml.Vector3d;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.modules.tools.Tools;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.*;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.*;

import java.awt.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.misc.util.Utils;
import ru.fruzz.extazyy.misc.util.math.MathHandler;





@ModuleAnnotation(name = "AttackAura", type = CategoryUtil.Combat, icon = "A", risk = true)
public class Aura extends Module {

    //Приколы
    public Vector2f currentRotation = new Vector2f(0f,0f);
    public LivingEntity target = null;


    //Сеттинги епта
    private ModeTools currentRotationMode = new ModeTools("Мод ротации", "Default", "Default");
    private ModeTools attackSpeedMode = new ModeTools("Тип атаки", "1.9", "1.9", "1.8");
    private MultiBoxTools targets = new MultiBoxTools("Цели",
            new BooleanOption("Players", false),
            new BooleanOption("Mobs", false),
            new BooleanOption("Animals", false));
    private ModeTools sortMode = new ModeTools("Метод сортировки", "По здоровью", "По здоровью", "По дистанции");
    private BooleanOption attackNaked = new BooleanOption("Бить голых", true).setVisible(() -> targets.get("Players"));
    private BooleanOption attackFriends = new BooleanOption("Бить друзей", true).setVisible(() -> targets.get("Players"));
    private NumberTools distance = new NumberTools("Дистанция атаки", 3, 1, 6,0.1f);

    //Супер ауры жоский
    public Aura() {
        addSettings(
                currentRotationMode, attackSpeedMode,
                targets,
                sortMode,
                distance,
                attackNaked,
                attackFriends);
    }

    @EventHandler
    public void update(TickEvent e) {
        if(target == null || !isTargetValid(target)) {
            findTarget();
            if(target == null) {
                currentRotation = new Vector2f(mc.player.getXRot(), mc.player.getYRot());
                return;
            }

        }
    }

    @EventHandler
    public void rotate(RotationEvent event) {
        if(target != null) {
            event.setYRot(currentRotation.y);
            event.setXRot(currentRotation.x);
        }
    }



    private void findTarget() {
        ArrayList<LivingEntity> AllTargets = new ArrayList<>();
        for(Entity entity : mc.level.getEntities().getAll()) {
            if(!(entity instanceof LivingEntity)) continue;
            if(!isTargetValid((LivingEntity) entity)) continue;

            AllTargets.add((LivingEntity) entity);
        }
        if(AllTargets.size() > 1) {
            switch (sortMode.get()) {
                case "По здоровью":
                    AllTargets.sort(Comparator.comparingDouble(this::getEntityHealth).thenComparingDouble(mc.player::distanceTo));
                    break;
                case "По дистанции":
                    AllTargets.sort(Comparator.comparingDouble(this::getDist).thenComparingDouble(this::getEntityHealth));
            }
        }
        target = AllTargets.get(0);
    }

    private boolean isTargetValid(LivingEntity target) {
        if(Extazyy.getModuleManager().getAntiBot().isBot(target))
            return false;
        if(target.isDeadOrDying() || !target.isAlive() || target == mc.player)
            return false;
        if(target instanceof Player && !targets.get("Players"))
            return false;
        if(target.getArmorValue() == 0 && !attackNaked.get())
            return false;
        if(Extazyy.getFriendmgr().isFriend(target.getName().getString()) && !attackFriends.get())
            return false;
        if(target instanceof Animal && !targets.get("Animals"))
            return false;
        if(target instanceof Mob && !targets.get("Mobs"))
            return false;
        if(target instanceof ArmorStand)
            return false;
        return getDist(target) <= distance.getValue().floatValue();
    }

    public double getDist(LivingEntity target) {
        double wHalf = target.getBbWidth() / 2.0f;
        double yExpand = MathHelpper.clamp(target.getEyeY() - target.getY(), 0.0, target.getBbHeight());
        double xExpand = MathHelpper.clamp(mc.player.getX() - target.getX(), -wHalf, wHalf);
        double zExpand = MathHelpper.clamp(mc.player.getZ() - target.getZ(), -wHalf, wHalf);
        return new Vector3d(target.getX() - mc.player.getX() + xExpand, target.getY() - mc.player.getEyeY() + yExpand, target.getZ() - mc.player.getZ() + zExpand).length();
    }

    public double getEntityHealth(Entity ent) {
        if (ent instanceof Player) {
            Player player = (Player)ent;
            double armorValue = 1;
            return (double)(player.getHealth() + player.getAbsorptionAmount()) * armorValue;
        }
        if (ent instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)ent;
            return livingEntity.getHealth() + livingEntity.getAbsorptionAmount();
        }
        return 0.0;
    }




}