package lambda_dynamodb;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

import java.util.Map;

public class ProcessNewItem implements RequestHandler<DynamodbEvent, String> {
    public String handleRequest(DynamodbEvent dynamodbEvent, Context context) {
        try {
            Map<String, AttributeValue> newImage = dynamodbEvent.getRecords().get(0).getDynamodb().getNewImage();
            System.out.println(newImage.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }
}
