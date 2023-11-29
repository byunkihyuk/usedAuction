package com.example.usedAuction.controller.auction;

import com.example.usedAuction.dto.auction.AuctionTransactionBidFormDto;
import com.example.usedAuction.dto.auction.AuctionTransactionFormDto;
import com.example.usedAuction.entity.TransactionImage;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.auction.AuctionTransactionImage;
import com.example.usedAuction.service.auction.AuctionTransactionService;
import lombok.RequiredArgsConstructor;
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
                                                           @RequestParam(required = false,defaultValue = "asc") String sort){
        return auctionTransactionService.getAllAuctionTransaction(page,size,sort);
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

    @PostMapping(value = "/auction/{auctionTransactionId}/bid")
    public ResponseEntity<Object> auctionBid(@PathVariable Integer auctionTransactionId,@RequestBody AuctionTransactionBidFormDto auctionTransactionBidFormDto){
        return auctionTransactionService.postAuctionTransactionBid(auctionTransactionBidFormDto,auctionTransactionId);
    }

    @GetMapping(value = "/auction/{auctionTransactionId}/bid")
    public ResponseEntity<Object> getAuctionBid(@PathVariable Integer auctionTransactionId){
        return auctionTransactionService.getAuctionTransactionBid(auctionTransactionId);
    }

}
