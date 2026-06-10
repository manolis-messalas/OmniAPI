package com.messalas.spring_boot_demo_A.service;

import com.messalas.spring_boot_demo_A.model.dto.UserDetails;
import com.messalas.spring_boot_demo_A.model.entities.UserEntity;
import com.messalas.spring_boot_demo_A.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void saveUser(UserDetails userDetails){
        log.info("Saving..."+ userDetails.toString());
        UserEntity userEntityToSave = new UserEntity(userDetails.getId(), userDetails.getUsername(), userDetails.getPassword(), userDetails.getRole());
        userRepository.save(userEntityToSave);
    }

    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username
                        )
                );

        return UserDetails.builder()
                .username(user.getUsername())
                .password(user.getPassword())   // already bcrypt hashed in DB
                .role(user.getRole().replace("ROLE_", ""))
                .build();
    }


}
