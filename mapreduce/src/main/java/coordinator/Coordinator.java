package coordinator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import reducer_wrapper.ReducerWrapperInfo;
import utils.Commons;
import utils.JobInfo;
import utils.JobInfoProvider;
import utils.ObjectInfoSimple;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Coordinator implements RequestHandler<CoordinatorInfo, String> {

    private Gson gson;
    private JobInfo jobInfo;

    private String jobId;


    @Override
    public String handleRequest(CoordinatorInfo coordinatorInfo, Context context) {
        try {
            this.jobInfo = JobInfoProvider.getJobInfo();
            this.gson = new Gson();

            this.jobId = this.jobInfo.getJobId();

            boolean notYetFinished = launchReducers(coordinatorInfo.getStep());

            if (notYetFinished) {
                CoordinatorInfo nextCoordinatorInfo = new CoordinatorInfo(coordinatorInfo.getStep() + 1);
                String payload = this.gson.toJson(nextCoordinatorInfo);
                Commons.invokeLambdaSync("coordinator", payload);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean launchReducers(int reduceStep) throws IOException, InterruptedException {

        List<List<ObjectInfoSimple>> batches = Commons
                .getBatches(reduceStep == 0 ? this.jobInfo.getMapperOutputBucket() : this.jobInfo.getReducerOutputBucket(),
                        this.jobInfo.getReducerMemory(),
                        reduceStep == 0 ? this.jobId + "/" : this.jobId + "/" + (reduceStep - 1) + "-", 2);

        invokeReducers(reduceStep, batches);

        return batches.size() > 1;
    }

    private void invokeReducers(int reduceStep, List<List<ObjectInfoSimple>> batches) throws InterruptedException {
        int id = 0;

        ExecutorService executorService = Executors.newFixedThreadPool(batches.size());

        for (List<ObjectInfoSimple> batch : batches) {
            final int finalId = id;
            executorService.submit(() -> {
                ReducerWrapperInfo reducerWrapperInfo = new ReducerWrapperInfo(finalId, batch, reduceStep);

                String payload = this.gson.toJson(reducerWrapperInfo);

                Commons.invokeLambdaSync(this.jobInfo.getReducerFunctionName(), payload);
            });

            id++;
        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
    }
}
