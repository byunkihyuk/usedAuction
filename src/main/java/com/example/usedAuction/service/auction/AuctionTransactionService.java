package com.example.usedAuction.service.auction;

import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.auction.*;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.dto.result.ResponseResultError;
import com.example.usedAuction.entity.auction.AuctionBid;
import com.example.usedAuction.entity.transactionenum.AuctionBidStateEnum;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.auction.AuctionTransactionImage;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import com.example.usedAuction.entity.user.User;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import com.example.usedAuction.repository.auction.AuctionBidRepository;
import com.example.usedAuction.repository.auction.AuctionTransactionImageRepository;
import com.example.usedAuction.repository.auction.AuctionTransactionRepository;
import com.example.usedAuction.repository.user.UserRepository;
import com.example.usedAuction.service.aws.S3UploadService;
import com.example.usedAuction.service.see.SseService;
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
    private final AuctionBidRepository auctionBidRepository;
    private final S3UploadService s3UploadService;
    private final SseService sseService;

    @Transactional
    public ResponseEntity<Object> postAuctionTransaction(AuctionTransactionFormDto auctionTransactionFormDto, List<MultipartFile> multipartFileList) {
        ResponseResult<Object> result = new ResponseResult<>();
        HttpStatus status = HttpStatus.CREATED;
        if(multipartFileList!=null && multipartFileList.size()>10){ // 이미지 10개 이상일 경우 실패
            Map<String,Object> map = new HashMap<>();
            Map<String,Object> data = new HashMap<>();
            data.put("message", ErrorEnum.IMAGE_MAX_COUNT);
            result.setStatus("fail");
            result.setData(data);
            status = HttpStatus.BAD_REQUEST;
        }else{
            // 로그인된 유저 정보
            User loginUser = SecurityUtil.getCurrentUsername()
                    .flatMap(userRepository::findOneWithAuthoritiesByUsername)
                    .orElseThrow(()->new ApiException(ErrorEnum.UNAUTHORIZED_ERROR));
            auctionTransactionFormDto.setSeller(loginUser.getUserId());
            AuctionTransaction auctionTransaction = DataMapper.instance.auctionTransactionFormToEntity(auctionTransactionFormDto);
            auctionTransaction.setViewCount(0);
            auctionTransaction.setHighestBid(0);
            AuctionTransaction getAuctionTransaction=null;
            // 게시글 저장 후 반환
            try{
                getAuctionTransaction = auctionTransactionRepository.save(auctionTransaction);
            }catch (Exception e){
                throw new ApiException(ErrorEnum.AUCTION_TRANSACTION_POST_ERROR);
            }
            // 이미지 저장(s3 업로드) 후 결과 반환
            List<AuctionTransactionImage> imageList = new ArrayList<>();
            List<AuctionTransactionImage> successGeneralImageDtoList = new ArrayList<>();
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
                    }
                    successGeneralImageDtoList =  auctionTransactionImageRepository.saveAll(imageList);
                    getAuctionTransaction.setThumbnail(successGeneralImageDtoList.get(0).getUploadUrl());
                }
                resultImageList = successGeneralImageDtoList.stream()
                        .map(DataMapper.instance::auctionImageEntityToDto)
                        .collect(Collectors.toList());
            }catch (Exception e ){
                e.printStackTrace();
                throw new ApiException(ErrorEnum.IMAGE_UPLOAD_ERROR);
            }
            AuctionTransactionDto resultGeneralTransactionDto = DataMapper.instance.auctionTransactionToDto(getAuctionTransaction);
            resultGeneralTransactionDto.setImages(resultImageList);

            result.setStatus("success");
            result.setData(resultGeneralTransactionDto);
        }
        return ResponseEntity.status(status).body(result);
    }

    @Transactional
    public ResponseEntity<Object> getAuctionTransaction(Integer auctionTransactionId) {
        AuctionTransaction auctionTransaction = auctionTransactionRepository.findByAuctionTransactionId(auctionTransactionId)
                .orElseThrow(() -> new ApiException(ErrorEnum.NOT_FOUND_AUCTION_TRANSACTION));
        auctionTransaction.setViewCount(auctionTransaction.getViewCount()+1);

        List<AuctionTransactionImage> auctionTransactionImages = auctionTransactionImageRepository.findAllByAuctionTransactionIdOrderByImageSeq(auctionTransaction);

        AuctionTransactionDto resultAuctionTransactionDto = DataMapper.instance.auctionTransactionToDto(auctionTransaction);
        resultAuctionTransactionDto.setImages(auctionTransactionImages.stream()
                .map(DataMapper.instance::auctionImageEntityToDto)
                .collect(Collectors.toList()));

        User loginUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElse(null);
        AuctionBidDto auctionBidDto = null;

        if(loginUser!=null){
            if(loginUser.getUserId().equals(resultAuctionTransactionDto.getSeller())){
                resultAuctionTransactionDto.setAuthor(true);
            }
            auctionBidDto = DataMapper.instance.auctionBidEntityToDto(
                    auctionBidRepository.findByAuctionTransactionIdAndBidder(auctionTransaction,loginUser)
                    .orElse(null));
        }
        ResponseResult<Object> result = new ResponseResult<>();
        Map<String, Object> map = new HashMap<>();
        map.put("transaction",resultAuctionTransactionDto);
        map.put("mybid",auctionBidDto);
        result.setData(map);
        result.setStatus("success");
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional(readOnly = true)
    public List<AuctionTransactionDto> getAllAuctionTransaction(Integer page, Integer size, String sort,String state,String keyword) {
        Sort pageableSort = sort.equals("asc") ? Sort.by("createdAt").ascending()  :
                Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page,size, pageableSort);
        List<AuctionTransactionDto> resultAuctionTransaction = new ArrayList<>();

        if(state.equals("전체")){
            resultAuctionTransaction = auctionTransactionRepository.findAll(pageable).stream()
                    .map(DataMapper.instance::auctionTransactionToDto).collect(Collectors.toList());
        }else{
            switch (state){
                case "판매중":
                    resultAuctionTransaction = auctionTransactionRepository.findAllByTransactionStateAndTitleContainingOrContentContaining(TransactionStateEnum.SALE,keyword,keyword,pageable).stream()
                            .map(DataMapper.instance::auctionTransactionToDto).collect(Collectors.toList());
                    break;
                case "거래중":
                    resultAuctionTransaction = auctionTransactionRepository.findAllByTransactionStateAndTitleContainingOrContentContaining(TransactionStateEnum.PROGRESS,keyword,keyword,pageable).stream()
                            .map(DataMapper.instance::auctionTransactionToDto).collect(Collectors.toList());
                    break;
                case "판매완료":
                    resultAuctionTransaction = auctionTransactionRepository.findAllByTransactionStateAndTitleContainingOrContentContaining(TransactionStateEnum.COMPLETE,keyword,keyword,pageable).stream()
                            .map(DataMapper.instance::auctionTransactionToDto).collect(Collectors.toList());
                    break;
            }
        }
        return resultAuctionTransaction;
    }

    @Transactional
    public ResponseEntity<Object> deleteAuctionTransaction(Integer auctionTransactionId) {
        ResponseResult<Object> result = new ResponseResult<>();
        HttpStatus status = HttpStatus.OK;

        AuctionTransaction auctionTransaction = auctionTransactionRepository.findByAuctionTransactionId(auctionTransactionId)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_AUCTION_TRANSACTION));
        //s3 이미지 삭제 하기 위해서 가져옴
        List<AuctionTransactionImage> auctionTransactionImageList = auctionTransactionImageRepository.findAllByAuctionTransactionId(auctionTransaction);

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

        if(multipartFileList!=null && !multipartFileList.isEmpty()){
            if(updateTransactionImage==null){
            throw  new ApiException(ErrorEnum.IMAGE_UPDATE_ERROR);
            }
        }

        AuctionTransactionDto resultGeneralDto = DataMapper.instance.auctionTransactionToDto(auctionTransaction);
        resultGeneralDto.setImages(updateTransactionImage);

        result.setStatus("success");
        result.setData(resultGeneralDto);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
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
        auctionTransaction.setAddress(auctionTransactionFormDto.getAddress());
        auctionTransaction.setDetailAddress(auctionTransactionFormDto.getDetailAddress());
        auctionTransaction.setPayment(auctionTransactionFormDto.getPayment());
        auctionTransaction.setStartedAt(auctionTransactionFormDto.getStartedAt());
        auctionTransaction.setFinishedAt(auctionTransactionFormDto.getFinishedAt());
    }

    private AuctionTransactionImage uploadAndConvertFileToEntity(MultipartFile multipartFile, int seqIndex, AuctionTransaction auctionTransaction) {
        if (multipartFile.isEmpty()) {
            return null;
        }
        AuctionTransactionImage result = new AuctionTransactionImage();
        result.setOriginName(multipartFile.getOriginalFilename());
        result.setImageSeq(seqIndex);
        result.setImageName(ServiceUtil.makeUploadFileName(result.getOriginName()));
        result.setUploadUrl(s3UploadService.uploadImage(multipartFile, result.getImageName()));
        result.setAuctionTransactionId(auctionTransaction);
        return result;
    }

    @Transactional(readOnly = true)
    public List<AuctionTransactionDto> topAuctionList(String sortOption) {
        Sort sort = Sort.by("viewCount").descending();
        if(sortOption.equals("createdAt")){
            sort = Sort.by("createdAt").descending();
        }

        return  auctionTransactionRepository.findTop10ByTransactionStateNot(TransactionStateEnum.COMPLETE,sort)
                .stream().map(DataMapper.instance::auctionTransactionToDto).collect(Collectors.toList());
    }

    @Transactional
    public ResponseEntity<Object> postAuctionTransactionBid(AuctionBidDto auctionBidDto) {
        User loginUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        // 경매 글이 존재하는지 확인
        AuctionTransaction auctionTransaction = auctionTransactionRepository.findByAuctionTransactionId(auctionBidDto.getAuctionTransactionId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_AUCTION_TRANSACTION));

        AuctionBid getAuctionBid = auctionBidRepository.findByAuctionTransactionIdAndBidder(auctionTransaction,loginUser)
                .orElse(null);

        if(getAuctionBid!=null){
            throw new ApiException(ErrorEnum.EXIST_BID);
        }

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        auctionBidDto.setAuctionBidState(AuctionBidStateEnum.BID);
        AuctionBid auctionBid = DataMapper.instance.auctionBidDtoToEntity(auctionBidDto);

        if( auctionTransaction.getHighestBid() >= auctionBidDto.getPrice()){
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseResultError("fail","입찰 금액은 최고가보다 높아야 합니다."));
        }
        auctionBid.setBidder(loginUser);
        auctionBid.setBidderNickname(loginUser.getNickname());

        try{
            AuctionBid resultBid = auctionBidRepository.save(auctionBid);
            result.setData(DataMapper.instance.auctionBidEntityToDto(resultBid));
            auctionTransaction.setHighestBid((resultBid.getPrice()));
            sseService.auctionPublish(String.valueOf(auctionBidDto.getAuctionTransactionId()),resultBid.getPrice());

        }catch (Exception e){
            throw new ApiException(ErrorEnum.FAIL_BID);
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> updateAuctionTransactionBid(AuctionBidDto auctionBidDto) {
        User loginUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        if(!loginUser.getUserId().equals(auctionBidDto.getBidder())){
            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
        }

        AuctionBid auctionBid = auctionBidRepository.findById(auctionBidDto.getAuctionBidId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_BID));
        AuctionTransaction auctionTransaction = auctionTransactionRepository.findByAuctionTransactionId(auctionBidDto.getAuctionTransactionId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_AUCTION_TRANSACTION));
        try{
            auctionBid.setPrice(auctionBidDto.getPrice());
            auctionBid.setAuctionBidState(AuctionBidStateEnum.BID);
            if(auctionTransaction.getHighestBid() < auctionBid.getPrice()) {
                auctionTransaction.setHighestBid(auctionBid.getPrice());
                sseService.auctionPublish(String.valueOf(auctionBidDto.getAuctionTransactionId()),auctionBid.getPrice());
            }
        }catch (Exception e ){
            throw new ApiException(ErrorEnum.FAIL_BID);
        }

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(DataMapper.instance.auctionBidEntityToDto(auctionBid));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> getAllUserAuctionTransactionBid() {
        String username = SecurityUtil.getCurrentUsername().orElse("");

        User loginUser = userRepository.findByUsername(username)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        List<AuctionBidDto> getAuctionBid = auctionBidRepository.findByBidder(loginUser)
                .stream().map(DataMapper.instance::auctionBidEntityToDto).collect(Collectors.toList());

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(getAuctionBid);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Object> getAllAuctionTransactionBid(Integer auctionTransactionId) {
        String username = SecurityUtil.getCurrentUsername().orElse("");

        User loginUser = userRepository.findByUsername(username)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        AuctionTransaction auctionTransaction = auctionTransactionRepository.findByAuctionTransactionId(auctionTransactionId)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_AUCTION_TRANSACTION));

        List<AuctionBidDto> getAuctionBid = auctionBidRepository.findAllByAuctionTransactionIdOrderByPriceDesc(auctionTransaction)
                .stream().map(DataMapper.instance::auctionBidEntityToDto).collect(Collectors.toList());

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(getAuctionBid);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Object> getAuctionTransactionBid(Integer auctionTransactionId) {
        String username = SecurityUtil.getCurrentUsername().orElse("");

        User loginUser = userRepository.findByUsername(username)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        AuctionTransaction auctionTransaction = auctionTransactionRepository.findByAuctionTransactionId(auctionTransactionId)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_AUCTION_TRANSACTION));

        AuctionBid getAuctionBid = auctionBidRepository.findByAuctionTransactionIdAndBidder(auctionTransaction,loginUser)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_BID));

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(DataMapper.instance.auctionBidEntityToDto(getAuctionBid));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional(readOnly = true)
    public int getAllTotalCount(String state,String keyword) {
        if(state.equals("전체")){
            return  auctionTransactionRepository.findAll().size();
        }else{
            switch (state){
                case "판매중":
                    return auctionTransactionRepository.findAllByTransactionStateAndTitleContainingOrContentContaining(TransactionStateEnum.SALE,keyword,keyword).size();
                case "거래중":
                    return auctionTransactionRepository.findAllByTransactionStateAndTitleContainingOrContentContaining(TransactionStateEnum.PROGRESS,keyword,keyword).size();
                case "판매완료":
                    return auctionTransactionRepository.findAllByTransactionStateAndTitleContainingOrContentContaining(TransactionStateEnum.COMPLETE,keyword,keyword).size();
                default:
                    return 0;
            }
        }
    }

    @Transactional(readOnly = true)
    public Object searchTopAuctionList(String keyword) {
        Pageable pageable = PageRequest.of(0,5);
        return auctionTransactionRepository.findAllBySearch(keyword,pageable).stream()
                .map(DataMapper.instance::auctionTransactionToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Object> getAuctionTransactionHighestBid(Integer auctionTransactionId) {
        AuctionTransaction auctionTransaction = auctionTransactionRepository.findById(auctionTransactionId)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_AUCTION_TRANSACTION));

        AuctionBidDto auctionBidDto = DataMapper.instance.auctionBidEntityToDto(auctionBidRepository.findFirstByAuctionTransactionIdOrderByPriceDesc(auctionTransaction)
                .orElse(new AuctionBid()));

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        Map<String,Object> map = new HashMap<>();
        map.put("highestBid",auctionBidDto.getPrice());
        result.setData(map);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
