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
import com.example.usedAuction.service.see.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "경매 거래",description = "AuctionTransactionController")
public class AuctionTransactionController {
    private final AuctionTransactionService auctionTransactionService;
    private final SseService sseService;

    @PostMapping(value = "/auction", consumes = {
        MediaType.APPLICATION_JSON_VALUE,
                MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "경매 글 등록 API (JWT 토큰 필요)",description = "경매 글을 새로 등록")
//            ,requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    content = @Content(
//                        mediaType = MediaType.APPLICATION_JSON_VALUE,
//                        schema = @Schema(
//                                allOf = {AuctionTransactionFormDto.class},
//                                requiredProperties = {"",""}
//                        )))
    public ResponseEntity<Object> postAuctionTransaction(@Parameter(description = "글 등록 정보")@RequestPart @Valid AuctionTransactionFormDto auctionTransactionFormDto,
                                                         @Parameter(description = "Multipart Type 이미지 정보")@RequestPart(required = false) List<MultipartFile> multipartFileList){
        return auctionTransactionService.postAuctionTransaction(auctionTransactionFormDto,multipartFileList);
    }

    @Operation(summary = "경매 글 상세 조회 API",description = "경매 글 상세 조회")
    @GetMapping(value = "/auction/{auctionTransactionId}")
    public ResponseEntity<Object> getAuctionTransaction(@PathVariable Integer auctionTransactionId){
        return auctionTransactionService.getAuctionTransaction(auctionTransactionId);
    }

    @Operation(summary = "경매 글 전체 조회 API",description = "경매 글 전체 조회")
    @Parameters({@Parameter(name = "page",description = "조회할 페이지"),
            @Parameter(name = "size",description = "한 페이지에 조회할 글 개수"),
            @Parameter(name = "sort",description = "정렬 방식 내림차순(desc), 오름차순(asc)"),
            @Parameter(name = "state",description = "판매중, 거래중, 판매완료"),
            @Parameter(name = "keyword",description = "검색할 키워드")})
    @GetMapping(value = "/auction")
    public ResponseEntity<Object> getAllAuctionTransaction(@RequestParam(required = false,defaultValue = "0") Integer page,
                                                           @RequestParam(required = false,defaultValue = "20") Integer size,
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

    @Operation(summary = "경매 글 수정 API (JWT 토큰 필요)",description = "경매 글 수정")
    @PutMapping(value = "/auction/{auctionTransactionId}", consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> updateAuctionTransaction(@PathVariable(value = "auctionTransactionId") Integer auctionTransactionId,
                                                           @RequestPart @Valid AuctionTransactionFormDto auctionTransactionFormDto,
                                                           @RequestPart(required = false) List<MultipartFile> multipartFileList){
        return auctionTransactionService.updateAuctionTransaction(auctionTransactionId,auctionTransactionFormDto,multipartFileList);
    }

    @Operation(summary = "경매 글 삭제 API (JWT 토큰 필요)",description = "경매 글 삭제")
    @DeleteMapping(value = "/auction/{auctionTransactionId}")
    public ResponseEntity<Object> deleteAuctionTransaction(@PathVariable Integer auctionTransactionId){
        return auctionTransactionService.deleteAuctionTransaction(auctionTransactionId);
    }

    @Operation(summary = "경매 입찰 API (JWT 토큰 필요)",description = "경매 글 입찰")
    @PostMapping(value = "/auction/bid")
    public ResponseEntity<Object> auctionBid(@RequestBody AuctionBidDto auctionBidDto){
        return auctionTransactionService.postAuctionTransactionBid(auctionBidDto);
    }

    @Operation(summary = "경매 글 입찰 수정 API (JWT 토큰 필요)",description = "경매 글 입찰 수정")
    @PutMapping(value = "/auction/bid")
    public ResponseEntity<Object> auctionBidUpdate(@RequestBody AuctionBidDto auctionBidDto){
        return auctionTransactionService.updateAuctionTransactionBid(auctionBidDto);
    }


    // 최고가 갱신
    @Operation(summary = "경매 글 입찰 최고가 갱신 API",description = "경매 글 입찰 최고가 갱신")
    @GetMapping(value = "/auction/{auctionTransactionId}/highest-bid")
    public ResponseEntity<Object> getAuctionTransactionHighestBid(@PathVariable Integer auctionTransactionId){
        return auctionTransactionService.getAuctionTransactionHighestBid(auctionTransactionId);
    }
    
    // 글 관련 전체
    @Operation(summary = "현재 경매 글 전체 입찰 내역 조회 API (JWT 토큰 필요)",description = "현재 경매 글 전체 입찰 내역 조회")
    @GetMapping(value = "/auction/{auctionTransactionId}/bid/all")
    public ResponseEntity<Object> getAllAuctionBid(@PathVariable Integer auctionTransactionId){
        return auctionTransactionService.getAllAuctionTransactionBid(auctionTransactionId);
    }

    // 사용자의 모든 입찰 정보
    @Operation(summary = "내가 입찰한 모든 내역 조회 API (JWT 토큰 필요)",description = "내가 입찰한 모든 내역 조회")
    @GetMapping(value = "/auction/bid/user")
    public ResponseEntity<Object> getAllUserAuctionBid(){
        return auctionTransactionService.getAllUserAuctionTransactionBid();
    }

    // 경매글 입장 시 로그인한 사용자 본인의 입찰정보
    @Operation(summary = "경매 글 상세 조회 시 내 입찰 내역 조회 API (JWT 토큰 필요)",description = "경매 글 상세 조회 시 내 입찰 내역 조회")
    @GetMapping(value = "/auction/{auctionTransactionId}/bid")
    public ResponseEntity<Object> getAuctionBid(@PathVariable Integer auctionTransactionId){
        return auctionTransactionService.getAuctionTransactionBid(auctionTransactionId);
    }

//    @Operation(summary = "경매 글 입찰 최고가 갱신 API",description = "경매 글 입찰 최고가 갱신")
//    @GetMapping(value = "/auction/{auctionTransactionId}/subscribe",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public ResponseEntity<SseEmitter> auctionBidSubscriber(@PathVariable String auctionTransactionId){
//        return sseService.auctionTransactionSubscribe(auctionTransactionId);
//    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    static class GetAllAuctionTransactionResultDto{
        List<AuctionTransactionDto> transactionList;
        int totalCount;
    }


}
