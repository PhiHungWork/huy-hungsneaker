package com.example.QuanLiThuChi.config;

import com.example.QuanLiThuChi.entity.Category;
import com.example.QuanLiThuChi.entity.Product;
import com.example.QuanLiThuChi.entity.ProductVariant;
import com.example.QuanLiThuChi.repository.CategoryRepository;
import com.example.QuanLiThuChi.repository.ProductRepository;
import com.example.QuanLiThuChi.repository.ProductVariantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class MockDataRunner implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;

    public MockDataRunner(ProductRepository productRepository, CategoryRepository categoryRepository, ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createProductIfNotExists("nike-af1", "Nike Air Force 1", "Nike", "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?q=80&w=600", new BigDecimal("2500000"));
        createProductIfNotExists("nike-dunk", "Nike Dunk Low", "Nike", "https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?q=80&w=600", new BigDecimal("3200000"));
        createProductIfNotExists("adidas-samba", "Adidas Samba OG", "Adidas", "https://images.unsplash.com/photo-1518002171953-a080ee817e1f?q=80&w=600", new BigDecimal("2800000"));
        createProductIfNotExists("adidas-campus", "Adidas Campus 00s", "Adidas", "https://images.unsplash.com/photo-1511556532299-8f662fc26c06?q=80&w=600", new BigDecimal("2600000"));
        createProductIfNotExists("jordan-1", "Air Jordan 1 High", "Jordan", "https://images.unsplash.com/photo-1552346154-21d32810baa3?q=80&w=600", new BigDecimal("4500000"));
        createProductIfNotExists("jordan-4", "Air Jordan 4 Retro", "Jordan", "https://images.unsplash.com/photo-1608231387042-66d1773070a5?q=80&w=600", new BigDecimal("5500000"));
    }

    private void createProductIfNotExists(String code, String name, String brand, String imageUrl, BigDecimal price) {
        if (productRepository.findByCode(code).isEmpty()) {
            Product product = new Product();
            product.setCode(code);
            product.setName(name);
            product.setBrand(brand);
            product.setPrice(price);
            product.setImageUrl(imageUrl);
            product.setStatus("ACTIVE");
            product.setCreatedAt(LocalDateTime.now());
            
            product = productRepository.save(product);
            
            // Add some variants
            addVariant(product, "39", 10);
            addVariant(product, "40", 15);
            addVariant(product, "41", 20);
            addVariant(product, "42", 12);
        }
    }

    private void addVariant(Product product, String size, int stock) {
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSize(size);
        variant.setStockQuantity(stock);
        productVariantRepository.save(variant);
    }
}
