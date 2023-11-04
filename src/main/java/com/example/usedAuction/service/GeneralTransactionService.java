package com.example.usedAuction.service;

import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.General.GeneralImageDto;
import com.example.usedAuction.dto.General.GeneralTransactionDto;
import com.example.usedAuction.dto.General.GeneralTransactionFormDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.entity.GeneralTransaction;
import com.example.usedAuction.entity.GeneralTransactionImage;
import com.example.usedAuction.entity.User;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import com.example.usedAuction.repository.GeneralTransactionImageRepository;
import com.example.usedAuction.repository.GeneralTransactionRepository;
import com.example.usedAuction.repository.UserRepository;
import com.example.usedAuction.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeneralTransactionService {
    private final GeneralTransactionRepository generalTransactionRepository;
    private final UserRepository userRepository;
    private final GeneralTransactionImageRepository generalTransactionImageRepository;
    private final S3UploadService s3UploadService;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public ResponseEntity<Object> postGeneralTransaction(GeneralTransactionFormDto generalTransactionFormDto, List<MultipartFile> imageFiles) {
        ResponseResult<Object> result = new ResponseResult<>();
        HttpStatus status = HttpStatus.CREATED;
        if(imageFiles.size()>10){ // 이미지 10개 이상일 경우 실패
            Map<String,Object> map = new HashMap<>();
            Map<String,Object> data = new HashMap<>();
            data.put("message", ErrorEnum.IMAGE_MAX_COUNT);
            map.put("data",data);
            result.setStatus("fail");
            result.setData(map);
            status = HttpStatus.BAD_REQUEST;
        }else{
            // 로그인된 유저 정보
            User loginUser = SecurityUtil.getCurrentUsername()
                    .flatMap(userRepository::findOneWithAuthoritiesByUsername)
                    .orElseThrow(()->new ApiException(ErrorEnum.UNAUTHORIZED_ERROR));
            GeneralTransaction generalTransaction = DataMapper.instance.generalTransactionFormToEntity(generalTransactionFormDto);
            generalTransaction.setUserId(loginUser);
            GeneralTransaction getGeneralTransaction=null;
            
            // 게시글 저장 
            try{
                getGeneralTransaction = generalTransactionRepository.save(generalTransaction);
            }catch (Exception e){
                throw new ApiException(ErrorEnum.GENERAL_TRANSACTION_POST_FAIL);
            }
            // 이미지 저장(s3 업로드) 후 결과 반환
            try{
                List<GeneralTransactionImage> imageList = new ArrayList<>();
                List<GeneralTransactionImage> successGeneralImageDtoList=null;

                for(int i=1;i<=imageFiles.size();i++){
                    GeneralImageDto generalImageDto = new GeneralImageDto();
                    MultipartFile image = imageFiles.get(i-1);
                    generalImageDto.setImageSeq(i);
                    generalImageDto.setOriginUrl(image.getOriginalFilename());

                    String origin_url = generalImageDto.getOriginUrl().substring(generalImageDto.getOriginUrl().lastIndexOf(".")+1);
                    String uploadFilename = UUID.randomUUID() + "."+origin_url;
                    generalImageDto.setImageUrl(uploadFilename);
                    generalImageDto.setUploadUrl(s3UploadService.uploadImage(image,uploadFilename));

                    GeneralTransactionImage generalTransactionImage = DataMapper.instance.generalImageDtoToEntity(generalImageDto);
                    generalTransactionImage.setGeneralTransactionId(getGeneralTransaction);
                    imageList.add(generalTransactionImage);

                }

                    successGeneralImageDtoList =  generalTransactionImageRepository.saveAll(imageList);
                    List<GeneralImageDto> resultImageList =   successGeneralImageDtoList.stream()
                            .map(DataMapper.instance::generalImageEntityToDto)
                            .collect(Collectors.toList());

                    GeneralTransactionDto resultGeneralTransactionDtoDto = DataMapper.instance.generalTransactionToDto(getGeneralTransaction);
                    resultGeneralTransactionDtoDto.setImages(resultImageList);
                    
                    Map<String,Object> map = new HashMap<>();
                    Map<Object,Object> data = new HashMap<>();
                    data.put(resultGeneralTransactionDtoDto.getGeneralTransactionId(), resultGeneralTransactionDtoDto);
                    map.put("data",data);
                    result.setStatus("success");
                    result.setData(map);

            }catch (Exception e ){
                throw  new ApiException(ErrorEnum.IMAGE_UPLOAD_FAIL);
            }
        }
        return ResponseEntity.status(status).body(result);
    }
}
