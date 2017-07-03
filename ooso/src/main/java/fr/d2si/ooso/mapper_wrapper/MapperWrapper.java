package fr.d2si.ooso.mapper_wrapper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import fr.d2si.ooso.mapper.MapperAbstract;
import fr.d2si.ooso.utils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import static fr.d2si.ooso.utils.Commons.IGNORED_RETURN_VALUE;

public class MapperWrapper implements RequestHandler<MapperWrapperInfo, String> {
    private MapperAbstract mapperLogic;

    private MapperWrapperInfo mapperWrapperInfo;
    private JobInfo jobInfo;
    private String jobId;

    @Override
    public String handleRequest(MapperWrapperInfo mapperWrapperInfo, Context context) {
        try {
            this.mapperWrapperInfo = mapperWrapperInfo;

            this.jobInfo = this.mapperWrapperInfo.getJobInfo();

            this.jobId = this.jobInfo.getJobId();

            this.mapperLogic = instantiateMapperClass();

            List<ObjectInfoSimple> batch = mapperWrapperInfo.getBatch();

            processBatch(batch);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return IGNORED_RETURN_VALUE;
    }

    private MapperAbstract instantiateMapperClass() throws ClassNotFoundException, IOException {
        return (MapperAbstract) Commons.base64ToObject(this.mapperWrapperInfo.getMapperLogicInBase64());
    }

    private void processBatch(List<ObjectInfoSimple> batch) throws IOException {
        for (ObjectInfoSimple object : batch) {
            String processResult = processKey(object);
            storeResult(processResult, object.getKey());
        }
    }

    private String processKey(ObjectInfoSimple objectInfoSimple) throws IOException {
        BufferedReader reader = Commons.getReaderFromObjectInfo(objectInfoSimple);

        String result = this.mapperLogic.map(reader);

        reader.close();

        return result;
    }

    private void storeResult(String result, String key) throws IOException {
        String realKey = getRealKey(key);

        String destBucket = getDestBucket();

        Commons.storeObject(Commons.TEXT_TYPE,
                result,
                destBucket,
                this.jobId + "/" + realKey);
    }

    private String getRealKey(String key) {
        return key.substring(key.lastIndexOf("/") + 1, key.length());
    }

    private String getDestBucket() {
        if (this.jobInfo.getDisableReducer())
            return this.jobInfo.getReducerOutputBucket();
        return this.jobInfo.getMapperOutputBucket();
    }
}
