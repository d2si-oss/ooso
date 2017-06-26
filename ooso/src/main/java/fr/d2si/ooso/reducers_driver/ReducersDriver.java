package fr.d2si.ooso.reducers_driver;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import fr.d2si.ooso.reducer_wrapper.ReducerWrapperInfo;
import fr.d2si.ooso.reducers_listener.ReducersListenerInfo;
import static fr.d2si.ooso.utils.Commons.*;

import fr.d2si.ooso.utils.Commons;
import fr.d2si.ooso.utils.JobInfo;
import fr.d2si.ooso.utils.ObjectInfoSimple;

import java.util.List;

import static fr.d2si.ooso.utils.Commons.IGNORED_RETURN_VALUE;

public class ReducersDriver implements RequestHandler<ReducersDriverInfo, String> {

    private JobInfo jobInfo;

    private String jobId;
    private ReducersDriverInfo reducersDriverInfo;


    @Override
    public String handleRequest(ReducersDriverInfo reducersDriverInfo, Context context) {
        try {
            this.reducersDriverInfo = reducersDriverInfo;

            this.jobInfo = this.reducersDriverInfo.getJobInfo();

            this.jobId = this.jobInfo.getJobId();

            launchReducers(reducersDriverInfo.getStep());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return IGNORED_RETURN_VALUE;
    }

    private void launchReducers(int reduceStep) {

        String inputPrefix = getInputPrefix(reduceStep);
        String inputBucket = whichInputBucket(reduceStep);
        List<List<ObjectInfoSimple>> batches = getBatches(inputPrefix, inputBucket);

        invokeReducersListener(reduceStep, batches.size());
        invokeReducers(reduceStep, batches);
    }

    private String getInputPrefix(int reduceStep) {
        if (reduceStep == 0)
            return this.jobId + "/";
        return this.jobId + "/" + (reduceStep - 1) + "-";
    }

    private String whichInputBucket(int reduceStep) {
        if (reduceStep == 0)
            return this.jobInfo.getMapperOutputBucket();
        return this.jobInfo.getReducerOutputBucket();
    }

    private List<List<ObjectInfoSimple>> getBatches(String inputPrefix, String inputBucket) {
        return Commons.getBatches(
                        inputBucket,
                        this.jobInfo.getReducerMemory(),
                        inputPrefix,
                        this.jobInfo.getReducerForceBatchSize());
    }

    private void invokeReducersListener(int step, int batchSize) {
        ReducersListenerInfo reducersListenerInfo = new ReducersListenerInfo(step, batchSize, this.reducersDriverInfo.getReducerInBase64(), this.jobInfo);
        invokeLambdaAsync(this.jobInfo.getReducersListenerFunctionName(), reducersListenerInfo);
    }

    private void invokeReducers(int reduceStep, List<List<ObjectInfoSimple>> batches) {
        int id = 0;
        for (List<ObjectInfoSimple> batch : batches) {
            ReducerWrapperInfo reducerWrapperInfo = new ReducerWrapperInfo(
                    id++,
                    batch,
                    reduceStep,
                    this.reducersDriverInfo.getReducerInBase64(),
                    batches.size() == 1,
                    this.jobInfo);

            invokeLambdaAsync(this.jobInfo.getReducerFunctionName(), reducerWrapperInfo);
        }
    }
}
