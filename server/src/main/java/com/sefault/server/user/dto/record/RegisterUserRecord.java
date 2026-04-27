package com.sefault.server.user.dto.record;

import com.sefault.server.annotation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record RegisterUserRecord(
        @NotEmpty String firstName,
        @NotEmpty String lastName,
        @NotEmpty @Email String email,
        @NotEmpty String password,
        @NotEmpty @PhoneNumber String phoneNumber) {}
