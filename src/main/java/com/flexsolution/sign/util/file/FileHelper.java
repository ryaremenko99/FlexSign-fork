package com.flexsolution.sign.util.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileHelper {

    /**
     * Create temp file in the default temp folder of the OS
     *
     * @param filename String filename of original file
     * @return File
     * @throws IOException if temp file can't be created
     */
    public static File createTempFile(String filename) throws IOException {
        String prefix = FilenameUtils.getPrefix(filename);
        String extension = FilenameUtils.getExtension(filename);
        Path tempFilePath = Files.createTempFile(prefix, extension);
        if (log.isDebugEnabled()) {
            log.debug("Тимчасовий файл для {} було створено за наступним шляхом: {}", filename, tempFilePath.toAbsolutePath());
        }
        return tempFilePath.toFile();
    }

    /**
     * Convert MultipartFile to File
     *
     * @param multipartFile MultipartFile
     * @return File
     */
    public static File multipartFile2File(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        File tempFile = FileHelper.createTempFile(originalFilename);
        multipartFile.transferTo(tempFile);
        return tempFile;
    }
}
