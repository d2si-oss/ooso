package mapper_wrapper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import mapper_logic.MapperLogic;
import utils.Commons;
import utils.JobInfo;
import utils.JobInfoProvider;
import utils.ObjectInfoSimple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MapperWrapper implements RequestHandler<MapperWrapperInfo, String> {

    private AmazonS3 s3Client;
    private MapperWrapperInfo mapperWrapperInfo;
    private JobInfo jobInfo;
    private String jobId;

    @Override
    public String handleRequest(MapperWrapperInfo mapperWrapperInfo, Context context) {

        try {
            this.s3Client = AmazonS3ClientBuilder.standard().build();
            this.jobInfo = JobInfoProvider.getJobInfo();

            this.jobId = this.jobInfo.getJobId();

            this.mapperWrapperInfo = mapperWrapperInfo;
            List<ObjectInfoSimple> batch = mapperWrapperInfo.getBatch();

            for (ObjectInfoSimple object : batch) {
                String processResult = processKey(object.getKey());
                storeResult(processResult, object.getKey());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }


    private void storeResult(String result, String key) throws IOException {

        Commons.storeObject(Commons.JSON_TYPE,
                result,
                this.jobInfo.getMapperOutputBucket(),
                this.jobId + "/" + key + "-" + this.mapperWrapperInfo.getId());
    }

    private String processKey(String key) throws IOException {
        S3Object object = s3Client.getObject(this.jobInfo.getJobInputBucket(), key);
        S3ObjectInputStream objectContentRawStream = object.getObjectContent();
        BufferedReader objectBufferedReader = new BufferedReader(new InputStreamReader(objectContentRawStream));

        String result = MapperLogic.mapResultCalculator(objectBufferedReader);

        objectBufferedReader.close();
        objectContentRawStream.close();
        object.close();

        return result;
    }

}
