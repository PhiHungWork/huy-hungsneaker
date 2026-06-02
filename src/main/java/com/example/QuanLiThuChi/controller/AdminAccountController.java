package com.example.QuanLiThuChi.controller;

import com.example.QuanLiThuChi.entity.Order;
import com.example.QuanLiThuChi.entity.User;
import com.example.QuanLiThuChi.repository.OrderItemRepository;
import com.example.QuanLiThuChi.repository.OrderRepository;
import com.example.QuanLiThuChi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

@Controller
@RequestMapping("/admin")
public class AdminAccountController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @GetMapping("/customers")
    public String listCustomers(Model model) {
        List<User> registeredCustomers = userRepository.findByRole("ROLE_USER");
        List<Order> nonCancelledOrders = orderRepository.findByStatusNot("CANCELLED");

        Map<String, List<Long>> orderIdsByNormalizedPhone = new HashMap<>();
        Map<String, String> latestCustomerNameByPhone = new HashMap<>();
        for (Order order : nonCancelledOrders) {
            String normalizedPhone = normalizePhone(order.getCustomerPhone());
            if (!normalizedPhone.isEmpty()) {
                orderIdsByNormalizedPhone.computeIfAbsent(normalizedPhone, k -> new ArrayList<>()).add(order.getId());
                latestCustomerNameByPhone.put(normalizedPhone, order.getCustomerName());
            }
        }

        List<CustomerRow> customerRows = new ArrayList<>();
        Set<String> mappedPhones = new HashSet<>();

        for (User customer : registeredCustomers) {
            Set<Long> unifiedOrderIds = new HashSet<>();

            List<Order> linkedOrders = orderRepository.findByCreatedByIdAndStatusNot(customer.getId(), "CANCELLED");
            for (Order linkedOrder : linkedOrders) {
                unifiedOrderIds.add(linkedOrder.getId());
            }

            String normalizedCustomerPhone = normalizePhone(customer.getPhone());
            if (!normalizedCustomerPhone.isEmpty()) {
                List<Long> phoneOrderIds = orderIdsByNormalizedPhone.getOrDefault(normalizedCustomerPhone, List.of());
                unifiedOrderIds.addAll(phoneOrderIds);
                mappedPhones.add(normalizedCustomerPhone);
            }

            List<String> productNames = unifiedOrderIds.isEmpty()
                    ? List.of()
                    : orderItemRepository.findDistinctPurchasedProductNamesByOrderIds(new ArrayList<>(unifiedOrderIds));

            StringJoiner joiner = new StringJoiner(", ");
            productNames.forEach(joiner::add);

            customerRows.add(new CustomerRow(
                    customer.getId(),
                    customer.getFullName(),
                    customer.getUsername(),
                    customer.getPhone(),
                    (long) unifiedOrderIds.size(),
                    joiner.toString(),
                    true,
                    Boolean.TRUE.equals(customer.getEnabled())
            ));
        }

        for (Map.Entry<String, List<Long>> entry : orderIdsByNormalizedPhone.entrySet()) {
            String normalizedPhone = entry.getKey();
            if (mappedPhones.contains(normalizedPhone)) {
                continue;
            }

            List<Long> orderIds = entry.getValue();
            List<String> productNames = orderItemRepository.findDistinctPurchasedProductNamesByOrderIds(orderIds);
            StringJoiner joiner = new StringJoiner(", ");
            productNames.forEach(joiner::add);

            customerRows.add(new CustomerRow(
                    null,
                    latestCustomerNameByPhone.getOrDefault(normalizedPhone, "Khách vãng lai"),
                    "Khách vãng lai",
                    normalizedPhone,
                    (long) orderIds.size(),
                    joiner.toString(),
                    false,
                    true
            ));
        }

        customerRows.sort(Comparator
                .comparing(CustomerRow::isRegistered).reversed()
                .thenComparing(CustomerRow::getPurchaseCount, Comparator.reverseOrder()));

        model.addAttribute("customerRows", customerRows);
        model.addAttribute("title", "Quản lý Khách hàng");
        return "admin/customers";
    }

    @GetMapping("/staffs")
    public String listStaffs(Model model) {
        List<User> staffs = userRepository.findByRoleIn(Arrays.asList("ROLE_STAFF", "ROLE_ADMIN"));
        model.addAttribute("users", staffs);
        model.addAttribute("title", "Quản lý Nhân sự");
        return "admin/staffs";
    }

    @PostMapping("/staffs/add")
    public String addStaff(@RequestParam String fullName,
                           @RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String phone,
                           @RequestParam String role,
                           RedirectAttributes redirectAttributes) {

        if (userRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại!");
            return "redirect:/admin/staffs";
        }

        if (userRepository.existsByPhone(phone)) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại đã được sử dụng!");
            return "redirect:/admin/staffs";
        }

        User staff = new User();
        staff.setFullName(fullName);
        staff.setUsername(username);
        staff.setPassword(passwordEncoder.encode(password));
        staff.setPhone(phone);
        staff.setRole(role);
        staff.setEnabled(true);

        userRepository.save(staff);
        redirectAttributes.addFlashAttribute("success", "Thêm nhân sự thành công!");
        return "redirect:/admin/staffs";
    }

    @PostMapping("/accounts/{id}/toggle-status")
    public String toggleAccountStatus(@PathVariable Long id,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes,
                                      @RequestHeader(value = "referer", required = false) String referer) {
        User userToToggle = userRepository.findById(id).orElse(null);
        if (userToToggle != null) {
            if ("ROLE_ADMIN".equals(userToToggle.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Không thể khóa tài khoản Quản trị viên (Bất tử)!");
            } else {
                userToToggle.setEnabled(!userToToggle.getEnabled());
                userRepository.save(userToToggle);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái tài khoản!");
            }
        }
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    private String normalizePhone(String rawPhone) {
        return rawPhone == null ? "" : rawPhone.replaceAll("[^0-9]", "");
    }

    public static class CustomerRow {
        private final Long userId;
        private final String fullName;
        private final String username;
        private final String phone;
        private final Long purchaseCount;
        private final String purchasedProducts;
        private final boolean registered;
        private final boolean enabled;

        public CustomerRow(Long userId, String fullName, String username, String phone, Long purchaseCount, String purchasedProducts, boolean registered, boolean enabled) {
            this.userId = userId;
            this.fullName = fullName;
            this.username = username;
            this.phone = phone;
            this.purchaseCount = purchaseCount;
            this.purchasedProducts = purchasedProducts;
            this.registered = registered;
            this.enabled = enabled;
        }

        public Long getUserId() { return userId; }
        public String getFullName() { return fullName; }
        public String getUsername() { return username; }
        public String getPhone() { return phone; }
        public Long getPurchaseCount() { return purchaseCount; }
        public String getPurchasedProducts() { return purchasedProducts; }
        public boolean isRegistered() { return registered; }
        public boolean isEnabled() { return enabled; }
    }
}
