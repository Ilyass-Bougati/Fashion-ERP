package com.sefault.server.user.entity.controller;

import com.sefault.server.user.service.impl.AuthorityServiceImpl;
import io.lettuce.core.dynamic.annotation.Param;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authority")
@RequiredArgsConstructor
public class AuthorityController {
    private final AuthorityServiceImpl authorityService;

    @PostMapping
    public void grantAuthority(@Param("granteeId") UUID granteeId, @Param("grantorId") UUID grantorId, @Param("authorityId") UUID authorityId) {
        authorityService.grantAuthority(granteeId, grantorId, authorityId);
    }
}
