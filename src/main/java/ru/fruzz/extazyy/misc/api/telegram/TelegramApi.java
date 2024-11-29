package ru.fruzz.extazyy.misc.api.telegram;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.misc.api.extazyy.utils.packet.Packet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TelegramApi {

    //CONSTANTS
    @Getter
    private String TOKEN;
    @Getter
    private String CHAT_ID;
    private int LAST_MESSAGE;

    public TelegramApi(String token, String chatid) {
        System.out.println("[Extazyy] TelegramApi inited! Current data token: " + token + " chatid:" + chatid);
        TOKEN = token;
        CHAT_ID = chatid;


        startUpdates();
    }

    public void startUpdates() {
        ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);
        schedule.scheduleAtFixedRate(() -> {
            if(TOKEN.length() == 46 && CHAT_ID.length() == 10) return;

            JsonArray jsonArray = getUpdates();
            Iterator iter = jsonArray.iterator();

            while (iter.hasNext()) {
                JsonElement jsonElement = (JsonElement) iter.next();
                JsonObject object = jsonElement.getAsJsonObject();

                LAST_MESSAGE = object.get("update_id").getAsInt();

                JsonObject messageObject = object.get("message").getAsJsonObject();
                String id = messageObject.get("from").getAsJsonObject().get("id").getAsString();
                String m = messageObject.get("text").getAsString();
                System.out.println(m);
                if(!id.contains(getCHAT_ID())) return;

                Extazyy.apiCommandManager.execute(m);
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    private JsonArray getUpdates() {
        AtomicReference<JsonArray> jsonArray = new AtomicReference<>(new JsonArray());
        Packet packet = Packet.newInstance("https://api.telegram.org", (connection) -> {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();

            String inputLine;
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            JsonObject object = (new JsonParser()).parse(response.toString()).getAsJsonObject();
            jsonArray.set(object.getAsJsonArray("result"));
        }, String.format("bot%s", getTOKEN()), "getUpdates?offset=" + (LAST_MESSAGE + 1));
        packet.send();
        return jsonArray.get();
    }

    public void sendMessage(String msg, String chatId) {
        String encodedMessage = URLEncoder.encode(msg, StandardCharsets.UTF_8);
        Packet.newInstance("https://api.telegram.org",
                (connection) -> {},
                String.format("bot%s", getTOKEN()), "sendMessage?chat_id=" + chatId + "&text=" + encodedMessage)
                .setPost(true)
                .send();
    }

    public void sendMessage(String msg) {
        this.sendMessage(msg, getCHAT_ID());
    }

}
