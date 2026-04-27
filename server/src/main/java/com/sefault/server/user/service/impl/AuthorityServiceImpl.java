package com.sefault.server.user.service.impl;

import com.sefault.server.user.dto.record.AuthorityRecord;
import com.sefault.server.user.entity.Authority;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.entity.UserAuthority;
import com.sefault.server.user.mapper.AuthorityMapper;
import com.sefault.server.user.repository.AuthorityRepository;
import com.sefault.server.user.repository.UserAuthorityRepository;
import com.sefault.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthorityServiceImpl {
    private final AuthorityRepository authorityRepository;
    private final UserAuthorityRepository userAuthorityRepository;
    private final UserRepository userRepository;
    private final AuthorityMapper authorityMapper;

    public void grantAuthority(UUID granteeId, UUID grantorId, UUID authorityId) {
        User grantee = userRepository.getReferenceById(granteeId);
        User grantor = userRepository.getReferenceById(grantorId);
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
}
