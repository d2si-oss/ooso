package fr.d2si.ooso.mappers_driver;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import fr.d2si.ooso.mapper_wrapper.MapperWrapperInfo;
import fr.d2si.ooso.mappers_listener.MappersListenerInfo;
import fr.d2si.ooso.utils.*;

import java.util.List;

import static fr.d2si.ooso.utils.Commons.IGNORED_RETURN_VALUE;

public class MappersDriver implements RequestHandler<MappersDriverInfo, String> {

    private AmazonS3 s3Client;
    private JobInfo jobInfo;

    private String jobId;
    private MappersDriverInfo mappersDriverInfo;

    @Override
    public String handleRequest(MappersDriverInfo mappersDriverInfo, Context context) {
        try {
            this.mappersDriverInfo = mappersDriverInfo;

            this.s3Client = AmazonS3Provider.getS3Client();

            this.jobInfo = this.mappersDriverInfo.getJobInfo();

            this.jobId = this.jobInfo.getJobId();

            cleanup();

            validateParamsOrFail();

            List<List<ObjectInfoSimple>> batches = getBatches();

            invokeMappersListener();

            invokeMappers(batches);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return IGNORED_RETURN_VALUE;
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

    private List<List<ObjectInfoSimple>> getBatches() {
        return Commons.getBatches(
                this.jobInfo.getJobInputBucket(),
                this.jobInfo.getMapperMemory(),
                this.jobInfo.getMapperForceBatchSize());
    }

    private void invokeMappersListener() {
        Commons.invokeLambdaAsync(
                this.jobInfo.getMappersListenerFunctionName(),
                new MappersListenerInfo(
                        this.mappersDriverInfo.getReducerInBase64(),
                        this.jobInfo));
    }

    private void invokeMappers(List<List<ObjectInfoSimple>> batches) {
        int currentMapperId = 0;

        for (List<ObjectInfoSimple> batch : batches) {
            MapperWrapperInfo mapperWrapperInfo = new MapperWrapperInfo(
                    batch,
                    currentMapperId++,
                    this.mappersDriverInfo.getMapperInBase64(),
                    this.jobInfo);

            Commons.invokeLambdaAsync(this.jobInfo.getMapperFunctionName(), mapperWrapperInfo);
        }
    }

}
