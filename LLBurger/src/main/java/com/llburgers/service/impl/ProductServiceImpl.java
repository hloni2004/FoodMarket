package com.llburgers.service.impl;

import com.llburgers.domain.Product;
import com.llburgers.domain.enums.ProductCategory;
import com.llburgers.repository.ProductRepository;
import com.llburgers.service.IProductService;
import com.llburgers.service.IStorageService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements IProductService {

    private final ProductRepository repository;
    private final IStorageService storageService;

    public ProductServiceImpl(ProductRepository repository, IStorageService storageService) {
        this.repository = repository;
        this.storageService = storageService;
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    @CacheEvict(value = "products", allEntries = true)
    @Override
    public Product create(Product product) {
        return repository.save(product);
    }

    @Cacheable(value = "products", key = "#id")
    @Override
    public Product read(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    @CacheEvict(value = "products", allEntries = true)
    @Override
    public Product update(Product product) {
        if (product.getId() == null || !repository.existsById(product.getId())) {
            throw new IllegalArgumentException("Product not found with id: " + product.getId());
        }
        return repository.save(product);
    }

    @Cacheable(value = "products", key = "'all'")
    @Override
    public List<Product> getAll() {
        return repository.findAll();
    }

    @CacheEvict(value = "products", allEntries = true)
    @Override
    public void delete(UUID id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        // Remove image from Supabase Storage before deleting the record
        if (product.getImageUrl() != null) {
            storageService.deleteImage(product.getImageUrl());
        }
        repository.deleteById(id);
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    // ─── Filtering ────────────────────────────────────────────────────────────

    @Override
    public List<Product> findByCategory(ProductCategory category) {
        return repository.findByCategory(category);
    }

    @Override
    public List<Product> findByAvailability(boolean availability) {
        return repository.findByAvailability(availability);
    }

    @Override
    public List<Product> findByCategoryAndAvailability(ProductCategory category, boolean availability) {
        return repository.findByCategoryAndAvailability(category, availability);
    }

    @Override
    public List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return repository.findByPriceBetween(minPrice, maxPrice);
    }

    // ─── Stock Management ─────────────────────────────────────────────────────

    @Override
    public List<Product> findInStock() {
        return repository.findByStockQuantityGreaterThan(0);
    }

    @Override
    public List<Product> findOutOfStock() {
        return repository.findByStockQuantityEquals(0);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Override
    public Product updateStock(UUID id, int quantity) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        product.setStockQuantity(quantity);
        // Auto-hide when out of stock, auto-show when restocked
        product.setAvailability(quantity > 0);
        return repository.save(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Override
    public Product reduceStock(UUID id, int quantity) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        int newStock = product.getStockQuantity() - quantity;
        if (newStock < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock for product: " + product.getName()
                    + " (available: " + product.getStockQuantity() + ", requested: " + quantity + ")");
        }
        product.setStockQuantity(newStock);
        // Auto-hide when stock hits zero
        if (newStock == 0) {
            product.setAvailability(false);
        }
        return repository.save(product);
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @CacheEvict(value = "products", allEntries = true)
    @Override
    public Product markUnavailable(UUID id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        product.setAvailability(false);
        return repository.save(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Override
    public Product markAvailable(UUID id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        product.setAvailability(true);
        return repository.save(product);
    }

    // ─── Image Management (Supabase Storage) ──────────────────────────────────

    @CacheEvict(value = "products", allEntries = true)
    @Override
    public Product uploadImage(UUID id, MultipartFile file) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        // Delete old image if one already exists
        if (product.getImageUrl() != null) {
            storageService.deleteImage(product.getImageUrl());
        }
        String publicUrl = storageService.uploadImage(file, "products");
        product.setImageUrl(publicUrl);
        return repository.save(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Override
    public Product removeImage(UUID id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        if (product.getImageUrl() != null) {
            storageService.deleteImage(product.getImageUrl());
            product.setImageUrl(null);
            return repository.save(product);
        }
        return product;
    }
}

