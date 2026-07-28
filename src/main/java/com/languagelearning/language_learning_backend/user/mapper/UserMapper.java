package com.languagelearning.language_learning_backend.user.mapper;

import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import com.languagelearning.language_learning_backend.user.entity.User;
import org.mapstruct.Mapper;

/**
 * Map User entity sang UserResponse - dùng chung giữa AuthService (register) và UserService
 * (GET/PUT /api/users/me) để không lặp lại cùng 1 logic map field ở 2 nơi. Tên field khớp
 * nhau giữa entity và DTO nên không cần khai báo @Mapping thủ công.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
