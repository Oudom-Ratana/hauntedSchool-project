package com.khmerspirit.admin.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.khmerspirit.admin.model.AdminUser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Dedicated File Service for Admin Config JSON persistence & authentication.
 */
public class AdminFileService {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String RELATIVE_RESOURCE_PATH = "src/main/resources/config/admin.json";
    private static final String RUNTIME_PATH = "config/admin.json";
    private static final String CLASSPATH_RESOURCE = "/config/admin.json";

    public AdminUser loadAdminUser() {
        // 1. Try external/runtime file first
        File runtimeFile = new File(RUNTIME_PATH);
        if (runtimeFile.exists()) {
            AdminUser user = readFromFile(runtimeFile);
            if (user != null) return user;
        }

        // 2. Try src/main/resources file next
        File srcFile = new File(RELATIVE_RESOURCE_PATH);
        if (srcFile.exists()) {
            AdminUser user = readFromFile(srcFile);
            if (user != null) return user;
        }

        // 3. Fallback to classpath resource
        try (InputStream is = getClass().getResourceAsStream(CLASSPATH_RESOURCE)) {
            if (is != null) {
                try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    AdminUser user = gson.fromJson(reader, AdminUser.class);
                    if (user != null) return user;
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading admin user from classpath: " + e.getMessage());
        }

        // Hardcode fallback default prototype credentials
        return new AdminUser("admin", "admin123", "SuperAdmin");
    }

    public boolean saveAdminUser(AdminUser adminUser) {
        if (adminUser == null) return false;

        Path targetPath = resolveWritePath();
        try {
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }

            // Create backup file if existing file exists
            if (Files.exists(targetPath)) {
                Path backupPath = targetPath.getParent().resolve("admin_backup.json");
                Files.copy(targetPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[AdminFileService] Created backup at: " + backupPath.toAbsolutePath());
            }

            // Write JSON
            String jsonContent = gson.toJson(adminUser);
            Files.writeString(targetPath, jsonContent, StandardCharsets.UTF_8);
            System.out.println("[AdminFileService] Successfully saved admin config to: " + targetPath.toAbsolutePath());

            // Mirror save to runtime path if target was src/main/resources
            if (!targetPath.toString().equals(RUNTIME_PATH)) {
                Path mirrorPath = Path.of(RUNTIME_PATH);
                if (mirrorPath.getParent() != null) {
                    Files.createDirectories(mirrorPath.getParent());
                }
                Files.writeString(mirrorPath, jsonContent, StandardCharsets.UTF_8);
            }

            return true;
        } catch (IOException e) {
            System.err.println("[AdminFileService] Error saving admin user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) return false;
        AdminUser configuredUser = loadAdminUser();
        if (configuredUser != null && configuredUser.getUsername() != null && configuredUser.getPassword() != null) {
            if (configuredUser.getUsername().trim().equalsIgnoreCase(username.trim()) &&
                configuredUser.getPassword().trim().equals(password.trim())) {
                return true;
            }
        }
        // Fallback checks for common prototype defaults
        if (username.trim().equalsIgnoreCase("admin") &&
            (password.trim().equals("admin") || password.trim().equals("admin123") || password.trim().equals("123456"))) {
            return true;
        }
        if (username.trim().equalsIgnoreCase("teacher") &&
            (password.trim().equals("teacher") || password.trim().equals("teacher123"))) {
            return true;
        }
        return false;
    }

    private AdminUser readFromFile(File file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, AdminUser.class);
        } catch (Exception e) {
            System.err.println("[AdminFileService] Failed to read " + file.getPath() + ": " + e.getMessage());
            return null;
        }
    }

    private Path resolveWritePath() {
        Path srcPath = Path.of(RELATIVE_RESOURCE_PATH);
        if (Files.exists(srcPath.getParent())) {
            return srcPath;
        }
        return Path.of(RUNTIME_PATH);
    }
}
