package reducers_listener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import coordinator.CoordinatorInfo;
import utils.Commons;
import utils.JobInfo;
import utils.JobInfoProvider;

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
        CoordinatorInfo coordinatorInfo = new CoordinatorInfo(this.reducersListenerInfo.getStep() + 1);
        Commons.invokeLambdaAsync("coordinator", coordinatorInfo);
    }
}
