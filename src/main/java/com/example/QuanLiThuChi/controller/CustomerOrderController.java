package com.example.QuanLiThuChi.controller;

import com.example.QuanLiThuChi.entity.Order;
import com.example.QuanLiThuChi.entity.OrderItem;
import com.example.QuanLiThuChi.entity.Product;
import com.example.QuanLiThuChi.entity.ProductVariant;
import com.example.QuanLiThuChi.entity.User;
import com.example.QuanLiThuChi.repository.OrderRepository;
import com.example.QuanLiThuChi.repository.ProductRepository;
import com.example.QuanLiThuChi.repository.ProductVariantRepository;
import com.example.QuanLiThuChi.repository.UserRepository;
import com.example.QuanLiThuChi.service.NotificationResult;
import com.example.QuanLiThuChi.service.NotificationService;
import com.example.QuanLiThuChi.component.ShoppingCart;
import com.example.QuanLiThuChi.dto.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ShoppingCart shoppingCart;

    @GetMapping("/checkout")
    public String checkout(Model model, Authentication authentication) {
        if (shoppingCart.isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cartItems", shoppingCart.getItems());
        model.addAttribute("cartTotal", shoppingCart.getTotalPrice());

        if (authentication != null) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                model.addAttribute("customerName", user.getFullName());
                model.addAttribute("customerPhone", user.getPhone());
                orderRepository.findTopByCreatedByIdOrderByOrderDateDesc(user.getId())
                        .ifPresent(lastOrder -> model.addAttribute("customerAddress", lastOrder.getCustomerAddress()));
            }
        }

        return "orders/checkout";
    }

    @PostMapping("/place")
    @Transactional(rollbackFor = Exception.class)
    public String placeOrder(@RequestParam String customerName,
                             @RequestParam String customerPhone,
                             @RequestParam String customerAddress,
                             @RequestParam(required = false) String customerEmail,
                             @RequestParam String paymentMethod,
                             @RequestParam(required = false) String note,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        if (shoppingCart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng của bạn đang trống!");
            return "redirect:/cart";
        }

        // Validate kho trước khi tạo Order
        for (CartItem item : shoppingCart.getItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
            if (variant == null || variant.getStockQuantity() < item.getQuantity()) {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm " + item.getProductName() + " (Size " + item.getSize() + ") không đủ số lượng trong kho!");
                return "redirect:/cart";
            }
        }

        User user = null;
        if (authentication != null) {
            user = userRepository.findByUsername(authentication.getName()).orElse(null);
        }

        Order order = new Order();
        order.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setCustomerAddress(customerAddress);
        
        String emailTrimmed = customerEmail != null ? customerEmail.trim() : "";
        order.setCustomerEmail(emailTrimmed.isEmpty() ? null : emailTrimmed);

        String finalNote = "PTTT: " + paymentMethod;
        if (note != null && !note.trim().isEmpty()) {
            finalNote = note.trim() + " (" + finalNote + ")";
        }
        order.setNote(finalNote);
        order.setCreatedBy(user);
        order.setStatus("NEW");
        order.setTotalAmount(shoppingCart.getTotalPrice());

        for (CartItem cartItem : shoppingCart.getItems()) {
            ProductVariant variant = productVariantRepository.findById(cartItem.getVariantId()).orElse(null);
            if (variant != null) {
                variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
                productVariantRepository.save(variant);

                Product product = variant.getProduct();
                Integer totalStock = productVariantRepository.sumStockByProductId(product.getId());
                if (totalStock != null && totalStock <= 0) {
                    product.setStatus("OUT_OF_STOCK");
                } else if ("OUT_OF_STOCK".equals(product.getStatus())) {
                    product.setStatus("ACTIVE");
                }
                productRepository.save(product);

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setVariant(variant);
                item.setQuantity(cartItem.getQuantity());
                item.setUnitPrice(cartItem.getPrice());
                item.setLineTotal(cartItem.getLineTotal());

                order.getItems().add(item);
            }
        }

        orderRepository.save(order);
        shoppingCart.clear();
        notificationService.notifyOrderCreated(order);

        return "redirect:/orders/success/" + order.getId();
    }

    @GetMapping("/success/{id}")
    public String orderSuccess(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return "redirect:/";
        }
        model.addAttribute("order", order);

        String paymentMethod = "COD";
        if (order.getNote() != null && order.getNote().contains("PTTT: BANK")) {
            paymentMethod = "BANK";
        }
        model.addAttribute("paymentMethod", paymentMethod);
        return "orders/success";
    }

    @GetMapping("/track")
    public String trackOrder(@RequestParam(required = false) String orderCode,
                             @RequestParam(required = false) String phone,
                             Model model) {
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("phone", phone);

        if (orderCode != null && !orderCode.isBlank() && phone != null && !phone.isBlank()) {
            String normalizedInputPhone = normalizePhone(phone);
            Order order = orderRepository.findTopByOrderCodeIgnoreCaseOrderByOrderDateDesc(orderCode.trim()).orElse(null);
            if (order == null || !normalizePhone(order.getCustomerPhone()).equals(normalizedInputPhone)) {
                model.addAttribute("error", "Không tìm thấy đơn hàng. Vui lòng kiểm tra lại mã đơn và số điện thoại.");
            } else {
                model.addAttribute("order", order);
            }
        }
        return "orders/track";
    }

    private String normalizePhone(String raw) {
        return raw == null ? "" : raw.replaceAll("[^0-9]", "");
    }
}
