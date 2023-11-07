package com.example.usedAuction.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.usedAuction.entity.GeneralTransactionImage;
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

    public String uploadImage(MultipartFile multipartFile,String imgName) throws IOException {

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(multipartFile.getContentType());
        meta.setContentLength(multipartFile.getSize());
        amazonS3.putObject(bucket,imgName,multipartFile.getInputStream(),meta);

        return amazonS3.getUrl(bucket,imgName).toString();
    }

    public void deleteImages(List<GeneralTransactionImage> generalTransactionImageList) {
        if(generalTransactionImageList.isEmpty()){
            return;
        }
        for(GeneralTransactionImage img : generalTransactionImageList) {
            boolean isImage = amazonS3.doesObjectExist(bucket, img.getImageUrl());
            if (isImage) {
                amazonS3.deleteObject(bucket, img.getImageUrl());
            }
        }
    }
}
