package com.messalas.spring_boot_demo_A.unit;

import com.messalas.spring_boot_demo_A.model.dto.UserDetails;
import com.messalas.spring_boot_demo_A.model.entities.UserEntity;
import com.messalas.spring_boot_demo_A.repository.UserRepository;
import com.messalas.spring_boot_demo_A.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserEntity userEntity;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
                .id(1L)
                .username("ManoloAdmin")
                .password("$2a$10$bcryptHashedPassword")
                .role("ROLE_ADMIN")
                .build();

        userDetails = UserDetails.builder()
                .id(1L)
                .username("ManoloAdmin")
                .password("$2a$10$bcryptHashedPassword")
                .role("ROLE_ADMIN")
                .build();
    }

    @Test
    void saveUser_ShouldSaveUserAndReturnId() {
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        Long result = userService.saveUser(userDetails);

        assertNotNull(result);
        assertEquals(1L, result);

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        when(userRepository.findByUsername("ManoloAdmin"))
                .thenReturn(Optional.of(userEntity));

        UserDetails result = userService.loadUserByUsername("ManoloAdmin");

        assertNotNull(result);
        assertEquals("ManoloAdmin", result.getUsername());
        assertEquals("$2a$10$bcryptHashedPassword", result.getPassword());
        assertEquals("ADMIN", result.getRole());

        verify(userRepository, times(1)).findByUsername("ManoloAdmin");
    }

    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowUsernameNotFoundException() {
        when(userRepository.findByUsername("unknownUser"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("unknownUser")
        );

        assertEquals("User not found: unknownUser", exception.getMessage());

        verify(userRepository, times(1)).findByUsername("unknownUser");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        UserEntity secondUser = UserEntity.builder()
                .id(2L)
                .username("RegularUser")
                .password("$2a$10$anotherHashedPassword")
                .role("ROLE_USER")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(userEntity, secondUser));

        List<UserDetails> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals("ManoloAdmin", result.get(0).getUsername());
        assertEquals("$2a$10$bcryptHashedPassword", result.get(0).getPassword());
        assertEquals("ROLE_ADMIN", result.get(0).getRole());

        assertEquals(2L, result.get(1).getId());
        assertEquals("RegularUser", result.get(1).getUsername());
        assertEquals("$2a$10$anotherHashedPassword", result.get(1).getPassword());
        assertEquals("ROLE_USER", result.get(1).getRole());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldThrowEntityNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.deleteUser(99L)
        );

        assertEquals("User with id 99 not found", exception.getMessage());

        verify(userRepository, times(1)).existsById(99L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}