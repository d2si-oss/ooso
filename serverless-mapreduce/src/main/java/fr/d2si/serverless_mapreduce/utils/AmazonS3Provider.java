package fr.d2si.serverless_mapreduce.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class AmazonS3Provider {

    public static AmazonS3 getS3Client() {
        return AmazonS3Holder.S3_CLIENT;
    }

    private static class AmazonS3Holder {
        private static final AmazonS3 S3_CLIENT = AmazonS3ClientBuilder.standard().build();
    }

    private AmazonS3Provider() {
    }
}
