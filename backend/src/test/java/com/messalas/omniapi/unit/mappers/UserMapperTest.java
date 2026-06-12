package com.messalas.omniapi.unit.mappers;

import com.messalas.omniapi.model.dto.UserDetails;
import com.messalas.omniapi.model.entities.UserEntity;
import com.messalas.omniapi.model.mappers.UserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserMapperTest {

    private static final Logger logger = LoggerFactory.getLogger(UserMapperTest.class);

    private UserEntity userEntity;
    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        // Mock Data for testUserEntityToUserDTO
        userEntity = UserEntity.builder().
                id(1L).
                username("testuser").
                password("testpassword").
                role("USER").
                build();
        // Mock Data for testUserDTOToUserEntity
        userDetails = UserDetails.builder().
                id(2L).
                username("testuser2").
                password("testpassword2").
                role("ADMIN").
                build();
    }

    @Test
    public void testUserEntityToUserDetails(){
        logger.info("Testing testUserEntityToUserDTO...");
        UserDetails userDetailsToTest = UserMapper.INSTANCE.userEntityToUserDetails(userEntity);
        logger.info("UserMapperTest instance created");

        Assertions.assertNotNull(userDetails, "userDetails fixture is null before mapping");
        Assertions.assertEquals(2L, userDetails.getId());
        Assertions.assertEquals("testuser2", userDetails.getUsername());

        Assertions.assertNotNull(userDetailsToTest);
        Assertions.assertEquals(userEntity.getId(), userDetailsToTest.getId());
        Assertions.assertEquals(userEntity.getUsername(), userDetailsToTest.getUsername());
        Assertions.assertEquals(userEntity.getPassword(), userDetailsToTest.getPassword());
        Assertions.assertEquals(userEntity.getRole(), userDetailsToTest.getRole());
        logger.info("testUserEntityToUserDetails passed successfully");
    }

    @Test
    public void testUserDetailsToUserEntity(){
        logger.info("Testing testUserDTOToUserEntity...");
        UserEntity userEntityToTest = UserMapper.INSTANCE.userDetailsToUserEntity(userDetails);
        logger.info("UserMapperTest instance created");

        Assertions.assertNotNull(userEntityToTest);
        Assertions.assertEquals(userDetails.getId(), userEntityToTest.getId());
        Assertions.assertEquals(userDetails.getUsername(), userEntityToTest.getUsername());
        Assertions.assertEquals(userDetails.getPassword(), userEntityToTest.getPassword());
        Assertions.assertEquals(userDetails.getRole(), userEntityToTest.getRole());
        logger.info("testUserDetailsToUserEntity passed successfully");

    }

}
