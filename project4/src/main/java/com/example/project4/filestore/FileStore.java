package com.example.project4.filestore;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
@Service
public class FileStore {

    private final AmazonS3 s3;

    @Autowired
    public FileStore(AmazonS3 amazonS3){
        this.s3 = amazonS3;
    }

    public void save(String bucketName,
                     String path,
                     Optional<Map<String, String>> optionalMetaData,
                     InputStream inputStream
    ){
        // extract the metadata of the file
        ObjectMetadata metaData = new ObjectMetadata();
        optionalMetaData.ifPresent(map->{
            if(!map.isEmpty()){
                map.forEach(metaData::addUserMetadata);
            }
        });

        try{
            s3.putObject(bucketName, path, inputStream, metaData);
        }
        catch (AmazonServiceException e){
            throw new IllegalStateException("failed to store file to s3", e);
        }
    }

    // downloads image from s3.
    public byte[] download(String bucketName, String path){
        try{
            S3Object object =  s3.getObject(bucketName, path );
            return IOUtils.toByteArray(object.getObjectContent());
        }
        catch (AmazonServiceException | IOException e) {
            throw new IllegalStateException("failed to download the file from s3", e);
        }
    }
    public void  createFolderIfNotExists(String bucketName, String folderPath ) throws IOException {
        try {
            // Check if the folder (object) already exists
            s3.getObjectMetadata(bucketName, folderPath);
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                // Create the folder by uploading an empty file
                File emptyFile = new File("/tmp/empty");
                emptyFile.createNewFile();
                s3.putObject(new PutObjectRequest(bucketName, folderPath, emptyFile));
            } else {

                throw e;
            }
        }
    }
}
