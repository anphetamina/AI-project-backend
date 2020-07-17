package it.polito.ai.backend.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Utils {

    public static Timestamp getNow() {
        long nowLong = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return new Timestamp(nowLong);
    }

    public static Byte[] getBytes(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Byte[] byteObjects = new Byte[file.getBytes().length];

        int i = 0;

        for (byte b : file.getBytes()){
            byteObjects[i++] = b;
        }
        return byteObjects;
    }
}
