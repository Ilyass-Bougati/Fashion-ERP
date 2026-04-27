package com.sefault.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sefault.server.user.entity.Session;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.repository.SessionRepository;
import com.sefault.server.user.repository.UserRepository;
import com.sefault.server.user.service.impl.SessionServiceImpl;
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
@DisplayName("SessionServiceImpl")
class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @Nested
    @DisplayName("startSession()")
    class StartSession {

        private UUID userId;
        private User mockUser;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            mockUser = User.builder().id(userId).build();
        }

        @Test
        @DisplayName("fetches user reference by the provided userId")
        void shouldFetchUserReferenceById() {
            when(userRepository.getReferenceById(userId)).thenReturn(mockUser);

            sessionService.startSession(userId);

            verify(userRepository).getReferenceById(userId);
        }

        @Test
        @DisplayName("saves a new session linked to the fetched user")
        void shouldSaveSessionWithCorrectUser() {
            when(userRepository.getReferenceById(userId)).thenReturn(mockUser);

            sessionService.startSession(userId);

            ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
            verify(sessionRepository).save(sessionCaptor.capture());

            Session savedSession = sessionCaptor.getValue();
            assertThat(savedSession.getUser()).isEqualTo(mockUser);
        }

        @Test
        @DisplayName("saves a session that is active by default")
        void shouldSaveActiveSession() {
            when(userRepository.getReferenceById(userId)).thenReturn(mockUser);

            sessionService.startSession(userId);

            ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
            verify(sessionRepository).save(sessionCaptor.capture());

            assertThat(sessionCaptor.getValue().getActive()).isTrue();
        }

        @Test
        @DisplayName("saves exactly one session per call")
        void shouldSaveExactlyOneSession() {
            when(userRepository.getReferenceById(userId)).thenReturn(mockUser);

            sessionService.startSession(userId);

            verify(sessionRepository, times(1)).save(any(Session.class));
        }

        @Test
        @DisplayName("does not call endById when starting a session")
        void shouldNotCallEndById() {
            when(userRepository.getReferenceById(userId)).thenReturn(mockUser);

            sessionService.startSession(userId);

            verify(sessionRepository, never()).endById(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("endSession()")
    class EndSession {

        private UUID sessionId;

        @BeforeEach
        void setUp() {
            sessionId = UUID.randomUUID();
        }

        @Test
        @DisplayName("calls endById with the correct sessionId")
        void shouldCallEndByIdWithCorrectId() {
            sessionService.endSession(sessionId);

            verify(sessionRepository).endById(sessionId);
        }

        @Test
        @DisplayName("calls endById exactly once")
        void shouldCallEndByIdExactlyOnce() {
            sessionService.endSession(sessionId);

            verify(sessionRepository, times(1)).endById(sessionId);
        }

        @Test
        @DisplayName("does not interact with userRepository")
        void shouldNotInteractWithUserRepository() {
            sessionService.endSession(sessionId);

            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("does not call save when ending a session")
        void shouldNotCallSave() {
            sessionService.endSession(sessionId);

            verify(sessionRepository, never()).save(any(Session.class));
        }
    }
}
