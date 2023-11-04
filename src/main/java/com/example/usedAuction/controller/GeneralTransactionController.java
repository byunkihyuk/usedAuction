package com.example.usedAuction.controller;

import com.example.usedAuction.dto.General.GeneralTransactionFormDto;
import com.example.usedAuction.service.GeneralTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GeneralTransactionController {
    private final GeneralTransactionService generalTransactionService;

    @PostMapping(value = "/general",consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> postGeneralTransaction(@RequestPart @Valid GeneralTransactionFormDto generalTransactionFormDto,
                                                         @RequestPart List<MultipartFile> multipartFile ){
        return generalTransactionService.postGeneralTransaction(generalTransactionFormDto,multipartFile);
    }
}
