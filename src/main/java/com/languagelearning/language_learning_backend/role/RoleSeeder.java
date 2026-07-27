package com.languagelearning.language_learning_backend.role;

import com.languagelearning.language_learning_backend.role.entity.Role;
import com.languagelearning.language_learning_backend.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Tạo sẵn 2 Role mặc định (ADMIN, USER) khi ứng dụng khởi động lần đầu, idempotent —
 * không tạo trùng nếu Role đã tồn tại. Thay thế việc phải insert SQL thủ công vì
 * dự án chưa dùng Flyway.
 */
@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        seedRoleIfMissing("ADMIN", "Administrator");
        seedRoleIfMissing("USER", "User");
    }

    private void seedRoleIfMissing(String code, String name) {
        if (roleRepository.findByCode(code).isEmpty()) {
            Role role = new Role();
            role.setCode(code);
            role.setName(name);
            roleRepository.save(role);
        }
    }
}
