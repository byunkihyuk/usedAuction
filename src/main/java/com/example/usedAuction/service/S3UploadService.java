package com.example.usedAuction.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.usedAuction.entity.GeneralTransactionImage;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadImage(MultipartFile multipartFile,String imgName) {

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(multipartFile.getContentType());
        meta.setContentLength(multipartFile.getSize());
        try {
            amazonS3.putObject(bucket,imgName,multipartFile.getInputStream(),meta);
        } catch (IOException e) {
            throw new ApiException(ErrorEnum.IMAGE_UPLOAD_ERROR);
        }

        return amazonS3.getUrl(bucket,imgName).toString();
    }

    public boolean deleteImages(List<GeneralTransactionImage> generalTransactionImageList) {
        if(generalTransactionImageList.isEmpty()){
            return true;
        }
        int count = generalTransactionImageList.size();
        for(GeneralTransactionImage img : generalTransactionImageList) {
            boolean isImage = amazonS3.doesObjectExist(bucket, img.getImageName());
            if (isImage) {
                amazonS3.deleteObject(bucket, img.getImageName());
                count--;
            }
        }
        return count == 0;
    }
}
