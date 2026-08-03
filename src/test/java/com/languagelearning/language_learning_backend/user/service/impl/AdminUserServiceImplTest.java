package com.languagelearning.language_learning_backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.auth.entity.RefreshToken;
import com.languagelearning.language_learning_backend.auth.repository.RefreshTokenRepository;
import com.languagelearning.language_learning_backend.exception.BadRequestException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.progress.entity.CourseEnrollment;
import com.languagelearning.language_learning_backend.progress.mapper.CourseEnrollmentMapper;
import com.languagelearning.language_learning_backend.progress.repository.CourseEnrollmentRepository;
import com.languagelearning.language_learning_backend.user.dto.response.AdminUserProgressResponse;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.enums.UserStatus;
import com.languagelearning.language_learning_backend.user.mapper.UserMapper;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private CourseEnrollmentRepository courseEnrollmentRepository;

    private AdminUserServiceImpl adminUserService;

    @BeforeEach
    void setUp() {
        UserMapper userMapper = Mappers.getMapper(UserMapper.class);
        CourseEnrollmentMapper courseEnrollmentMapper = Mappers.getMapper(CourseEnrollmentMapper.class);
        adminUserService = new AdminUserServiceImpl(
                userRepository, userMapper, refreshTokenRepository, courseEnrollmentRepository, courseEnrollmentMapper);
    }

    private User user(Long id, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@test.com");
        user.setStatus(status);
        user.setXp(100);
        user.setCurrentStreak(3);
        user.setLongestStreak(5);
        return user;
    }

    @Test
    void getUsers_returnsMappedPage() {
        Page<User> page = new PageImpl<>(List.of(user(1L, UserStatus.ACTIVE)));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = adminUserService.getUsers("user", Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("user1");
    }

    @Test
    void getUserById_found_returnsResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserStatus.ACTIVE)));

        UserResponse response = adminUserService.getUserById(1L);

        assertThat(response.getUsername()).isEqualTo("user1");
    }

    @Test
    void getUserById_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUserById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void activateUser_setsStatusActive() {
        User user = user(1L, UserStatus.DISABLED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = adminUserService.activateUser(1L);

        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void disableUser_setsStatusDisabledAndRevokesRefreshTokens() {
        User user = user(1L, UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        RefreshToken token = new RefreshToken();
        token.setId(1L);
        token.setRevoked(false);
        when(refreshTokenRepository.findAllByUserIdAndRevokedFalse(1L)).thenReturn(List.of(token));

        UserResponse response = adminUserService.disableUser(1L, 999L);

        assertThat(response.getStatus()).isEqualTo(UserStatus.DISABLED);
        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).saveAll(List.of(token));
        verify(userRepository).save(user);
    }

    @Test
    void disableUser_targetIsSelf_throwsBadRequestException() {
        assertThatThrownBy(() -> adminUserService.disableUser(999L, 999L)).isInstanceOf(BadRequestException.class);
        verify(userRepository, never()).findById(any());
        verify(refreshTokenRepository, never()).findAllByUserIdAndRevokedFalse(any());
    }

    @Test
    void lockUser_setsStatusLockedAndRevokesRefreshTokens() {
        User user = user(1L, UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findAllByUserIdAndRevokedFalse(1L)).thenReturn(List.of());

        UserResponse response = adminUserService.lockUser(1L, 999L);

        assertThat(response.getStatus()).isEqualTo(UserStatus.LOCKED);
    }

    @Test
    void lockUser_targetIsSelf_throwsBadRequestException() {
        assertThatThrownBy(() -> adminUserService.lockUser(999L, 999L)).isInstanceOf(BadRequestException.class);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUserProgress_returnsXpStreakAndEnrollments() {
        User user = user(1L, UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(1L);
        enrollment.setEnrolledAt(LocalDateTime.now());
        when(courseEnrollmentRepository.findAllByUserId(1L)).thenReturn(List.of(enrollment));

        AdminUserProgressResponse response = adminUserService.getUserProgress(1L);

        assertThat(response.getUsername()).isEqualTo("user1");
        assertThat(response.getXp()).isEqualTo(100);
        assertThat(response.getCurrentStreak()).isEqualTo(3);
        assertThat(response.getCourseEnrollments()).hasSize(1);
    }
}
