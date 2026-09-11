package org.aiknowledge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.config.properties.R2Properties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class R2ObjectStorageService implements ObjectStorageService {
    private final S3Client r2Client;
    private final R2Properties r2Properties;

    @Override
    public boolean exists(String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder().bucket(r2Properties.getBucket()).key(objectKey).build();
            r2Client.headObject(request);

            return true;
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                log.info("R2 object does not exist: bucket={}, objectKey={}", r2Properties.getBucket(), objectKey);
                return false;
            }
            log.error("Failed to check R2 object existence: bucket={}, objectKey={}, statusCode={}",
                    r2Properties.getBucket(), objectKey, exception.statusCode(), exception);
            throw exception;
        }
    }

    @Override
    public void delete(String objectKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(r2Properties.getBucket())
                .key(objectKey)
                .build();
        r2Client.deleteObject(request);
        log.info("Document deleted from R2 successfully, objectKey={}", objectKey);
    }

    @Override
    public void upload(String objectKey, MultipartFile file) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(r2Properties.getBucket())
                .key(objectKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        r2Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        log.info("Document uploaded to R2 successfully, objectKey={}", objectKey);
    }
}
