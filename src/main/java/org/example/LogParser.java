package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogParser {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<LogRequest> parseLogs(List<Path> files) throws IOException {
        List<LogRequest> allLogs = new ArrayList<>();

        for (Path file : files) {
            List<String> lines = Files.readAllLines(file);
            for (String line : lines) {
                allLogs.add(parseLine(line));
            }
        }

        return allLogs;
    }

    private LogRequest parseLine(String line) {
        try {
            String timestamp = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
            LocalDateTime date = LocalDateTime.parse(timestamp, FORMATTER);

            String[] parts = line.substring(line.indexOf("]") + 2).split(" ");
            String user = parts[0];

            switch (parts[1]) {
                case "balance":
                    return new LogRequest(date.toLocalDate(), user, LogOptions.Operation.balance_inquiry,
                            Double.parseDouble(parts[3]), null);
                case "transferred":
                    double amount = Double.parseDouble(parts[2]);
                    String targetUser = parts[4];
                    return new LogRequest(date.toLocalDate(), user, LogOptions.Operation.transferred, amount, targetUser);
                case "withdrew":
                    return new LogRequest(date.toLocalDate(), user, LogOptions.Operation.withdrew,
                            Double.parseDouble(parts[2]), null);
                default:
                    throw new RuntimeException("Unknown operation: "+parts[1]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
