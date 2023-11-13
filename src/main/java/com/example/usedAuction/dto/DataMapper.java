package com.example.usedAuction.dto;

import com.example.usedAuction.dto.General.GeneralTransactionImageDto;
import com.example.usedAuction.dto.General.GeneralTransactionDto;
import com.example.usedAuction.dto.General.GeneralTransactionFormDto;
import com.example.usedAuction.dto.user.UserSignUpFormDto;
import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.general.GeneralTransactionImage;
import com.example.usedAuction.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DataMapper {
    DataMapper instance = Mappers.getMapper(DataMapper.class);

    User userToEntity(UserSignUpFormDto userSignUpFormDto);
    GeneralTransaction generalTransactionFormToEntity(GeneralTransactionFormDto generalTransactionFormDto);
    @Mapping(source = "generalTransactionId",target = "generalTransactionId.generalTransactionId")
    GeneralTransactionImage generalImageDtoToEntity(GeneralTransactionImageDto generalTransactionImageDto);

    /***
     * entity to dto method
     * */
    @Mapping(source = "userId.userId",target = "userId")
    GeneralTransactionDto generalTransactionToDto(GeneralTransaction generalTransaction);

    @Mapping(source = "generalTransactionId.generalTransactionId",target = "generalTransactionId")
    GeneralTransactionImageDto generalImageEntityToDto(GeneralTransactionImage img);
}
