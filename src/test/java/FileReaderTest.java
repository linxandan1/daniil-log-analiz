import org.example.FileReader;
import org.example.LogOptions;
import org.example.LogRequest;
import org.example.LogMapping;
import org.example.LogWriter;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class FileReaderTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Проверка, правильно ли читаются файлы: .log only
    @Test
    public void testListLogFiles() throws IOException {
        Path tempDirectory = Files.createTempDirectory("logs");
        Files.createFile(tempDirectory.resolve("a.log"));
        Files.createFile(tempDirectory.resolve("b.txt"));
        List<Path> logFiles = new FileReader().listLogFiles(tempDirectory);
        assertEquals("Только 1 файл",1, logFiles.size());
    }

    @Test
    public void testPassMappingLog() throws IOException {
        List<LogRequest> logs = List.of(
                new LogRequest(LocalDate.now(), "A", LogOptions.Operation.transferred, 100, "B"),
                new LogRequest(LocalDate.now(), "S", LogOptions.Operation.transferred, 100, "B"),
                new LogRequest(LocalDate.now(), "B", LogOptions.Operation.withdrew, 50, null)
        );
        Map<String, List<LogRequest>> map = new LogMapping().logMapping(logs);

        assertEquals("у юзера А - 1 лог",1, map.get("A").size());
        assertEquals("у юзера В - 3 лога",3, map.get("B").size());
    }

    @Test
    public void testFailMappingLog() throws IOException {
        List<LogRequest> logs = List.of(
                new LogRequest(LocalDate.now(), "A", LogOptions.Operation.transferred, 100, "B"),
                new LogRequest(LocalDate.now(), "S", LogOptions.Operation.transferred, 100, "B"),
                new LogRequest(LocalDate.now(), "B", LogOptions.Operation.withdrew, 50, null)
        );
        Map<String, List<LogRequest>> map = new LogMapping().logMapping(logs);

        // обратная проверка, возможно не нужна, но пусть будет
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

        String expectedUserA_log1 = String.format("[%s] %s balance inquiry %.2f", LocalDate.of(2025, 5, 9).atStartOfDay().format(FORMATTER), "userA", 1000.00);
        String expectedUserA_received = String.format("[%s] %s received %.2f from %s", LocalDate.of(2025, 5, 10).atStartOfDay().format(FORMATTER), "userA", 150.50, "userB");
        String expectedUserA_log3 = String.format("[%s] %s withdrew %.2f", LocalDate.of(2025, 5, 11).atStartOfDay().format(FORMATTER), "userA", 200.00);

        assertEquals(4, userALines.size());
        assertEquals(expectedUserA_log1, userALines.get(0));
        assertEquals(expectedUserA_received, userALines.get(1));
        assertEquals(expectedUserA_log3, userALines.get(2));

        Path userBFile = outputDir.resolve("userB.log");
        assertTrue(Files.exists(userBFile));
        List<String> userBLines = Files.readAllLines(userBFile);

        String expectedUserB_log2 = String.format("[%s] %s transferred %.2f to %s", LocalDate.of(2025, 5, 10).atStartOfDay().format(FORMATTER), "userB", 150.50, "userA");

        assertEquals(2, userBLines.size());
        assertEquals(expectedUserB_log2, userBLines.get(0));
    }

    @Test
    public void testFinalBalanceCalculationAndAppendNoRegex() throws IOException {
        List<LogRequest> logsUserA = Arrays.asList(
                new LogRequest(LocalDate.of(2025, 5, 9), "userA", LogOptions.Operation.balance_inquiry, 1000.00, null),
                new LogRequest(LocalDate.of(2025, 5, 10), "userA", LogOptions.Operation.withdrew, 200.00, null),
                new LogRequest(LocalDate.of(2025, 5, 11), "userA", LogOptions.Operation.received, 150.50, "userB")
        );
        // Ожидаемый баланс: 1000.00 - 200.00 + 150.50 = 950.50
        BigDecimal expectedBalanceA = new BigDecimal("950.50");

        List<LogRequest> logsUserC = Arrays.asList(
                new LogRequest(LocalDate.of(2025, 5, 10), "userC", LogOptions.Operation.transferred, 50.00, "userD"),
                new LogRequest(LocalDate.of(2025, 5, 11), "userC", LogOptions.Operation.withdrew, 25.00, null)
        );
        // Ожидаемый баланс: 0 - 50.00 - 25.00 = -75.00
        BigDecimal expectedBalanceC = new BigDecimal("-75.00");

        Map<String, List<LogRequest>> userLogsMap = Map.of(
                "userA", logsUserA,
                "userC", logsUserC
        );

        Path outputDir = Files.createTempDirectory("Test_logs");
        LogWriter writer = new LogWriter();
        writer.writeLogs(outputDir, userLogsMap);

        // Проверка userA
        Path userAFile = outputDir.resolve("userA.log");
        List<String> userALines = Files.readAllLines(userAFile);
        String finalBalanceLineA = userALines.get(userALines.size() - 1);

        String expectedPartialLineA = String.format("userA final balance %.2f", expectedBalanceA.doubleValue());
        assertTrue("Должна быть строка финального баланса", finalBalanceLineA.contains(expectedPartialLineA));
        assertTrue("Дата должны быть текущей", finalBalanceLineA.startsWith("[2025-06"));


        // Проверка userC
        Path userCFile = outputDir.resolve("userC.log");
        List<String> userCLines = Files.readAllLines(userCFile);
        String finalBalanceLineC = userCLines.get(userCLines.size() - 1);

        String expectedPartialLineC = String.format("userC final balance %.2f", expectedBalanceC.doubleValue());
        assertTrue("Должна быть строка финального баланса", finalBalanceLineC.contains(expectedPartialLineC));
        assertTrue("Дата должны быть текущей", finalBalanceLineC.startsWith("[2025-06"));
    }

}
