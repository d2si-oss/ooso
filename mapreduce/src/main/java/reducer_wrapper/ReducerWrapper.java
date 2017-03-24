package reducer_wrapper;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import reducer_logic.ReducerLogic;
import utils.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ReducerWrapper implements RequestHandler<ReducerWrapperInfo, String> {

    private JobInfo jobInfo;
    private ReducerWrapperInfo reducerWrapperInfo;

    @Override
    public String handleRequest(ReducerWrapperInfo reducerWrapperInfo, Context context) {

        try {

            this.jobInfo = JobInfoProvider.getJobInfo();
            this.reducerWrapperInfo = reducerWrapperInfo;

            List<ObjectInfoSimple> batch = reducerWrapperInfo.getBatch();

            String reduceResult = processBatch(batch);


            storeResult(reduceResult, reducerWrapperInfo.getStep() + "-reducer-" + reducerWrapperInfo.getId());

        } catch (Exception e) {
            e.printStackTrace();
        }


        return "OK";
    }

    private String processBatch(List<ObjectInfoSimple> batch) throws Exception {

        String result = ReducerLogic.reduceResultCalculator(batch);

        Commons.incrementFilesProcessed(this.reducerWrapperInfo.getStep(), batch.size());

        if (Commons.getStepInfo(this.reducerWrapperInfo.getStep()).getBatchesCount() != 1) {
            Table statusTable = StatusTableProvider.getStatusTable();
            Item step = statusTable.getItem(new GetItemSpec()
                    .withPrimaryKey("step", this.reducerWrapperInfo.getStep() + 1)
                    .withConsistentRead(true));
            if (step == null) {
                Commons.updateStepInfo(this.reducerWrapperInfo.getStep() + 1, 1, 0);
            }
            else {
                Commons.incrementFilesToProcess(this.reducerWrapperInfo.getStep() + 1, 1);
            }

        }

        return result;
    }


    private void storeResult(String result, String key) throws IOException {
        Commons.storeObject(Commons.JSON_TYPE,
                result,
                jobInfo.getReducerOutputBucket(),
                key);
    }
}
