package coordinator;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.Gson;
import driver.MappersInfo;
import reducer_wrapper.ReducerStepInfo;
import reducer_wrapper.ReducerWrapperInfo;
import utils.Commons;
import utils.JobInfo;
import utils.JobInfoProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Coordinator implements RequestHandler<S3Event, String> {
    private static final String MAP_DONE_MARKER = "map_done";
    private static final String REDUCE_STEP_DONE_MARKER_SUFFIX = "-done";

    private AmazonS3 s3Client;
    private Gson gson;
    private JobInfo jobInfo;

    @Override
    public String handleRequest(S3Event event, Context context) {
        try {
            this.s3Client = AmazonS3ClientBuilder.standard().build();
            this.jobInfo = JobInfoProvider.getJobInfo();
            this.gson = new Gson();


            S3EventNotification.S3Entity sourceRecord = event.getRecords().get(0).getS3();
            String sourceBucket = sourceRecord.getBucket().getName();
            String sourceKey = sourceRecord.getObject().getKey();

            if (sourceBucket.equals(this.jobInfo.getMapperOutputBucket())) {
                //map finish not yet detected
                if (checkMapComplete()) {
                    if (!checkMapFinishAlreadyMarked()) {
                        s3Client.putObject(jobInfo.getStatusBucket(),
                                MAP_DONE_MARKER,
                                "done");

                        //launch first step of reducer
                        startReducePhase();
                    }
                }
            } else if (sourceBucket.equals(this.jobInfo.getReducerOutputBucket())) {

                String currentStep = sourceKey.substring(0, sourceKey.indexOf("-"));

                ReducerStepInfo stepInfo = Commons.getStepInfo(Integer.parseInt(currentStep));

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

    private void startReducePhase() throws IOException {
        launchReducers(0);
    }

    private void launchReducers(int reduceStep) throws IOException {
        List<List<Map<String, String>>> batches = Commons
                .getBatches(reduceStep == 0 ? this.jobInfo.getMapperOutputBucket() : this.jobInfo.getReducerOutputBucket(),
                        this.jobInfo.getReducerMemory(),
                        reduceStep == 0 ? "" : (reduceStep - 1) + "-");

        if (reduceStep == 0) {
            int filesToProcess = batches.stream()
                    .map(List::size)
                    .reduce((s1, s2) -> s1 + s2)
                    .get();

            Commons.updateStepInfo(reduceStep, filesToProcess, 0, batches.size());
        } else {
            Commons.setBatchesCount(reduceStep, batches.size());
        }

        AWSLambdaAsync lambda = AWSLambdaAsyncClientBuilder.defaultClient();

        int id = 0;
        for (List<Map<String, String>> batch : batches) {
            ReducerWrapperInfo reducerWrapperInfo = new ReducerWrapperInfo(id++, batch, reduceStep);

            String payload = this.gson.toJson(reducerWrapperInfo);

            InvokeRequest request = new InvokeRequest()
                    .withFunctionName(this.jobInfo.getReducerFunctionName())
                    .withInvocationType(InvocationType.Event)
                    .withPayload(payload);

            lambda.invoke(request);
        }
    }


    private boolean checkMapFinishAlreadyMarked() {
        return this.s3Client.doesObjectExist(this.jobInfo.getStatusBucket(), MAP_DONE_MARKER);
    }

    private boolean checkReduceStepFinishAlreadyMarked(int step) {
        return this.s3Client.doesObjectExist(this.jobInfo.getStatusBucket(), step + REDUCE_STEP_DONE_MARKER_SUFFIX);
    }

    private boolean checkMapComplete() {
        List<S3ObjectSummary> currentMapOutputs = Commons.getBucketObjectSummaries(this.jobInfo.getMapperOutputBucket());
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
        S3Object jobInfoS3 = s3Client.getObject(this.jobInfo.getStatusBucket(), this.jobInfo.getMappersInfoName());
        S3ObjectInputStream objectContent = jobInfoS3.getObjectContent();
        BufferedReader objectReader = new BufferedReader(new InputStreamReader(objectContent));
        return this.gson.fromJson(objectReader, MappersInfo.class);
    }
}
