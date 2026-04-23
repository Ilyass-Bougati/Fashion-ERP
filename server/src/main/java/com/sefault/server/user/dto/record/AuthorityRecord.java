package com.sefault.server.user.dto.record;

import com.sefault.server.user.entity.UserAuthority;
import java.util.List;
import java.util.UUID;

public record AuthorityRecord(UUID id, List<UserAuthority> userAuthorities, String name) {}
