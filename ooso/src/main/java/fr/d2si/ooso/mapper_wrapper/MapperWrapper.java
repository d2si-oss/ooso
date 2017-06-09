package fr.d2si.ooso.mapper_wrapper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import fr.d2si.ooso.mapper.MapperAbstract;
import fr.d2si.ooso.utils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MapperWrapper implements RequestHandler<MapperWrapperInfo, String> {
    private MapperAbstract mapperLogic;

    private AmazonS3 s3Client;
    private MapperWrapperInfo mapperWrapperInfo;
    private JobInfo jobInfo;
    private String jobId;

    @Override
    public String handleRequest(MapperWrapperInfo mapperWrapperInfo, Context context) {

        try {
            this.s3Client = AmazonS3Provider.getS3Client();
            this.jobInfo = JobInfoProvider.getJobInfo();

            this.jobId = this.jobInfo.getJobId();

            this.mapperWrapperInfo = mapperWrapperInfo;

            this.mapperLogic = instantiateMapperClass();


            List<ObjectInfoSimple> batch = mapperWrapperInfo.getBatch();

            processBatch(batch);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "OK";
    }

    private MapperAbstract instantiateMapperClass() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return (MapperAbstract) getClass().getClassLoader().loadClass("mapper.Mapper").newInstance();
    }

    private void processBatch(List<ObjectInfoSimple> batch) throws IOException {
        for (ObjectInfoSimple object : batch) {
            String processResult = processKey(object);
            storeResult(processResult, object.getKey());
        }
    }


    private void storeResult(String result, String key) throws IOException {
        String realKey = key.substring(key.lastIndexOf("/") + 1, key.length());

        String destBucket = getDestBucket();

        Commons.storeObject(Commons.TEXT_TYPE,
                result,
                destBucket,
                this.jobId + "/" + realKey);
    }

    private String getDestBucket() {
        return this.jobInfo.getDisableReducer() ? this.jobInfo.getReducerOutputBucket() : this.jobInfo.getMapperOutputBucket();
    }

    private String processKey(ObjectInfoSimple objectInfoSimple) throws IOException {
        S3Object object = s3Client.getObject(objectInfoSimple.getBucket(), objectInfoSimple.getKey());
        S3ObjectInputStream objectContentRawStream = object.getObjectContent();
        BufferedReader objectBufferedReader = new BufferedReader(new InputStreamReader(objectContentRawStream));

        String result = this.mapperLogic.map(objectBufferedReader);

        objectBufferedReader.close();
        objectContentRawStream.close();
        object.close();

        return result;
    }

}
