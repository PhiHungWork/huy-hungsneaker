package com.example.QuanLiThuChi.controller;

import com.example.QuanLiThuChi.entity.Product;
import com.example.QuanLiThuChi.entity.ProductApproval;
import com.example.QuanLiThuChi.entity.ProductVariant;
import com.example.QuanLiThuChi.entity.Category;
import com.example.QuanLiThuChi.repository.ProductRepository;
import com.example.QuanLiThuChi.repository.ProductApprovalRepository;
import com.example.QuanLiThuChi.repository.CategoryRepository;
import com.example.QuanLiThuChi.repository.ProductVariantRepository;
import com.example.QuanLiThuChi.service.ProductService;
import com.example.QuanLiThuChi.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products/admin")
@RequiredArgsConstructor
public class ProductAdminController {
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductApprovalRepository productApprovalRepository;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productRepository.findAll();
        Map<Long, Integer> productStockMap = new HashMap<>();
        
        List<Long> productIds = products.stream().map(Product::getId).toList();
        if (!productIds.isEmpty()) {
            List<Object[]> stocks = productVariantRepository.sumStockByProductIds(productIds);
            for (Object[] stock : stocks) {
                Long pId = (Long) stock[0];
                Number stockQty = (Number) stock[1];
                productStockMap.put(pId, stockQty != null ? stockQty.intValue() : 0);
            }
        }
        
        Map<Long, String> productSizesMap = new HashMap<>();
        for (Product product : products) {
            productStockMap.putIfAbsent(product.getId(), 0);
            List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
            String sizes = variants.stream().map(ProductVariant::getSize).collect(Collectors.joining(", "));
            productSizesMap.put(product.getId(), sizes);
        }

        List<ProductApproval> approvals = productApprovalRepository.findAllByOrderByRequestedAtDesc();

