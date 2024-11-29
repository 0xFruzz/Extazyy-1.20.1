
package ru.fruzz.extazyy.main.commands.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.commands.Command;
import ru.fruzz.extazyy.main.commands.CommandInfo;
import ru.fruzz.extazyy.main.friends.Friend;
import ru.fruzz.extazyy.main.friends.FriendManager;
import ru.fruzz.extazyy.misc.util.ClientUtil;


@CommandInfo(name="friend", description="Используется для добавления в друзья.")
public class FriendCommand extends Command {

    @Override
    public void run(String[] args) throws Exception {
        if (args.length > 1) {
            switch (args[1]) {
                case "add": {
                    String friendName = args[2];
                    addFriend(friendName);
                    break;
                }
                case "remove": {
                    String friendName = args[2];
                    removeFriend(friendName);
                    break;
                }
                case "list": {
                    friendList();
                    break;
                }
            }
        } else {
            error();
        }
    }

    private void addFriend(String friendName) {
        FriendManager friendManager = Extazyy.friendmgr;
        if (friendName.contains(Minecraft.getInstance().player.getName().getString())) {
            ClientUtil.sendMessage("Вы не можете добавить сами себя :(");
            return;
        }
        if (friendManager.getFriends().stream().map(Friend::getName).toList().contains(friendName)) {
            ClientUtil.sendMessage(friendName + " уже находится в списке друзей!");
            return;
        }
        ClientUtil.sendMessage(friendName + " добавлен в друзья!");
        friendManager.addFriend(friendName);
    }

    private void removeFriend(String friendName) {
        FriendManager friendManager = Extazyy.friendmgr;
        if (friendManager.isFriend(friendName)) {
            friendManager.removeFriend(friendName);
            ClientUtil.sendMessage(friendName + " больше не ваш друг! Туда его!");
            return;
        }

        ClientUtil.sendMessage(friendName + " не находится в списке ваших друзей!");
    }

    private void friendList() {
        FriendManager friendManager = Extazyy.friendmgr;
        if (friendManager.getFriends().isEmpty()) {
            ClientUtil.sendMessage("У вас нет друзей! Выйдите на улицу!");
            return;
        }

        ClientUtil.sendMessage("Ваш список друзей:");
        for (Friend friend : friendManager.getFriends()) {
            ClientUtil.sendMessage(ChatFormatting.GRAY + friend.getName());
        }
    }



    @Override
    public void error() {
        ClientUtil.sendMessage(ChatFormatting.GRAY + "Список всех доступных комманд." + ChatFormatting.WHITE + ":");
        ClientUtil.sendMessage(ChatFormatting.WHITE + ".friend add " + ChatFormatting.GRAY + "" + ChatFormatting.RED + "name" + ChatFormatting.GRAY + "");
        ClientUtil.sendMessage(ChatFormatting.WHITE + ".friend remove " + ChatFormatting.GRAY + "" + ChatFormatting.RED + "name" + ChatFormatting.GRAY + "");
        ClientUtil.sendMessage(ChatFormatting.WHITE + ".friend list" + ChatFormatting.GRAY + " - Показывает весь список ваших друзей!");
    }
}
