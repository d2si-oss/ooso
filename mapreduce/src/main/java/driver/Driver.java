package driver;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
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
import utils.StatusTableProvider;

import java.io.IOException;
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
            this.s3Client = AmazonS3ClientBuilder.standard().build();
            this.jobInfo = JobInfoProvider.getJobInfo();
            this.gson = new Gson();

            cleanup();

            List<List<Map<String, String>>> batches = Commons.getBatches(this.jobInfo.getJobInputBucket(), this.jobInfo.getMapperMemory());

            AWSLambdaAsync lambda = AWSLambdaAsyncClientBuilder.defaultClient();

            int currentMapperId = 0;

            Map<Integer, Integer> batchSizePerMapper = new HashMap<>(batches.size());

            for (List<Map<String, String>> batch : batches) {
                MapperWrapperInfo mapperWrapperInfo = new MapperWrapperInfo(batch, currentMapperId);

                String payload = this.gson.toJson(mapperWrapperInfo);

                InvokeRequest request = new InvokeRequest()
                        .withFunctionName(this.jobInfo.getMapperFunctionName())
                        .withInvocationType(InvocationType.Event)
                        .withPayload(payload);

                lambda.invoke(request);

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

        Commons.storeObject(Commons.JSON_TYPE,
                jobInfoJson, this.jobInfo.getStatusBucket(),
                this.jobInfo.getMappersInfoName());
    }

    private void cleanup() {

        List<S3ObjectSummary> statusObjects = Commons.getBucketObjectSummaries(this.jobInfo.getStatusBucket());
        List<S3ObjectSummary> mapOutput = Commons.getBucketObjectSummaries(this.jobInfo.getMapperOutputBucket());
        List<S3ObjectSummary> reduceOutput = Commons.getBucketObjectSummaries(this.jobInfo.getReducerOutputBucket());

//        Stream.concat(Stream.concat(statusObjects.stream(), mapOutput.stream()), reduceOutput.stream())
//                .forEach(object -> this.s3Client.deleteObject(object.getBucketName(), object.getKey()));

        for (S3ObjectSummary object : statusObjects)
            this.s3Client.deleteObject(object.getBucketName(), object.getKey());

        for (S3ObjectSummary object : mapOutput)
            this.s3Client.deleteObject(object.getBucketName(), object.getKey());

        for (S3ObjectSummary object : reduceOutput)
            this.s3Client.deleteObject(object.getBucketName(), object.getKey());


        Table statusTable = StatusTableProvider.getStatusTable();

        ItemCollection<ScanOutcome> scan = statusTable.scan(new ScanSpec());

        for (Item item : scan) {
            statusTable.deleteItem("step",item.getInt("step"));
        }

    }

}
