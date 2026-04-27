package com.sefault.server.user.service;

import com.sefault.server.user.dto.record.RegisterUserRecord;
import com.sefault.server.user.dto.record.UserRecord;
import java.util.UUID;

public interface UserService {
    UserRecord findUserById(UUID id);

    UserRecord registerUser(RegisterUserRecord registerUserRecord);

    UserRecord updateUser(UUID id, RegisterUserRecord registerUserRecord);

    UserRecord activateUser(UUID id);

    void deleteUserById(UUID id);
}
