package driver;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import mapper_wrapper.MapperWrapperInfo;
import utils.*;

import java.util.List;

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
        String realInputBucket = Commons.getBucketFromFullPath(this.jobInfo.getJobInputBucket());
        if (!this.s3Client.doesBucketExist(realInputBucket))
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

        for (List<ObjectInfoSimple> batch : batches) {

            MapperWrapperInfo mapperWrapperInfo = new MapperWrapperInfo(batch, currentMapperId);
            Commons.invokeLambdaAsync(this.jobInfo.getMapperFunctionName(), mapperWrapperInfo);
            currentMapperId++;
        }
    }

}
