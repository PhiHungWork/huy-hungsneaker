package com.example.QuanLiThuChi.controller;

import com.example.QuanLiThuChi.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", productService.getPublicProducts());
        return "storefront/home";
    }

    @GetMapping("/about-us")
    public String aboutUs() {
        return "storefront/about-us";
    }

    @GetMapping("/bo-suu-tap")
    public String boSuuTap() {
        return "storefront/bo-suu-tap";
    }

    @GetMapping("/chinh-sach")
    public String chinhSach() {
        return "storefront/chinh-sach";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
}
