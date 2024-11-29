package ru.fruzz.extazyy.info;


import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import ru.fruzz.extazyy.Extazyy;

public class Discord {
    private static final DiscordRPC discordRPC = DiscordRPC.INSTANCE;
    private static final String discordID = "1271160279794847918";
    private static final DiscordRichPresence discordRichPresence = new DiscordRichPresence();
    public static void stopRPC() {
        discordRPC.Discord_Shutdown();
    }
    public static void startRPC() {
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        discordRPC.Discord_Initialize(discordID, eventHandlers, true, null);
        Discord.discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
        Discord.discordRichPresence.largeImageKey = "extazyy";
        Discord.discordRichPresence.largeImageText = "Buy >> dsc.gg/extz";
        new Thread(() -> {
            while (true) {
                Discord.discordRichPresence.details = "Role: > " + Extazyy.userInfo.getRole() + " | UID: " + Extazyy.userInfo.getUid();
                Discord.discordRichPresence.state = "Build: > " + Extazyy.buildInfo.getBuildname() + " | v" + Extazyy.buildInfo.getVersion();
                discordRPC.Discord_UpdatePresence(discordRichPresence);
                try {
                    Thread.sleep(9999);
                } catch (InterruptedException e) {
                }

            }
        }).start();
    }
}


