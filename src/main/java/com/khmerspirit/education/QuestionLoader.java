package com.khmerspirit.education;

import com.khmerspirit.config.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads simple text-based question files from resources/questions/{roomId}.txt
 * File format for each question block (blocks separated by an empty line):
 * Q: question text
 * A: option1
 * B: option2
 * C: option3
 * D: option4
 * Answer: 1   (1-based index: 1..4)
 */
public class QuestionLoader {

    public List<Question> loadQuestionsForRoom(String roomId) {
        // Prefer external questions directory so teachers can edit files without repackaging
        Path externalTxt = Path.of("questions", roomId + ".txt");
        Path externalJson = Path.of("questions", roomId + ".json");
        try {
            if (Files.exists(externalJson)) {
                String content = Files.readString(externalJson, StandardCharsets.UTF_8);
                List<Question> fromJson = parseJson(content);
                if (!fromJson.isEmpty()) return fromJson;
            }
            if (Files.exists(externalTxt)) {
                List<String> lines = Files.readAllLines(externalTxt, StandardCharsets.UTF_8);
                List<Question> parsed = parseBlocks(lines);
                if (!parsed.isEmpty()) return parsed;
            }
        } catch (IOException e) {
            // fall back to classpath
        }

        String resourcePath = "/questions/" + roomId + ".txt";
        InputStream stream = QuestionLoader.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            return loadDefaultQuestions();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            return parseBlocks(lines);
        } catch (IOException e) {
            return loadDefaultQuestions();
        }
    }

    private List<Question> parseBlocks(List<String> lines) {
        List<Question> questions = new ArrayList<>();
        List<String> block = new ArrayList<>();
        for (String raw : lines) {
            String line = raw.strip();
            if (line.isEmpty()) {
                if (!block.isEmpty()) {
                    Question q = parseBlock(block);
                    if (q != null) questions.add(q);
                    block.clear();
                }
            } else {
                block.add(line);
            }
        }
        if (!block.isEmpty()) {
            Question q = parseBlock(block);
            if (q != null) questions.add(q);
        }
        return questions;
    }

    private Question parseBlock(List<String> block) {
        try {
            String qText = null;
            List<String> opts = new ArrayList<>();
            int answer = 0;
            for (String line : block) {
                if (line.startsWith("Q:")) {
                    qText = line.substring(2).trim();
                } else if (line.startsWith("A:") || line.startsWith("B:") || line.startsWith("C:") || line.startsWith("D:")) {
                    opts.add(line.substring(2).trim());
                } else if (line.toLowerCase().startsWith("answer:")) {
                    String v = line.substring(7).trim();
                    answer = Integer.parseInt(v) - 1; // file uses 1-based
                }
            }
            if (qText != null && opts.size() >= 2) {
                return new Question(qText, opts, Math.max(0, Math.min(answer, opts.size() - 1)));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<Question> loadDefaultQuestions() {
        List<Question> list = new ArrayList<>();
        List<String> opts = List.of("Option 1", "Option 2", "Option 3", "Option 4");
        list.add(new Question("What is 2 + 2?", opts, 1));
        list.add(new Question("What color is the sky?", opts, 0));
        return list;
    }

    // Very small JSON parser for our teacher-written files (expects the writer's format)
    private List<Question> parseJson(String content) {
        List<Question> result = new ArrayList<>();
        String lower = content.trim();
        if (!lower.startsWith("{")) return result;
        int idx = content.indexOf("\"questions\"");
        if (idx < 0) return result;
        int arrStart = content.indexOf('[', idx);
        int arrEnd = content.indexOf(']', arrStart);
        if (arrStart < 0 || arrEnd < 0) return result;
        String arrayBody = content.substring(arrStart + 1, arrEnd);
        String[] items = arrayBody.split("\\},\\s*\\{");
        for (String item : items) {
            String obj = item.trim();
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";
            String text = extractJsonString(obj, "text");
            List<String> opts = extractJsonArray(obj, "options");
            int answer = 0;
            try { answer = Integer.parseInt(extractJsonString(obj, "answer")); } catch (Exception ignored) {}
            if (text != null && opts != null && !opts.isEmpty()) {
                result.add(new Question(text, opts, Math.max(0, Math.min(answer, opts.size() - 1))));
            }
        }
        return result;
    }

    private String extractJsonString(String obj, String key) {
        int i = obj.indexOf('"' + key + '"');
        if (i < 0) return null;
        int colon = obj.indexOf(':', i);
        if (colon < 0) return null;
        int firstQuote = obj.indexOf('"', colon);
        if (firstQuote < 0) return null;
        int secondQuote = obj.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) return null;
        return obj.substring(firstQuote + 1, secondQuote);
    }

    private List<String> extractJsonArray(String obj, String key) {
        int i = obj.indexOf('"' + key + '"');
        if (i < 0) return null;
        int colon = obj.indexOf(':', i);
        if (colon < 0) return null;
        int start = obj.indexOf('[', colon);
        int end = obj.indexOf(']', start);
        if (start < 0 || end < 0) return null;
        String body = obj.substring(start + 1, end);
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inString = false;
        for (int p = 0; p < body.length(); p++) {
            char c = body.charAt(p);
            if (c == '"') { inString = !inString; continue; }
            if (!inString && c == ',') { result.add(cur.toString().trim()); cur.setLength(0); continue; }
            if (inString) cur.append(c);
        }
        if (cur.length() > 0) result.add(cur.toString().trim());
        return result;
    }
}