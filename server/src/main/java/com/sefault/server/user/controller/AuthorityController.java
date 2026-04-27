package com.sefault.server.user.controller;

import com.sefault.server.user.dto.record.AuthorityRecord;
import com.sefault.server.user.service.AuthorityService;
import com.sefault.server.user.service.impl.AuthorityServiceImpl;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authority")
@RequiredArgsConstructor
public class AuthorityController {
    private final AuthorityService authorityService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<AuthorityRecord>> getAuthorities(@PathVariable UUID userId) {
        return ResponseEntity.ok(authorityService.getUserAuthorities(userId));
    }

    @PostMapping
    public void grantAuthority(
            @Param("granteeId") UUID granteeId,
            @Param("grantorId") UUID grantorId,
            @Param("authorityId") UUID authorityId) {
        authorityService.grantAuthority(granteeId, grantorId, authorityId);
    }

    @DeleteMapping
    public void grantAuthority(@Param("userId") UUID userId, @Param("authorityId") UUID authorityId) {
        authorityService.removeAuthority(userId, authorityId);
    }
}
