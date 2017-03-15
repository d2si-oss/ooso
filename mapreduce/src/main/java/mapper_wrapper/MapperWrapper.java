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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class MapperWrapper implements RequestHandler<MapperWrapperInfo, String> {

    @Override
    public String handleRequest(MapperWrapperInfo mapperWrapperInfo, Context context) {

        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
            List<String> batch = mapperWrapperInfo.getBatch();


            for (String key : batch) {
                String processResult = processKey(mapperWrapperInfo, s3Client, key);
                storeResult(mapperWrapperInfo, s3Client, processResult, key);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }

    private void storeResult(MapperWrapperInfo mapperWrapperInfo, AmazonS3 s3Client, String result, String key) throws UnsupportedEncodingException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        metadata.setContentLength(result.getBytes().length);
        s3Client.putObject(mapperWrapperInfo.getOutputBucket(),
                key + "-" + mapperWrapperInfo.getId(),
                new StringInputStream(result),
                metadata);
    }

    private String processKey(MapperWrapperInfo mapperWrapperInfo, AmazonS3 s3Client, String key) throws IOException {
        S3Object object = s3Client.getObject(mapperWrapperInfo.getInputBucket(), key);
        S3ObjectInputStream objectContentRawStream = object.getObjectContent();
        BufferedReader objectBufferedReader = new BufferedReader(new InputStreamReader(objectContentRawStream));


        String result = MapperLogic.mapResultCalculator(objectBufferedReader);

        objectBufferedReader.close();
        objectContentRawStream.close();
        object.close();

        return result;
    }

}
