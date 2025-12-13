package com.edusoftwerks.gwtboot.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PomUtils {

    public static String extractPackageFromPom(Path pomPath) throws IOException {
        String content = Files.readString(pomPath);

        // Try to extract from <start-class> tag
        Pattern pattern = Pattern.compile("<start-class>([a-zA-Z0-9_.]+)\\.Application</start-class>");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}
