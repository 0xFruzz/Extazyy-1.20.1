package ru.fruzz.extazyy.misc.util;

import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class KeyMappings {

    public static final Map<String, Integer> keyMap = new HashMap<>();
    public static final Map<Integer, String> reverseKeyMap = new HashMap<>();

    static {
        putMappings();
        reverseMappings();
    }
    public static int getKeyByName(String keyName) {
        for (int i = 30; i < GLFW.GLFW_KEY_LAST; i++) {
            String name = GLFW.glfwGetKeyName(i, -1);

            if (name != null && name.equalsIgnoreCase(keyName)) {
                return i;
            }
        }
        return -1;
    }


    private static void putMappings() {
        keyMap.put("A", GLFW.GLFW_KEY_A);
        keyMap.put("B", GLFW.GLFW_KEY_B);
        keyMap.put("C", GLFW.GLFW_KEY_C);
        keyMap.put("D", GLFW.GLFW_KEY_D);
        keyMap.put("E", GLFW.GLFW_KEY_E);
        keyMap.put("F", GLFW.GLFW_KEY_F);
        keyMap.put("G", GLFW.GLFW_KEY_G);
        keyMap.put("H", GLFW.GLFW_KEY_H);
        keyMap.put("I", GLFW.GLFW_KEY_I);
        keyMap.put("J", GLFW.GLFW_KEY_J);
        keyMap.put("K", GLFW.GLFW_KEY_K);
        keyMap.put("L", GLFW.GLFW_KEY_L);
        keyMap.put("M", GLFW.GLFW_KEY_M);
        keyMap.put("N", GLFW.GLFW_KEY_N);
        keyMap.put("O", GLFW.GLFW_KEY_O);
        keyMap.put("P", GLFW.GLFW_KEY_P);
        keyMap.put("Q", GLFW.GLFW_KEY_Q);
        keyMap.put("R", GLFW.GLFW_KEY_R);
        keyMap.put("S", GLFW.GLFW_KEY_S);
        keyMap.put("T", GLFW.GLFW_KEY_T);
        keyMap.put("U", GLFW.GLFW_KEY_U);
        keyMap.put("V", GLFW.GLFW_KEY_V);
        keyMap.put("W", GLFW.GLFW_KEY_W);
        keyMap.put("X", GLFW.GLFW_KEY_X);
        keyMap.put("Y", GLFW.GLFW_KEY_Y);
        keyMap.put("Z", GLFW.GLFW_KEY_Z);
        keyMap.put("0", GLFW.GLFW_KEY_0);
        keyMap.put("1", GLFW.GLFW_KEY_1);
        keyMap.put("2", GLFW.GLFW_KEY_2);
        keyMap.put("3", GLFW.GLFW_KEY_3);
        keyMap.put("4", GLFW.GLFW_KEY_4);
        keyMap.put("5", GLFW.GLFW_KEY_5);
        keyMap.put("6", GLFW.GLFW_KEY_6);
        keyMap.put("7", GLFW.GLFW_KEY_7);
        keyMap.put("8", GLFW.GLFW_KEY_8);
        keyMap.put("9", GLFW.GLFW_KEY_9);
        keyMap.put("F1", GLFW.GLFW_KEY_F1);
        keyMap.put("F2", GLFW.GLFW_KEY_F2);
        keyMap.put("F3", GLFW.GLFW_KEY_F3);
        keyMap.put("F4", GLFW.GLFW_KEY_F4);
        keyMap.put("F5", GLFW.GLFW_KEY_F5);
        keyMap.put("F6", GLFW.GLFW_KEY_F6);
        keyMap.put("F7", GLFW.GLFW_KEY_F7);
        keyMap.put("F8", GLFW.GLFW_KEY_F8);
        keyMap.put("F9", GLFW.GLFW_KEY_F9);
        keyMap.put("F10", GLFW.GLFW_KEY_F10);
        keyMap.put("F11", GLFW.GLFW_KEY_F11);
        keyMap.put("F12", GLFW.GLFW_KEY_F12);
        keyMap.put("NUM1", GLFW.GLFW_KEY_KP_1);
        keyMap.put("NUM2", GLFW.GLFW_KEY_KP_2);
        keyMap.put("NUM3", GLFW.GLFW_KEY_KP_3);
        keyMap.put("NUM4", GLFW.GLFW_KEY_KP_4);
        keyMap.put("NUM5", GLFW.GLFW_KEY_KP_5);
        keyMap.put("NUM6", GLFW.GLFW_KEY_KP_6);
        keyMap.put("NUM7", GLFW.GLFW_KEY_KP_7);
        keyMap.put("NUMPAD8", GLFW.GLFW_KEY_KP_8);
        keyMap.put("NUM9", GLFW.GLFW_KEY_KP_9);
        keyMap.put("SPACE", GLFW.GLFW_KEY_SPACE);
        keyMap.put("ENTER", GLFW.GLFW_KEY_ENTER);
        keyMap.put("ESC", GLFW.GLFW_KEY_ESCAPE);
        keyMap.put("HOME", GLFW.GLFW_KEY_HOME);
        keyMap.put("INS", GLFW.GLFW_KEY_INSERT);
        keyMap.put("DEL", GLFW.GLFW_KEY_DELETE);
        keyMap.put("END", GLFW.GLFW_KEY_END);
        keyMap.put("PGUP", GLFW.GLFW_KEY_PAGE_UP);
        keyMap.put("PGDOWN", GLFW.GLFW_KEY_PAGE_DOWN);
        keyMap.put("RIGHT", GLFW.GLFW_KEY_RIGHT);
        keyMap.put("LEFT", GLFW.GLFW_KEY_LEFT);
        keyMap.put("DOWN", GLFW.GLFW_KEY_DOWN);
        keyMap.put("UP", GLFW.GLFW_KEY_UP);
        keyMap.put("RSHIFT", GLFW.GLFW_KEY_RIGHT_SHIFT);
        keyMap.put("LSHIFT", GLFW.GLFW_KEY_LEFT_SHIFT);
        keyMap.put("RCTRL", GLFW.GLFW_KEY_RIGHT_CONTROL);
        keyMap.put("LCRTL", GLFW.GLFW_KEY_LEFT_CONTROL);
        keyMap.put("RALT", GLFW.GLFW_KEY_RIGHT_ALT);
        keyMap.put("LALT", GLFW.GLFW_KEY_LEFT_ALT);
        keyMap.put("RS", GLFW.GLFW_KEY_RIGHT_SUPER);
        keyMap.put("LS", GLFW.GLFW_KEY_LEFT_SUPER);
        keyMap.put("MENU", GLFW.GLFW_KEY_MENU);
        keyMap.put("CAPS", GLFW.GLFW_KEY_CAPS_LOCK);
        keyMap.put("NUM", GLFW.GLFW_KEY_NUM_LOCK);
        keyMap.put("SCROLL", GLFW.GLFW_KEY_SCROLL_LOCK);
        keyMap.put("KP_DECIMAL", GLFW.GLFW_KEY_KP_DECIMAL);
        keyMap.put("KP_DIVIDE", GLFW.GLFW_KEY_KP_DIVIDE);
        keyMap.put("KP_MULTIPLY", GLFW.GLFW_KEY_KP_MULTIPLY);
        keyMap.put("KP_SUBTRACT", GLFW.GLFW_KEY_KP_SUBTRACT);
        keyMap.put("KP_PLUS", GLFW.GLFW_KEY_KP_ADD);
        keyMap.put("KP_ENTER", GLFW.GLFW_KEY_KP_ENTER);
        keyMap.put("KP_EQUAL", GLFW.GLFW_KEY_KP_EQUAL);
        keyMap.put("'", GLFW.GLFW_KEY_APOSTROPHE);
        keyMap.put("/", GLFW.GLFW_KEY_SLASH);
        keyMap.put("-", GLFW.GLFW_KEY_MINUS);
        keyMap.put("+", GLFW.GLFW_KEY_EQUAL);
        keyMap.put("BACK", GLFW.GLFW_KEY_BACKSPACE);
        keyMap.put("BS", GLFW.GLFW_KEY_BACKSLASH);
        keyMap.put(".", GLFW.GLFW_KEY_PERIOD);
        keyMap.put("COMMA", GLFW.GLFW_KEY_COMMA);
        keyMap.put("PAUSE", GLFW.GLFW_KEY_PAUSE);

    }

    private static void reverseMappings() {
        for (Map.Entry<String, Integer> entry : keyMap.entrySet()) {
            reverseKeyMap.put(entry.getValue(), entry.getKey());
        }
    }
    public static String getNameByKey(int keyCode) {
        return reverseKeyMap.get(keyCode);
    }
    public static void addCustomMapping(String keyName, int keyCode) {
        keyMap.put(keyName, keyCode);
        reverseKeyMap.put(keyCode, keyName);
    }
    public static void removeMapping(String keyName) {
        Integer keyCode = keyMap.remove(keyName);
        if (keyCode != null) {
            reverseKeyMap.remove(keyCode);
        }
    }
    public static boolean hasMapping(String keyName) {
        return keyMap.containsKey(keyName);
    }
    public static Set<String> getAllKeys() {
        return Collections.unmodifiableSet(keyMap.keySet());
    }
    public static boolean isKeySymbol(String keyName) {
        return keyName.length() == 1;
    }
    public static void clearMappings() {
        keyMap.clear();
        reverseKeyMap.clear();
    }
    public static boolean isKeyNumeric(String keyName) {
        return keyName.matches("\\d+");
    }
    public static void updateMappings(Map<String, Integer> externalMappings) {
        keyMap.putAll(externalMappings);
        reverseMappings();
    }

}
