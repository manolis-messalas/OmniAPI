package com.messalas.spring_boot_demo_A.model.mappers;

import com.messalas.spring_boot_demo_A.model.dto.UserDetails;
import com.messalas.spring_boot_demo_A.model.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDetails userEntityToUserDetails(UserEntity userEntity);

    UserEntity userDetailsToUserEntity(UserDetails userDetails);


}
