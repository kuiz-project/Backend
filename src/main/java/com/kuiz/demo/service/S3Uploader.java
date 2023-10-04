package com.kuiz.demo.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class S3Uploader {
    @Autowired
    private AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFileToS3(MultipartFile multipartFile, Integer userId) {
        String fileName = userId + "_" + UUID.randomUUID();
        try {
            File uploadFile = convert(multipartFile).orElseThrow(() ->
                    new IllegalArgumentException("Failed to convert the MultipartFile to a File"));

            String uploadImageUrl = putS3(uploadFile, fileName);
            removeNewFile(uploadFile);

            return uploadImageUrl;
        } catch (IOException e) {
            log.error("S3 upload error for file: {}", fileName, e);
            throw new RuntimeException("Error during S3 upload.");
        }
    }

    public String putS3(File uploadFile, String fileName) {
        try {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, uploadFile));
            return amazonS3.getUrl(bucket, fileName).toString();
        } catch (AmazonServiceException e) {
            log.error("S3 upload error: {}", e.getErrorMessage());
            throw new RuntimeException("Error during S3 upload.");
        }
    }

    public void deleteS3(String filePath) {
        try {
            String key = extractS3KeyFromUrl(filePath);
            amazonS3.deleteObject(bucket, key);
        } catch (AmazonServiceException e) {
            log.error("S3 file deletion error: {}", e.getMessage());
            throw new RuntimeException("Error deleting file from S3.");
        } catch (Exception e) {
            log.error("Unexpected S3 error: {}", e.getMessage());
            throw new RuntimeException("Unexpected error during S3 file deletion.");
        }
    }

    private void removeNewFile(File targetFile) {
        if (!targetFile.delete()) {
            log.warn("Failed to delete local file.");
        }
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        String dirPath = System.getProperty("user.dir") + "/" + file.getOriginalFilename();
        File convertFile = new File(dirPath);

        if (convertFile.exists()) {
            if (!convertFile.delete()) {
                log.warn("Failed to delete existing local file.");
            }
        }

        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        } else {
            log.error("Failed to create new local file.");
            return Optional.empty();
        }
    }

    public String extractS3KeyFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new RuntimeException("Error decoding URL for S3 file.", e);
        }
    }

    public URL getPresignedUrl(String objectKey, int expirationTimeInMinutes) {
        Date expiration = new Date();
        long msec = expiration.getTime();
        msec += 1000 * 60 * expirationTimeInMinutes;  // Add 'expirationTimeInMinutes' minutes in milliseconds
        expiration.setTime(msec);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, objectKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);
        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
    }
}
