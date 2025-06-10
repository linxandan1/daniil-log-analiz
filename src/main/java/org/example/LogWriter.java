package org.example;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LogWriter {

    private static final DateTimeFormatter FINAL_BALANCE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
                finalBalance(w, user, list);
            }
        }
    }

    private void finalBalance(BufferedWriter writer, String user, List<LogRequest> logs) throws IOException {
        BigDecimal balance = BigDecimal.ZERO;
        boolean initialBalanceSet = false;

        for (LogRequest log : logs) {
            if (log.getOperationType() == LogOptions.Operation.balance_inquiry) {
                balance = log.getAmount();
                initialBalanceSet = true;
                break;
            }
        }

        if (!initialBalanceSet) {
            balance = BigDecimal.ZERO;
        }

        for (LogRequest log : logs) {
            switch (log.getOperationType()) {
                case transferred:
                case withdrew:
                    if (log.getUser().equals(user)) {
                        balance = balance.subtract(log.getAmount());
                    }
                    break;
                case received:
                    if (log.getUser().equals(user)) {
                        balance = balance.add(log.getAmount());
                    }
                    break;
            }
        }

        String now = LocalDateTime.now().format(FINAL_BALANCE_FORMATTER);
        String finalBalanceLine = String.format("[%s] %s final balance %.2f", now, user, balance);
        writer.write(finalBalanceLine);
        writer.newLine();
    }
}
