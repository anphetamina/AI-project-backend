package it.polito.ai.backend.services;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
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

    public static byte[] getBytes(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        byte[] byteObjects = new byte[file.getBytes().length];

        int i = 0;

        for (byte b : file.getBytes()){
            byteObjects[i++] = b;
        }
        return byteObjects;
    }

    public static void checkTypeImage(MultipartFile file) throws TikaException, IOException {
        TikaConfig tika = new TikaConfig();
        Metadata metadata = new Metadata();
        metadata.set(Metadata.RESOURCE_NAME_KEY, file.getOriginalFilename());
        MediaType mimeType = tika.getDetector().detect(TikaInputStream.get(file.getBytes()), metadata);
        String type = mimeType.toString();
        if (!type.equalsIgnoreCase("image/png") && !type.equalsIgnoreCase("image/jpg") && !type.equalsIgnoreCase("image/jpeg")) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, type);
        }
    }
}
