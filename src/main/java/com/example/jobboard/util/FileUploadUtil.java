package com.example.jobboard.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class FileUploadUtil {

    public static void saveFile(String directory, String fileName, MultipartFile file) throws IOException {
        Path directoryPath = Paths.get(directory);
        if(!Files.exists(directoryPath)){
            Files.createDirectories(directoryPath);
        }
        InputStream inputStream = file.getInputStream();
        Path filePath = directoryPath.resolve(fileName);
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

    }
}
