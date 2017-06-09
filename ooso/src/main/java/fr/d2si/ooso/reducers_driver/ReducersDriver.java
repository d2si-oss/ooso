package fr.d2si.ooso.reducers_driver;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import fr.d2si.ooso.reducer_wrapper.ReducerWrapperInfo;
import fr.d2si.ooso.reducers_listener.ReducersListenerInfo;
import fr.d2si.ooso.utils.Commons;
import fr.d2si.ooso.utils.JobInfo;
import fr.d2si.ooso.utils.JobInfoProvider;
import fr.d2si.ooso.utils.ObjectInfoSimple;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class ReducersDriver implements RequestHandler<ReducersDriverInfo, String> {

    private JobInfo jobInfo;

    private String jobId;


    @Override
    public String handleRequest(ReducersDriverInfo reducersDriverInfo, Context context) {
        try {
            this.jobInfo = JobInfoProvider.getJobInfo();

            this.jobId = this.jobInfo.getJobId();

            launchReducers(reducersDriverInfo.getStep());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "OK";
    }

    private void launchReducers(int reduceStep) throws IOException, InterruptedException {

        String inputPrefix = getInputPrefix(reduceStep);
        String inputBucket = whichInputBucket(reduceStep);
        List<List<ObjectInfoSimple>> batches = Commons
                .getBatches(
                        inputBucket,
                        this.jobInfo.getReducerMemory(),
                        inputPrefix,
                        this.jobInfo.getReducerForceBatchSize());

        invokeReducersListener(reduceStep, batches.size());
        invokeReducers(reduceStep, batches);
    }

    private String getInputPrefix(int reduceStep) {
        return reduceStep == 0 ?
                this.jobId + "/" :
                this.jobId + "/" + (reduceStep - 1) + "-";
    }

    private String whichInputBucket(int reduceStep) {
        return reduceStep == 0 ?
                this.jobInfo.getMapperOutputBucket() :
                this.jobInfo.getReducerOutputBucket();
    }

    private void invokeReducers(int reduceStep, List<List<ObjectInfoSimple>> batches) throws InterruptedException, UnsupportedEncodingException {
        int id = 0;
        for (List<ObjectInfoSimple> batch : batches) {
            ReducerWrapperInfo reducerWrapperInfo = new ReducerWrapperInfo(
                    id++,
                    batch,
                    reduceStep,
                    batches.size() == 1);

            Commons.invokeLambdaAsync(this.jobInfo.getReducerFunctionName(), reducerWrapperInfo);
        }
    }

    private void invokeReducersListener(int step, int batchSize) {
        ReducersListenerInfo reducersListenerInfo = new ReducersListenerInfo(step, batchSize);
        Commons.invokeLambdaAsync(this.jobInfo.getReducersListenerFunctionName(), reducersListenerInfo);
    }

}
