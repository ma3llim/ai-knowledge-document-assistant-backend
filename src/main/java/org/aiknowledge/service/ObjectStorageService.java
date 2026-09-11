package org.aiknowledge.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ObjectStorageService {
    void upload(String objectKey, MultipartFile file) throws IOException;

    void delete(String objectKey);

    boolean exists(String objectKey);
}
