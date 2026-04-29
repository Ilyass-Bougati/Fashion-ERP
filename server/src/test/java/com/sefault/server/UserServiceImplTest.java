package com.sefault.server;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.user.dto.projection.UserProjection;
import com.sefault.server.user.dto.record.RegisterUserRecord;
import com.sefault.server.user.dto.record.UserRecord;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.mapper.UserMapper;
import com.sefault.server.user.repository.UserRepository;
import com.sefault.server.user.service.impl.UserServiceImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private User user;
    private UserRecord userRecord;
    private RegisterUserRecord registerUserRecord;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("securePassword123")
                .phoneNumber("+1234567890")
                .active(true)
                .build();

        userRecord = new UserRecord(
                userId,
                "John",
                "Doe",
                "john.doe@example.com",
                "+1234567890",
                true,
                LocalDateTime.now(),
                LocalDateTime.now());

        registerUserRecord =
                new RegisterUserRecord("John", "Doe", "john.doe@example.com", "securePassword123", "+1234567890");
    }

    // -------------------------------------------------------------------------
    // findUserById
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findUserById")
    class FindUserById {

        @Test
        @DisplayName("returns UserRecord when user exists")
        void returnsUserRecord_whenUserExists() {
            // Arrange
            var projection = mock(UserProjection.class);
            when(userRepository.getUserProjectionById(userId)).thenReturn(Optional.of(projection));
            when(userMapper.projectionToRecord(projection)).thenReturn(userRecord);

            // Act
            UserRecord result = userService.findUserById(userId);

            // Assert
            assertThat(result).isEqualTo(userRecord);
            verify(userRepository).getUserProjectionById(userId);
            verify(userMapper).projectionToRecord(projection);
        }

        @Test
        @DisplayName("throws NotFoundException when user does not exist")
        void throwsNotFoundException_whenUserNotFound() {
            // Arrange
            when(userRepository.getUserProjectionById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.findUserById(userId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(userId.toString());

            verify(userMapper, never()).projectionToRecord(any());
        }
    }

    // -------------------------------------------------------------------------
    // registerUser
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("registerUser")
    class RegisterUser {

        @Test
        @DisplayName("saves and returns mapped UserRecord")
        void savesAndReturnsMappedUserRecord() {
            // Arrange
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.entityToRecord(user)).thenReturn(userRecord);

            // Act
            UserRecord result = userService.registerUser(registerUserRecord);

            // Assert
            assertThat(result).isEqualTo(userRecord);
            verify(userRepository).save(any(User.class));
            verify(userMapper).entityToRecord(user);
        }

        @Test
        @DisplayName("trims and lowercases fields before saving")
        void trimsAndLowercasesFields_beforeSaving() {
            // Arrange
            RegisterUserRecord dirtyRecord = new RegisterUserRecord(
                    "  Jane  ", "  Smith  ", "  Jane.Smith@Example.COM  ", "pass123", "  9876543210  ");

            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.entityToRecord(any())).thenReturn(userRecord);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            // Act
            userService.registerUser(dirtyRecord);

            // Assert
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();

            assertThat(saved.getFirstName()).isEqualTo("Jane");
            assertThat(saved.getLastName()).isEqualTo("Smith");
            assertThat(saved.getEmail()).isEqualTo("jane.smith@example.com");
            assertThat(saved.getPhoneNumber()).isEqualTo("9876543210");
        }

        @Test
        @DisplayName("sets active to true for new users")
        void setsActiveTrue_forNewUser() {
            // Arrange
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.entityToRecord(any())).thenReturn(userRecord);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            // Act
            userService.registerUser(registerUserRecord);

            // Assert
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getActive()).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // updateUser
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("updates fields and returns mapped UserRecord")
        void updatesFieldsAndReturnsMappedUserRecord() {
            // Arrange
            RegisterUserRecord updateRecord =
                    new RegisterUserRecord("Jane", "Smith", "jane.smith@example.com", "newPass", "9876543210");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.entityToRecord(user)).thenReturn(userRecord);

            // Act
            UserRecord result = userService.updateUser(userId, updateRecord);

            // Assert
            assertThat(result).isEqualTo(userRecord);
            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getEmail()).isEqualTo("jane.smith@example.com");
            assertThat(user.getPhoneNumber()).isEqualTo("9876543210");

            verify(userRepository).findById(userId);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("throws NotFoundException when user does not exist")
        void throwsNotFoundException_whenUserNotFound() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.updateUser(userId, registerUserRecord))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(userId.toString());

            verify(userRepository, never()).save(any());
            verify(userMapper, never()).entityToRecord(any());
        }
    }

    // -------------------------------------------------------------------------
    // activateUser
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("activateUser")
    class ActivateUser {

        @Test
        @DisplayName("toggles active from true to false")
        void togglesActiveFromTrueToFalse() {
            // Arrange
            user.setActive(true);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.entityToRecord(user)).thenReturn(userRecord);

            // Act
            userService.activateUser(userId);

            // Assert
            assertThat(user.getActive()).isFalse();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("toggles active from false to true")
        void togglesActiveFromFalseToTrue() {
            // Arrange
            user.setActive(false);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.entityToRecord(user)).thenReturn(userRecord);

            // Act
            userService.activateUser(userId);

            // Assert
            assertThat(user.getActive()).isTrue();
        }

        @Test
        @DisplayName("returns mapped UserRecord after toggling")
        void returnsMappedUserRecord_afterToggling() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.entityToRecord(user)).thenReturn(userRecord);

            // Act
            UserRecord result = userService.activateUser(userId);

            // Assert
            assertThat(result).isEqualTo(userRecord);
        }

        @Test
        @DisplayName("throws NotFoundException when user does not exist")
        void throwsNotFoundException_whenUserNotFound() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.activateUser(userId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(userId.toString());

            verify(userRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // deleteUserById
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteUserById")
    class DeleteUserById {

        @Test
        @DisplayName("delegates deletion to repository")
        void delegatesDeletionToRepository() {
            // Arrange
            doNothing().when(userRepository).deleteById(userId);

            // Act
            userService.deleteUserById(userId);

            // Assert
            verify(userRepository).deleteById(userId);
            verifyNoInteractions(userMapper);
        }
    }
}
