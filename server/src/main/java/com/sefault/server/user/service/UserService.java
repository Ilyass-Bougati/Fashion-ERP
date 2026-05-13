package com.sefault.server.user.service;

import com.sefault.server.user.dto.record.RegisterUserRecord;
import com.sefault.server.user.dto.record.UserRecord;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserRecord> findAllUsers(Pageable pageable);

    UserRecord findUserById(UUID id);

    UserRecord registerUser(RegisterUserRecord registerUserRecord);

    UserRecord updateUser(UUID id, RegisterUserRecord registerUserRecord);

    UserRecord activateUser(UUID id);

    UserRecord deactivateUser(UUID id);

    void deleteUserById(UUID id);
}
