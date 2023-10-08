package com.example.usedAuction.dto;

import com.example.usedAuction.dto.user.UserSignUpFormDto;
import com.example.usedAuction.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DataMapper {
    DataMapper instance = Mappers.getMapper(DataMapper.class);

    User userToEntity(UserSignUpFormDto userSignUpFormDto);

}
