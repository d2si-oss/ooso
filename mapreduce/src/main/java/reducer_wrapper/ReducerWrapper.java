package reducer_wrapper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import reducer_logic.ReducerLogic;
import utils.Commons;
import utils.JobInfo;
import utils.JobInfoProvider;
import utils.ObjectInfoSimple;

import java.io.IOException;
import java.util.List;

public class ReducerWrapper implements RequestHandler<ReducerWrapperInfo, String> {

    private JobInfo jobInfo;

    private String jobId;

    @Override
    public String handleRequest(ReducerWrapperInfo reducerWrapperInfo, Context context) {

        try {

            this.jobInfo = JobInfoProvider.getJobInfo();

            this.jobId = this.jobInfo.getJobId();

            List<ObjectInfoSimple> batch = reducerWrapperInfo.getBatch();

            String reduceResult = processBatch(batch);


            storeResult(reduceResult, this.jobId + "/" + reducerWrapperInfo.getStep() + "-reducer-" + reducerWrapperInfo.getId());

        } catch (Exception e) {
            e.printStackTrace();
        }


        return "OK";
    }

    private String processBatch(List<ObjectInfoSimple> batch) throws Exception {
        return ReducerLogic.reduceResultCalculator(batch);
    }


    private void storeResult(String result, String key) throws IOException {
        Commons.storeObject(Commons.JSON_TYPE,
                result,
                jobInfo.getReducerOutputBucket(),
                key);
    }
}
