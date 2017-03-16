package mapper_wrapper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.StringInputStream;
import utils.JobInfo;
import mapper_logic.MapperLogic;
import utils.JobInfoProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MapperWrapper implements RequestHandler<MapperWrapperInfo, String> {

    private AmazonS3 s3Client;
    private MapperWrapperInfo mapperWrapperInfo;
    private JobInfo jobInfo;

    @Override
    public String handleRequest(MapperWrapperInfo mapperWrapperInfo, Context context) {

        try {
            this.s3Client = AmazonS3ClientBuilder.standard().build();
            this.mapperWrapperInfo = mapperWrapperInfo;
            this.jobInfo = JobInfoProvider.getJobInfo();
            List<String> batch = mapperWrapperInfo.getBatch();


            for (String key : batch) {
                String processResult = processKey(key);
                storeResult(processResult, key);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }

    private void storeResult(String result, String key) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/json");
        metadata.setContentLength(result.getBytes().length);
        s3Client.putObject(this.jobInfo.getMapperOutputBucket(),
                key + "-" + mapperWrapperInfo.getId(),
                new StringInputStream(result),
                metadata);
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
