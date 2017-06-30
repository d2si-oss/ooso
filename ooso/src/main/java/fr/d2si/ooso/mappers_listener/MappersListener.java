package fr.d2si.ooso.mappers_listener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import fr.d2si.ooso.reducers_driver.ReducersDriverInfo;
import fr.d2si.ooso.utils.JobInfo;

import static fr.d2si.ooso.utils.Commons.*;

public class MappersListener implements RequestHandler<MappersListenerInfo, String> {
    private static final int HEARTBEAT_INTERVAL = 500;

    private JobInfo jobInfo;
    private MappersListenerInfo mappersListenerInfo;

    @Override
    public String handleRequest(MappersListenerInfo mappersListenerInfo, Context context) {
        try {
            this.mappersListenerInfo = mappersListenerInfo;

            this.jobInfo = mappersListenerInfo.getJobInfo();

            int currentMappersOutputFiles = getCurrentMappersOutputCount();

            int expectedMappersOutputFiles = getExpectedMappersOutputCount();

            if (currentMappersOutputFiles == expectedMappersOutputFiles) {
                if (!jobInfo.getDisableReducer())
                    invokeReducerCoordinator();
            } else {
                Thread.sleep(HEARTBEAT_INTERVAL);
                invokeMappersListener();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return IGNORED_RETURN_VALUE;
    }

    private int getCurrentMappersOutputCount() {
        if (!jobInfo.getDisableReducer())
            return getBucketObjectSummaries(jobInfo.getMapperOutputBucket(), jobInfo.getJobId()).size();
        return getBucketObjectSummaries(jobInfo.getReducerOutputBucket(), jobInfo.getJobId()).size();
    }

    private int getExpectedMappersOutputCount() {
        return getBucketObjectSummaries(jobInfo.getJobInputBucket()).size();
    }

    private void invokeReducerCoordinator() {
        ReducersDriverInfo reducersDriverInfo = new ReducersDriverInfo(0, this.mappersListenerInfo.getReducerInBase64(), this.jobInfo);
        invokeLambdaAsync(this.jobInfo.getReducersDriverFunctionName(), reducersDriverInfo);
    }

    private void invokeMappersListener() {
        invokeLambdaAsync(this.jobInfo.getMappersListenerFunctionName(), this.mappersListenerInfo);
    }
}
