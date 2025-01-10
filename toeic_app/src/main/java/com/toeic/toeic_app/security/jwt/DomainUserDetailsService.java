package com.toeic.toeic_app.security.jwt;

import com.toeic.toeic_app.model.User;
import com.toeic.toeic_app.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UserRepo userRepo;

    public DomainUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String username) {
        log.debug("Authenticating {}", username);

        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Perform additional case-sensitive check
        if (!user.getEmail().equals(username)) {
            throw new UsernameNotFoundException("login.failed");
        }
        return userRepo
                .findByEmail(username)
                .map(this::createSpringSecurityUser)
                .orElseThrow(() -> new UsernameNotFoundException("username was not found in the database"));

    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(User userCustom) {
        return new org.springframework.security.core.userdetails.User(userCustom.getEmail(), userCustom.getPassword(), Collections.emptyList());
    }
}
