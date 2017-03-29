package utils;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringInputStream;
import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Commons {
    public final static String JSON_TYPE = "application/json";
    public final static String TEXT_TYPE = "text/plain";


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

    public static void invokeLambdaAsync(String function, String payload) {
        AWSLambdaAsync lambda = AWSLambdaProvider.getLambdaClient();

        InvokeRequest request = new InvokeRequest()
                .withFunctionName(function)
                .withInvocationType(InvocationType.Event)
                .withPayload(payload);

        lambda.invoke(request);
    }


    public static void setStartDate(String job, DateTime startDate) {

        Table mapreduce_state = StatusTableProvider.getStatusTable();

        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("job", job)
                .withUpdateExpression("set startDate = :sd")
                .withValueMap(new ValueMap()
                        .withString(":sd", startDate.toString()));

        mapreduce_state.updateItem(updateItemSpec);

    }

    public static void setFinishDate(String job, DateTime finishDate) {

        Table mapreduce_state = StatusTableProvider.getStatusTable();

        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("job", job)
                .withUpdateExpression("set finishDate = :fd")
                .withValueMap(new ValueMap()
                        .withString(":fd", finishDate.toString()));

        mapreduce_state.updateItem(updateItemSpec);

    }

    public static InvokeResult invokeLambdaSync(String function, String payload) {
        AWSLambdaAsync lambda = AWSLambdaProvider.getLambdaClient();

        InvokeRequest request = new InvokeRequest()
                .withFunctionName(function)
                .withPayload(payload);

        return lambda.invoke(request);
    }
}
