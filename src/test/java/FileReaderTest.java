import org.example.FileReader;
import org.example.LogOptions;
import org.example.LogRequest;
import org.example.LogMapping;
import org.junit.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

    

}
