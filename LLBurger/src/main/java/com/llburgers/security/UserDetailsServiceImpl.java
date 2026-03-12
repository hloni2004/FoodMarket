package com.llburgers.security;

import com.llburgers.domain.Admin;
import com.llburgers.domain.User;
import com.llburgers.domain.enums.AdminLevel;
import com.llburgers.domain.enums.Role;
import com.llburgers.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads a {@link User} entity from the database so Spring Security can verify
 * credentials during the authentication step.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Super admins get both ADMIN and SUPER roles for full permission inheritance
        List<SimpleGrantedAuthority> authorities;
        if (user.getEffectiveRole() == Role.SUPER) {
            authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_SUPER")
            );
        } else {
            authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountLocked(!user.isActive())
                .build();
    }
}
