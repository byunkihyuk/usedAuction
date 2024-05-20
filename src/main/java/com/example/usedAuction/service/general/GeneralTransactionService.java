package com.example.usedAuction.service.general;

import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.general.GeneralTransactionImageDto;
import com.example.usedAuction.dto.general.GeneralTransactionDto;
import com.example.usedAuction.dto.general.GeneralTransactionFormDto;
import com.example.usedAuction.dto.payment.PayInfoDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.dto.result.ResponseResultError;
import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.general.GeneralTransactionImage;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import com.example.usedAuction.entity.user.User;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import com.example.usedAuction.repository.general.GeneralTransactionImageRepository;
import com.example.usedAuction.repository.general.GeneralTransactionRepository;
import com.example.usedAuction.repository.payment.PayInfoRepository;
import com.example.usedAuction.repository.user.UserRepository;
import com.example.usedAuction.service.aws.S3UploadService;
import com.example.usedAuction.util.SecurityUtil;
import com.example.usedAuction.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class GeneralTransactionService {
    private final GeneralTransactionRepository generalTransactionRepository;
    private final UserRepository userRepository;
    private final GeneralTransactionImageRepository generalTransactionImageRepository;
    private final S3UploadService s3UploadService;
    private final PayInfoRepository payInfoRepository;

    @Transactional
    public ResponseEntity<Object> postGeneralTransaction(GeneralTransactionFormDto generalTransactionFormDto, List<MultipartFile> imageFiles) {
        ResponseResult<Object> result = new ResponseResult<>();
        HttpStatus status = HttpStatus.CREATED;
        if(imageFiles!=null && imageFiles.size()>10){ // 이미지 10개 이상일 경우 실패
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
            generalTransaction.setSeller(loginUser);
            GeneralTransaction getGeneralTransaction=null;
            // 게시글 저장
            try{
                getGeneralTransaction = generalTransactionRepository.save(generalTransaction);
            }catch (Exception e){
                throw new ApiException(ErrorEnum.GENERAL_TRANSACTION_POST_ERROR);
            }
            // 이미지 저장(s3 업로드) 후 결과 반환
            List<GeneralTransactionImageDto> resultImageList = null;
            if(imageFiles!=null) {
                try {
                    List<GeneralTransactionImage> imageList = new ArrayList<>();
                    List<GeneralTransactionImage> successGeneralImageDtoList = null;
                    for (int i = 1; i <= imageFiles.size(); i++) {
                        GeneralTransactionImageDto generalTransactionImageDto = new GeneralTransactionImageDto();
                        MultipartFile image = imageFiles.get(i - 1);
                        generalTransactionImageDto.setImageSeq(i);
                        generalTransactionImageDto.setOriginName(image.getOriginalFilename());

                        String imageFilename = ServiceUtil.makeUploadFileName(generalTransactionImageDto.getOriginName());
                        generalTransactionImageDto.setImageName(imageFilename);
                        generalTransactionImageDto.setUploadUrl(s3UploadService.uploadImage(image, imageFilename));

                        GeneralTransactionImage generalTransactionImage = DataMapper.instance.generalImageDtoToEntity(generalTransactionImageDto);
                        generalTransactionImage.setGeneralTransactionId(getGeneralTransaction);
                        imageList.add(generalTransactionImage);
                    }

                    successGeneralImageDtoList = generalTransactionImageRepository.saveAll(imageList);
                    resultImageList = successGeneralImageDtoList.stream()
                            .map(DataMapper.instance::generalImageEntityToDto)
                            .collect(Collectors.toList());
                    getGeneralTransaction.setThumbnail(resultImageList.get(0).getUploadUrl());
                } catch (Exception e) {
                    throw new ApiException(ErrorEnum.IMAGE_UPLOAD_ERROR);
                }
            }
            GeneralTransactionDto resultGeneralTransactionDtoDto = DataMapper.instance.generalTransactionToDto(getGeneralTransaction);
            resultGeneralTransactionDtoDto.setImages(resultImageList);
            result.setStatus("success");
            result.setData(resultGeneralTransactionDtoDto);
        }
        return ResponseEntity.status(status).body(result);
    }


    @Transactional(readOnly = true)
    public ResponseEntity<Object> getGeneralTransaction(Integer generalTransactionId) {
        GeneralTransaction generalTransaction = generalTransactionRepository.findByGeneralTransactionId(generalTransactionId);

        List<GeneralTransactionImage> generalTransactionImages = generalTransactionImageRepository.findAllByGeneralTransactionId(generalTransaction);

        GeneralTransactionDto resultGeneralTransactionDto = DataMapper.instance.generalTransactionToDto(generalTransaction);
        resultGeneralTransactionDto.setImages(generalTransactionImages.stream()
                .map(DataMapper.instance::generalImageEntityToDto)
                .collect(Collectors.toList()));

        ResponseResult<Object> result = new ResponseResult<>();
        Map<Integer,Object> data = new HashMap<>();
        data.put(resultGeneralTransactionDto.getGeneralTransactionId(),resultGeneralTransactionDto);
        result.setData(data);
        result.setStatus("success");
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    public List<GeneralTransactionDto>  getAllGeneralTransaction(Integer page, Integer size, String sort,String state) {
        Sort s = sort.equals("asc") ? Sort.by("createdAt").ascending()  :
                Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page,size, s);

        List<GeneralTransactionDto> resultGeneralTransaction = new ArrayList<>();

        if(state.equals("전체")){
            resultGeneralTransaction = generalTransactionRepository.findAll(pageable).stream()
                    .map(DataMapper.instance::generalTransactionToDto).collect(Collectors.toList());
        }else{
            switch (state){
                case "판매중":
                    resultGeneralTransaction = generalTransactionRepository.findAllByTransactionState(TransactionStateEnum.SALE,pageable).stream()
                            .map(DataMapper.instance::generalTransactionToDto).collect(Collectors.toList());
                    break;
                case "거래중":
                    resultGeneralTransaction = generalTransactionRepository.findAllByTransactionState(TransactionStateEnum.PROGRESS,pageable).stream()
                            .map(DataMapper.instance::generalTransactionToDto).collect(Collectors.toList());
                    break;
                case "판매완료":
                    resultGeneralTransaction = generalTransactionRepository.findAllByTransactionState(TransactionStateEnum.COMPLETE,pageable).stream()
                            .map(DataMapper.instance::generalTransactionToDto).collect(Collectors.toList());
                    break;
            }
        }

        return resultGeneralTransaction;
    }

    public ResponseEntity<Object> deleteGeneralTransaction(Integer generalTransactionId) {
        ResponseResult<Object> result = new ResponseResult<>();
        HttpStatus status = HttpStatus.OK;

        GeneralTransaction generalTransaction = generalTransactionRepository.findByGeneralTransactionId(generalTransactionId);
        //s3 이미지 삭제 하기 위해서 가져옴
        List<GeneralTransactionImage> generalTransactionImageList = generalTransactionImageRepository.findAllByGeneralTransactionId(generalTransaction);

        try{
            generalTransactionRepository.delete(generalTransaction);

            //generalTransactionImageRepository.deleteAll(generalTransactionImageList);

            s3UploadService.deleteImages(generalTransactionImageList);
            result.setStatus("success");
            GeneralTransactionDto generalTransactionDto = DataMapper.instance.generalTransactionToDto(generalTransaction);
            Map<String,Object> data = new HashMap<>();
            data.put("message",generalTransactionDto.getGeneralTransactionId()+"번 글 삭제 성공");
            result.setData(data);
        }catch (Exception e){
            ResponseResultError error = new ResponseResultError("error","글 삭제 실패");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        return ResponseEntity.status(status).body(result);
    }

    @Transactional
    public ResponseEntity<Object> updateGeneralTransaction(Integer generalTransactionId, GeneralTransactionFormDto generalTransactionFormDto, List<MultipartFile> multipartFile) {
        ResponseResult<Object> result = new ResponseResult<>();
        HttpStatus resultHttp = HttpStatus.OK;
        GeneralTransaction generalTransaction = generalTransactionRepository.findByGeneralTransactionId(generalTransactionId);

        // 글 정보 수정
        updateTransactionEntity(generalTransaction,generalTransactionFormDto);

        List<GeneralTransactionImage> generalTransactionImages = generalTransactionImageRepository.findAllByGeneralTransactionIdOrderByImageSeq(generalTransaction);
        List<GeneralTransactionImageDto> updateTransactionImage=null;

        try{ // 이미지 수정
            updateTransactionImage = updateTransactionImage(generalTransactionImages,multipartFile,generalTransaction);
        }catch (Exception e){
            throw new ApiException(ErrorEnum.IMAGE_UPDATE_ERROR);
        }
        result.setStatus("success");
        Map<String,Object> data = new HashMap<>();
        GeneralTransactionDto resultGeneralDto = DataMapper.instance.generalTransactionToDto(generalTransaction);
        resultGeneralDto.setImages(updateTransactionImage);
        data.put("data",resultGeneralDto);
        result.setData(data);

        if(multipartFile!=null && !multipartFile.isEmpty()){
            if(updateTransactionImage==null){
                data = new HashMap<>();
                data.put("message","일반 거래 글 수정 실패");
                result.setData(data);
                result.setStatus("fail");
                resultHttp = HttpStatus.BAD_REQUEST;
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    private List<GeneralTransactionImageDto> updateTransactionImage(List<GeneralTransactionImage> generalTransactionImages, List<MultipartFile> multipartFile, GeneralTransaction generalTransaction) throws IOException {
        List<GeneralTransactionImage> deleteList = new ArrayList<>();
        List<GeneralTransactionImage> updateList = new ArrayList<>();
        List<GeneralTransactionImage> resultUpdateList = null;
        // 업데이트 하려는 이미지가 없을 경우
        if(multipartFile==null ){
            // DB에는 이미지가 있으면 DB 이미지 삭제
            generalTransaction.setThumbnail(null);
            if(!generalTransactionImages.isEmpty()){
                deleteList.addAll(generalTransactionImages);
                generalTransactionImageRepository.deleteAll(generalTransactionImages);
                if(!s3UploadService.deleteImages(deleteList)){
                    throw new ApiException(ErrorEnum.IMAGE_DELETE_ERROR);
                }
                return null;
            }
            return null;
        }

        if(!multipartFile.isEmpty()){
            //이름 목록
            List<String> newOriginUrlList =  multipartFile.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.toList());
            List<String> updateOriginUrlList =  generalTransactionImages.stream().map(GeneralTransactionImage::getOriginName).collect(Collectors.toList());

            int maxIndex = Math.max(generalTransactionImages.size(), multipartFile.size());
            for(int i=0;i<maxIndex;i++){
                GeneralTransactionImage imgEntity = null;
                if(i > updateOriginUrlList.size()-1){ // 입력 이미지가 많은 경우
                    // 기존
                    if(updateOriginUrlList.contains(newOriginUrlList.get(i))){
                        int updateIndex = updateOriginUrlList.indexOf(newOriginUrlList.get(i));
                        imgEntity = generalTransactionImages.get(updateIndex);
                        imgEntity.setImageSeq(i+1);
                        updateList.add(imgEntity);
                    }else{
                        updateList.add(uploadAndConvertFileToEntity(multipartFile.get(i),i+1,generalTransaction));
                    }
                }else if(i > newOriginUrlList.size()-1){ // 기존 이미지가 많은 경우
                    // 새로운 목록은 다 수정 했기 때문에 남은건 새로운 목록에 있는지 확인 후 삭제
                    imgEntity = generalTransactionImages.get(i);
                    if(!newOriginUrlList.contains(imgEntity.getOriginName())){
                        deleteList.add(imgEntity);
                    }
                }else{ // 크기 같을 때
                    imgEntity = generalTransactionImages.get(i);
                    if(newOriginUrlList.contains(imgEntity.getOriginName())){ // 새로운 목록에 기존 이미지 있음
                        if(imgEntity.getOriginName().equals(newOriginUrlList.get(i))){ // 기존과 수정 위치가 같음
                            updateList.add(imgEntity); // 그대로 수정 목록에 추가
                        }else if(updateOriginUrlList.contains(newOriginUrlList.get(i))){ //----------- 여기 수정해야됨 양쪽 목록에 있지만 서로 위치가 다를때
                            int updateIndex = updateOriginUrlList.indexOf(newOriginUrlList.get(i));
                            imgEntity = generalTransactionImages.get(updateIndex);
                            imgEntity.setImageSeq(i+1);
                            updateList.add(imgEntity);
                        }else{
                            // 새로 추가된 사진이므로 업로드 후 수정 목록에 추가
                            updateList.add(uploadAndConvertFileToEntity(multipartFile.get(i),i+1,generalTransaction));
                        }
                    }else{ // 현재 기존 이미지 새로운 목록에 없음
                        if(updateOriginUrlList.contains(newOriginUrlList.get(i))){ // 현재 기존과는 다르지만 기존 목록에 있던것 이므로 위치 수정
                            int updateIndex = updateOriginUrlList.indexOf(newOriginUrlList.get(i));
                            imgEntity = generalTransactionImages.get(updateIndex);
                            imgEntity.setImageSeq(i+1);
                            updateList.add(imgEntity);
                            imgEntity = generalTransactionImages.get(i);
                            deleteList.add(imgEntity);
                        }else{ //  현재 목록 기존 목록 아무것도 일치 안함 삭제
                            deleteList.add(imgEntity);
                            updateList.add(uploadAndConvertFileToEntity(multipartFile.get(i),i+1,generalTransaction));
                        }
                    }
                }
            }

            // 삭제 목록 이미지 s3에서 삭제 후 DB 삭제
            if(!deleteList.isEmpty()){
                generalTransactionImageRepository.deleteAll(deleteList);
                if(!s3UploadService.deleteImages(deleteList)){
                    throw new ApiException(ErrorEnum.IMAGE_DELETE_ERROR);
                }
            }

            // 나머지 이미지 DB 삽입 /DB 삽입 실패 시 업로드된 이미지 삭제
            if(!updateList.isEmpty()){
                try{
                    resultUpdateList= generalTransactionImageRepository.saveAll(updateList);
                }catch (Exception e ){
                    s3UploadService.deleteImages(updateList);
                }
                generalTransaction.setThumbnail(resultUpdateList.get(0).getUploadUrl());
            }
        }
        return resultUpdateList==null?null: resultUpdateList.stream().map(DataMapper.instance::generalImageEntityToDto).collect(Collectors.toList());
    }

    private void updateTransactionEntity(GeneralTransaction generalTransaction, GeneralTransactionFormDto generalTransactionFormDto) {
        generalTransaction.setTitle(generalTransactionFormDto.getTitle());
        generalTransaction.setContent(generalTransactionFormDto.getContent());
        generalTransaction.setPrice(generalTransactionFormDto.getPrice());
        generalTransaction.setTransactionMode(generalTransactionFormDto.getTransactionMode());
        generalTransaction.setAddress(generalTransactionFormDto.getAddress());
        generalTransaction.setDetailAddress(generalTransactionFormDto.getDetailAddress());
        generalTransaction.setPayment(generalTransactionFormDto.getPayment());
    }

//    private List<GeneralTransactionImage> listUploadAndConvertFileToEntity(List<MultipartFile> multipartFiles,GeneralTransaction generalTransaction){
//        if (multipartFiles.isEmpty()) {
//            return null;
//        }
//        List<GeneralTransactionImage> result = new ArrayList<>();
//        for(int i=0;i<multipartFiles.size();i++){
//            MultipartFile file = multipartFiles.get(i);
//            GeneralTransactionImage generalTransactionImage = new GeneralTransactionImage();
//            generalTransactionImage.setOriginName(file.getOriginalFilename());
//            generalTransactionImage.setImageSeq(i+1);
//            generalTransactionImage.setImageName(makeUploadFileName(generalTransactionImage.getOriginName()));
//            generalTransactionImage.setUploadUrl(s3UploadService.uploadImage(file,generalTransactionImage.getImageName()));
//            generalTransactionImage.setGeneralTransactionId(generalTransaction);
//            result.add(generalTransactionImage);
//        }
//        generalTransaction.setThumbnail(result.get(0).getUploadUrl());
//        return result;
//    }

    private GeneralTransactionImage uploadAndConvertFileToEntity(MultipartFile multipartFile, int seqIndex, GeneralTransaction generalTransaction){
        if (multipartFile.isEmpty()) {
            return null;
        }
        GeneralTransactionImage result = new GeneralTransactionImage();
        result.setOriginName(multipartFile.getOriginalFilename());
        result.setImageSeq(seqIndex);
        result.setImageName(ServiceUtil.makeUploadFileName(result.getOriginName()));
        result.setUploadUrl(s3UploadService.uploadImage(multipartFile,result.getImageName()));
        result.setGeneralTransactionId(generalTransaction);
        return result;
    }

    @Transactional(readOnly = true)
    public List<GeneralTransactionDto> topGeneralList(String sortOption) {
        Sort sort = Sort.by("viewCount").descending();
        if(sortOption.equals("createdAt")){
            sort = Sort.by("createdAt").descending();
        }
        return generalTransactionRepository.findTop10ByTransactionStateNot(TransactionStateEnum.COMPLETE,sort)
                .stream().map(DataMapper.instance::generalTransactionToDto).collect(Collectors.toList());
    }

    public int getAllTotalCount(String state) {
        if(state.equals("전체")){
            return  generalTransactionRepository.findAll().size();
        }else{
            switch (state){
                case "판매중":
                    return generalTransactionRepository.findAllByTransactionState(TransactionStateEnum.SALE).size();
                case "거래중":
                    return generalTransactionRepository.findAllByTransactionState(TransactionStateEnum.PROGRESS).size();
                case "판매완료":
                    return generalTransactionRepository.findAllByTransactionState(TransactionStateEnum.COMPLETE).size();
                default:
                    return 0;
            }
        }

    }

    public ResponseEntity<Object> getGeneralBuyRequestList(Integer generalTransactionId) {
        String username = SecurityUtil.getCurrentUsername().orElseThrow(()->new ApiException(ErrorEnum.UNAUTHORIZED_ERROR));

        User loginUser = userRepository.findByUsername(username)
                .orElseThrow(()-> new ApiException(ErrorEnum.NOT_FOUND_USER));

        GeneralTransaction generalTransaction = generalTransactionRepository.findByGeneralTransactionId(generalTransactionId);

        List<PayInfoDto> getRequestList = payInfoRepository.findAllByGeneralTransactionId(generalTransaction)
                .stream().map(DataMapper.instance::payInfoEntityToDto).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseResult<>("success",getRequestList));
    }
}

