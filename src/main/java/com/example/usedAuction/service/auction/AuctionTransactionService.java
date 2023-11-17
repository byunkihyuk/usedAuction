package com.example.usedAuction.service.auction;

import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.auction.AuctionTransactionDto;
import com.example.usedAuction.dto.auction.AuctionTransactionFormDto;
import com.example.usedAuction.dto.auction.AuctionTransactionImageDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.dto.result.ResponseResultError;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.auction.AuctionTransactionImage;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @Transactional(readOnly = true)
    public ResponseEntity<Object> getAllAuctionTransaction(Integer page, Integer size, String sort) {
        Sort pageableSort = sort.equals("asc") ? Sort.by("createdAt").ascending()  :
                Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page,size, pageableSort);
        List<AuctionTransactionDto> resultGeneralTransaction = auctionTransactionRepository.findAll(pageable).stream()
                .map(DataMapper.instance::auctionTransactionToDto).collect(Collectors.toList());
        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(resultGeneralTransaction);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> deleteAuctionTransaction(Integer auctionTransactionId) {
        ResponseResult<Object> result = new ResponseResult<>();
        HttpStatus status = HttpStatus.OK;

        AuctionTransaction auctionTransaction = auctionTransactionRepository.findByAuctionTransactionId(auctionTransactionId)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_AUCTION_TRANSACTION));
        //s3 이미지 삭제 하기 위해서 가져옴
        List<AuctionTransactionImage> auctionTransactionImageList = auctionTransactionImageRepository.findAllByAuctionTransactionId(auctionTransaction);
        int a =1;
        try{
            auctionTransactionRepository.delete(auctionTransaction);
            auctionTransactionImageRepository.deleteAll(auctionTransactionImageList);
            s3UploadService.deleteImages(auctionTransactionImageList);
            result.setStatus("success");
            AuctionTransactionDto auctionTransactionDto = DataMapper.instance.auctionTransactionToDto(auctionTransaction);
            Map<String,Object> data = new HashMap<>();
            data.put("message",auctionTransactionDto.getAuctionTransactionId()+"번 글 삭제 성공");
            result.setData(data);
        }catch (Exception e){
            ResponseResultError error = new ResponseResultError("error","글 삭제 실패");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        return ResponseEntity.status(status).body(result);
    }

    @Transactional
    public ResponseEntity<Object> updateAuctionTransaction(Integer auctionTransactionId, AuctionTransactionFormDto auctionTransactionFormDto, List<MultipartFile> multipartFileList) {
        ResponseResult<Object> result = new ResponseResult<>();
        HttpStatus resultHttp = HttpStatus.OK;
        AuctionTransaction auctionTransaction = auctionTransactionRepository.findByAuctionTransactionId(auctionTransactionId)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_AUCTION_TRANSACTION));

        // 글 정보 수정
        updateTransactionEntity(auctionTransaction,auctionTransactionFormDto);

        List<AuctionTransactionImage> generalTransactionImages = auctionTransactionImageRepository.findAllByAuctionTransactionIdOrderByImageSeq(auctionTransaction);
        List<AuctionTransactionImageDto> updateTransactionImage=null;

        try{ // 이미지 수정
            updateTransactionImage = updateTransactionImage(generalTransactionImages,multipartFileList,auctionTransaction);
        }catch (Exception e){
            throw new ApiException(ErrorEnum.IMAGE_UPDATE_ERROR);
        }
        result.setStatus("success");
        Map<String,Object> data = new HashMap<>();
        AuctionTransactionDto resultGeneralDto = DataMapper.instance.auctionTransactionToDto(auctionTransaction);
        resultGeneralDto.setImages(updateTransactionImage);
        data.put("data",resultGeneralDto);
        result.setData(data);

        if(multipartFileList!=null && !multipartFileList.isEmpty()){
            if(updateTransactionImage==null){
                data = new HashMap<>();
                data.put("message","일반 거래 글 수정 실패");
                result.setData(data);
                result.setStatus("fail");
                resultHttp = HttpStatus.BAD_REQUEST;
            }
        }
        return ResponseEntity.status(resultHttp).body(result);
    }
    private List<AuctionTransactionImageDto> updateTransactionImage(List<AuctionTransactionImage> auctionTransactionImages, List<MultipartFile> multipartFile, AuctionTransaction auctionTransaction) throws IOException {
        List<AuctionTransactionImage> deleteList = new ArrayList<>();
        List<AuctionTransactionImage> updateList = new ArrayList<>();
        List<AuctionTransactionImage> resultUpdateList = null;
        // 업데이트 하려는 이미지가 없을 경우
        if (multipartFile == null) {
            // DB에는 이미지가 있으면 DB 이미지 삭제
            auctionTransaction.setThumbnail(null);
            if (!auctionTransactionImages.isEmpty()) {
                deleteList.addAll(auctionTransactionImages);
                auctionTransactionImageRepository.deleteAll(auctionTransactionImages);
                if (!s3UploadService.deleteImages(deleteList)) {
                    throw new ApiException(ErrorEnum.IMAGE_DELETE_ERROR);
                }
                return null;
            }
            return null;
        }

        if (!multipartFile.isEmpty()) {
            //이름 목록
            List<String> newOriginUrlList = multipartFile.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.toList());
            List<String> updateOriginUrlList = auctionTransactionImages.stream().map(AuctionTransactionImage::getOriginName).collect(Collectors.toList());

            int maxIndex = Math.max(auctionTransactionImages.size(), multipartFile.size());
            for (int i = 0; i < maxIndex; i++) {
                AuctionTransactionImage imgEntity = null;
                if (i > updateOriginUrlList.size() - 1) { // 입력 이미지가 많은 경우
                    // 기존
                    if (updateOriginUrlList.contains(newOriginUrlList.get(i))) {
                        int updateIndex = updateOriginUrlList.indexOf(newOriginUrlList.get(i));
                        imgEntity = auctionTransactionImages.get(updateIndex);
                        imgEntity.setImageSeq(i + 1);
                        updateList.add(imgEntity);
                    } else {
                        updateList.add(uploadAndConvertFileToEntity(multipartFile.get(i), i + 1, auctionTransaction));
                    }
                } else if (i > newOriginUrlList.size() - 1) { // 기존 이미지가 많은 경우
                    // 새로운 목록은 다 수정 했기 때문에 남은건 새로운 목록에 있는지 확인 후 삭제
                    imgEntity = auctionTransactionImages.get(i);
                    if (!newOriginUrlList.contains(imgEntity.getOriginName())) {
                        deleteList.add(imgEntity);
                    }
                } else { // 크기 같을 때
                    imgEntity = auctionTransactionImages.get(i);
                    if (newOriginUrlList.contains(imgEntity.getOriginName())) { // 새로운 목록에 기존 이미지 있음
                        if (imgEntity.getOriginName().equals(newOriginUrlList.get(i))) { // 기존과 수정 위치가 같음
                            updateList.add(imgEntity); // 그대로 수정 목록에 추가
                        } else if (updateOriginUrlList.contains(newOriginUrlList.get(i))) { //----------- 여기 수정해야됨 양쪽 목록에 있지만 서로 위치가 다를때
                            int updateIndex = updateOriginUrlList.indexOf(newOriginUrlList.get(i));
                            imgEntity = auctionTransactionImages.get(updateIndex);
                            imgEntity.setImageSeq(i + 1);
                            updateList.add(imgEntity);
                        } else {
                            // 새로 추가된 사진이므로 업로드 후 수정 목록에 추가
                            updateList.add(uploadAndConvertFileToEntity(multipartFile.get(i), i + 1, auctionTransaction));
                        }
                    } else { // 현재 기존 이미지 새로운 목록에 없음
                        if (updateOriginUrlList.contains(newOriginUrlList.get(i))) { // 현재 기존과는 다르지만 기존 목록에 있던것 이므로 위치 수정
                            int updateIndex = updateOriginUrlList.indexOf(newOriginUrlList.get(i));
                            imgEntity = auctionTransactionImages.get(updateIndex);
                            imgEntity.setImageSeq(i + 1);
                            updateList.add(imgEntity);
                            imgEntity = auctionTransactionImages.get(i);
                            deleteList.add(imgEntity);
                        } else { //  현재 목록 기존 목록 아무것도 일치 안함 삭제
                            deleteList.add(imgEntity);
                            updateList.add(uploadAndConvertFileToEntity(multipartFile.get(i), i + 1, auctionTransaction));
                        }
                    }
                }
            }

            // 삭제 목록 이미지 s3에서 삭제 후 DB 삭제
            if (!deleteList.isEmpty()) {
                auctionTransactionImageRepository.deleteAll(deleteList);
                if (!s3UploadService.deleteImages(deleteList)) {
                    throw new ApiException(ErrorEnum.IMAGE_DELETE_ERROR);
                }
            }

            // 나머지 이미지 DB 삽입 /DB 삽입 실패 시 업로드된 이미지 삭제
            if (!updateList.isEmpty()) {
                try {
                    resultUpdateList = auctionTransactionImageRepository.saveAll(updateList);
                } catch (Exception e) {
                    s3UploadService.deleteImages(updateList);
                }
                auctionTransaction.setThumbnail(resultUpdateList.get(0).getUploadUrl());
            }
        }
        return resultUpdateList == null ? null : resultUpdateList.stream().map(DataMapper.instance::auctionImageEntityToDto).collect(Collectors.toList());
    }

    private void updateTransactionEntity(AuctionTransaction auctionTransaction, AuctionTransactionFormDto auctionTransactionFormDto) {
        auctionTransaction.setTitle(auctionTransactionFormDto.getTitle());
        auctionTransaction.setContent(auctionTransactionFormDto.getContent());
        auctionTransaction.setPrice(auctionTransactionFormDto.getPrice());
        auctionTransaction.setTransactionMode(auctionTransactionFormDto.getTransactionMode());
        auctionTransaction.setLocation(auctionTransactionFormDto.getLocation());
        auctionTransaction.setPayment(auctionTransactionFormDto.getPayment());
    }

    private AuctionTransactionImage uploadAndConvertFileToEntity(MultipartFile multipartFile, int seqIndex, AuctionTransaction auctionTransaction){
        if (multipartFile.isEmpty()) {
            return null;
        }
        AuctionTransactionImage result = new AuctionTransactionImage();
        result.setOriginName(multipartFile.getOriginalFilename());
        result.setImageSeq(seqIndex);
        result.setImageName(ServiceUtil.makeUploadFileName(result.getOriginName()));
        result.setUploadUrl(s3UploadService.uploadImage(multipartFile,result.getImageName()));
        result.setAuctionTransactionId(auctionTransaction);
        return result;
    }
}
