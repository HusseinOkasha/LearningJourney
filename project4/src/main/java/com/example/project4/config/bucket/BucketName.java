package com.example.project4.config.bucket;

public enum BucketName {

    PROFILE_IMAGE( "amigos-code-upload-image");
    private final String bucketName;
    BucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
