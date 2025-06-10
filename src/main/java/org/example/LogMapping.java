package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogMapping {

    public Map<String, List<LogRequest>> logMapping(List<LogRequest> entries) {
        Map<String, List<LogRequest>> map = new HashMap<>();

        for (LogRequest e : entries) {
            map.computeIfAbsent(e.getUser(), k -> new ArrayList<>()).add(e);

            if (e.getOperationType() == LogOptions.Operation.transferred) {
                LogRequest in = new LogRequest(
                        e.getDate().toLocalDate(),
                        e.getTargetUser(),
                        LogOptions.Operation.received,
                        e.getAmount().doubleValue(),
                        e.getUser()
                );
                map.computeIfAbsent(e.getTargetUser(), k -> new ArrayList<>()).add(in);
            }
        }
        return map;
    }
}
