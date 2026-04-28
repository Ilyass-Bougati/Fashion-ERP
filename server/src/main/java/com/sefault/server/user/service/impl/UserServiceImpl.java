package com.sefault.server.user.service.impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.user.dto.record.RegisterUserRecord;
import com.sefault.server.user.dto.record.UserRecord;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.mapper.UserMapper;
import com.sefault.server.user.repository.UserRepository;
import com.sefault.server.user.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserRecord findUserById(UUID id) {
        return userRepository
                .getUserProjectionById(id)
                .map(userMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id.toString()));
    }

    public UserRecord registerUser(RegisterUserRecord registerUserRecord) {
        User user = User.builder()
                .firstName(registerUserRecord.firstName().trim())
                .lastName(registerUserRecord.lastName().trim())
                .email(registerUserRecord.email().trim().toLowerCase())
                // TODO: Change this later
                .password(passwordEncoder.encode(registerUserRecord.password()))
                .phoneNumber(registerUserRecord.phoneNumber().trim())
                .active(true)
                .build();

        return userMapper.entityToRecord(userRepository.save(user));
    }

    public UserRecord updateUser(UUID id, RegisterUserRecord registerUserRecord) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id.toString()));

        user.setFirstName(registerUserRecord.firstName());
        user.setLastName(registerUserRecord.lastName());
        user.setEmail(registerUserRecord.email());
        user.setPhoneNumber(registerUserRecord.phoneNumber());

        return userMapper.entityToRecord(userRepository.save(user));
    }

    public UserRecord activateUser(UUID id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id.toString()));

        user.setActive(!user.getActive());
        return userMapper.entityToRecord(userRepository.save(user));
    }

    public void deleteUserById(UUID id) {
        userRepository.deleteById(id);
    }
}
