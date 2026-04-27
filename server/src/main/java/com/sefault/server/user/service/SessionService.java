package com.sefault.server.user.service;

import java.util.UUID;

public interface SessionService {
    void startSession(UUID userId);

    void endSession(UUID sessionId);
}
