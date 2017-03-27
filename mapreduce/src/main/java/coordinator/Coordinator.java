package coordinator;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.Gson;
import driver.MappersInfo;
import reducer_wrapper.ReducerStepInfo;
import reducer_wrapper.ReducerWrapperInfo;
import utils.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Coordinator implements RequestHandler<S3Event, String> {

    private AmazonS3 s3Client;
    private Gson gson;
    private JobInfo jobInfo;

    private String jobId;

    @Override
    public String handleRequest(S3Event event, Context context) {
        try {
            this.s3Client = AmazonS3ClientBuilder.standard().build();
            this.jobInfo = JobInfoProvider.getJobInfo();
            this.gson = new Gson();

            this.jobId = this.jobInfo.getJobId();


            S3EventNotification.S3Entity sourceRecord = event.getRecords().get(0).getS3();
            String sourceBucket = sourceRecord.getBucket().getName();
            String sourceKey = sourceRecord.getObject().getKey();

            if (sourceBucket.equals(this.jobInfo.getMapperOutputBucket())) {
                //map finish not yet detected
                if (checkMapComplete()) {
                    if (!checkMapFinishAlreadyMarked()) {
                        markMapFinished();

                        //launch first step of reducer
                        startReducePhase();
                    }
                }
            } else if (sourceBucket.equals(this.jobInfo.getReducerOutputBucket())) {

                String currentStep = sourceKey.substring(0, sourceKey.indexOf("-"));

                ReducerStepInfo stepInfo = Commons.getStepInfo(this.jobId, Integer.parseInt(currentStep));

                if (stepInfo.getFilesProcessed() == stepInfo.getFilesToProcess()) {
                    if (stepInfo.getBatchesCount() != 1) {
                        launchReducers(Integer.valueOf(currentStep) + 1);
                    }
                }
            }


            return "OK";


        } catch (IOException e) {
            e.printStackTrace();
            return "KO";
        }
    }

    private void markMapFinished() {
        Table statusTable = StatusTableProvider.getStatusTable();

        Item item = new Item()
                .withString("job", this.jobId)
                .withNumber("step", Commons.MAP_DONE_DUMMY_STEP)
                .withBoolean("map_done", true);

        statusTable.putItem(item);
    }

    private void startReducePhase() throws IOException {
        launchReducers(0);
    }

    private void launchReducers(int reduceStep) throws IOException {
        List<List<ObjectInfoSimple>> batches = Commons
                .getBatches(reduceStep == 0 ? this.jobInfo.getMapperOutputBucket() : this.jobInfo.getReducerOutputBucket(),
                        this.jobInfo.getReducerMemory(),
                        reduceStep == 0 ? this.jobId + "-" : this.jobId + "-" + (reduceStep - 1) + "-");

        if (reduceStep == 0) {
            int filesToProcess = batches.stream()
                    .map(List::size)
                    .reduce((s1, s2) -> s1 + s2)
                    .get();

            Commons.updateStepInfo(this.jobId, reduceStep, filesToProcess, 0, batches.size());
        } else {
            Commons.setBatchesCount(this.jobId, reduceStep, batches.size());
        }


        int id = 0;
        for (List<ObjectInfoSimple> batch : batches) {
            ReducerWrapperInfo reducerWrapperInfo = new ReducerWrapperInfo(id++, batch, reduceStep);

            String payload = this.gson.toJson(reducerWrapperInfo);

            Commons.invokeLambdaAsync(this.jobInfo.getReducerFunctionName(), payload);
        }
    }


    private boolean checkMapFinishAlreadyMarked() {
        Table statusTable = StatusTableProvider.getStatusTable();

        Item item = statusTable.getItem(new GetItemSpec()
                .withPrimaryKey("job", this.jobId, "step", Commons.MAP_DONE_DUMMY_STEP)
                .withConsistentRead(true));
        return item != null;
    }

    private boolean checkMapComplete() {
        List<S3ObjectSummary> currentMapOutputs = Commons.getBucketObjectSummaries(this.jobInfo.getMapperOutputBucket(), this.jobId + "-");
        MappersInfo mappersInfo = getMappersInfo();
        Map<Integer, Integer> currentMapProgress = currentMapOutputs.stream()
                .map(s3ObjectSummary -> {
                    String key = s3ObjectSummary.getKey();
                    String mapperIdRaw = key.substring(key.lastIndexOf("-") + 1, key.length());
                    return Integer.parseInt(mapperIdRaw);
                })
                .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));

        return currentMapProgress.equals(mappersInfo.getBatchCountPerMapper());
    }

    private MappersInfo getMappersInfo() {
        Table statusTable = StatusTableProvider.getStatusTable();

        String mappersInfoJson = statusTable.getItem(new GetItemSpec().withPrimaryKey("job", this.jobId, "step", Commons.MAP_INFO_DUMMY_STEP))
                .getJSON("map_info");

        return this.gson.fromJson(mappersInfoJson, MappersInfo.class);
    }
}
