package com.trancuong.ecommerce.media.service;

import com.trancuong.ecommerce.config.MinioProperties;
import com.trancuong.ecommerce.media.dto.MediaUploadResponse;
import com.trancuong.ecommerce.media.exception.MediaUploadException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MediaService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final MinioProperties properties;

    public MediaUploadResponse upload(MultipartFile file) {
        validate(file);
        String contentType = file.getContentType();
        String objectName = buildObjectName(file.getOriginalFilename());

        try {
            MinioClient minioClient = minioClient();
            ensureBucket(minioClient);
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(properties.bucket())
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(contentType)
                        .build());
            }
        } catch (Exception exception) {
            throw new MediaUploadException("Upload media failed", exception);
        }

        return new MediaUploadResponse(
                objectName,
                buildPublicUrl(objectName),
                contentType,
                file.getSize()
        );
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MediaUploadException("File is required");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new MediaUploadException("Only JPEG, PNG, WEBP, and GIF files are allowed");
        }
    }

    private MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
    }

    private void ensureBucket(MinioClient minioClient) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(properties.bucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(properties.bucket())
                    .build());
        }
    }

    private String buildObjectName(String originalFilename) {
        String extension = "";
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalFilename.substring(dotIndex).toLowerCase();
            }
        }

        LocalDate today = LocalDate.now();
        return "products/"
                + today.getYear() + "/"
                + String.format("%02d", today.getMonthValue()) + "/"
                + UUID.randomUUID()
                + extension;
    }

    private String buildPublicUrl(String objectName) {
        String baseUrl = properties.publicUrl().endsWith("/")
                ? properties.publicUrl().substring(0, properties.publicUrl().length() - 1)
                : properties.publicUrl();
        String encodedObjectName = URLEncoder.encode(objectName, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%2F", "/");
        return baseUrl + "/" + properties.bucket() + "/" + encodedObjectName;
    }
}
