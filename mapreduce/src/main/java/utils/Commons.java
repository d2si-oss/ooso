package utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringInputStream;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class Commons {
    public static List<S3ObjectSummary> getBucketObjectSummaries(AmazonS3 s3Client, String jobInputBucket) {
        final ListObjectsRequest req = new ListObjectsRequest().withBucketName(jobInputBucket);
        ObjectListing objectListing = s3Client.listObjects(req);
        return objectListing.getObjectSummaries();
    }

    public static int getBatchSize(List<S3ObjectSummary> objectSummaries, int mapperMemory) {
        int maxUsableMemory = (int) (0.6 * 1024 * 1024 * mapperMemory);
        long totalSize = 0;
        for (S3ObjectSummary summary : objectSummaries)
            totalSize += summary.getSize();
        double averageSize = totalSize / objectSummaries.size();
        return (int) (maxUsableMemory / averageSize);
    }

    public static void storeObject(AmazonS3 s3Client,
                                   String contentType,
                                   String content,
                                   String destBucket,
                                   String destKey) throws UnsupportedEncodingException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(content.getBytes().length);
        s3Client.putObject(
                destBucket,
                destKey,
                new StringInputStream(content),
                metadata);
    }
}
