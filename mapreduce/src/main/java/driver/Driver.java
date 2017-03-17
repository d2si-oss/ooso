package driver;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.Gson;
import mapper_wrapper.MapperWrapperInfo;
import utils.Commons;
import utils.JobInfo;
import utils.JobInfoProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Driver implements RequestHandler<Void, String> {
    private AmazonS3 s3Client;
    private JobInfo jobInfo;
    private Gson gson;

    @Override
    public String handleRequest(Void event, Context context) {
        try {
            this.s3Client = this.s3Client == null ? AmazonS3ClientBuilder.standard().build() : this.s3Client;
            this.jobInfo = this.jobInfo == null ? JobInfoProvider.getJobInfo() : this.jobInfo;
            this.gson = new Gson();

            List<List<String>> batches = getBatches(this.jobInfo.getJobInputBucket(), this.jobInfo.getMapperMemory());

            AWSLambdaAsync lambda = AWSLambdaAsyncClientBuilder.defaultClient();

            int currentMapperId = 0;

            Map<Integer, Integer> batchSizePerMapper = new HashMap<>(batches.size());

            for (List<String> batch : batches) {
                MapperWrapperInfo mapperWrapperInfo = new MapperWrapperInfo(batch, currentMapperId);

                String payload = this.gson.toJson(mapperWrapperInfo);

                InvokeRequest request = new InvokeRequest()
                        .withFunctionName("mapper")
                        .withInvocationType(InvocationType.Event)
                        .withPayload(payload);

                lambda.invokeAsync(request);

                batchSizePerMapper.put(currentMapperId++, batch.size());

            }

            updateMappersInfo(batches.size(), batchSizePerMapper);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Ok";
    }

    private void updateMappersInfo(int mapperCount, Map<Integer, Integer> batchSizePerMapper) throws IOException {
        MappersInfo mappersInfo = new MappersInfo();
        mappersInfo.setMapperCount(mapperCount);
        mappersInfo.setBatchCountPerMapper(batchSizePerMapper);

        String jobInfoJson = this.gson.toJson(mappersInfo);

        Commons.storeObject(this.s3Client,
                "application/json",
                jobInfoJson, this.jobInfo.getStatusBucket(),
                this.jobInfo.getMappersInfoName());
    }


    private List<List<String>> getBatches(String jobInputBucket, int mapperMemory) {
        List<S3ObjectSummary> objectSummaries = Commons.getBucketObjectSummaries(this.s3Client, jobInputBucket);
        int batchSize = Commons.getBatchSize(objectSummaries, mapperMemory);

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

}
