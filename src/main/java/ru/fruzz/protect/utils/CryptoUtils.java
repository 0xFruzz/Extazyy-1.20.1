package ru.fruzz.protect.utils;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

public class CryptoUtils {

    private static final String PASSWORD = "DENISPIDORAS";

    public static String encryptString(String text) {
        String cipherText = null;
        String salt = null;
        for (int i = 0; i < 10; i++) {
            salt = KeyGenerators.string().generateKey();
            TextEncryptor encryptor = Encryptors.text(PASSWORD, salt);
            cipherText = encryptor.encrypt(text);
        }
        return cipherText + salt;
    }

    public static String decryptString(String text) {
        String salt = text.substring(text.length() - 16);
        String decryptedText = null;
        for (int i = 0; i < 10; i++) {
            TextEncryptor encryptor = Encryptors.text(PASSWORD, salt);
            decryptedText = encryptor.decrypt(text.substring(0, text.length() - 16));
        }
        return decryptedText;
    }
}
