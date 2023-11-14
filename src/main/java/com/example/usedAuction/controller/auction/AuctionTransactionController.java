package com.example.usedAuction.controller.auction;

import com.example.usedAuction.dto.auction.AuctionTransactionFormDto;
import com.example.usedAuction.service.auction.AuctionTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
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


}
