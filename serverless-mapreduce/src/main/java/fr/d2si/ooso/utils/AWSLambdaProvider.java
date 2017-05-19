package fr.d2si.ooso.utils;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;

public class AWSLambdaProvider {

    public static AWSLambda getLambdaClient() {
        String smr_stage = System.getenv().get("SMR_STAGE");
        if (smr_stage.equals("DEV"))
            return FakeAWSLambdaHolder.LAMBDA_CLIENT;
        else
            return AWSLambdaHolder.LAMBDA_CLIENT;
    }

    private static class AWSLambdaHolder {
        private static final AWSLambda LAMBDA_CLIENT = AWSLambdaClientBuilder.defaultClient();
    }

    private static class FakeAWSLambdaHolder {
        private static final AWSLambdaAsyncMockClient LAMBDA_CLIENT = new AWSLambdaAsyncMockClient();
    }

    private AWSLambdaProvider() {
    }
}
