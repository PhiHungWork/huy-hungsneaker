package com.example.QuanLiThuChi.service;

import com.example.QuanLiThuChi.entity.Product;
import com.example.QuanLiThuChi.entity.ProductVariant;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    List<Product> getPublicProducts();
    Product getProductById(Long id);
    Product getProductByCode(String code);
    List<Product> getProductsByBrand(String brand);
    List<ProductVariant> getVariantsByProductId(Long productId);
}
