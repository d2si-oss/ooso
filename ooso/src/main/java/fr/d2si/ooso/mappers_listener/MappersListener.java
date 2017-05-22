package fr.d2si.ooso.mappers_listener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import fr.d2si.ooso.reducers_driver.ReducersDriverInfo;
import fr.d2si.ooso.utils.Commons;
import fr.d2si.ooso.utils.JobInfo;
import fr.d2si.ooso.utils.JobInfoProvider;

public class MappersListener implements RequestHandler<Void, String> {
    private static final int HEARTBEAT_INTERVAL = 500;

    @Override
    public String handleRequest(Void aVoid, Context context) {
        try {
            JobInfo jobInfo = JobInfoProvider.getJobInfo();

            int currentMappersOutputFiles = Commons.getBucketObjectSummaries(jobInfo.getMapperOutputBucket(), jobInfo.getJobId()).size();
            int expectedMappersOutputFiles = Commons.getBucketObjectSummaries(jobInfo.getJobInputBucket()).size();

            if (!jobInfo.getDisableReducer()) {
                if (currentMappersOutputFiles == expectedMappersOutputFiles)
                    invokeReducerCoordinator();
                else {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                    invokeMappersListener();
                }
            }

            return String.valueOf(currentMappersOutputFiles == expectedMappersOutputFiles);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeMappersListener() {
        Commons.invokeLambdaAsync("mappers_listener", null);
    }

    private void invokeReducerCoordinator() {
        ReducersDriverInfo reducersDriverInfo = new ReducersDriverInfo(0);
        Commons.invokeLambdaAsync("reducers_driver", reducersDriverInfo);
    }

}
