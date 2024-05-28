package com.example.usedAuction.controller.auction;

import com.example.usedAuction.dto.auction.AuctionBidDto;
import com.example.usedAuction.dto.auction.AuctionTransactionBidFormDto;
import com.example.usedAuction.dto.auction.AuctionTransactionDto;
import com.example.usedAuction.dto.auction.AuctionTransactionFormDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.entity.TransactionImage;
import com.example.usedAuction.entity.auction.AuctionBid;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.auction.AuctionTransactionImage;
import com.example.usedAuction.service.auction.AuctionTransactionService;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuctionTransactionController {
    private final AuctionTransactionService auctionTransactionService;

    @PostMapping(value = "/auction", consumes = {
        MediaType.APPLICATION_JSON_VALUE,
                MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> postAuctionTransaction(@RequestPart @Valid AuctionTransactionFormDto auctionTransactionFormDto,
                                                         @RequestPart(required = false) List<MultipartFile> multipartFileList){
        return auctionTransactionService.postAuctionTransaction(auctionTransactionFormDto,multipartFileList);
    }

    @GetMapping(value = "/auction/{auctionTransactionId}")
    public ResponseEntity<Object> getAuctionTransaction(@PathVariable Integer auctionTransactionId){
        return auctionTransactionService.getAuctionTransaction(auctionTransactionId);
    }

    @GetMapping(value = "/auction")
    public ResponseEntity<Object> getAllAuctionTransaction(@RequestParam(required = false,defaultValue = "0") Integer page,
                                                           @RequestParam(required = false,defaultValue = "10") Integer size,
                                                           @RequestParam(required = false,defaultValue = "asc") String sort,
                                                           @RequestParam(required = false,defaultValue = "전체") String state,
                                                           @RequestParam(required = false,defaultValue = "") String keyword){
        String likeKeyword = "";
        if(keyword!=null){
            likeKeyword = keyword;
        }
        GetAllAuctionTransactionResultDto getAllAuctionTransactionResultDto = new GetAllAuctionTransactionResultDto();
        getAllAuctionTransactionResultDto.setTransactionList(auctionTransactionService.getAllAuctionTransaction(page,size,sort,state,likeKeyword));
        getAllAuctionTransactionResultDto.setTotalCount(auctionTransactionService.getAllTotalCount(state,likeKeyword));

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(getAllAuctionTransactionResultDto);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PutMapping(value = "/auction/{auctionTransactionId}", consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> updateAuctionTransaction(@PathVariable(value = "auctionTransactionId") Integer auctionTransactionId,
                                                           @RequestPart @Valid AuctionTransactionFormDto auctionTransactionFormDto,
                                                           @RequestPart(required = false) List<MultipartFile> multipartFileList){
        return auctionTransactionService.updateAuctionTransaction(auctionTransactionId,auctionTransactionFormDto,multipartFileList);
    }

    @DeleteMapping(value = "/auction/{auctionTransactionId}")
    public ResponseEntity<Object> deleteAuctionTransaction(@PathVariable Integer auctionTransactionId){
        return auctionTransactionService.deleteAuctionTransaction(auctionTransactionId);
    }

    @PostMapping(value = "/auction/bid")
    public ResponseEntity<Object> auctionBid(@RequestBody AuctionBidDto auctionBidDto){
        return auctionTransactionService.postAuctionTransactionBid(auctionBidDto);
    }

    @PutMapping(value = "/auction/bid")
    public ResponseEntity<Object> auctionBidUpdate(@RequestBody AuctionBidDto auctionBidDto){
        return auctionTransactionService.updateAuctionTransactionBid(auctionBidDto);
    }

    // 최고가 갱신
    @GetMapping(value = "/auction/{auctionTransactionId}/highest-bid")
    public ResponseEntity<Object> getAuctionTransactionHighestBid(@PathVariable Integer auctionTransactionId){
        return auctionTransactionService.getAuctionTransactionHighestBid(auctionTransactionId);
    }
    
    // 글 관련 전체
    @GetMapping(value = "/auction/{auctionTransactionId}/bid/all")
    public ResponseEntity<Object> getAllAuctionBid(@PathVariable Integer auctionTransactionId){
        return auctionTransactionService.getAllAuctionTransactionBid(auctionTransactionId);
    }

    // 사용자의 모든 입찰 정보
    @GetMapping(value = "/auction/bid/user")
    public ResponseEntity<Object> getAllUserAuctionBid(){
        return auctionTransactionService.getAllUserAuctionTransactionBid();
    }

    // 경매글 입장 시 로그인한 사용자 본인의 입찰정보
    @GetMapping(value = "/auction/{auctionTransactionId}/bid")
    public ResponseEntity<Object> getAuctionBid(@PathVariable Integer auctionTransactionId){
        return auctionTransactionService.getAuctionTransactionBid(auctionTransactionId);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    static class GetAllAuctionTransactionResultDto{
        List<AuctionTransactionDto> transactionList;
        int totalCount;
    }


}
