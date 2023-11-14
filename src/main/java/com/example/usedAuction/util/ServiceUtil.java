package com.example.usedAuction.util;

import java.util.UUID;

public class ServiceUtil {

    public static String makeUploadFileName(String imageFilename) {
        String extension = imageFilename.substring(imageFilename.lastIndexOf(".")+1);
        return  UUID.randomUUID() + "."+extension;
    }
}
