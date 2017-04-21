package mappers_listener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import coordinator.CoordinatorInfo;
import utils.Commons;
import utils.JobInfo;
import utils.JobInfoProvider;

public class MappersListener implements RequestHandler<Void, String> {
    private static final int HEARTBEAT_INTERVAL = 500;

    @Override
    public String handleRequest(Void aVoid, Context context) {
        try {
            JobInfo jobInfo = JobInfoProvider.getJobInfo();

            int currentMappersOutputFiles = Commons.getBucketObjectSummaries(jobInfo.getMapperOutputBucket(), jobInfo.getJobId()).size();
            int expectedMappersOutputFiles = Commons.getBucketObjectSummaries(jobInfo.getJobInputBucket()).size();

            if (currentMappersOutputFiles == expectedMappersOutputFiles) {
                if (!jobInfo.getDisableReducer())
                    invokeReducerCoordinator();
            } else {
                Thread.sleep(HEARTBEAT_INTERVAL);
                invokeMappersListener();
            }

            return String.valueOf(currentMappersOutputFiles == expectedMappersOutputFiles);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
        return "Ok";
    }

    private void invokeMappersListener() {
        Commons.invokeLambdaAsync("mappers_listener", null);
    }

    private void invokeReducerCoordinator() {
        CoordinatorInfo coordinatorInfo = new CoordinatorInfo(0);
        Commons.invokeLambdaAsync("coordinator", coordinatorInfo);
    }

}
