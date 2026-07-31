package com.languagelearning.language_learning_backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.auth.entity.RefreshToken;
import com.languagelearning.language_learning_backend.auth.exception.InvalidCredentialsException;
import com.languagelearning.language_learning_backend.auth.exception.NewPasswordSameAsCurrentException;
import com.languagelearning.language_learning_backend.auth.exception.PasswordMismatchException;
import com.languagelearning.language_learning_backend.auth.repository.RefreshTokenRepository;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.user.dto.request.ChangePasswordRequest;
import com.languagelearning.language_learning_backend.user.dto.request.UserUpdateRequest;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.enums.DailyGoalType;
import com.languagelearning.language_learning_backend.user.mapper.UserMapper;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        UserMapper userMapper = Mappers.getMapper(UserMapper.class);
        userService = new UserServiceImpl(userRepository, userMapper, passwordEncoder, refreshTokenRepository);
    }

    private User existingUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user01");
        user.setEmail("user01@test.com");
        user.setPasswordHash("hashed-current-password");
        user.setDisplayName("Old Name");
        user.setTimezone("Asia/Ho_Chi_Minh");
        return user;
    }

    @Test
    void getMyProfile_withExistingUser_returnsMappedResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));

        UserResponse response = userService.getMyProfile(1L);

        assertThat(response.getUsername()).isEqualTo("user01");
        assertThat(response.getDisplayName()).isEqualTo("Old Name");
    }

    @Test
    void getMyProfile_whenUserNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyProfile(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    private UserUpdateRequest updateRequest() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setDisplayName("New Name");
        request.setAvatarUrl("https://example.com/avatar.png");
        request.setBirthday(LocalDate.of(2000, 1, 1));
        request.setGender("MALE");
        request.setCountry("Vietnam");
        request.setCurrentLevel("B1");
        request.setDailyGoalType(DailyGoalType.TIME);
        request.setDailyGoalValue(20);
        return request;
    }

    @Test
    void updateMyProfile_withValidData_updatesEditableFieldsOnly() {
        User user = existingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateMyProfile(1L, updateRequest());

        assertThat(response.getDisplayName()).isEqualTo("New Name");
        assertThat(response.getCountry()).isEqualTo("Vietnam");
        assertThat(response.getCurrentLevel()).isEqualTo("B1");
        assertThat(response.getDailyGoalType()).isEqualTo(DailyGoalType.TIME);
        assertThat(response.getDailyGoalValue()).isEqualTo(20);
        // Field không được phép sửa qua endpoint này (username/email) phải giữ nguyên.
        assertThat(response.getUsername()).isEqualTo("user01");
        assertThat(response.getEmail()).isEqualTo("user01@test.com");
    }

    @Test
    void updateMyProfile_whenUserNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateMyProfile(1L, updateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).save(any());
    }

    private ChangePasswordRequest changePasswordRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("CurrentPass1");
        request.setNewPassword("NewPassw0rd1");
        request.setConfirmPassword("NewPassw0rd1");
        return request;
    }

    @Test
    void changePassword_withValidData_updatesPasswordAndRevokesOldRefreshTokens() {
        User user = existingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CurrentPass1", "hashed-current-password")).thenReturn(true);
        when(passwordEncoder.matches("NewPassw0rd1", "hashed-current-password")).thenReturn(false);
        when(passwordEncoder.encode("NewPassw0rd1")).thenReturn("hashed-new-password");
        RefreshToken oldToken = new RefreshToken();
        oldToken.setId(1L);
        oldToken.setUser(user);
        oldToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        oldToken.setRevoked(false);
        when(refreshTokenRepository.findAllByUserIdAndRevokedFalse(1L)).thenReturn(List.of(oldToken));

        userService.changePassword(1L, changePasswordRequest());

        assertThat(user.getPasswordHash()).isEqualTo("hashed-new-password");
        assertThat(oldToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).saveAll(List.of(oldToken));
    }

    @Test
    void changePassword_whenConfirmPasswordMismatch_throwsPasswordMismatchException() {
        ChangePasswordRequest request = changePasswordRequest();
        request.setConfirmPassword("Different1");

        assertThatThrownBy(() -> userService.changePassword(1L, request)).isInstanceOf(PasswordMismatchException.class);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void changePassword_whenCurrentPasswordWrong_throwsInvalidCredentialsException() {
        User user = existingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CurrentPass1", "hashed-current-password")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, changePasswordRequest()))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_whenNewPasswordSameAsCurrent_throwsNewPasswordSameAsCurrentException() {
        User user = existingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CurrentPass1", "hashed-current-password")).thenReturn(true);
        when(passwordEncoder.matches("NewPassw0rd1", "hashed-current-password")).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword(1L, changePasswordRequest()))
                .isInstanceOf(NewPasswordSameAsCurrentException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_whenUserNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword(1L, changePasswordRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
