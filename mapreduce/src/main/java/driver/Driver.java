package driver;

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
import utils.ObjectInfoSimple;

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

            List<List<ObjectInfoSimple>> batches = Commons.getBatches(this.jobInfo.getJobInputBucket(), this.jobInfo.getMapperMemory());


            int currentMapperId = 0;

            Map<Integer, Integer> batchSizePerMapper = new HashMap<>(batches.size());

            for (List<ObjectInfoSimple> batch : batches) {
                MapperWrapperInfo mapperWrapperInfo = new MapperWrapperInfo(batch, currentMapperId);

                String payload = this.gson.toJson(mapperWrapperInfo);

                Commons.invokeLambdaAsync(this.jobInfo.getMapperFunctionName(),payload);

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

    }

}
