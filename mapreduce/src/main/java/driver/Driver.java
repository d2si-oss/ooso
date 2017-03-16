package driver;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringInputStream;
import com.google.gson.Gson;
import mapper_wrapper.MapperWrapperInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Driver implements RequestHandler<DriverInfo, String> {
    private AmazonS3 s3Client;
    private DriverInfo driverInfo;
    private Gson gson;

    @Override
    public String handleRequest(DriverInfo driverInfo, Context context) {
        try {
            this.s3Client = AmazonS3ClientBuilder.standard().build();
            this.driverInfo = driverInfo;
            this.gson = new Gson();

            List<List<String>> batches = getBatches(driverInfo.getJobInputBucket(), driverInfo.getMapperMemory());

            AWSLambdaAsync lambda = AWSLambdaAsyncClientBuilder.defaultClient();

            long currentMapperId = 0;

            Map<Long, Integer> batchSizePerMapper = new HashMap<>(batches.size());

            for (List<String> batch : batches) {
                MapperWrapperInfo mapperWrapperInfo = new MapperWrapperInfo(batch, driverInfo.getJobInputBucket(), driverInfo.getMapperOutputBucket(), currentMapperId);

                String payload = this.gson.toJson(mapperWrapperInfo);

                InvokeRequest request = new InvokeRequest()
                        .withFunctionName("mapper")
                        .withInvocationType(InvocationType.Event)
                        .withPayload(payload);

                lambda.invokeAsync(request);

                batchSizePerMapper.put(currentMapperId++, batch.size());

            }

            updateJobInfo(batches.size(), batchSizePerMapper);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Ok";
    }

    private void updateJobInfo(long mapperCount, Map<Long, Integer> batchSizePerMapper) throws UnsupportedEncodingException {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setMapperCount(mapperCount);
        jobInfo.setBatchCountPerMapper(batchSizePerMapper);
        String jobInfoJson = this.gson.toJson(jobInfo);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/json");
        metadata.setContentLength(jobInfoJson.getBytes().length);

        this.s3Client.putObject(this.driverInfo.getStatusBucket(),
                this.driverInfo.getJobInfoName(),
                new StringInputStream(jobInfoJson),
                metadata);
    }


    private List<List<String>> getBatches(String jobInputBucket, int mapperMemory) {
        List<S3ObjectSummary> objectSummaries = getBucketObjectSummaries(jobInputBucket);
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

    private List<S3ObjectSummary> getBucketObjectSummaries(String jobInputBucket) {
        final ListObjectsRequest req = new ListObjectsRequest().withBucketName(jobInputBucket);
        ObjectListing objectListing = s3Client.listObjects(req);
        return objectListing.getObjectSummaries();
    }

    private int getBatchSize(List<S3ObjectSummary> objectSummaries, int mapperMemory) {
        int maxUsableMemory = (int) (0.6 * 1024 * 1024 * mapperMemory);
        long totalSize = 0;
        for (S3ObjectSummary summary : objectSummaries)
            totalSize += summary.getSize();
        double averageSize = totalSize / objectSummaries.size();
        return (int) (maxUsableMemory / averageSize);
    }
}
