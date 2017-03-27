package driver;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.Gson;
import mapper_wrapper.MapperWrapperInfo;
import utils.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Driver implements RequestHandler<Void, String> {

    private AmazonS3 s3Client;
    private JobInfo jobInfo;
    private Gson gson;

    private String jobId;

    @Override
    public String handleRequest(Void event, Context context) {
        try {
            this.s3Client = AmazonS3ClientBuilder.standard().build();
            this.jobInfo = JobInfoProvider.getJobInfo();
            this.gson = new Gson();

            this.jobId = this.jobInfo.getJobId();

            cleanup();

            List<List<ObjectInfoSimple>> batches = Commons.getBatches(this.jobInfo.getJobInputBucket(), this.jobInfo.getMapperMemory());


            int currentMapperId = 0;

            Map<Integer, Integer> batchSizePerMapper = new HashMap<>(batches.size());

            for (List<ObjectInfoSimple> batch : batches) {
                MapperWrapperInfo mapperWrapperInfo = new MapperWrapperInfo(batch, currentMapperId);

                String payload = this.gson.toJson(mapperWrapperInfo);

                Commons.invokeLambdaAsync(this.jobInfo.getMapperFunctionName(), payload);

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

        Table statusTable = StatusTableProvider.getStatusTable();
        Item info = new Item()
                .withString("job", this.jobId)
                .withNumber("step", Commons.MAP_INFO_DUMMY_STEP)
                .withJSON("map_info", jobInfoJson);

        statusTable.putItem(info);
    }

    private void cleanup() {

        List<S3ObjectSummary> mapOutput = Commons.getBucketObjectSummaries(this.jobInfo.getMapperOutputBucket());
        List<S3ObjectSummary> reduceOutput = Commons.getBucketObjectSummaries(this.jobInfo.getReducerOutputBucket());

//        Stream.concat(Stream.concat(statusObjects.stream(), mapOutput.stream()), reduceOutput.stream())
//                .forEach(object -> this.s3Client.deleteObject(object.getBucketName(), object.getKey()));

        for (S3ObjectSummary object : mapOutput)
            this.s3Client.deleteObject(object.getBucketName(), object.getKey());

        for (S3ObjectSummary object : reduceOutput)
            this.s3Client.deleteObject(object.getBucketName(), object.getKey());


        Table statusTable = StatusTableProvider.getStatusTable();

        ItemCollection<QueryOutcome> query = statusTable.query("job", this.jobId);

        for (Item item : query) {
            statusTable.deleteItem("job", item.getString("job"),
                    "step", item.getInt("step"));
        }

    }

}
