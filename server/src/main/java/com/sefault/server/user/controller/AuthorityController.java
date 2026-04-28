package com.sefault.server.user.controller;

import com.sefault.server.user.dto.record.AuthorityRecord;
import com.sefault.server.user.service.AuthorityService;
import io.lettuce.core.dynamic.annotation.Param;
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

    @GetMapping("/{userId}")
    public ResponseEntity<List<AuthorityRecord>> getAuthorities(@PathVariable UUID userId) {
        return ResponseEntity.ok(authorityService.getUserAuthorities(userId));
    }

    @PostMapping
    public void grantAuthority(
            Principal principal, @Param("granteeId") UUID granteeId, @Param("authorityId") UUID authorityId) {
        String grantorEmail = principal.getName();
        authorityService.grantAuthority(granteeId, grantorEmail, authorityId);
    }

    @DeleteMapping
    public void grantAuthority(@Param("userId") UUID userId, @Param("authorityId") UUID authorityId) {
        authorityService.removeAuthority(userId, authorityId);
    }
}
