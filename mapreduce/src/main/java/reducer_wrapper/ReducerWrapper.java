package reducer_wrapper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import reducer_logic.ReducerLogic;
import utils.*;

import java.io.IOException;
import java.util.List;

public class ReducerWrapper implements RequestHandler<ReducerWrapperInfo, String> {

    private JobInfo jobInfo;

    private String jobId;
    private ReducerWrapperInfo reducerWrapperInfo;

    @Override
    public String handleRequest(ReducerWrapperInfo reducerWrapperInfo, Context context) {

        try {

            this.jobInfo = JobInfoProvider.getJobInfo();

            this.jobId = this.jobInfo.getJobId();

            this.reducerWrapperInfo = reducerWrapperInfo;

            List<ObjectInfoSimple> batch = reducerWrapperInfo.getBatch();

            String reduceResult = processBatch(batch);

            storeResult(reduceResult);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return "OK";
    }

    private String processBatch(List<ObjectInfoSimple> batch) throws Exception {
        return ReducerLogic.reduceResultCalculator(batch);
    }


    private void storeResult(String result) throws IOException {
        Commons.storeObject(Commons.JSON_TYPE,
                result,
                jobInfo.getReducerOutputBucket(),
                this.jobId + "/" + this.reducerWrapperInfo.getStep() + "-reducer-" + this.reducerWrapperInfo.getId());
    }
}
