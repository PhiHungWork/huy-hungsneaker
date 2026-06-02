package com.example.QuanLiThuChi.controller;

import com.example.QuanLiThuChi.entity.Product;
import com.example.QuanLiThuChi.entity.ProductVariant;
import com.example.QuanLiThuChi.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class CustomerProductController {
    private final ProductService productService;

    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productService.getPublicProducts();
        model.addAttribute("products", products);
        return "storefront/public-products";
    }

    @GetMapping("/brand/{brandName}")
    public String brandProducts(@PathVariable String brandName, Model model) {
        List<Product> products = productService.getProductsByBrand(brandName);
        model.addAttribute("products", products);
        
        String brandTitle = "";
        String brandDesc = "";
        String bannerUrl = "";
        
        if ("nike".equalsIgnoreCase(brandName)) {
            brandTitle = "Nike Basketball";
            brandDesc = "Khám phá bộ sưu tập giày bóng rổ đỉnh cao từ Nike, mang đến hiệu năng xuất sắc và phong cách không thể chối từ.";
            bannerUrl = "https://images.unsplash.com/photo-1552346154-21d32810baa3?q=80&w=1920";
        } else if ("adidas".equalsIgnoreCase(brandName)) {
            brandTitle = "Adidas Originals";
            brandDesc = "Biểu tượng thời trang đường phố bền vững theo thời gian. Sự kết hợp hoàn hảo giữa tính di sản và xu hướng hiện đại.";
            bannerUrl = "https://images.unsplash.com/photo-1518002171953-a080ee817e1f?q=80&w=1920";
        } else if ("jordan".equalsIgnoreCase(brandName)) {
            brandTitle = "Air Jordan";
            brandDesc = "Huyền thoại bay trên sân bóng. Không chỉ là giày, mà còn là một phần văn hóa sneaker không bao giờ phai mờ.";
            bannerUrl = "https://images.unsplash.com/photo-1608231387042-66d1773070a5?q=80&w=1920";
        } else {
            brandTitle = brandName.substring(0, 1).toUpperCase() + brandName.substring(1).toLowerCase();
            brandDesc = "Khám phá các sản phẩm nổi bật từ thương hiệu " + brandTitle;
            bannerUrl = "https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?q=80&w=1920";
        }
        
        model.addAttribute("brandTitle", brandTitle);
        model.addAttribute("brandDesc", brandDesc);
        model.addAttribute("bannerUrl", bannerUrl);
        model.addAttribute("currentBrand", brandName.toLowerCase());
        
        return "storefront/public-brand";
    }

    @GetMapping("/{code}")
    public String productDetail(@PathVariable String code, Model model) {
        try {
            Product product = productService.getProductByCode(code);
            List<ProductVariant> variants = productService.getVariantsByProductId(product.getId());
            model.addAttribute("product", product);
            model.addAttribute("variants", variants);
            return "storefront/public-product-detail";
        } catch (Exception e) {
            // Redirect to products list or show error if product not found
            return "redirect:/products";
        }
    }
}
