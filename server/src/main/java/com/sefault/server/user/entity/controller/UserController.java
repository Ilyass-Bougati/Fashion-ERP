package com.sefault.server.user.entity.controller;

import com.sefault.server.user.dto.record.RegisterUserRecord;
import com.sefault.server.user.dto.record.UserRecord;
import com.sefault.server.user.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userService;

    @PostMapping
    public ResponseEntity<UserRecord> registerUser(@RequestBody @Valid RegisterUserRecord registerUserRecord) {
        return ResponseEntity.ok(userService.registerUser(registerUserRecord));
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<UserRecord> activateUsers(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRecord> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserRecord> updateUser(@PathVariable UUID id, @RequestBody @Valid RegisterUserRecord registerUserRecord) {
        return ResponseEntity.ok(userService.updateUser(id, registerUserRecord));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteUserById(id);
    }
}
