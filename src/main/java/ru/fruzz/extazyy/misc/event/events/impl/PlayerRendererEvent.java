package ru.fruzz.extazyy.misc.event.events.impl;

import lombok.Getter;
import net.minecraft.world.entity.player.Player;
import ru.fruzz.extazyy.misc.event.events.Event;

public class PlayerRendererEvent extends Event {

    @Getter
    private final Runnable runnable;
    @Getter
    private final Player player;


    public PlayerRendererEvent(Runnable runnable, Player player) {
        this.runnable = runnable;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
