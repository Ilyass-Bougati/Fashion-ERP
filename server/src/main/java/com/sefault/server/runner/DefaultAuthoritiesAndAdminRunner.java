package com.sefault.server.runner;

import com.sefault.server.security.properties.ApplicationAuthorities;
import com.sefault.server.user.dto.record.RegisterUserRecord;
import com.sefault.server.user.entity.Authority;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.repository.AuthorityRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.sefault.server.user.repository.UserRepository;
import com.sefault.server.user.service.AuthorityService;
import com.sefault.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAuthoritiesAndAdminRunner implements CommandLineRunner {
    private final AuthorityRepository authorityRepository;
    private final AuthorityService authorityService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ApplicationAuthorities applicationAuthorities;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.existsByEmail("admin@gmail.com")) {
            return;
        }

        log.warn("The logic for creating the authorities is not sound, update it in the future");
        authorityRepository.deleteAll();
        List<Authority> savedAuthorities = new ArrayList<>();
        List<Authority> authorities = new ArrayList<>();

        // Using reflection to create all the authorities present on the applicationAuthorities bean
        for (Method method : applicationAuthorities.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("get") && method.getParameterCount() == 0
                    && method.getReturnType() == String.class) {
                try {
                    String authorityName = (String) method.invoke(applicationAuthorities);
                    authorities.add(new Authority(null, new ArrayList<>(), authorityName));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to invoke method: " + method.getName(), e);
                }
            }
        }

        for (Authority authority : authorities) {
                savedAuthorities.add(authorityRepository.save(authority));
        }

        RegisterUserRecord adminUser = new RegisterUserRecord(
                "admin",
                "admin",
                "admin@gmail.com",
                "adminadmin",
                "0000000000"
        );

        // Creating the admin user
        userService.registerUser(adminUser);
        User admin = userRepository.findByEmail("admin@gmail.com");

        for (Authority authority : savedAuthorities) {
            authorityService.grantAuthority(admin.getId(), admin.getEmail(), authority.getId());
        }
    }
}
