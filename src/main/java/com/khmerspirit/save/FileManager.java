package com.khmerspirit.save;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class FileManager {

    public Properties loadProperties(Path path) {
        Properties properties = new Properties();
        if (!Files.exists(path)) {
            return properties;
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        } catch (IOException exception) {
            return new Properties();
        }
        return properties;
    }

    public void saveProperties(Path path, Properties properties) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream outputStream = Files.newOutputStream(path)) {
                properties.store(outputStream, "Khmer Spirit save data");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save game data to " + path, exception);
        }
    }

    public void deleteIfExists(Path path) {
        try {
            if (Files.exists(path)) Files.delete(path);
        } catch (IOException e) {
            // ignore
        }
    }
}

