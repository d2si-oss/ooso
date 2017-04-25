package fr.d2si.serverless_mapreduce.mappers_listener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import fr.d2si.serverless_mapreduce.reducers_driver.ReducersDriverInfo;
import fr.d2si.serverless_mapreduce.utils.Commons;
import fr.d2si.serverless_mapreduce.utils.JobInfo;
import fr.d2si.serverless_mapreduce.utils.JobInfoProvider;

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
        ReducersDriverInfo reducersDriverInfo = new ReducersDriverInfo(0);
        Commons.invokeLambdaAsync("reducers_driver", reducersDriverInfo);
    }

}
