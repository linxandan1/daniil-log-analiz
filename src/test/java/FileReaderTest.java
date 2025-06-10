import org.example.FileReader;
import org.example.LogOptions;
import org.example.LogRequest;
import org.example.LogMapping;
import org.example.LogWriter;
import org.junit.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class FileReaderTest {

    // Проверка, правильно ли читаются файлы: .log only
    @Test
    public void testListLogFiles() throws IOException {
        Path tempDirectory = Files.createTempDirectory("logs");
        Files.createFile(tempDirectory.resolve("a.log"));
        Files.createFile(tempDirectory.resolve("b.txt"));
        List<Path> logFiles = new FileReader().listLogFiles(tempDirectory);
        assertEquals(1, logFiles.size());
    }

    @Test
    public void testPassMappingLog() throws IOException {
        List<LogRequest> logs = List.of(
                new LogRequest(LocalDate.now(), "A", LogOptions.Operation.transferred, 100, "B"),
                new LogRequest(LocalDate.now(), "S", LogOptions.Operation.transferred, 100, "B"),
                new LogRequest(LocalDate.now(), "B", LogOptions.Operation.withdrew, 50, null)
        );
        Map<String, List<LogRequest>> map = new LogMapping().logMapping(logs);

        assertEquals(1, map.get("A").size());
        assertEquals(3, map.get("B").size());
    }

    @Test
    public void testFailMappingLog() throws IOException {
        List<LogRequest> logs = List.of(
                new LogRequest(LocalDate.now(), "A", LogOptions.Operation.transferred, 100, "B"),
                new LogRequest(LocalDate.now(), "S", LogOptions.Operation.transferred, 100, "B"),
                new LogRequest(LocalDate.now(), "B", LogOptions.Operation.withdrew, 50, null)
        );
        Map<String, List<LogRequest>> map = new LogMapping().logMapping(logs);

        assertNotEquals(100, map.get("A").size());
        assertNotEquals(100, map.get("B").size());
    }

    @Test
    public void testWriteLogs() throws IOException {
        LogRequest log1 = new LogRequest(LocalDate.of(2025, 5, 9), "userA", LogOptions.Operation.balance_inquiry, 1000, null);
        LogRequest log2 = new LogRequest(LocalDate.of(2025, 5, 10), "userB", LogOptions.Operation.transferred, 150.50, "userA");
        LogRequest log3 = new LogRequest(LocalDate.of(2025, 5, 11), "userA", LogOptions.Operation.withdrew, 200, null);

        List<LogRequest> initialLogs = Arrays.asList(log1, log2, log3);

        LogMapping mapper = new LogMapping();
        Map<String, List<LogRequest>> userLogsMap = mapper.logMapping(initialLogs);

        Path outputDir = Files.createTempDirectory("Test_logs");
        LogWriter writer = new LogWriter();
        writer.writeLogs(outputDir, userLogsMap);

        Path userAFile = outputDir.resolve("userA.log");
        assertTrue(Files.exists(userAFile));
        List<String> userALines = Files.readAllLines(userAFile);

        assertEquals(3, userALines.size());
        assertEquals(log1.toString(), userALines.get(0)); // 2025-05-09
        assertEquals(log3.toString(), userALines.get(2)); // 2025-05-11

        Path userBFile = outputDir.resolve("userB.log");
        assertTrue(Files.exists(userBFile));
        List<String> userBLines = Files.readAllLines(userBFile);
        assertEquals(1, userBLines.size());
        assertEquals(log2.toString(), userBLines.get(0)); // 2025-05-10
    }
    

}
