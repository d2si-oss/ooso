package fr.d2si.serverless_mapreduce.utils;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;

public class AWSLambdaProvider {

    public static AWSLambda getLambdaClient() {
        return AWSLambdaHolder.LAMBDA_CLIENT;
    }

    private static class AWSLambdaHolder {
        private static final AWSLambda LAMBDA_CLIENT = AWSLambdaClientBuilder.defaultClient();
    }

    private AWSLambdaProvider() {
    }
}
