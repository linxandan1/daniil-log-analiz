package org.example;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LogWriter {
    public void writeLogs(Path outDir, Map<String, List<LogRequest>> map) throws IOException {
        for (Map.Entry<String, List<LogRequest>> entry : map.entrySet()) {
            String user = entry.getKey();
            List<LogRequest> list = entry.getValue();
            list.sort(Comparator.comparing(LogRequest::getDate));
            try (BufferedWriter w = Files.newBufferedWriter(outDir.resolve(user + ".log"))) {
                for (LogRequest e : list) {
                    w.write(e.toString());
                    w.newLine();
                }
            }
        }
    }
}
