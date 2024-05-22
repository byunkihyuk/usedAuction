package com.example.usedAuction.controller;

import com.example.usedAuction.dto.general.GeneralTransactionDto;
import com.example.usedAuction.dto.auction.AuctionTransactionDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.service.auction.AuctionTransactionService;
import com.example.usedAuction.service.general.GeneralTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController()
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class HomeController {
    private final GeneralTransactionService generalTransactionService;
    private final AuctionTransactionService auctionTransactionService;

    @GetMapping(value = "/main")
    public ResponseEntity<Object> getMain(){
        ResponseResult<Object> result = new ResponseResult<>();
        List<GeneralTransactionDto> topGeneralList = generalTransactionService.topGeneralList("viewCount");
        List<AuctionTransactionDto> topAuctionList = auctionTransactionService.topAuctionList("viewCount");
        List<Object> topList = new ArrayList<>();
        int generalIndex=0;
        int auctionIndex=0;
        int maxIndex=Math.max(topGeneralList.size(),topAuctionList.size());

        for(int i=0;i<maxIndex;i++){
            if(auctionIndex < topAuctionList.size() && generalIndex < topGeneralList.size()){
                if(topAuctionList.get(auctionIndex).getViewCount()>topGeneralList.get(generalIndex).getViewCount()){
                    topList.add(topAuctionList.get(auctionIndex++));
                }else{
                    topList.add(topGeneralList.get(generalIndex++));
                }
            }else if(auctionIndex >= topAuctionList.size()){
                topList.add(topGeneralList.get(generalIndex++));
            }else {
                topList.add(topAuctionList.get(auctionIndex++));
            }
        }

        Map<String,Object> data = new HashMap<>();
        result.setStatus("success");
        data.put("topList",topList);
        data.put("generalTransactionList", generalTransactionService.topGeneralList("createdAt"));
        data.put("auctionTransactionList",auctionTransactionService.topAuctionList("createdAt"));
        result.setData(data);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<Object> getSearch(@RequestParam String keyword){
        String keywordReg = "%"+keyword+"%";
        Map<String,Object> data = new HashMap<>();
        data.put("generalTransactionList",generalTransactionService.searchTopGeneralList(keywordReg));
        data.put("auctionTransactionList",auctionTransactionService.searchTopAuctionList(keywordReg));
        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(data);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


}
