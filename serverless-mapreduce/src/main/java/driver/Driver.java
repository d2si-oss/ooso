package driver;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import coordinator.CoordinatorInfo;
import mapper_wrapper.MapperWrapperInfo;
import utils.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Driver implements RequestHandler<Void, String> {

    private AmazonS3 s3Client;
    private JobInfo jobInfo;

    private String jobId;

    @Override
    public String handleRequest(Void event, Context context) {
        try {
            this.s3Client = AmazonS3Provider.getS3Client();
            this.jobInfo = JobInfoProvider.getJobInfo();

            this.jobId = this.jobInfo.getJobId();

            cleanup();

            validateParamsOrFail();

            List<List<ObjectInfoSimple>> batches = Commons.getBatches(
                    this.jobInfo.getJobInputBucket(),
                    this.jobInfo.getMapperMemory(),
                    this.jobInfo.getMapperForceBatchSize());


            invokeMappers(batches);

            invokeReducerCoordinator();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "Ok";
    }

    private void cleanup() {

        List<S3ObjectSummary> mapOutput = Commons.getBucketObjectSummaries(this.jobInfo.getMapperOutputBucket(), this.jobId + "/");
        List<S3ObjectSummary> reduceOutput = Commons.getBucketObjectSummaries(this.jobInfo.getReducerOutputBucket(), this.jobId + "/");

        for (S3ObjectSummary object : mapOutput)
            this.s3Client.deleteObject(object.getBucketName(), object.getKey());

        for (S3ObjectSummary object : reduceOutput)
            this.s3Client.deleteObject(object.getBucketName(), object.getKey());

    }

    private void validateParamsOrFail() {
        if (!this.s3Client.doesBucketExist(this.jobInfo.getJobInputBucket()))
            throw new AmazonS3Exception("Bad parameter <jobInputBucket>: Bucket does not exist");

        if (!this.s3Client.doesBucketExist(this.jobInfo.getMapperOutputBucket()))
            throw new AmazonS3Exception("Bad parameter <mapperOutputBucket>: Bucket does not exist");

        if (!this.s3Client.doesBucketExist(this.jobInfo.getReducerOutputBucket()))
            throw new AmazonS3Exception("Bad parameter <reducerOutputBucket>: Bucket does not exist");

        if (this.jobInfo.getReducerForceBatchSize() == 1)
            throw new AmazonS3Exception("Bad parameter <reducerForceBatchSize>: Reducer batch size must be greater or equal than 2");
    }

    private void invokeMappers(List<List<ObjectInfoSimple>> batches) throws InterruptedException {
        int currentMapperId = 0;


        ExecutorService executorService = Executors.newFixedThreadPool(batches.size());

        for (List<ObjectInfoSimple> batch : batches) {
            int finalCurrentMapperId = currentMapperId;

            executorService.submit(() -> {
                MapperWrapperInfo mapperWrapperInfo = new MapperWrapperInfo(batch, finalCurrentMapperId);
                Commons.invokeLambdaSync(this.jobInfo.getMapperFunctionName(), mapperWrapperInfo);
            });

            currentMapperId++;
        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
    }

    private void invokeReducerCoordinator() {
        CoordinatorInfo coordinatorInfo = new CoordinatorInfo(0);
        Commons.invokeLambdaAsync("coordinator", coordinatorInfo);
    }

}
