package utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class AmazonS3Provider {
    private static AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();

    public static AmazonS3 getInstance() {
        return s3Client;
    }

    private AmazonS3Provider() {
    }
}
