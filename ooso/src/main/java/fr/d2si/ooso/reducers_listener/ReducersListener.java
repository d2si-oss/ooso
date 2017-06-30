package fr.d2si.ooso.reducers_listener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import fr.d2si.ooso.reducers_driver.ReducersDriverInfo;

import static fr.d2si.ooso.utils.Commons.*;

import fr.d2si.ooso.utils.JobInfo;

import static fr.d2si.ooso.utils.Commons.IGNORED_RETURN_VALUE;

public class ReducersListener implements RequestHandler<ReducersListenerInfo, String> {
    private static final int HEARTBEAT_INTERVAL = 200;

    private ReducersListenerInfo reducersListenerInfo;
    private JobInfo jobInfo;

    @Override
    public String handleRequest(ReducersListenerInfo reducersListenerInfo, Context context) {
        try {
            this.reducersListenerInfo = reducersListenerInfo;

            this.jobInfo = this.reducersListenerInfo.getJobInfo();

            //if there is only one file to return, we know that it's the final reducer, there is no need to listen for results
            if (reducersListenerInfo.getExpectedFilesCount() != 1) {

                int currentReducersOutputFiles = getCurrentReducerOutputCount(reducersListenerInfo);

                if (currentReducersOutputFiles == reducersListenerInfo.getExpectedFilesCount())
                    invokeNextReducerCoordinator();
                else {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                    invokeReducersListener();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return IGNORED_RETURN_VALUE;
    }

    private int getCurrentReducerOutputCount(ReducersListenerInfo reducersListenerInfo) {
        return getBucketObjectSummaries(
                jobInfo.getReducerOutputBucket(),
                jobInfo.getJobId() + "/" + reducersListenerInfo.getStep() + "-").size();
    }

    private void invokeNextReducerCoordinator() {
        ReducersDriverInfo reducersDriverInfo = new ReducersDriverInfo(
                this.reducersListenerInfo.getStep() + 1,
                this.reducersListenerInfo.getReducerInBase64(),
                this.jobInfo);
        invokeLambdaAsync(this.jobInfo.getReducersDriverFunctionName(), reducersDriverInfo);
    }

    private void invokeReducersListener() {
        invokeLambdaAsync(this.jobInfo.getReducersListenerFunctionName(), this.reducersListenerInfo);
    }
}
