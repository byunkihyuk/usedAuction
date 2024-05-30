package com.example.usedAuction.controller.general;

import com.example.usedAuction.dto.general.GeneralTransactionDto;
import com.example.usedAuction.dto.general.GeneralTransactionFormDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import com.example.usedAuction.service.general.GeneralTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "중고 거래",description = "GeneralTransactionController")
public class GeneralTransactionController {
    private final GeneralTransactionService generalTransactionService;

    @Operation(summary = "중고 거래 글 등록 API (JWT 토큰 필요)",description = "중고 거래 글을 등록 ")
    @PostMapping(value = "/general")
    public ResponseEntity<Object> postGeneralTransaction(@RequestPart @Valid GeneralTransactionFormDto generalTransactionFormDto,
                                                         @RequestPart(required = false) List<MultipartFile> multipartFile ){
        return generalTransactionService.postGeneralTransaction(generalTransactionFormDto,multipartFile);
    }

    @Operation(summary = "중고 거래 글 상세 조회 API",description = "중고 거래 글 상세 조회")
    @GetMapping(value = "/general/{generalTransactionId}")
    public ResponseEntity<Object> getGeneralTransaction(@PathVariable Integer generalTransactionId){
        return generalTransactionService.getGeneralTransaction(generalTransactionId);
    }


    @Operation(summary = "중고 거래 글 전체 조회 API",description = "중고 거래 글 전체 조회")
    @Parameters({@Parameter(name = "page",description = "조회할 페이지"),
            @Parameter(name = "size",description = "한 페이지에 조회할 글 개수"),
            @Parameter(name = "sort",description = "정렬 방식 내림차순(desc), 오름차순(asc)"),
            @Parameter(name = "state",description = "판매중, 거래중, 판매완료"),
            @Parameter(name = "keyword",description = "검색할 키워드")})
    @GetMapping(value = "/general")
    public ResponseEntity<Object> getAllGeneralTransaction(@RequestParam(required = false,defaultValue = "0") Integer page,
                                                           @RequestParam(required = false,defaultValue = "20") Integer size,
                                                           @RequestParam(required = false,defaultValue = "asc") String sort,
                                                           @RequestParam(required = false,defaultValue = "전체") String state,
                                                           @RequestParam(required = false,defaultValue = "") String keyword){
        String likeKeyword = "";
        if(keyword!=null){
            likeKeyword = keyword;
        }
        GetAllGeneralTransactionResultDto getAllGeneralTransactionResultDto = new GetAllGeneralTransactionResultDto();
        getAllGeneralTransactionResultDto.setTransactionList(generalTransactionService.getAllGeneralTransaction(page,size,sort,state,likeKeyword));
        getAllGeneralTransactionResultDto.setTotalCount(generalTransactionService.getAllTotalCount(state,likeKeyword));

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(getAllGeneralTransactionResultDto);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "중고 거래 글 수정 API (JWT 토큰 필요)",description = "중고 거래 글 수정")
    @PutMapping(value = "/general/{generalTransactionId}",
            consumes = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> updateGeneralTransaction(
            @PathVariable(value = "generalTransactionId") Integer generalTransactionId,
            @RequestPart @Valid GeneralTransactionFormDto generalTransactionFormDto,
            @RequestPart(required = false) List<MultipartFile> multipartFile ){
        return generalTransactionService.updateGeneralTransaction(generalTransactionId,generalTransactionFormDto,multipartFile);

    }

    @Operation(summary = "중고 거래 글 삭제 API (JWT 토큰 필요)",description = "중고 거래 글 삭제")
    @DeleteMapping(value = "/general/{generalTransactionId}")
    public ResponseEntity<Object> deleteGeneralTransaction(@PathVariable Integer generalTransactionId) {
        return generalTransactionService.deleteGeneralTransaction(generalTransactionId);
    }

    @Operation(summary = "중고 거래 글 구매 요청 목록 조회 API (JWT 토큰 필요)",description = "중고 거래 글 구매 요청 목록 조회")
    @GetMapping(value = "/general/{generalTransactionId}/buy-request-list")
    public ResponseEntity<Object> getGeneralBuyRequestList(@PathVariable Integer generalTransactionId) {
        return generalTransactionService.getGeneralBuyRequestList(generalTransactionId);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    static class GetAllGeneralTransactionResultDto{
        List<GeneralTransactionDto> transactionList;
        int totalCount;
    }

}
