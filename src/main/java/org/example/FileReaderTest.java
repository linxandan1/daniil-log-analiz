package org.example;

import org.junit.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

    

}
