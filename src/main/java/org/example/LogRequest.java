package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogRequest {
    private LocalDateTime date;
    private String user;
    private LogOptions.Operation operationType;
    private BigDecimal amount;
    private String targetUser;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LogRequest(LocalDate date, String user, LogOptions.Operation operationType, double amount, String targetUser) {
        this.date = date.atStartOfDay();
        this.user = user;
        this.operationType = operationType;
        this.amount = BigDecimal.valueOf(amount);
        this.targetUser = targetUser;
    }


    public LocalDateTime  getDate() {
        return date;
    }

    public String getUser() {
        return user;
    }

    public LogOptions.Operation getOperationType() {
        return operationType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTargetUser() {
        return targetUser;
    }

    @Override
    public String toString() {
        String formattedDate = date.format(FORMATTER);

        switch (operationType) {
            case transferred:
                return String.format("[%s] %s transferred %.2f to %s", formattedDate, user, amount, targetUser);
            case received:
                return String.format("[%s] %s received %.2f from %s", formattedDate, user, amount, targetUser);
            case withdrew:
                return String.format("[%s] %s withdrew %.2f", formattedDate, user, amount);
            case balance_inquiry:
                return String.format("[%s] %s balance inquiry %.2f", formattedDate, user, amount);
            default:
                return "Unknown operation";
        }
    }
}
