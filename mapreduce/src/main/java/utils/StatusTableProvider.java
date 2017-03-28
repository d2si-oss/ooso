package utils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

public class StatusTableProvider {
    private static Table statusTable;

    static {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();

        DynamoDB dynamoDB = new DynamoDB(client);

        JobInfo jobInfo = JobInfoProvider.getJobInfo();

        statusTable = dynamoDB.getTable(jobInfo.getStatusTable());
    }

    public static Table getStatusTable() {
        return statusTable;
    }

    private StatusTableProvider() {
    }
}
