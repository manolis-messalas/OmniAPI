package com.messalas.omniapi.service;

import com.messalas.omniapi.model.dto.UserDetails;
import com.messalas.omniapi.model.entities.UserEntity;
import com.messalas.omniapi.model.mappers.UserMapper;
import com.messalas.omniapi.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Long saveUser(UserDetails userDetails){
        log.info("Saving..."+ userDetails.toString());
        UserEntity userEntityToSave = UserMapper.INSTANCE.userDetailsToUserEntity(userDetails);
        return userRepository.save(userEntityToSave).getId();
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

    @Transactional
    public List<UserDetails> getAllUsers(){
        List<UserEntity> userEntities = userRepository.findAll();
        return userEntities.stream()
                .map(UserMapper.INSTANCE::userEntityToUserDetails)
                .toList();
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User with id " + id + " not found");
        }

        userRepository.deleteById(id);
    }


}
