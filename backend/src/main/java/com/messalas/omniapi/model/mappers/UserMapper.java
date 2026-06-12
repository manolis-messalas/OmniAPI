package com.messalas.omniapi.model.mappers;

import com.messalas.omniapi.model.dto.UserDetails;
import com.messalas.omniapi.model.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDetails userEntityToUserDetails(UserEntity userEntity);

    UserEntity userDetailsToUserEntity(UserDetails userDetails);


}
