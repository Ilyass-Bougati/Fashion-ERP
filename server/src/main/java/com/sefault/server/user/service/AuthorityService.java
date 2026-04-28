package com.sefault.server.user.service;

import com.sefault.server.user.dto.record.AuthorityRecord;
import java.util.List;
import java.util.UUID;

public interface AuthorityService {
    void grantAuthority(UUID granteeId, UUID grantorId, UUID authorityId);

    void saveAuthority(String name);

    void removeAuthority(UUID userId, UUID authorityId);

    List<AuthorityRecord> getUserAuthorities(UUID userId);
}