        model.addAttribute("products", products);
        model.addAttribute("approvals", approvals);
        model.addAttribute("productStockMap", productStockMap);
        model.addAttribute("productSizesMap", productSizesMap);
        model.addAttribute("categories", categoryRepository.findAll());
        return "products/list";
    }

    @PostMapping("/add")
    public String addProduct(@RequestParam String code,
                             @RequestParam String name,
                             @RequestParam Long categoryId,
                             @RequestParam String brand,
                             @RequestParam BigDecimal price,
                             @RequestParam(required = false) String imageUrl,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             @RequestParam String status,
                             @RequestParam(required = false) String sizes,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (productRepository.existsByCodeIgnoreCase(code)) {
            redirectAttributes.addFlashAttribute("error", "Mã sản phẩm đã tồn tại!");
            return "redirect:/products/admin";
        }

        String finalImageUrl = imageUrl;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                finalImageUrl = cloudinaryService.uploadImage(imageFile);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Lỗi khi upload ảnh lên Cloudinary!");
                return "redirect:/products/admin";
            }
        }
        
        if (finalImageUrl == null || finalImageUrl.trim().isEmpty()) {
             finalImageUrl = "/img/default-sneaker.jpg";
        }

        Category category = categoryRepository.findById(categoryId).orElse(null);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            Product product = new Product();
            product.setCode(code);
            product.setName(name);
            product.setBrand(brand);
            product.setPrice(price);
            product.setImageUrl(finalImageUrl);
            product.setCategory(category);
            product.setStatus(status);
            productRepository.save(product);
            
            if (sizes != null && !sizes.trim().isEmpty()) {
                java.util.Arrays.stream(sizes.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(s -> s.length() > 10 ? s.substring(0, 10) : s)
                        .distinct()
                        .forEach(sizeVal -> {
                            ProductVariant variant = new ProductVariant();
                            variant.setProduct(product);
                            variant.setSize(sizeVal);
                            variant.setStockQuantity(0);
                            productVariantRepository.save(variant);
                        });
            }
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm mới thành công!");
        } else {
            ProductApproval approval = new ProductApproval();
            approval.setCode(code);
            approval.setName(name);
            approval.setCategory(category);
            approval.setBrand(brand);
            approval.setPrice(price);
            approval.setImageUrl(finalImageUrl);
            approval.setDesiredStatus(status);
            approval.setAction("ADD");
            approval.setRequestedBy(authentication.getName());
            approval.setSizes(sizes);
            productApprovalRepository.save(approval);
            redirectAttributes.addFlashAttribute("success", "Yêu cầu thêm sản phẩm mới đã được gửi và đang chờ Admin duyệt!");
        }
        return "redirect:/products/admin";
    }

    @PostMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id,
                              @RequestParam String code,
                              @RequestParam String name,
                              @RequestParam Long categoryId,
                              @RequestParam String brand,
                              @RequestParam BigDecimal price,
                              @RequestParam(required = false) String imageUrl,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              @RequestParam String status,
                              @RequestParam(required = false) String sizes,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại!");
            return "redirect:/products/admin";
        }

        if (!product.getCode().equalsIgnoreCase(code) && 
            productRepository.existsByCodeIgnoreCase(code)) {
            redirectAttributes.addFlashAttribute("error", "Mã sản phẩm mới đã tồn tại!");
            return "redirect:/products/admin";
        }

        String finalImageUrl = imageUrl;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                finalImageUrl = cloudinaryService.uploadImage(imageFile);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Lỗi khi upload ảnh lên Cloudinary!");
                return "redirect:/products/admin";
            }
        }
        
        if (finalImageUrl == null || finalImageUrl.trim().isEmpty()) {
            finalImageUrl = product.getImageUrl();
        }

        Category category = categoryRepository.findById(categoryId).orElse(null);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            product.setCode(code);
            product.setName(name);
            product.setBrand(brand);
            product.setPrice(price);
            product.setImageUrl(finalImageUrl);
            product.setCategory(category);
            product.setStatus(status);
            productRepository.save(product);
            
            if (sizes != null && !sizes.trim().isEmpty()) {
                List<ProductVariant> existingVariants = productVariantRepository.findByProductId(product.getId());
                List<String> existingSizes = existingVariants.stream().map(ProductVariant::getSize).toList();
                
                java.util.Arrays.stream(sizes.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(s -> s.length() > 10 ? s.substring(0, 10) : s)
                        .distinct()
                        .filter(s -> !existingSizes.contains(s))
                        .forEach(sizeVal -> {
                            ProductVariant variant = new ProductVariant();
                            variant.setProduct(product);
                            variant.setSize(sizeVal);
                            variant.setStockQuantity(0);
                            productVariantRepository.save(variant);
                        });
            }
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
        } else {
            ProductApproval approval = new ProductApproval();
            approval.setProduct(product);
            approval.setCode(code);
            approval.setName(name);
            approval.setCategory(category);
            approval.setBrand(brand);
            approval.setPrice(price);
            approval.setImageUrl(finalImageUrl);
            approval.setDesiredStatus(status);
            approval.setAction("EDIT");
            approval.setRequestedBy(authentication.getName());
            approval.setSizes(sizes);
            productApprovalRepository.save(approval);
            redirectAttributes.addFlashAttribute("success", "Yêu cầu chỉnh sửa sản phẩm đã được gửi và đang chờ Admin duyệt!");
        }
        return "redirect:/products/admin";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                try {
                    productRepository.delete(product);
                    productRepository.flush();
                    redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm thành công!");
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Không thể xóa sản phẩm này vì đã có dữ liệu liên kết (biến thể, hóa đơn, v.v.)!");
                }
            } else {
                ProductApproval approval = new ProductApproval();
                approval.setProduct(product);
                approval.setCode(product.getCode());
                approval.setName(product.getName());
                approval.setCategory(product.getCategory());
                approval.setBrand(product.getBrand());
                approval.setPrice(product.getPrice());
                approval.setImageUrl(product.getImageUrl());
                approval.setDesiredStatus(product.getStatus());
                approval.setAction("DELETE");
                approval.setRequestedBy(authentication.getName());
                productApprovalRepository.save(approval);
                redirectAttributes.addFlashAttribute("success", "Yêu cầu xóa sản phẩm đã được gửi và đang chờ Admin duyệt!");
            }
        }
        return "redirect:/products/admin";
    }

    @PostMapping("/approve/{approvalId}")
    public String approveProduct(@PathVariable Long approvalId, RedirectAttributes redirectAttributes) {
        ProductApproval approval = productApprovalRepository.findById(approvalId).orElse(null);
        if (approval != null) {
            if ("ADD".equals(approval.getAction())) {
                Product product = new Product();
                product.setCode(approval.getCode());
                product.setName(approval.getName());
                product.setCategory(approval.getCategory());
                product.setBrand(approval.getBrand());
                product.setPrice(approval.getPrice());
                product.setImageUrl(approval.getImageUrl());
                product.setStatus(approval.getDesiredStatus());
                productRepository.save(product);
                
                String sizes = approval.getSizes();
                if (sizes != null && !sizes.trim().isEmpty()) {
                    java.util.Arrays.stream(sizes.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(s -> s.length() > 10 ? s.substring(0, 10) : s)
                            .distinct()
                            .forEach(sizeVal -> {
                                ProductVariant variant = new ProductVariant();
                                variant.setProduct(product);
                                variant.setSize(sizeVal);
                                variant.setStockQuantity(0);
                                productVariantRepository.save(variant);
                            });
                }
                redirectAttributes.addFlashAttribute("success", "Đã duyệt yêu cầu thêm mới sản phẩm!");
            } else if ("EDIT".equals(approval.getAction())) {
                Product product = approval.getProduct();
                if (product != null) {
                    product.setCode(approval.getCode());
                    product.setName(approval.getName());
                    product.setCategory(approval.getCategory());
                    product.setBrand(approval.getBrand());
                    product.setPrice(approval.getPrice());
                    product.setImageUrl(approval.getImageUrl());
                    product.setStatus(approval.getDesiredStatus());
                    productRepository.save(product);
                    
                    String sizes = approval.getSizes();
                    if (sizes != null && !sizes.trim().isEmpty()) {
                        List<ProductVariant> existingVariants = productVariantRepository.findByProductId(product.getId());
                        List<String> existingSizes = existingVariants.stream().map(ProductVariant::getSize).toList();
                        
                        java.util.Arrays.stream(sizes.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(s -> s.length() > 10 ? s.substring(0, 10) : s)
                                .distinct()
                                .filter(s -> !existingSizes.contains(s))
                                .forEach(sizeVal -> {
                                    ProductVariant variant = new ProductVariant();
                                    variant.setProduct(product);
                                    variant.setSize(sizeVal);
                                    variant.setStockQuantity(0);
                                    productVariantRepository.save(variant);
                                });
                    }
                    redirectAttributes.addFlashAttribute("success", "Đã duyệt yêu cầu chỉnh sửa sản phẩm!");
                }
            } else if ("DELETE".equals(approval.getAction())) {
                Product product = approval.getProduct();
                if (product != null) {
                    try {
                        productRepository.delete(product);
                        productRepository.flush();
                        redirectAttributes.addFlashAttribute("success", "Đã duyệt yêu cầu xóa sản phẩm!");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "Không thể xóa sản phẩm này vì đã có dữ liệu liên kết!");
                    }
                }
            }
            productApprovalRepository.delete(approval);
        }
        return "redirect:/products/admin";
    }

    @PostMapping("/reject/{approvalId}")
    public String rejectProduct(@PathVariable Long approvalId, RedirectAttributes redirectAttributes) {
        ProductApproval approval = productApprovalRepository.findById(approvalId).orElse(null);
        if (approval != null) {
            productApprovalRepository.delete(approval);
            redirectAttributes.addFlashAttribute("success", "Đã từ chối yêu cầu từ Nhân viên!");
        }
        return "redirect:/products/admin";
    }

    @GetMapping("/{id}/variants")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getProductVariants(@PathVariable Long id) {
        List<ProductVariant> variants = productVariantRepository.findByProductId(id);
        List<Map<String, Object>> response = variants.stream().map(v -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", v.getId());
            map.put("size", v.getSize());
            map.put("stockQuantity", v.getStockQuantity());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-stock")
    public String updateStock(@RequestParam Map<String, String> allParams, Authentication authentication, RedirectAttributes redirectAttributes) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            redirectAttributes.addFlashAttribute("error", "Chỉ Admin mới có quyền cập nhật kho!");
            return "redirect:/products/admin";
        }
        
        int updatedCount = 0;
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("stock_")) {
                try {
                    Long variantId = Long.parseLong(entry.getKey().substring(6));
                    Integer stock = Integer.parseInt(entry.getValue());
                    if (stock >= 0) {
                        ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
                        if (variant != null) {
                            variant.setStockQuantity(stock);
                            productVariantRepository.save(variant);
                            updatedCount++;
                        }
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            }
        }
        
        if (updatedCount > 0) {
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật kho thành công cho " + updatedCount + " size!");
        }
        return "redirect:/products/admin";
    }
}
