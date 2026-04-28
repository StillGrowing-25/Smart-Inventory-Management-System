package com.inventory.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final class CsvPathResolver {

    private CsvPathResolver() {
    }

    static String resolveDataFilePath(String fileName) {
        String userDir = System.getProperty("user.dir");

        Path[] candidates = new Path[] {
                Paths.get(userDir, "src", "main", "data", fileName),
                Paths.get(userDir, "smart-inventory", "src", "main", "data", fileName),
                Paths.get(userDir, "src", "main", "resources", "data", fileName),
                Paths.get(userDir, "smart-inventory", "src", "main", "resources", "data", fileName)
        };

        for (Path candidate : candidates) {
            if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                return candidate.toString();
            }
        }

        // Fall back to the standard module-relative path for clear error output.
        return candidates[0].toString();
    }
}
