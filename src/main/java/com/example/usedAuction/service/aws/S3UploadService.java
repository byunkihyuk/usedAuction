package com.example.usedAuction.service.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.example.usedAuction.entity.TransactionImage;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.cloud-front-url}")
    private String cloudFrontUrl;

    @Value("${azure.connection-string}")
    private  String connectionString;
    @Value("${azure.container-name}")
    private  String containerName;


    public String uploadImage(MultipartFile multipartFile,String imgName) {
        if(multipartFile.isEmpty()){
            return null;
        }

        BlobClient blobClient = new BlobClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .blobName(imgName)
                .buildClient();

        try {
            blobClient.upload(multipartFile.getInputStream(), multipartFile.getSize(), true);
        } catch (IOException e) {
            throw new ApiException(ErrorEnum.IMAGE_UPLOAD_ERROR);
        }

        return blobClient.getBlobUrl();
    }

    public boolean deleteImages(List<?> deleteImageList) {
        if(deleteImageList.isEmpty()){
            return true;
        }
        int count = deleteImageList.size();
        for(Object obj : deleteImageList){
            BlobClient blobClient = new BlobClientBuilder()
                    .connectionString(connectionString)
                    .containerName(containerName)
                    .blobName(((TransactionImage)obj).getImageName())
                    .buildClient();
            boolean isImage = blobClient.exists();
            if (isImage) {
                try{
                    blobClient.delete();
                }catch (Exception e ){
                    throw new ApiException(ErrorEnum.IMAGE_DELETE_ERROR);
                }
                count--;
            }
        }
        return count == 0;
    }

//    public String uploadImage(MultipartFile multipartFile,String imgName) {
//
//        ObjectMetadata meta = new ObjectMetadata();
//        meta.setContentType(multipartFile.getContentType());
//        meta.setContentLength(multipartFile.getSize());
//        try {
//            amazonS3.putObject(bucket,imgName,multipartFile.getInputStream(),meta);
//        } catch (IOException e) {
//            throw new ApiException(ErrorEnum.IMAGE_UPLOAD_ERROR);
//        }
//
//        return cloudFrontUrl + amazonS3.getUrl(bucket,imgName).getPath();
//    }
//
//    public boolean deleteImages(List<?> deleteImageList) {
//        if(deleteImageList.isEmpty()){
//            return true;
//        }
//        int count = deleteImageList.size();
//        for(Object obj : deleteImageList){
//            boolean isImage = amazonS3.doesObjectExist(bucket,  ((TransactionImage)obj).getImageName());
//            if (isImage) {
//                amazonS3.deleteObject(bucket,  ((TransactionImage)obj).getImageName());
//                count--;
//            }
//        }
//        return count == 0;
//    }
}
