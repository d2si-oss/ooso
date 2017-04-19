package mappers_listener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import coordinator.CoordinatorInfo;
import utils.Commons;
import utils.JobInfo;
import utils.JobInfoProvider;

public class MappersListener implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        try {
            JobInfo jobInfo = JobInfoProvider.getJobInfo();

            int currentMappersOutputFiles = Commons.getBucketObjectSummaries(jobInfo.getMapperOutputBucket(), jobInfo.getJobId()).size();
            int expectedMappersOutputFiles = Commons.getBucketObjectSummaries(jobInfo.getJobInputBucket()).size();

            System.out.println(String.valueOf(currentMappersOutputFiles == expectedMappersOutputFiles));

            if (currentMappersOutputFiles == expectedMappersOutputFiles)
                invokeReducerCoordinator();

            return String.valueOf(currentMappersOutputFiles == expectedMappersOutputFiles);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
        return "Ok";
    }

    private void invokeReducerCoordinator() {
        CoordinatorInfo coordinatorInfo = new CoordinatorInfo(0);
        Commons.invokeLambdaAsync("coordinator", coordinatorInfo);
    }

}
