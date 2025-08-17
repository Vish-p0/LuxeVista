package com.example.luxevista;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Security Manager for handling passcode storage, encryption, and security preferences.
 * Uses simple encryption for security questions/answers and plain storage for passcode.
 */
public class SecurityManager {
    
    private static final String PREFS_NAME = "luxevista_security";
    private static final String KEY_SECURITY_ENABLED = "security_enabled";
    private static final String KEY_PASSCODE = "passcode";
    private static final String KEY_SECURITY_QUESTION = "security_question";
    private static final String KEY_SECURITY_ANSWER = "security_answer_encrypted";
    private static final String KEY_ENCRYPTION_KEY = "encryption_key";
    
    private final SharedPreferences prefs;
    private final Context context;
    
    public SecurityManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // Security Toggle
    public boolean isSecurityEnabled() {
        return prefs.getBoolean(KEY_SECURITY_ENABLED, false);
    }
    
    public void setSecurityEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SECURITY_ENABLED, enabled).apply();
    }
    
    // Passcode Management
    public void setPasscode(String passcode) {
        prefs.edit().putString(KEY_PASSCODE, passcode).apply();
    }
    
    public boolean verifyPasscode(String inputPasscode) {
        String storedPasscode = prefs.getString(KEY_PASSCODE, null);
        return storedPasscode != null && storedPasscode.equals(inputPasscode);
    }
    
    public boolean hasPasscode() {
        return prefs.getString(KEY_PASSCODE, null) != null;
    }
    
    // Security Question Management
    public void setSecurityQuestion(String question, String answer) {
        try {
            String encryptedAnswer = encryptAnswer(answer);
            prefs.edit()
                    .putString(KEY_SECURITY_QUESTION, question)
                    .putString(KEY_SECURITY_ANSWER, encryptedAnswer)
                    .apply();
        } catch (Exception e) {
            // Fallback to plain storage if encryption fails
            prefs.edit()
                    .putString(KEY_SECURITY_QUESTION, question)
                    .putString(KEY_SECURITY_ANSWER, answer)
                    .apply();
        }
    }
    
    public String getSecurityQuestion() {
        return prefs.getString(KEY_SECURITY_QUESTION, null);
    }
    
    public boolean verifySecurityAnswer(String inputAnswer) {
        String storedAnswer = prefs.getString(KEY_SECURITY_ANSWER, null);
        if (storedAnswer == null) return false;
        
        try {
            String decryptedAnswer = decryptAnswer(storedAnswer);
            return decryptedAnswer != null && decryptedAnswer.trim().equalsIgnoreCase(inputAnswer.trim());
        } catch (Exception e) {
            // Fallback to plain comparison if decryption fails
            return storedAnswer.trim().equalsIgnoreCase(inputAnswer.trim());
        }
    }
    
    public boolean hasSecurityQuestion() {
        return prefs.getString(KEY_SECURITY_QUESTION, null) != null;
    }
    
    // Clear all security data
    public void clearSecurityData() {
        prefs.edit()
                .remove(KEY_SECURITY_ENABLED)
                .remove(KEY_PASSCODE)
                .remove(KEY_SECURITY_QUESTION)
                .remove(KEY_SECURITY_ANSWER)
                .remove(KEY_ENCRYPTION_KEY)
                .apply();
    }
    
    // Simple encryption for security answers
    private String encryptAnswer(String answer) throws Exception {
        SecretKey key = getOrCreateEncryptionKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        
        byte[] iv = cipher.getIV();
        byte[] encryptedData = cipher.doFinal(answer.getBytes());
        
        // Combine IV and encrypted data
        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
        
        return Base64.encodeToString(combined, Base64.DEFAULT);
    }
    
    private String decryptAnswer(String encryptedAnswer) throws Exception {
        SecretKey key = getOrCreateEncryptionKey();
        byte[] combined = Base64.decode(encryptedAnswer, Base64.DEFAULT);
        
        // Extract IV and encrypted data
        byte[] iv = Arrays.copyOfRange(combined, 0, 12); // GCM standard IV size
        byte[] encryptedData = Arrays.copyOfRange(combined, 12, combined.length);
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        
        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData);
    }
    
    private SecretKey getOrCreateEncryptionKey() {
        String encodedKey = prefs.getString(KEY_ENCRYPTION_KEY, null);
        
        if (encodedKey == null) {
            // Generate new key
            byte[] keyBytes = new byte[32]; // 256 bits
            new SecureRandom().nextBytes(keyBytes);
            encodedKey = Base64.encodeToString(keyBytes, Base64.DEFAULT);
            prefs.edit().putString(KEY_ENCRYPTION_KEY, encodedKey).apply();
        }
        
        byte[] keyBytes = Base64.decode(encodedKey, Base64.DEFAULT);
        return new SecretKeySpec(keyBytes, "AES");
    }
}
