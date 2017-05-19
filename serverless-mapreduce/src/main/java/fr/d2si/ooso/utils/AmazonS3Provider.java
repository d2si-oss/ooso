package fr.d2si.ooso.utils;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class AmazonS3Provider {

    public static AmazonS3 getS3Client() {
        String smr_stage = System.getenv().get("SMR_STAGE");
        if (smr_stage.equals("DEV"))
            return FakeAmazonS3Holder.S3_CLIENT;
        else
            return AmazonS3Holder.S3_CLIENT;
    }

    private static class AmazonS3Holder {
        private static final AmazonS3 S3_CLIENT = AmazonS3ClientBuilder.defaultClient();
    }

    private static class FakeAmazonS3Holder {
        private static final AmazonS3 S3_CLIENT = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://0.0.0.0:4567", "eu-west-1"))
                .withChunkedEncodingDisabled(true)
                .withPathStyleAccessEnabled(true)
                .build();
    }

    private AmazonS3Provider() {
    }
}
