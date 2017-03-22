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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MapperWrapper implements RequestHandler<MapperWrapperInfo, String> {

    private AmazonS3 s3Client;
    private MapperWrapperInfo mapperWrapperInfo;
    private JobInfo jobInfo;

    @Override
    public String handleRequest(MapperWrapperInfo mapperWrapperInfo, Context context) {

        try {
            Date startTime = new Date();

            this.s3Client = AmazonS3ClientBuilder.standard().build();
            this.jobInfo = JobInfoProvider.getJobInfo();
            this.mapperWrapperInfo = mapperWrapperInfo;
            List<Map<String, String>> batch = mapperWrapperInfo.getBatch();

            for (Map<String, String> object : batch) {
                String processResult = processKey(object.get("key"));
                storeResult(processResult, object.get("key"));
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

        Commons.storeObject(Commons.TEXT_TYPE,
                durationString,
                this.jobInfo.getStatusBucket(),
                String.valueOf(this.mapperWrapperInfo.getId()));
    }


    private void storeResult(String result, String key) throws IOException {

        Commons.storeObject(Commons.JSON_TYPE,
                result,
                this.jobInfo.getMapperOutputBucket(),
                key + "-" + this.mapperWrapperInfo.getId());
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
