package com.sefault.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sefault.server.user.dto.projection.AuthorityProjection;
import com.sefault.server.user.dto.record.AuthorityRecord;
import com.sefault.server.user.entity.Authority;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.entity.UserAuthority;
import com.sefault.server.user.mapper.AuthorityMapper;
import com.sefault.server.user.repository.AuthorityRepository;
import com.sefault.server.user.repository.UserAuthorityRepository;
import com.sefault.server.user.repository.UserRepository;
import com.sefault.server.user.service.impl.AuthorityServiceImpl;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorityServiceImplTest {

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private UserAuthorityRepository userAuthorityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorityMapper authorityMapper;

    @InjectMocks
    private AuthorityServiceImpl authorityService;

    private UUID granteeId;
    private UUID grantorId;
    private UUID authorityId;
    private UUID userId;
    private User grantee;
    private User grantor;
    private Authority authority;

    @BeforeEach
    void setUp() {
        granteeId = UUID.randomUUID();
        grantorId = UUID.randomUUID();
        authorityId = UUID.randomUUID();
        userId = UUID.randomUUID();

        grantee = User.builder().id(granteeId).build();
        grantor = User.builder().id(grantorId).build();
        authority = Authority.builder().id(authorityId).name("ROLE_ADMIN").build();
    }

    // -----------------------------------------------------------------------
    // grantAuthority
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("grantAuthority()")
    class GrantAuthority {

        @Test
        @DisplayName("builds a UserAuthority from the three references and saves it")
        void shouldBuildAndSaveUserAuthority() {
            when(userRepository.getReferenceById(granteeId)).thenReturn(grantee);
            when(userRepository.getReferenceById(grantorId)).thenReturn(grantor);
            when(authorityRepository.getReferenceById(authorityId)).thenReturn(authority);

            authorityService.grantAuthority(granteeId, grantorId, authorityId);

            ArgumentCaptor<UserAuthority> captor = ArgumentCaptor.forClass(UserAuthority.class);
            verify(userAuthorityRepository).save(captor.capture());

            UserAuthority saved = captor.getValue();
            assertThat(saved.getUser()).isEqualTo(grantee);
            assertThat(saved.getGrantedBy()).isEqualTo(grantor);
            assertThat(saved.getAuthority()).isEqualTo(authority);
        }

        @Test
        @DisplayName("fetches references for all three IDs exactly once")
        void shouldFetchEachReferenceOnce() {
            when(userRepository.getReferenceById(granteeId)).thenReturn(grantee);
            when(userRepository.getReferenceById(grantorId)).thenReturn(grantor);
            when(authorityRepository.getReferenceById(authorityId)).thenReturn(authority);

            authorityService.grantAuthority(granteeId, grantorId, authorityId);

            verify(userRepository).getReferenceById(granteeId);
            verify(userRepository).getReferenceById(grantorId);
            verify(authorityRepository).getReferenceById(authorityId);
            verifyNoMoreInteractions(userRepository, authorityRepository);
        }
    }

    // -----------------------------------------------------------------------
    // saveAuthority
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("saveAuthority()")
    class SaveAuthority {

        @Test
        @DisplayName("persists an Authority built from the given name")
        void shouldSaveAuthorityWithGivenName() {
            String name = "ROLE_MODERATOR";

            authorityService.saveAuthority(name);

            ArgumentCaptor<Authority> captor = ArgumentCaptor.forClass(Authority.class);
            verify(authorityRepository).save(captor.capture());

            assertThat(captor.getValue().getName()).isEqualTo(name);
        }

        @Test
        @DisplayName("does not interact with any other repository")
        void shouldOnlyUseAuthorityRepository() {
            authorityService.saveAuthority("ROLE_X");

            verifyNoInteractions(userRepository, userAuthorityRepository);
        }
    }

    // -----------------------------------------------------------------------
    // removeAuthority
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("removeAuthority()")
    class RemoveAuthority {

        @Test
        @DisplayName("delegates deletion to the join-table repository with correct IDs")
        void shouldDeleteByUserIdAndAuthorityId() {
            authorityService.removeAuthority(userId, authorityId);

            verify(userAuthorityRepository).deleteByUser_IdAndAuthority_Id(userId, authorityId);
            verifyNoMoreInteractions(userAuthorityRepository);
        }

        @Test
        @DisplayName("does not interact with any other repository")
        void shouldOnlyUseUserAuthorityRepository() {
            authorityService.removeAuthority(userId, authorityId);

            verifyNoInteractions(userRepository, authorityRepository);
        }
    }

    // -----------------------------------------------------------------------
    // getUserAuthorities
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("getUserAuthorities()")
    class GetUserAuthorities {

        @Test
        @DisplayName("returns mapped records for all authorities found")
        void shouldReturnMappedRecords() {
            AuthorityProjection proj1 = mock(AuthorityProjection.class);
            when(proj1.getId()).thenReturn(UUID.randomUUID());
            when(proj1.getName()).thenReturn("ROLE_A");

            AuthorityProjection proj2 = mock(AuthorityProjection.class);
            when(proj2.getId()).thenReturn(UUID.randomUUID());
            when(proj2.getName()).thenReturn("ROLE_B");

            AuthorityRecord record1 = new AuthorityRecord(proj1.getId(), proj1.getName());
            AuthorityRecord record2 = new AuthorityRecord(proj2.getId(), proj2.getName());

            when(authorityRepository.getAuthoritiesByUserId(userId)).thenReturn(List.of(proj1, proj2));
            when(authorityMapper.projectionToRecord(proj1)).thenReturn(record1);
            when(authorityMapper.projectionToRecord(proj2)).thenReturn(record2);

            List<AuthorityRecord> result = authorityService.getUserAuthorities(userId);

            assertThat(result).containsExactly(record1, record2);
        }

        @Test
        @DisplayName("returns an empty list when the user has no authorities")
        void shouldReturnEmptyListWhenNoAuthorities() {
            when(authorityRepository.getAuthoritiesByUserId(userId)).thenReturn(List.of());

            List<AuthorityRecord> result = authorityService.getUserAuthorities(userId);

            assertThat(result).isEmpty();
            verify(authorityMapper, never()).projectionToRecord(any());
        }
    }
}
