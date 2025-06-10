package org.example;

import java.time.LocalDate;

public class LogRequest {
    private LocalDate date;
    private String user;
    private LogOptions.Operation operationType;
    private long amount;
    private String targetUser;

    public LogRequest(LocalDate date, String user, LogOptions.Operation operationType, long amount, String targetUser) {
        this.date = date;
        this.user = user;
        this.operationType = operationType;
        this.amount = amount;
        this.targetUser = targetUser;
    }


    public LocalDate getDate() {
        return date;
    }

    public String getUser() {
        return user;
    }

    public LogOptions.Operation getOperationType() {
        return operationType;
    }

    public long getAmount() {
        return amount;
    }

    public String getTargetUser() {
        return targetUser;
    }
}
