package integration_test;

import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import fr.d2si.serverless_mapreduce.utils.AWSLambdaAsyncMockClient;
import fr.d2si.serverless_mapreduce.utils.AWSLambdaProvider;
import org.junit.Before;
import org.junit.Test;

public class NormalExecutionTest {
    private AWSLambdaAsyncMockClient lambdaClient;

    @Before
    public void setUp() throws Exception {
        lambdaClient = (AWSLambdaAsyncMockClient) AWSLambdaProvider.getLambdaClient();
    }

    @Test
    public void normalExec() throws Exception {
        lambdaClient.invoke(new InvokeRequest()
                .withPayload("")
                .withInvocationType(InvocationType.Event)
                .withFunctionName("mappers_driver"));

        lambdaClient.awaitWorkflowEnd();
    }
}
