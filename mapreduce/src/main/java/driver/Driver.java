package driver;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.Gson;
import mapper_wrapper.MapperWrapperInfo;

import java.util.ArrayList;
import java.util.List;

public class Driver implements RequestHandler<DriverInfo, String> {

    @Override
    public String handleRequest(DriverInfo driverInfo, Context context) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        List<List<String>> batches = getBatches(s3Client, driverInfo.getJobInputBucket(), driverInfo.getMapperMemory());

        AWSLambdaAsync lambda = AWSLambdaAsyncClientBuilder.defaultClient();

        long currentMapperId = 0;

        for (List<String> batch : batches) {
            MapperWrapperInfo mapperWrapperInfo = new MapperWrapperInfo(batch, driverInfo.getJobInputBucket(), driverInfo.getMapperOutputBucket(), currentMapperId++);

            Gson gson = new Gson();
            String payload = gson.toJson(mapperWrapperInfo);

            InvokeRequest request = new InvokeRequest()
                    .withFunctionName("mapper")
                    .withInvocationType(InvocationType.Event)
                    .withPayload(payload);

            lambda.invokeAsync(request);
        }
        return "Ok";
    }

    private static List<List<String>> getBatches(AmazonS3 s3Client, String jobInputBucket, int mapperMemory) {
        List<S3ObjectSummary> objectSummaries = getBucketObjectSummaries(s3Client, jobInputBucket);
        int batchSize = getBatchSize(objectSummaries, mapperMemory);

        List<List<String>> batches = new ArrayList<>(objectSummaries.size() / batchSize);
        List<String> batch = new ArrayList<>(batchSize);
        int currentBatchSize = 0;
        for (S3ObjectSummary summary : objectSummaries) {
            if (currentBatchSize == batchSize) {
                batches.add(batch);
                batch = new ArrayList<>(batchSize);
                currentBatchSize = 0;
            }
            batch.add(summary.getKey());
            currentBatchSize++;
        }
        if (currentBatchSize != 0)
            batches.add(batch);

        return batches;
    }

    private static List<S3ObjectSummary> getBucketObjectSummaries(AmazonS3 s3Client, String jobInputBucket) {
        final ListObjectsRequest req = new ListObjectsRequest().withBucketName(jobInputBucket);
        ObjectListing objectListing = s3Client.listObjects(req);
        return objectListing.getObjectSummaries();
    }

    private static int getBatchSize(List<S3ObjectSummary> objectSummaries, int mapperMemory) {
        int maxUsableMemory = (int) (0.6 * 1024 * 1024 * mapperMemory);
        long totalSize = 0;
        for (S3ObjectSummary summary : objectSummaries)
            totalSize += summary.getSize();
        double averageSize = totalSize / objectSummaries.size();
        return (int) (maxUsableMemory / averageSize);
    }
}
