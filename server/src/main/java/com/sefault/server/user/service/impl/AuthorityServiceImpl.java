package com.sefault.server.user.service.impl;

import com.sefault.server.user.dto.record.AuthorityRecord;
import com.sefault.server.user.entity.Authority;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.entity.UserAuthority;
import com.sefault.server.user.mapper.AuthorityMapper;
import com.sefault.server.user.repository.AuthorityRepository;
import com.sefault.server.user.repository.UserAuthorityRepository;
import com.sefault.server.user.repository.UserRepository;
import com.sefault.server.user.service.AuthorityService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthorityServiceImpl implements AuthorityService {
    private final AuthorityRepository authorityRepository;
    private final UserAuthorityRepository userAuthorityRepository;
    private final UserRepository userRepository;
    private final AuthorityMapper authorityMapper;

    public void grantAuthority(UUID granteeId, String grantorEmail, UUID authorityId) {
        User grantee = userRepository.getReferenceById(granteeId);
        User grantor = userRepository.findByEmail(grantorEmail);
        Authority authority = authorityRepository.getReferenceById(authorityId);

        UserAuthority userAuthority = UserAuthority.builder()
                .user(grantee)
                .grantedBy(grantor)
                .authority(authority)
                .build();
        userAuthorityRepository.save(userAuthority);
    }

    public void saveAuthority(String name) {
        Authority authority = Authority.builder().name(name).build();
        authorityRepository.save(authority);
    }

    public void removeAuthority(UUID userId, UUID authorityId) {
        userAuthorityRepository.deleteByUser_IdAndAuthority_Id(userId, authorityId);
    }

    public List<AuthorityRecord> getUserAuthorities(UUID userId) {
        return authorityRepository.getAuthoritiesByUserId(userId).stream()
                .map(authorityMapper::projectionToRecord)
                .toList();
    }
}
