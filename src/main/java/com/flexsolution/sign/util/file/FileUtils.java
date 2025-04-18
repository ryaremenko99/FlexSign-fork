package com.flexsolution.sign.util.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Slf4j
public class FileUtils {


    public static File createTempFile(String base64) throws IOException {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decoded = decoder.decode(base64);
        Path tempFile = Files.createTempFile("key-", ".tmp");
        Files.write(tempFile, decoded);
        tempFile.toFile().deleteOnExit();
        return tempFile.toFile();
    }
}
