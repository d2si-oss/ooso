package utils;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringInputStream;
import com.google.gson.Gson;
import coordinator.CoordinatorInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Commons {
    public final static String JSON_TYPE = "application/json";
    public final static String TEXT_TYPE = "text/plain";
    private final static Gson GSON = new Gson();


    public static List<S3ObjectSummary> getBucketObjectSummaries(String bucket) {
        return getBucketObjectSummaries(bucket, "");
    }

    public static List<S3ObjectSummary> getBucketObjectSummaries(String bucket, String prefix) {
        AmazonS3 s3Client = AmazonS3Provider.getS3Client();

        final ListObjectsRequest req = new ListObjectsRequest()
                .withBucketName(bucket)
                .withPrefix(prefix);
        ObjectListing objectListing = s3Client.listObjects(req);
        return objectListing.getObjectSummaries();
    }

    private static int getBatchSize(List<S3ObjectSummary> objectSummaries, int availableMemory) {
        int maxUsableMemory = (int) (0.6 * 1024 * 1024 * availableMemory);
        long totalSize = 0;
        for (S3ObjectSummary summary : objectSummaries)
            totalSize += summary.getSize();
        double averageSize = totalSize / objectSummaries.size();
        return (int) (maxUsableMemory / averageSize);
    }

    public static void storeObject(String contentType,
                                   String content,
                                   String destBucket,
                                   String destKey) throws UnsupportedEncodingException {
        AmazonS3 s3Client = AmazonS3Provider.getS3Client();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(content.getBytes().length);
        s3Client.putObject(
                destBucket,
                destKey,
                new StringInputStream(content),
                metadata);
    }

    public static List<List<ObjectInfoSimple>> getBatches(String bucket, int memory, String prefix, int desiredBatchSize) {
        List<S3ObjectSummary> objectSummaries = Commons.getBucketObjectSummaries(bucket, prefix);
        int batchSize = desiredBatchSize <= 0 ? Commons.getBatchSize(objectSummaries, memory) : desiredBatchSize;

        List<List<ObjectInfoSimple>> batches = new ArrayList<>(objectSummaries.size() / batchSize);
        List<ObjectInfoSimple> batch = new ArrayList<>(batchSize);
        int currentBatchSize = 0;
        for (S3ObjectSummary summary : objectSummaries) {
            if (currentBatchSize == batchSize) {
                batches.add(batch);
                batch = new ArrayList<>(batchSize);
                currentBatchSize = 0;
            }

            ObjectInfoSimple bucketAndKey = new ObjectInfoSimple(summary);

            batch.add(bucketAndKey);
            currentBatchSize++;
        }
        if (currentBatchSize != 0)
            batches.add(batch);

        return batches;
    }

    public static List<List<ObjectInfoSimple>> getBatches(String bucket, int memory) {
        return getBatches(bucket, memory, "", -1);
    }

    public static List<List<ObjectInfoSimple>> getBatches(String bucket, int memory, String prefix) {
        return getBatches(bucket, memory, prefix, -1);
    }

    public static List<List<ObjectInfoSimple>> getBatches(String bucket, int memory, int desiredBatchSize) {
        return getBatches(bucket, memory, "", desiredBatchSize);
    }

    public static void invokeLambdaAsync(String function, Object payload) {
        String payloadString = GSON.toJson(payload);

        AWSLambda lambda = AWSLambdaProvider.getLambdaClient();

        InvokeRequest request = new InvokeRequest()
                .withFunctionName(function)
                .withInvocationType(InvocationType.Event)
                .withPayload(payloadString);

        lambda.invoke(request);
    }

    public static void invokeLambdaSync(String function, Object payload) {
        String payloadString = GSON.toJson(payload);

        AWSLambda lambda = AWSLambdaProvider.getLambdaClient();

        InvokeRequest request = new InvokeRequest()
                .withFunctionName(function)
                .withPayload(payloadString);

        lambda.invoke(request);
    }

    public static BufferedReader getReaderFromObjectInfo(ObjectInfoSimple objectInfo) {
        AmazonS3 s3Client = AmazonS3Provider.getS3Client();

        S3Object object = s3Client.getObject(objectInfo.getBucket(), objectInfo.getKey());
        S3ObjectInputStream objectContentRawStream = object.getObjectContent();

        return new BufferedReader(new InputStreamReader(objectContentRawStream));
    }
}
