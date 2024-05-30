package com.example.usedAuction.controller;

import com.example.usedAuction.dto.general.GeneralTransactionDto;
import com.example.usedAuction.dto.auction.AuctionTransactionDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.service.auction.AuctionTransactionService;
import com.example.usedAuction.service.general.GeneralTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
@Tag(name = "메인",description = "HomeController")
public class HomeController {
    private final GeneralTransactionService generalTransactionService;
    private final AuctionTransactionService auctionTransactionService;

    @GetMapping(value = "/main")
    @Operation(summary = "메인 API",description = "조회수 기반의 인기글, 최근 작성된 일반 거래, 경매 거래 글을 각 10개씩 조회")
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
    @Operation(summary = "검색 API",description = "글 제목과 내용에 검색어가 포함된 글들을 조회")
    @Parameter(name = "keyword", description = "검색할 키워드",in = ParameterIn.QUERY)
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
