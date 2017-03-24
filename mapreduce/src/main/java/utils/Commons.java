package utils;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringInputStream;
import reducer_wrapper.ReducerStepInfo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Commons {

    public final static String JSON_TYPE = "application/json";
    public final static String TEXT_TYPE = "text/plain";


    private final static int TIME_TO_RETRY = 100;

    public static List<S3ObjectSummary> getBucketObjectSummaries(String bucket) {
        return getBucketObjectSummaries(bucket, "");
    }

    public static List<S3ObjectSummary> getBucketObjectSummaries(String bucket, String prefix) {
        AmazonS3 s3Client = AmazonS3Provider.getInstance();

        final ListObjectsRequest req = new ListObjectsRequest()
                .withBucketName(bucket)
                .withPrefix(prefix);
        ObjectListing objectListing = s3Client.listObjects(req);
        return objectListing.getObjectSummaries();
    }

    public static int getBatchSize(List<S3ObjectSummary> objectSummaries, int availableMemory) {
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
        AmazonS3 s3Client = AmazonS3Provider.getInstance();

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
        int batchSize = desiredBatchSize == -1 ? Commons.getBatchSize(objectSummaries, memory) : desiredBatchSize;

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

    private static S3Object getObjectWithRetries(String bucket, String key, int retries) throws InterruptedException {
        AmazonS3 s3Client = AmazonS3Provider.getInstance();

        S3Object jobInfoS3;
        if (retries <= 0)
            jobInfoS3 = null;
        else {
            try {
                jobInfoS3 = s3Client.getObject(bucket, key);
            } catch (AmazonS3Exception e) {
                if (e.getErrorCode().equals("NoSuchKey")) {
                    Thread.sleep(TIME_TO_RETRY);
                    return getObjectWithRetries(bucket, key, retries - 1);
                } else
                    throw e;
            }
        }
        return jobInfoS3;
    }

    public static ReducerStepInfo getStepInfo(int step) {
        Table mapreduce_state = StatusTableProvider.getStatusTable();

        GetItemSpec getItemSpec = new GetItemSpec()
                .withPrimaryKey("step", step)
                .withConsistentRead(true);

        Item item = mapreduce_state.getItem(getItemSpec);

        ReducerStepInfo stepInfo = new ReducerStepInfo();
        stepInfo.setStep(step);
        stepInfo.setFilesProcessed(item.getInt("filesProcessed"));
        stepInfo.setFilesToProcess(item.getInt("filesToProcess"));
        stepInfo.setBatchesCount(item.getInt("batchesCount"));

        return stepInfo;
    }

    public static void incrementFilesToProcess(int step, int increment) {
        Table mapreduce_state = StatusTableProvider.getStatusTable();

        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("step", step)
                .withUpdateExpression("set filesToProcess = filesToProcess + :ftp")
                .withValueMap(new ValueMap()
                        .withNumber(":ftp", increment));


        mapreduce_state.updateItem(updateItemSpec);
    }

    public static void incrementFilesProcessed(int step, int increment) {

        Table mapreduce_state = StatusTableProvider.getStatusTable();

        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("step", step)
                .withUpdateExpression("set filesProcessed = filesProcessed + :fp")
                .withValueMap(new ValueMap()
                        .withNumber(":fp", increment));


        mapreduce_state.updateItem(updateItemSpec);
    }

    public static void updateStepInfo(int step,
                                      int filesToProcess,
                                      int filesProcessed,
                                      int batchesCount) {


        Table mapreduce_state = StatusTableProvider.getStatusTable();

        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("step", step)
                .withUpdateExpression("set filesProcessed = :fp, filesToProcess = :ftp, batchesCount = :bc")
                .withValueMap(new ValueMap()
                        .withNumber(":fp", filesProcessed)
                        .withNumber(":ftp", filesToProcess)
                        .withNumber(":bc", batchesCount));


        mapreduce_state.updateItem(updateItemSpec);

    }

    public static void updateStepInfo(int step,
                                      int filesToProcess,
                                      int filesProcessed) {


        Table mapreduce_state = StatusTableProvider.getStatusTable();

        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("step", step)
                .withUpdateExpression("set filesProcessed = :fp, filesToProcess = :ftp")
                .withValueMap(new ValueMap()
                        .withNumber(":fp", filesProcessed)
                        .withNumber(":ftp", filesToProcess));


        mapreduce_state.updateItem(updateItemSpec);

    }

    public static void setBatchesCount(int step,
                                       int batchesCount) {


        Table mapreduce_state = StatusTableProvider.getStatusTable();

        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("step", step)
                .withUpdateExpression("set batchesCount = :bc")
                .withValueMap(new ValueMap()
                        .withNumber(":bc", batchesCount));


        mapreduce_state.updateItem(updateItemSpec);

    }

    public static void invokeLambdaAsync(String function, String payload) {
        AWSLambdaAsync lambda = AWSLambdaProvider.getInstance();

        InvokeRequest request = new InvokeRequest()
                .withFunctionName(function)
                .withInvocationType(InvocationType.Event)
                .withPayload(payload);

        lambda.invoke(request);
    }
}
