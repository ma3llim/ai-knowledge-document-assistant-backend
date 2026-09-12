package org.aiknowledge.integration.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface ObjectStorageService {
    InputStream download(String objectKey);

    void upload(String objectKey, MultipartFile file) throws IOException;

    void delete(String objectKey);

    boolean exists(String objectKey);

    Resource loadFromR2(String objectKey);
}
