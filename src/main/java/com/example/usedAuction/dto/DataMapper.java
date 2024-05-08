package com.example.usedAuction.dto;

import com.example.usedAuction.dto.auction.*;
import com.example.usedAuction.dto.chat.ChattingMessageForm;
import com.example.usedAuction.dto.chat.ChattingMessageDto;
import com.example.usedAuction.dto.chat.ChattingRoomDto;
import com.example.usedAuction.dto.general.GeneralTransactionImageDto;
import com.example.usedAuction.dto.general.GeneralTransactionDto;
import com.example.usedAuction.dto.general.GeneralTransactionFormDto;
import com.example.usedAuction.dto.payment.PayInfoDto;
import com.example.usedAuction.dto.user.UserDto;
import com.example.usedAuction.dto.user.UserSignUpFormDto;
import com.example.usedAuction.entity.auction.AuctionBid;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.auction.AuctionTransactionImage;
import com.example.usedAuction.entity.chat.ChattingMessage;
import com.example.usedAuction.entity.chat.ChattingRoom;
import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.general.GeneralTransactionImage;
import com.example.usedAuction.entity.payment.PayInfo;
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


    @Mapping(source = "bidder.userId",target = "bidder")
    @Mapping(source = "auctionTransactionId.auctionTransactionId",target = "auctionTransactionId")
    AuctionBidDto auctionBidEntityToDto(AuctionBid auctionBid);

    @Mapping(source = "bidder",target = "bidder.userId")
    @Mapping(source = "auctionTransactionId",target = "auctionTransactionId.auctionTransactionId")
    AuctionBid auctionBidDtoToEntity(AuctionBidDto auctionBidDto);

    @Mapping(source = "sender.userId",target = "sender")
    @Mapping(source = "receiver.userId",target = "receiver")
    ChattingRoomDto chattingRoomEntityToDto(ChattingRoom chattingRoom);

//    @Mapping(source = "sender", target = "sender.userId")
//    @Mapping(source = "receiver", target = "receiver.userId")
//    @Mapping(source = "roomId", target = "roomId.roomId")
//    ChattingMessage chattingFormToEntity(ChattingMessageForm msg);
//
//    @Mapping(source = "sender.userId", target = "sender")
//    @Mapping(source = "receiver.userId", target = "receiver")
//    @Mapping(source = "roomId.roomId", target = "roomId")
//    ChattingMessageForm chattingEntityToForm(ChattingMessage resultMessage);

    @Mapping(source = "sender", target = "sender.userId")
    @Mapping(source = "receiver", target = "receiver.userId")
    ChattingRoom chattingRoomDtoToEntity(ChattingRoomDto createRoomDto);

    @Mapping(source = "roomId.roomId", target = "roomId")
    @Mapping(source = "sender.userId", target = "sender")
    //@Mapping(source = "receiver.userId", target = "receiver")
    ChattingMessageDto chattingMessageEntityToDto(ChattingMessage message);

    @Mapping(source = "roomId", target = "roomId.roomId")
    @Mapping(source = "sender", target = "sender.userId")
    ChattingMessage chattingMessageDtoToEntity(ChattingMessageDto msg);

    @Mapping(source = "seller", target = "seller.userId")
    @Mapping(source = "buyer", target = "buyer.userId")
    @Mapping(source = "generalTransactionId", target = "generalTransactionId.generalTransactionId")
    @Mapping(source = "auctionTransactionId", target = "auctionTransactionId.auctionTransactionId")
    PayInfo payInfoDtoToEntity(PayInfoDto payInfoDto);

    @Mapping(source = "seller.userId", target = "seller")
    @Mapping(source = "buyer.userId", target = "buyer")
    @Mapping(source = "generalTransactionId.generalTransactionId", target = "generalTransactionId")
    @Mapping(source = "auctionTransactionId.auctionTransactionId", target = "auctionTransactionId")
    PayInfoDto payInfoEntityToDto(PayInfo payInfoEntity);

}
