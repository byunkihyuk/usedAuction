package com.example.usedAuction.dto;

import com.example.usedAuction.dto.General.GeneralTransactionImageDto;
import com.example.usedAuction.dto.General.GeneralTransactionDto;
import com.example.usedAuction.dto.General.GeneralTransactionFormDto;
import com.example.usedAuction.dto.auction.AuctionTransactionDto;
import com.example.usedAuction.dto.auction.AuctionTransactionFormDto;
import com.example.usedAuction.dto.auction.AuctionTransactionImageDto;
import com.example.usedAuction.dto.user.UserDto;
import com.example.usedAuction.dto.user.UserSignUpFormDto;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.auction.AuctionTransactionImage;
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
    @Mapping(source = "seller.userId",target = "seller")
    @Mapping(source = "buyer.userId",target = "buyer")
    GeneralTransactionDto generalTransactionToDto(GeneralTransaction generalTransaction);

    @Mapping(source = "generalTransactionId.generalTransactionId",target = "generalTransactionId")
    GeneralTransactionImageDto generalImageEntityToDto(GeneralTransactionImage img);

    // Auction
    AuctionTransaction auctionTransactionFormToEntity(AuctionTransactionFormDto auctionTransactionFormDto);

    @Mapping(source = "auctionTransactionId",target = "auctionTransactionId.auctionTransactionId")
    AuctionTransactionImage auctionImageDtoToEntity(AuctionTransactionImageDto auctionTransactionImageDto);

    @Mapping(source = "auctionTransactionId.auctionTransactionId",target = "auctionTransactionId")
    AuctionTransactionImageDto auctionImageEntityToDto(AuctionTransactionImage auctionTransactionImage);

    @Mapping(source = "seller.userId",target = "seller")
    @Mapping(source = "buyer.userId",target = "buyer")
    AuctionTransactionDto auctionTransactionToDto(AuctionTransaction auctionTransaction);

    UserDto UserEntityToDto(User user);
}
