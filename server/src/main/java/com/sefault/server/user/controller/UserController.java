package com.sefault.server.user.controller;

import com.sefault.server.user.dto.record.RegisterUserRecord;
import com.sefault.server.user.dto.record.UserRecord;
import com.sefault.server.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority(@authorities.listUsersAuthority)")
    public ResponseEntity<Page<UserRecord>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.findAllUsers(pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(@authorities.createUsersAuthority)")
    public ResponseEntity<UserRecord> registerUser(@RequestBody @Valid RegisterUserRecord registerUserRecord) {
        return ResponseEntity.ok(userService.registerUser(registerUserRecord));
    }

    @PostMapping("/activate/{id}")
    @PreAuthorize("hasAuthority(@authorities.activateUsersAuthority)")
    public ResponseEntity<UserRecord> activateUsers(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    @PostMapping("/deactivate/{id}")
    @PreAuthorize("hasAuthority(@authorities.activateUsersAuthority)")
    public ResponseEntity<UserRecord> deactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.getUsersAuthority)")
    public ResponseEntity<UserRecord> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.updateUsersAuthority)")
    public ResponseEntity<UserRecord> updateUser(
            @PathVariable UUID id, @RequestBody @Valid RegisterUserRecord registerUserRecord) {
        return ResponseEntity.ok(userService.updateUser(id, registerUserRecord));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(@authorities.deleteUsersAuthority)")
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteUserById(id);
    }
}
