package com.languagelearning.language_learning_backend.security;

import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Tra User theo username hoặc email (đăng nhập chấp nhận cả 2) để Spring Security xác thực.
 * Message lỗi khi không tìm thấy cố tình giống hệt message sai mật khẩu (xử lý ở AuthService)
 * để không tiết lộ username/email có tồn tại hay không — xem 11_FRS_TC_AUTH.md mục 1.2.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
    }
}
