package it.polito.ai.backend.services;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Utils {

    public static Timestamp getNow() {
        long nowLong = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return new Timestamp(nowLong);
    }
}
