package com.messalas.omniapi.security;

import com.messalas.omniapi.model.entities.UserEntity;
import com.messalas.omniapi.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class OmniApiUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public OmniApiUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity entity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.withUsername(entity.getUsername())
                .password(entity.getPassword())
                .roles(entity.getRole().replace("ROLE_", ""))
                .build();
    }
}
