package com.example.QuanLiThuChi;

import com.example.QuanLiThuChi.entity.User;
import com.example.QuanLiThuChi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableAsync
public class QuanLiThuChiApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuanLiThuChiApplication.class, args);
	}

	@Bean
	public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			User admin = userRepository.findByUsername("admin").orElse(new User());
			admin.setUsername("admin");
			admin.setPassword(passwordEncoder.encode("123456"));
			admin.setFullName("Quản Trị Viên");
			admin.setRole("ROLE_ADMIN");
			admin.setEnabled(true);
			userRepository.save(admin);
		};
	}

}
