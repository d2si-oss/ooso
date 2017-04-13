package coordinator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import reducer_wrapper.ReducerWrapperInfo;
import utils.Commons;
import utils.JobInfo;
import utils.JobInfoProvider;
import utils.ObjectInfoSimple;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Coordinator implements RequestHandler<CoordinatorInfo, String> {

    private JobInfo jobInfo;

    private String jobId;


    @Override
    public String handleRequest(CoordinatorInfo coordinatorInfo, Context context) {
        try {
            this.jobInfo = JobInfoProvider.getJobInfo();

            if (!this.jobInfo.getDisableReducer()) {
                this.jobId = this.jobInfo.getJobId();

                boolean notYetFinished = launchReducers(coordinatorInfo.getStep());

                if (notYetFinished) {
                    CoordinatorInfo nextCoordinatorInfo = new CoordinatorInfo(coordinatorInfo.getStep() + 1);
                    Commons.invokeLambdaAsync("coordinator", nextCoordinatorInfo);
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "OK";
    }

    private boolean launchReducers(int reduceStep) throws IOException, InterruptedException {

        String inputPrefix = getInputPrefix(reduceStep);
        String inputBucket = whichInputBucket(reduceStep);
        List<List<ObjectInfoSimple>> batches = Commons
                .getBatches(
                        inputBucket,
                        this.jobInfo.getReducerMemory(),
                        inputPrefix,
                        this.jobInfo.getReducerForceBatchSize());

        invokeReducers(reduceStep, batches);

        return batches.size() > 1;
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

        ExecutorService executorService = Executors.newFixedThreadPool(batches.size());

        for (List<ObjectInfoSimple> batch : batches) {
            final int finalId = id;
            executorService.submit(() -> {

                ReducerWrapperInfo reducerWrapperInfo = new ReducerWrapperInfo(
                        finalId,
                        batch,
                        reduceStep,
                        batches.size() == 1);

                Commons.invokeLambdaSync(this.jobInfo.getReducerFunctionName(), reducerWrapperInfo);
            });

            id++;
        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
    }
}
