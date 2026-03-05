package com.llburgers.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service for managing image uploads and retrieval via Supabase Storage.
 * <p>
 * Images (burgers, sides, drinks) are stored in Supabase Storage.
 * Only the public URLs are saved in the database.
 */
public interface IStorageService {

    /**
     * Uploads an image to Supabase Storage and returns the public URL.
     *
     * @param file   the image file to upload
     * @param folder the subfolder (e.g. "products", "sides")
     * @return the public URL of the uploaded image
     */
    String uploadImage(MultipartFile file, String folder);

    /**
     * Deletes an image from Supabase Storage by its full path.
     *
     * @param filePath the full path within the bucket (e.g. "products/uuid.jpg")
     */
    void deleteImage(String filePath);

    /**
     * Returns the public URL for a given storage file path.
     *
     * @param filePath the file path within the bucket
     * @return the public URL
     */
    String getPublicUrl(String filePath);
}

