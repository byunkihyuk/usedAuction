package com.example.usedAuction.service.auction;

import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.General.GeneralTransactionDto;
import com.example.usedAuction.dto.auction.AuctionTransactionDto;
import com.example.usedAuction.dto.auction.AuctionTransactionFormDto;
import com.example.usedAuction.dto.auction.AuctionTransactionImageDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.auction.AuctionTransactionImage;
import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.general.GeneralTransactionImage;
import com.example.usedAuction.entity.user.User;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import com.example.usedAuction.repository.auction.AuctionTransactionImageRepository;
import com.example.usedAuction.repository.auction.AuctionTransactionRepository;
import com.example.usedAuction.repository.user.UserRepository;
import com.example.usedAuction.service.aws.S3UploadService;
import com.example.usedAuction.util.SecurityUtil;
import com.example.usedAuction.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionTransactionService {
    private final UserRepository userRepository;
    private final AuctionTransactionRepository auctionTransactionRepository;
    private final AuctionTransactionImageRepository auctionTransactionImageRepository;
    private final S3UploadService s3UploadService;

    @Transactional
    public ResponseEntity<Object> postAuctionTransaction(AuctionTransactionFormDto auctionTransactionFormDto, List<MultipartFile> multipartFileList) {
        ResponseResult<Object> result = new ResponseResult<>();
        HttpStatus status = HttpStatus.CREATED;
        if(multipartFileList!=null && multipartFileList.size()>10){ // 이미지 10개 이상일 경우 실패
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
            AuctionTransaction auctionTransaction = DataMapper.instance.auctionTransactionFormToEntity(auctionTransactionFormDto);
            auctionTransaction.setSeller(loginUser);
            AuctionTransaction getAuctionTransaction=null;
            // 게시글 저장 후 반환
            try{
                getAuctionTransaction = auctionTransactionRepository.save(auctionTransaction);
            }catch (Exception e){
                throw new ApiException(ErrorEnum.GENERAL_TRANSACTION_POST_ERROR);
            }
            // 이미지 저장(s3 업로드) 후 결과 반환
            List<AuctionTransactionImage> imageList = new ArrayList<>();
            List<AuctionTransactionImage> successGeneralImageDtoList=null;
            List<AuctionTransactionImageDto> resultImageList = new ArrayList<>();
            try{
                if(multipartFileList!=null){ // 이미지가 있을때만 실행
                    for(int i=1;i<=multipartFileList.size();i++){
                        AuctionTransactionImageDto auctionTransactionImageDto = new AuctionTransactionImageDto();
                        MultipartFile image = multipartFileList.get(i-1);
                        auctionTransactionImageDto.setImageSeq(i);
                        auctionTransactionImageDto.setOriginName(image.getOriginalFilename());

                        // 이미지 uuid로 만들어진 이름으로 저장
                        String imageFilename = ServiceUtil.makeUploadFileName(auctionTransactionImageDto.getOriginName());
                        auctionTransactionImageDto.setImageName(imageFilename);
                        // 업로드 후 url 저장
                        auctionTransactionImageDto.setUploadUrl(s3UploadService.uploadImage(image,imageFilename));

                        AuctionTransactionImage auctionTransactionImage = DataMapper.instance.auctionImageDtoToEntity(auctionTransactionImageDto);
                        auctionTransactionImage.setAuctionTransactionId(getAuctionTransaction);
                        imageList.add(auctionTransactionImage);
                        successGeneralImageDtoList =  auctionTransactionImageRepository.saveAll(imageList);
                        getAuctionTransaction.setThumbnail(successGeneralImageDtoList.get(0).getUploadUrl());
                        resultImageList = successGeneralImageDtoList.stream()
                                .map(DataMapper.instance::auctionImageEntityToDto)
                                .collect(Collectors.toList());
                    }
                }
            }catch (Exception e ){
                throw  new ApiException(ErrorEnum.IMAGE_UPLOAD_ERROR);
            }
            AuctionTransactionDto resultGeneralTransactionDto = DataMapper.instance.auctionTransactionToDto(getAuctionTransaction);
            resultGeneralTransactionDto.setImages(resultImageList);
            Map<String,Object> map = new HashMap<>();
            map.put("data",resultGeneralTransactionDto);
            result.setStatus("success");
            result.setData(map);
        }
        return ResponseEntity.status(status).body(result);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Object> getAuctionTransaction(Integer auctionTransactionId) {
        AuctionTransaction auctionTransaction = auctionTransactionRepository.findByAuctionTransactionId(auctionTransactionId)
                .orElseThrow(() -> new ApiException(ErrorEnum.NOT_FOUND_AUCTION_TRANSACTION));

        List<AuctionTransactionImage> auctionTransactionImages = auctionTransactionImageRepository.findAllByAuctionTransactionIdOrderByImageSeq(auctionTransaction);

        AuctionTransactionDto resultAuctionTransactionDto = DataMapper.instance.auctionTransactionToDto(auctionTransaction);
        resultAuctionTransactionDto.setImages(auctionTransactionImages.stream()
                .map(DataMapper.instance::auctionImageEntityToDto)
                .collect(Collectors.toList()));

        ResponseResult<Object> result = new ResponseResult<>();
        result.setData(resultAuctionTransactionDto);
        result.setStatus("success");
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
