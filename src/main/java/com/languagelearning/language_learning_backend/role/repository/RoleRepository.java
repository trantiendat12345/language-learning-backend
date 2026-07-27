package com.languagelearning.language_learning_backend.role.repository;

import com.languagelearning.language_learning_backend.role.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);
}
