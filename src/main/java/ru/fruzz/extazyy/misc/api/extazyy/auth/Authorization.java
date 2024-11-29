package ru.fruzz.extazyy.misc.api.extazyy.auth;



import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

/**
 * @author itskekoff
 * @since 0:31 of 04.11.2024
 */
public class Authorization {
    private static final String SERVER_URL = "https://extazyy.xyz/";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
/*
    @SneakyThrows
    public boolean checkHWID(String hwid) {
        String url = HttpUrl.get(SERVER_URL + "/api/check")
                .newBuilder()
                .addQueryParameter("hwid", hwid)
                .build()
                .toString();
        String response = sendRequest(url);
        if (!response.isEmpty()) {
            String decrypted = decryptResponse(response);
            JsonNode jsonResponse = objectMapper.readTree(decrypted);
            return "ok".equals(jsonResponse.get("status").asText());
        }
        return false;
    }

    @SneakyThrows
    public boolean authenticateUser(String login, String password, String hwid) {
        String url = HttpUrl.get(SERVER_URL + "/api/auth")
                .newBuilder()
                .addQueryParameter("login", login)
                .addQueryParameter("password", password)
                .addQueryParameter("hwid", hwid)
                .build()
                .toString();
        String response = sendRequest(url);
        if (!response.isEmpty()) {
            String decrypted = decryptResponse(response);
            JsonNode jsonResponse = objectMapper.readTree(decrypted);
            return "ok".equals(jsonResponse.get("status").asText());
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private String sendRequest(String url) throws IOException {
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request request = new Request.Builder()
                .url(url).post(emptyBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }



    public String decryptResponse(String response) {
        try {
            String apiKey = "std::sex<<std::endl";
            JSONObject jsonResponse = new JSONObject(response);
            String data = jsonResponse.getString("data");
            String key = jsonResponse.getString("key");

            StringBuilder decryptedData = new StringBuilder();
            int[] xorKey = new int[key.length()];

            for (int i = 0; i < key.length(); i++) {
                xorKey[i] = key.charAt(i) ^ apiKey.charAt(i % apiKey.length());
            }
            for (int i = 0; i < data.length(); i++) {
                decryptedData.append((char) (data.charAt(i) ^ xorKey[i % xorKey.length]));
            }

            return decryptedData.toString();

        } catch (Exception e) {
            return null;
        }
    }*/
}
