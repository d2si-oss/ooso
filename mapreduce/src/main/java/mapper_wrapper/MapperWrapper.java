package mapper_wrapper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.StringInputStream;
import mapper_logic.MapperLogic;
import utils.JobInfo;
import utils.JobInfoProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

public class MapperWrapper implements RequestHandler<MapperWrapperInfo, String> {

    private AmazonS3 s3Client;
    private MapperWrapperInfo mapperWrapperInfo;
    private JobInfo jobInfo;

    @Override
    public String handleRequest(MapperWrapperInfo mapperWrapperInfo, Context context) {

        try {
            Date startTime = new Date();

            this.s3Client = this.s3Client == null ? AmazonS3ClientBuilder.standard().build() : this.s3Client;
            this.jobInfo = this.jobInfo == null ? JobInfoProvider.getJobInfo() : this.jobInfo;
            this.mapperWrapperInfo = mapperWrapperInfo;
            List<String> batch = mapperWrapperInfo.getBatch();

            for (String key : batch) {
                String processResult = processKey(key);
                storeResult(processResult, key);
            }

            Date finishTime = new Date();

            storeDuration(startTime, finishTime);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return "OK";
    }

    private void storeDuration(Date startTime, Date finishTime) throws UnsupportedEncodingException {
        int duration = (int) ((finishTime.getTime() - startTime.getTime()) / 1000);
        String durationString = String.valueOf(duration);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        metadata.setContentLength(durationString.getBytes().length);
        this.s3Client.putObject(
                this.jobInfo.getStatusBucket(),
                String.valueOf(this.mapperWrapperInfo.getId()),
                new StringInputStream(durationString),
                metadata);
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
