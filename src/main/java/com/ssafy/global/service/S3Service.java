package com.ssafy.global.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String upload(MultipartFile file, String folder) throws IOException {
        String fileName = folder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(request,
                RequestBody.fromBytes(file.getBytes()));
        return getS3Url(fileName);
    }
    
    public String uploadFromUrl(String imageUrl, String folder) throws IOException {
        String fileName = folder + "/" + UUID.randomUUID() + ".jpg";
        URL url = new URL(imageUrl);
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
                    RequestBody.fromInputStream(inputStream, connection.getContentLengthLong())
            );
        }
        return getS3Url(fileName);
    }

    public String uploadGooglePlacePhoto(String googlePlaceId, String photoReference, String googlePhotoUrl, String folder) throws IOException {
        String fileName = String.format("%s/%s-%s.jpg", folder, googlePlaceId, photoReference);
        
        URL url = new URL(googlePhotoUrl);
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
                    RequestBody.fromInputStream(inputStream, connection.getContentLengthLong())
            );
        }
        
        return getS3Url(fileName);
    }

    private String getS3Url(String fileName) {
        return "https://" + bucketName + ".s3." +
                s3Client.serviceClientConfiguration().region().id() + ".amazonaws.com/" + fileName;
    }

}
