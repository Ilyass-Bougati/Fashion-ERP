package com.sefault.server.runner;

import com.sefault.server.user.entity.Authority;
import com.sefault.server.user.repository.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAuthoritiesRunner implements CommandLineRunner {
    private final AuthorityRepository authorityRepository;

    @Override
    public void run(String... args) throws Exception {
        log.warn("The logic for creating the authorities is not sound, update it in the future");
        authorityRepository.deleteAll();
        List<Authority> authorities = List.of(
                // User management authorities
                new Authority(null, new ArrayList<>(), "CREATE_USERS"),
                new Authority(null, new ArrayList<>(), "DELETE_USERS"),
                new Authority(null, new ArrayList<>(), "UPDATE_USERS"),
                new Authority(null, new ArrayList<>(), "GRANT_AUTHORITIES")
        );

        authorityRepository.saveAll(authorities);
        log.info("Create default authorities");
    }
}
