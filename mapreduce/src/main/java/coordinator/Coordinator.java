package coordinator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.google.gson.Gson;
import driver.MappersInfo;
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

    private AmazonS3 s3Client;
    private Gson gson;
    private JobInfo jobInfo;

    @Override
    public String handleRequest(S3Event event, Context context) {
        try {
            this.s3Client = AmazonS3ClientBuilder.standard().build();
            this.gson = new Gson();
            this.jobInfo = JobInfoProvider.getJobInfo();

            boolean mapComplete = checkMapComplete();

            if (mapComplete)
                s3Client.putObject(jobInfo.getStatusBucket(), MAP_DONE_MARKER, "done");

            return "OK";


        } catch (IOException e) {
            e.printStackTrace();
            return "KO";
        }
    }

    private boolean checkMapComplete() {
        MappersInfo mappersInfo = getMappersInfo();
        List<S3ObjectSummary> currentMapOutputs = getBucketObjectSummaries(this.jobInfo.getMapperOutputBucket());

        Map<Integer, Integer> currentMapProgress = currentMapOutputs.parallelStream()
                .map(s3ObjectSummary -> {
                    String key = s3ObjectSummary.getKey();
                    String mapperIdRaw = key.substring(key.lastIndexOf("-") + 1, key.length());
                    return Integer.parseInt(mapperIdRaw);
                })
                .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));

        return currentMapProgress.equals(mappersInfo.getBatchCountPerMapper());
    }

    //todo refactor this, a similar method exists elsewhere
    private List<S3ObjectSummary> getBucketObjectSummaries(String mapperOutputBucket) {
        final ListObjectsRequest req = new ListObjectsRequest().withBucketName(mapperOutputBucket);
        ObjectListing objectListing = s3Client.listObjects(req);
        return objectListing.getObjectSummaries();
    }

    private MappersInfo getMappersInfo() {
        S3Object jobInfoS3 = s3Client.getObject(this.jobInfo.getStatusBucket(), this.jobInfo.getMappersInfoName());
        S3ObjectInputStream objectContent = jobInfoS3.getObjectContent();
        BufferedReader objectReader = new BufferedReader(new InputStreamReader(objectContent));
        return this.gson.fromJson(objectReader, MappersInfo.class);
    }
}
