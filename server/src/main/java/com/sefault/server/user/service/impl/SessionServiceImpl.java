package com.sefault.server.user.service.impl;

import com.sefault.server.user.entity.Session;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.repository.SessionRepository;
import com.sefault.server.user.repository.UserRepository;
import java.util.UUID;

import com.sefault.server.user.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public void startSession(UUID userId) {
        User user = userRepository.getReferenceById(userId);
        Session session = Session.builder().user(user).build();
        sessionRepository.save(session);
    }

    public void endSession(UUID sessionId) {
        sessionRepository.endById(sessionId);
    }
}
