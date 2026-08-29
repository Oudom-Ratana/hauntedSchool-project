package com.khmerspirit.admin.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.khmerspirit.admin.model.RewardModel;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated File Service for Reward JSON persistence with automated backup before write.
 */
public class RewardFileService {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String RELATIVE_RESOURCE_PATH = "src/main/resources/rewards/rewards.json";
    private static final String RUNTIME_PATH = "rewards/rewards.json";
    private static final String CLASSPATH_RESOURCE = "/rewards/rewards.json";

    public List<RewardModel> loadRewards() {
        // 1. Try external/runtime file first
        File runtimeFile = new File(RUNTIME_PATH);
        if (runtimeFile.exists()) {
            List<RewardModel> rewards = readFromFile(runtimeFile);
            if (rewards != null && !rewards.isEmpty()) return rewards;
        }

        // 2. Try src/main/resources file next
        File srcFile = new File(RELATIVE_RESOURCE_PATH);
        if (srcFile.exists()) {
            List<RewardModel> rewards = readFromFile(srcFile);
            if (rewards != null && !rewards.isEmpty()) return rewards;
        }

        // 3. Fallback to classpath resource
        try (InputStream is = getClass().getResourceAsStream(CLASSPATH_RESOURCE)) {
            if (is != null) {
                try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    Type listType = new TypeToken<ArrayList<RewardModel>>() {}.getType();
                    List<RewardModel> rewards = gson.fromJson(reader, listType);
                    if (rewards != null) return rewards;
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading rewards from classpath: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    public boolean saveRewards(List<RewardModel> rewards) {
        if (rewards == null) return false;

        Path targetPath = resolveWritePath();
        try {
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }

            // Create backup file if existing file exists
            if (Files.exists(targetPath)) {
                Path backupPath = targetPath.getParent().resolve("rewards_backup.json");
                Files.copy(targetPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[RewardFileService] Created backup at: " + backupPath.toAbsolutePath());
            }

            // Write JSON
            String jsonContent = gson.toJson(rewards);
            Files.writeString(targetPath, jsonContent, StandardCharsets.UTF_8);
            System.out.println("[RewardFileService] Successfully saved rewards to: " + targetPath.toAbsolutePath());

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
            System.err.println("[RewardFileService] Error saving rewards: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private List<RewardModel> readFromFile(File file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<ArrayList<RewardModel>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            System.err.println("[RewardFileService] Failed to read " + file.getPath() + ": " + e.getMessage());
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
