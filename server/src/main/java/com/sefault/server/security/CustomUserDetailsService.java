package com.sefault.server.security;

import com.sefault.server.user.entity.User;
import com.sefault.server.user.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {

        User user = userRepository.findUserByEmailWithAuthorities(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        List<String> authorityNames = user.getUserAuthorities().stream()
                .map(userAuthority -> userAuthority.getAuthority().getName())
                .toList();

        return new CustomUserDetails(
                user.getEmail(),
                user.getPassword(),
                user.getActive(),
                authorityNames
        );
    }
}
