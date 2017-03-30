package utils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

public class StatusTableProvider {

    private static final class TableHolder {
        static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();

        static DynamoDB dynamoDB = new DynamoDB(client);

        static JobInfo jobInfo = JobInfoProvider.getJobInfo();

        private static final Table statusTable = dynamoDB.getTable(jobInfo.getStatusTable());
    }

    public static Table getStatusTable() {
        return TableHolder.statusTable;
    }

    private StatusTableProvider() {
    }
}
