package com.languagelearning.language_learning_backend.role.repository;

import com.languagelearning.language_learning_backend.role.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
