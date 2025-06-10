package org.example;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            Path logsDir = Path.of(ClassLoader.getSystemResource("logs").toURI());

            FileReader reader = new FileReader();
            List<Path> logFiles = reader.listLogFiles(logsDir);

            LogParser parser = new LogParser();
            List<LogRequest> parsedLogs = parser.parseLogs(logFiles);

            LogMapping mapper = new LogMapping();
            Map<String, List<LogRequest>> userLogs = mapper.logMapping(parsedLogs);

            Path outputDir = Path.of("src/main", "transactions_by_users");
            Files.createDirectories(outputDir);
            java.nio.file.Files.createDirectories(outputDir);

            LogWriter writer = new LogWriter();
            writer.writeLogs(outputDir, userLogs);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
