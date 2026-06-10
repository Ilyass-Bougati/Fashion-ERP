package com.sefault.server.runner;

import com.sefault.server.security.properties.ApplicationAuthorities;
import com.sefault.server.user.dto.record.AuthorityRecord;
import com.sefault.server.user.dto.record.RegisterUserRecord;
import com.sefault.server.user.entity.Authority;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.repository.AuthorityRepository;
import com.sefault.server.user.repository.UserRepository;
import com.sefault.server.user.service.AuthorityService;
import com.sefault.server.user.service.UserService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultAuthoritiesAndAdminRunner implements CommandLineRunner {
    private final AuthorityRepository authorityRepository;
    private final AuthorityService authorityService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ApplicationAuthorities applicationAuthorities;

    @Override
    public void run(String... args) {
        seedAuthorities();

        User admin = userRepository.findByEmail("admin@gmail.com");
        if (admin == null) {
            userService.registerUser(
                    new RegisterUserRecord("admin", "admin", "admin@gmail.com", "adminadmin", "0000000000"));
            admin = userRepository.findByEmail("admin@gmail.com");
        }

        grantMissingAuthorities(admin);
    }

    /** Creates any authority declared on the {@link ApplicationAuthorities} bean that is not yet persisted. */
    private void seedAuthorities() {
        for (String authorityName : declaredAuthorityNames()) {
            if (!authorityRepository.existsByName(authorityName)) {
                authorityRepository.save(new Authority(null, new ArrayList<>(), authorityName));
            }
        }
    }

    /** Grants the admin user every authority it does not already hold. */
    private void grantMissingAuthorities(User admin) {
        Set<String> alreadyGranted = authorityService.getUserAuthorities(admin.getId()).stream()
                .map(AuthorityRecord::name)
                .collect(Collectors.toSet());

        for (Authority authority : authorityRepository.findAll()) {
            if (!alreadyGranted.contains(authority.getName())) {
                authorityService.grantAuthority(admin.getId(), admin.getEmail(), authority.getId());
            }
        }
    }

    /** Reflectively reads every authority name exposed as a getter on the {@link ApplicationAuthorities} bean. */
    private List<String> declaredAuthorityNames() {
        List<String> names = new ArrayList<>();
        for (Method method : applicationAuthorities.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("get")
                    && method.getParameterCount() == 0
                    && method.getReturnType() == String.class) {
                try {
                    names.add((String) method.invoke(applicationAuthorities));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to invoke method: " + method.getName(), e);
                }
            }
        }
        return names;
    }
}
