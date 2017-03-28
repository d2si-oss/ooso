package driver;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.Gson;
import coordinator.CoordinatorInfo;
import mapper_wrapper.MapperWrapperInfo;
import utils.Commons;
import utils.JobInfo;
import utils.JobInfoProvider;
import utils.ObjectInfoSimple;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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


            invokeMappers(batches);

            invokeReducerCoordinator();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Ok";
    }

    private void invokeReducerCoordinator() {
        CoordinatorInfo coordinatorInfo = new CoordinatorInfo(0);
        String payload = this.gson.toJson(coordinatorInfo);
        Commons.invokeLambdaAsync("coordinator", payload);
    }

    private void invokeMappers(List<List<ObjectInfoSimple>> batches) throws InterruptedException {
        int currentMapperId = 0;


        ExecutorService executorService = Executors.newFixedThreadPool(batches.size());

        for (List<ObjectInfoSimple> batch : batches) {
            int finalCurrentMapperId = currentMapperId;

            executorService.submit(() -> {
                MapperWrapperInfo mapperWrapperInfo = new MapperWrapperInfo(batch, finalCurrentMapperId);
                String payload = this.gson.toJson(mapperWrapperInfo);
                Commons.invokeLambdaSync(this.jobInfo.getMapperFunctionName(), payload);
            });

            currentMapperId++;
        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
    }


    private void cleanup() {

        List<S3ObjectSummary> mapOutput = Commons.getBucketObjectSummaries(this.jobInfo.getMapperOutputBucket(), this.jobId + "/");
        List<S3ObjectSummary> reduceOutput = Commons.getBucketObjectSummaries(this.jobInfo.getReducerOutputBucket(), this.jobId + "/");

        for (S3ObjectSummary object : mapOutput)
            this.s3Client.deleteObject(object.getBucketName(), object.getKey());

        for (S3ObjectSummary object : reduceOutput)
            this.s3Client.deleteObject(object.getBucketName(), object.getKey());

    }

}
