package com.khmerspirit.admin.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.khmerspirit.admin.model.QuestionModel;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated File Service for Question JSON persistence with automated backup before write.
 */
public class QuestionFileService {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String RELATIVE_RESOURCE_PATH = "src/main/resources/questions/questions.json";
    private static final String RUNTIME_PATH = "questions/questions.json";
    private static final String CLASSPATH_RESOURCE = "/questions/questions.json";

    public List<QuestionModel> loadQuestions() {
        // 1. Try external/runtime file first
        File runtimeFile = new File(RUNTIME_PATH);
        if (runtimeFile.exists()) {
            List<QuestionModel> questions = readFromFile(runtimeFile);
            if (questions != null && !questions.isEmpty()) return questions;
        }

        // 2. Try src/main/resources file next
        File srcFile = new File(RELATIVE_RESOURCE_PATH);
        if (srcFile.exists()) {
            List<QuestionModel> questions = readFromFile(srcFile);
            if (questions != null && !questions.isEmpty()) return questions;
        }

        // 3. Fallback to classpath resource
        try (InputStream is = getClass().getResourceAsStream(CLASSPATH_RESOURCE)) {
            if (is != null) {
                try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    Type listType = new TypeToken<ArrayList<QuestionModel>>() {}.getType();
                    List<QuestionModel> questions = gson.fromJson(reader, listType);
                    if (questions != null) return questions;
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading questions from classpath: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    public boolean saveQuestions(List<QuestionModel> questions) {
        if (questions == null) return false;

        Path targetPath = resolveWritePath();
        try {
            // Ensure parent directory exists
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }

            // Create backup file if existing file exists
            if (Files.exists(targetPath)) {
                Path backupPath = targetPath.getParent().resolve("questions_backup.json");
                Files.copy(targetPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[QuestionFileService] Created backup at: " + backupPath.toAbsolutePath());
            }

            // Write JSON
            String jsonContent = gson.toJson(questions);
            Files.writeString(targetPath, jsonContent, StandardCharsets.UTF_8);
            System.out.println("[QuestionFileService] Successfully saved questions to: " + targetPath.toAbsolutePath());

            // Also mirror save to runtime path if target was src/main/resources
            if (!targetPath.toString().equals(RUNTIME_PATH)) {
                Path mirrorPath = Path.of(RUNTIME_PATH);
                if (mirrorPath.getParent() != null) {
                    Files.createDirectories(mirrorPath.getParent());
                }
                Files.writeString(mirrorPath, jsonContent, StandardCharsets.UTF_8);
            }

            return true;
        } catch (IOException e) {
            System.err.println("[QuestionFileService] Error saving questions: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private List<QuestionModel> readFromFile(File file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<ArrayList<QuestionModel>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            System.err.println("[QuestionFileService] Failed to read " + file.getPath() + ": " + e.getMessage());
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
