package com.example.QuanLiThuChi.controller;

import com.example.QuanLiThuChi.component.ShoppingCart;
import com.example.QuanLiThuChi.dto.CartItem;
import com.example.QuanLiThuChi.entity.Product;
import com.example.QuanLiThuChi.entity.ProductVariant;
import com.example.QuanLiThuChi.repository.ProductRepository;
import com.example.QuanLiThuChi.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ShoppingCart shoppingCart;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cartItems", shoppingCart.getItems());
        model.addAttribute("cartTotal", shoppingCart.getTotalPrice());
        return "storefront/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam String selectedSize,
                            @RequestParam int quantity,
                            @RequestParam(defaultValue = "false") boolean buyNow,
                            RedirectAttributes redirectAttributes) {

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || (!"ACTIVE".equals(product.getStatus()) && !"OUT_OF_STOCK".equals(product.getStatus()))) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại hoặc đã ngừng bán!");
            return "redirect:/";
        }

        if (quantity <= 0) {
            redirectAttributes.addFlashAttribute("error", "Số lượng không hợp lệ!");
            return "redirect:/products/" + product.getCode().toLowerCase();
        }

        ProductVariant variant = productVariantRepository.findByProductIdAndSize(productId, selectedSize).orElse(null);
        if (variant == null || variant.getStockQuantity() < quantity) {
            redirectAttributes.addFlashAttribute("error", "Số lượng trong kho không đủ cho size này!");
            return "redirect:/products/" + product.getCode().toLowerCase();
        }

        CartItem item = new CartItem(
                product.getId(),
                variant.getId(),
                product.getName(),
                variant.getSize(),
                product.getPrice(),
                quantity,
                product.getImageUrl(),
                variant.getStockQuantity()
        );

        shoppingCart.addItem(item);

        if (buyNow) {
            return "redirect:/orders/checkout";
        }

        redirectAttributes.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng!");
        return "redirect:/products/" + product.getCode().toLowerCase();
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam Long variantId,
                             @RequestParam int quantity,
                             RedirectAttributes redirectAttributes) {
        if (quantity <= 0) {
            shoppingCart.removeItem(variantId);
        } else {
            shoppingCart.updateItem(variantId, quantity);
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeCartItem(@RequestParam Long variantId) {
        shoppingCart.removeItem(variantId);
        return "redirect:/cart";
    }
}
