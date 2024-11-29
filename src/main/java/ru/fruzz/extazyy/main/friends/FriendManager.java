package ru.fruzz.extazyy.main.friends;

import lombok.Getter;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    @Getter
    private final List<Friend> friends = new ArrayList<>();
    public static final File file = new File("C:\\Extazyy\\friends\\friends.ext");

    public void init()  {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } else {
                readFriends();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[ERROR] Ошибка при инициализации друзей. Возможное решение: удалите папку Extazyy на диске C.");
        }
    }

    public void addFriend(String name) {
        friends.add(new Friend(name));
        updateFile();
    }

    public boolean isFriend(String friend) {
        return friends.stream()
                .anyMatch(isFriend -> isFriend.getName().equals(friend));
    }

    public void removeFriend(String name) {
        friends.removeIf(friend -> friend.getName().equalsIgnoreCase(name));
        updateFile();
    }

    public void updateFile() {
        try {
            StringBuilder builder = new StringBuilder();
            friends.forEach(friend -> builder.append(friend.getName()).append("\n"));
            Files.write(file.toPath(), builder.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readFriends() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file.getAbsolutePath()))));
            String line;
            while ((line = reader.readLine()) != null) {
                friends.add(new Friend(line));
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}