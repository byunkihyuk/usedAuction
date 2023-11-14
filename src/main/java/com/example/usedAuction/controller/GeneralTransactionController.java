package com.example.usedAuction.controller;

import com.example.usedAuction.dto.General.GeneralTransactionFormDto;
import com.example.usedAuction.service.general.GeneralTransactionService;
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
public class GeneralTransactionController {
    private final GeneralTransactionService generalTransactionService;

    @PostMapping(value = "/general",
            consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> postGeneralTransaction(@RequestPart @Valid GeneralTransactionFormDto generalTransactionFormDto,
                                                         @RequestPart List<MultipartFile> multipartFile ){
        return generalTransactionService.postGeneralTransaction(generalTransactionFormDto,multipartFile);
    }

    @GetMapping(value = "/general/{generalTransactionId}")
    public ResponseEntity<Object> getGeneralTransaction(@PathVariable Integer generalTransactionId){
        return generalTransactionService.getGeneralTransaction(generalTransactionId);
    }


    @GetMapping(value = "/general")
    public ResponseEntity<Object> getAllGeneralTransaction(@RequestParam(required = false,defaultValue = "0") Integer page,
                                                           @RequestParam(required = false,defaultValue = "10") Integer size,
                                                           @RequestParam(required = false,defaultValue = "asc") String sort ){
        return generalTransactionService.getAllGeneralTransaction(page,size,sort);
    }

    @PutMapping(value = "/general/{generalTransactionId}",
            consumes = {MediaType.APPLICATION_JSON_VALUE,
                    MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> updateGeneralTransaction(
            @PathVariable(value = "generalTransactionId") Integer generalTransactionId,
            @RequestPart @Valid GeneralTransactionFormDto generalTransactionFormDto,
            @RequestPart(required = false) List<MultipartFile> multipartFile ){
        return generalTransactionService.updateGeneralTransaction(generalTransactionId,generalTransactionFormDto,multipartFile);

    }

    @DeleteMapping(value = "/general/{generalTransactionId}")
    public ResponseEntity<Object> deleteGeneralTransaction(@PathVariable Integer generalTransactionId) {
        return generalTransactionService.deleteGeneralTransaction(generalTransactionId);
    }

}
