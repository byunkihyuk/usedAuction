package com.example.usedAuction.controller.auction;

import com.example.usedAuction.dto.auction.AuctionTransactionFormDto;
import com.example.usedAuction.service.auction.AuctionTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping(value = "/auction/{auctionTransactionId}")
    public ResponseEntity<Object> getAuctionTransaction(@PathVariable Integer auctionTransactionId){
        return auctionTransactionService.getAuctionTransaction(auctionTransactionId);
    }

    @GetMapping(value = "/auction")
    public ResponseEntity<Object> getAllAuctionTransaction(@RequestParam(required = false,defaultValue = "0") Integer page,
                                                           @RequestParam(required = false,defaultValue = "10") Integer size,
                                                           @RequestParam(required = false,defaultValue = "asc") String sort){
        return auctionTransactionService.getAllAuctionTransaciton(page,size,sort);
    }


}
