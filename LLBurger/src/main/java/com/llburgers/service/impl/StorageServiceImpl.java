package com.llburgers.service.impl;

import com.llburgers.service.IStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Supabase Storage service for uploading, deleting, and serving images.
 * <p>
 * Images are stored in the configured Supabase bucket.
 * Only the public URL is persisted in the database (Product.imageUrl, Side.imageUrl).
 */
@Service
public class StorageServiceImpl implements IStorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageServiceImpl.class);

    private final RestTemplate restTemplate;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    public StorageServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        // Generate unique file path: folder/uuid.ext
        String filePath = folder + "/" + UUID.randomUUID() + extension;

        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + filePath;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);
            headers.setContentType(MediaType.parseMediaType(
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream"));

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String publicUrl = getPublicUrl(filePath);
                log.info("[STORAGE-UPLOAD] path={}, url={}", filePath, publicUrl);
                return publicUrl;
            } else {
                throw new RuntimeException("Supabase upload failed with status: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file bytes: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        // Extract path from full URL if needed
        String path = filePath;
        String publicPrefix = supabaseUrl + "/storage/v1/object/public/" + bucket + "/";
        if (filePath.startsWith(publicPrefix)) {
            path = filePath.substring(publicPrefix.length());
        }

        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, String.class);
            log.info("[STORAGE-DELETE] path={}", path);
        } catch (Exception e) {
            log.error("[STORAGE-DELETE-FAILED] path={}, error={}", path, e.getMessage());
        }
    }

    @Override
    public String getPublicUrl(String filePath) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + filePath;
    }
}

