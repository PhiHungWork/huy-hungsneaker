package com.example.QuanLiThuChi.service.impl;

import com.example.QuanLiThuChi.entity.Product;
import com.example.QuanLiThuChi.entity.ProductVariant;
import com.example.QuanLiThuChi.repository.ProductRepository;
import com.example.QuanLiThuChi.repository.ProductVariantRepository;
import com.example.QuanLiThuChi.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getPublicProducts() {
        return productRepository.findByStatusIn(List.of("ACTIVE", "OUT_OF_STOCK"));
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public Product getProductByCode(String code) {
        return productRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Product not found by code"));
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrandIgnoreCase(brand);
    }

    @Override
    public List<ProductVariant> getVariantsByProductId(Long productId) {
        return productVariantRepository.findByProductId(productId);
    }
}
