package org.example;

import java.time.LocalDate;

public class LogRequest {
    private LocalDate date;
    private String user;
    private LogOptions.Operation operationType;
    private long amount;
    private String targetUser;
}
