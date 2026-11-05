package com.expensetracker.user;

import com.expensetracker.common.exception.BadRequestException;
import com.expensetracker.common.exception.ResourceNotFoundException;
import com.expensetracker.user.dto.ChangePasswordRequest;
import com.expensetracker.user.dto.UpdateProfileRequest;
import com.expensetracker.user.dto.UserProfileResponse;
import com.expensetracker.user.entity.User;
import com.expensetracker.user.repository.UserRepository;
import com.expensetracker.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should return user profile for existing user ID")
    void shouldReturnUserProfile() {
        UUID userId = UUID.randomUUID();
        User user = new User("bob@example.com", "$2a$12$hash", "Bob Jones", "EUR");
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.getUserProfile(userId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getEmail()).isEqualTo("bob@example.com");
        assertThat(response.getFullName()).isEqualTo("Bob Jones");
        assertThat(response.getCurrency()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user ID does not exist")
    void shouldThrowWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should update user profile details")
    void shouldUpdateUserProfile() {
        UUID userId = UUID.randomUUID();
        User user = new User("bob@example.com", "$2a$12$hash", "Bob Jones", "EUR");
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest("Bob Updated", "GBP");
        UserProfileResponse response = userService.updateProfile(userId, request);

        assertThat(response.getFullName()).isEqualTo("Bob Updated");
        assertThat(response.getCurrency()).isEqualTo("GBP");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should change user password when current password matches")
    void shouldChangePasswordSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = new User("bob@example.com", "$2a$12$oldHash", "Bob Jones", "EUR");
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword123#", "$2a$12$oldHash")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword456#", "$2a$12$oldHash")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword456#")).thenReturn("$2a$12$newHash");

        ChangePasswordRequest request = new ChangePasswordRequest("OldPassword123#", "NewPassword456#");
        userService.changePassword(userId, request);

        assertThat(user.getPasswordHash()).isEqualTo("$2a$12$newHash");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should reject password change if current password is wrong")
    void shouldRejectWrongCurrentPassword() {
        UUID userId = UUID.randomUUID();
        User user = new User("bob@example.com", "$2a$12$oldHash", "Bob Jones", "EUR");
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "$2a$12$oldHash")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("WrongPassword", "NewPassword456#");

        assertThatThrownBy(() -> userService.changePassword(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current password");

        verify(userRepository, never()).save(user);
    }
}
