package com.example.usedAuction.dto;

import com.example.usedAuction.dto.General.GeneralImageDto;
import com.example.usedAuction.dto.General.GeneralTransactionDto;
import com.example.usedAuction.dto.General.GeneralTransactionFormDto;
import com.example.usedAuction.dto.user.UserSignUpFormDto;
import com.example.usedAuction.entity.GeneralTransaction;
import com.example.usedAuction.entity.GeneralTransactionImage;
import com.example.usedAuction.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DataMapper {
    DataMapper instance = Mappers.getMapper(DataMapper.class);

    User userToEntity(UserSignUpFormDto userSignUpFormDto);
    GeneralTransaction generalTransactionFormToEntity(GeneralTransactionFormDto generalTransactionFormDto);
    @Mapping(source = "generalTransactionId",target = "generalTransactionId.generalTransactionId")
    GeneralTransactionImage generalImageDtoToEntity(GeneralImageDto generalImageDto);

    /***
     * entity to dto method
     * */
    @Mapping(source = "userId.userId",target = "userId")
    GeneralTransactionDto generalTransactionToDto(GeneralTransaction generalTransaction);

    @Mapping(source = "generalTransactionId.generalTransactionId",target = "generalTransactionId")
    GeneralImageDto generalImageEntityToDto(GeneralTransactionImage img);
}
