package com.sefault.server.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RateLimitExceededException extends RuntimeException {
    private final long retryAfterSeconds;
}
