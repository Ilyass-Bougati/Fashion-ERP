package com.sefault.server.user.controller;

import com.sefault.server.user.dto.record.AuthorityRecord;
import com.sefault.server.user.service.AuthorityService;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authority")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority(@authorities.manageAuthoritiesAuthority)")
public class AuthorityController {
    private final AuthorityService authorityService;

    @GetMapping
    public ResponseEntity<List<AuthorityRecord>> getAllAuthorities() {
        return ResponseEntity.ok(authorityService.getAllAuthorities());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<AuthorityRecord>> getAuthorities(@PathVariable UUID userId) {
        return ResponseEntity.ok(authorityService.getUserAuthorities(userId));
    }

    @PostMapping
    public void grantAuthority(
            Principal principal,
            @RequestParam UUID granteeId,
            @RequestParam UUID authorityId) {
        authorityService.grantAuthority(granteeId, principal.getName(), authorityId);
    }

    @DeleteMapping
    public void removeAuthority(
            @RequestParam UUID userId,
            @RequestParam UUID authorityId) {
        authorityService.removeAuthority(userId, authorityId);
    }
}
