package com.sefault.server.user.dto.projection;

import com.sefault.server.user.entity.UserAuthority;
import java.util.List;
import java.util.UUID;

public interface AuthorityProjection {
    UUID getId();

    List<UserAuthority> getUserAuthorities();

    String getName();
}
