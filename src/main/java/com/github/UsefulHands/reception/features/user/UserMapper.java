package com.github.UsefulHands.reception.features.user;

import com.github.UsefulHands.reception.features.user.dtos.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(UserEntity entity);

    @Mapping(target = "id", ignore = true)
    UserEntity toEntity(UserDto dto);
}