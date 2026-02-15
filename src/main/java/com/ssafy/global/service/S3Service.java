package com.ssafy.global.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @jakarta.annotation.PostConstruct
    public void validateConfig() {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "S3 bucket name is not configured. Please check S3_BUCKET_NAME in your .env file.");
        }
    }

    public String upload(MultipartFile file, String folder) throws IOException {
        String fileName = folder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(request,
                RequestBody.fromBytes(file.getBytes()));
        return fileName;
    }

    public String uploadFromUrl(String imageUrl, String folder) throws IOException {
        String fileName = folder + "/" + UUID.randomUUID() + ".jpg";
        URL url = URI.create(imageUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        try (InputStream inputStream = connection.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("image/jpeg")
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(inputStream, connection.getContentLengthLong()));
        }
        return fileName;
    }

    public String uploadGooglePlacePhoto(String googlePlaceId, String photoReference, String googlePhotoUrl,
            String folder) throws IOException {
        String fileName = String.format("%s/%s-%s.jpg", folder, googlePlaceId, photoReference);

        URL url = URI.create(googlePhotoUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        try (InputStream inputStream = connection.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("image/jpeg")
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(inputStream, connection.getContentLengthLong()));
        }

        return fileName;
    }

    public String generatePresignedUrl(String key) {
        if (key == null || key.isEmpty())
            return null;
        if (key.startsWith("http"))
            return key; // Already a full URL (legacy or external)

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(builder -> builder.bucket(bucketName).key(key).build())
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

}
