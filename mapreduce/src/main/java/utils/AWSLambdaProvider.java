package utils;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;

public class AWSLambdaProvider {
    private static AWSLambdaAsync client = AWSLambdaAsyncClientBuilder.defaultClient();

    public static AWSLambdaAsync getLambdaClient() {
        return client;
    }

    private AWSLambdaProvider() {
    }
}
