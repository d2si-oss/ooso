package integration_test;

import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.s3.AmazonS3;
import fr.d2si.ooso.utils.*;
import org.junit.*;
import org.junit.Test;

import java.io.File;

public class NormalExecutionTest {
    private static AWSLambdaAsyncMockClient lambdaClient;
    private static AmazonS3 s3Client;
    private static JobInfo jobInfo;

    @BeforeClass
    public static void setUp() throws Exception {
        jobInfo = JobInfoProvider.getJobInfo();

        lambdaClient = (AWSLambdaAsyncMockClient) AWSLambdaProvider.getLambdaClient();

        s3Client = AmazonS3Provider.getS3Client();

        s3Client.createBucket(Commons.getBucketFromFullPath(jobInfo.getJobInputBucket()));
        s3Client.createBucket(jobInfo.getMapperOutputBucket());
        s3Client.createBucket(jobInfo.getReducerOutputBucket());

        //putting test data in local s3 bucket
        File dataDir = new File(new File("").getAbsoluteFile().getParent() + "/test-data/250mb");
        for (File file : dataDir.listFiles()) {
            Commons.storeObject(Commons.TEXT_TYPE, file, Commons.getBucketFromFullPath(jobInfo.getJobInputBucket()), "250mb/" + file.getName());
        }

    }

    @Test
    public void normalExec() throws Exception {
        lambdaClient.invoke(new InvokeRequest()
                .withPayload("")
                .withInvocationType(InvocationType.Event)
                .withFunctionName("mappers_driver"));

        lambdaClient.awaitWorkflowEnd();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        File dataDir = new File(new File("").getAbsoluteFile().getParent() + "/test-data/250mb");
        for (File file : dataDir.listFiles())
            s3Client.deleteObject(Commons.getBucketFromFullPath(jobInfo.getJobInputBucket()), "250mb/" + file.getName());

        Commons.emptyBucket(Commons.getBucketFromFullPath(jobInfo.getJobInputBucket()));
        Commons.emptyBucket(jobInfo.getMapperOutputBucket());
        Commons.emptyBucket(jobInfo.getReducerOutputBucket());

        s3Client.deleteBucket(Commons.getBucketFromFullPath(jobInfo.getJobInputBucket()));
        s3Client.deleteBucket(jobInfo.getMapperOutputBucket());
        s3Client.deleteBucket(jobInfo.getReducerOutputBucket());

    }
}
