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
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileNotFoundException;


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
            log.error("S3 upload error for file: {}. Error message: {}. Stack trace: {}", fileName, e.getMessage(), getStackTrace(e));
            throw new RuntimeException("Error during S3 upload.");
        }
    }

    public String putS3(File uploadFile, String fileName) {
        try {
            // 파일 존재 확인 및 로깅
            if (uploadFile.exists()) {
                log.info("Uploading file {} to S3 bucket {}", uploadFile.getAbsolutePath(), bucket);
            } else {
                log.error("File {} does not exist", uploadFile.getAbsolutePath());
                throw new FileNotFoundException("File " + uploadFile.getAbsolutePath() + " not found");
            }
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, uploadFile));
            return amazonS3.getUrl(bucket, fileName).toString();
        } catch (AmazonServiceException e) {
            log.error("S3 upload error: {} for file: {}", e.getErrorMessage(), fileName);
            throw new RuntimeException("Error during S3 upload.", e);
        } catch (FileNotFoundException e) {
            log.error("File not found: {} for file: {}", e.getMessage(), fileName);
            throw new RuntimeException("File to upload not found.", e);
        }
    }

    public void deleteS3(String filePath) {
        try {
            String key = extractS3KeyFromUrl(filePath);
            amazonS3.deleteObject(bucket, key);
        } catch (AmazonServiceException e) {
            log.error("S3 file deletion error for bucket: {} and file path: {}. Error message: {}. Stack trace: {}", bucket, filePath, e.getMessage(), getStackTrace(e));
            throw new RuntimeException("Error deleting file from S3.");
        } catch (Exception e) {
            log.error("Unexpected S3 error: {}", e.getMessage());
            throw new RuntimeException("Unexpected error during S3 file deletion.");
        }
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("File {} deleted successfully", targetFile.getAbsolutePath());
        } else {
            log.warn("Failed to delete local file: {}", targetFile.getAbsolutePath());
        }
    }


    private Optional<File> convert(MultipartFile file) throws IOException {
        String dirPath = System.getProperty("user.dir") + "/" + file.getOriginalFilename();
        File convertFile = new File(dirPath);

        if (convertFile.exists() && !convertFile.delete()) {
            log.warn("Failed to delete existing local file.");
        }

        try (FileOutputStream fos = new FileOutputStream(convertFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error writing to file {}: {}", convertFile.getAbsolutePath(), e.getMessage());
            throw e;
        }

        if (!convertFile.exists()) {
            log.error("File {} not created successfully", convertFile.getAbsolutePath());
            return Optional.empty();
        }

        log.info("File {} created successfully", convertFile.getAbsolutePath());
        return Optional.of(convertFile);
    }


    public String extractS3KeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid or empty URL provided.");
        }

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
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

}