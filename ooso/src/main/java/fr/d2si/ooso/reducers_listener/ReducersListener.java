package fr.d2si.ooso.reducers_listener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import fr.d2si.ooso.reducers_driver.ReducersDriverInfo;
import fr.d2si.ooso.utils.Commons;
import fr.d2si.ooso.utils.JobInfo;
import fr.d2si.ooso.utils.JobInfoProvider;

public class ReducersListener implements RequestHandler<ReducersListenerInfo, String> {
    private static final int HEARTBEAT_INTERVAL = 200;

    private ReducersListenerInfo reducersListenerInfo;
    private JobInfo jobInfo;

    @Override
    public String handleRequest(ReducersListenerInfo reducersListenerInfo, Context context) {
        try {
            this.reducersListenerInfo = reducersListenerInfo;

            this.jobInfo = JobInfoProvider.getJobInfo();

            //if there is only one file to return, we know that it's the final reducer, there is no need to listen for results
            if (reducersListenerInfo.getExpectedFilesCount() != 1) {

                int currentReducersOutputFiles = Commons.getBucketObjectSummaries(
                        jobInfo.getReducerOutputBucket(),
                        jobInfo.getJobId() + "/" + reducersListenerInfo.getStep() + "-").size();

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
        return null;
    }

    private void invokeNextReducerCoordinator() {
        ReducersDriverInfo reducersDriverInfo = new ReducersDriverInfo(this.reducersListenerInfo.getStep() + 1);
        Commons.invokeLambdaAsync(this.jobInfo.getReducersDriverFunctionName(), reducersDriverInfo);
    }

    private void invokeReducersListener() {
        Commons.invokeLambdaAsync(this.jobInfo.getReducersListenerFunctionName(), this.reducersListenerInfo);
    }
}
