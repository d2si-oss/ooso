package fr.d2si.serverless_mapreduce.reducers_listener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import fr.d2si.serverless_mapreduce.reducers_driver.ReducersDriverInfo;
import fr.d2si.serverless_mapreduce.utils.Commons;
import fr.d2si.serverless_mapreduce.utils.JobInfo;
import fr.d2si.serverless_mapreduce.utils.JobInfoProvider;

public class ReducersListener implements RequestHandler<ReducersListenerInfo, String> {
    private static final int HEARTBEAT_INTERVAL = 200;

    private ReducersListenerInfo reducersListenerInfo;

    @Override
    public String handleRequest(ReducersListenerInfo reducersListenerInfo, Context context) {
        try {
            this.reducersListenerInfo = reducersListenerInfo;

            JobInfo jobInfo = JobInfoProvider.getJobInfo();

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

        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    private void invokeReducersListener() {
        Commons.invokeLambdaAsync("reducers_listener", this.reducersListenerInfo);
    }

    private void invokeNextReducerCoordinator() {
        ReducersDriverInfo reducersDriverInfo = new ReducersDriverInfo(this.reducersListenerInfo.getStep() + 1);
        Commons.invokeLambdaAsync("reducers_driver", reducersDriverInfo);
    }
}
