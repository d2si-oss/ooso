package producer;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import utils.ConfigurationUtils;
import utils.CredentialsUtils;

import java.nio.ByteBuffer;
import java.util.Random;


/**
 * Adapted from example in this url https://aws.amazon.com/kinesis/streams/getting-started/
 */
public class RandomNumberProducer {
    /**
     * Checks if the stream exists and is active
     *
     * @param kinesisClient Amazon Kinesis client instance
     * @param streamName    Name of stream
     */
    private static void validateStream(AmazonKinesis kinesisClient, String streamName) {
        try {
            DescribeStreamResult result = kinesisClient.describeStream(streamName);
            if (!"ACTIVE".equals(result.getStreamDescription().getStreamStatus())) {
                System.err.println("Stream " + streamName + " is not active. Please wait a few moments and try again.");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String streamName = args[0];
        String regionName = args[1];

        try {
            AWSCredentials credentials = CredentialsUtils.getCredentialsProvider().getCredentials();
            AmazonKinesis kinesisClient = new AmazonKinesisClient(credentials,
                    ConfigurationUtils.getClientConfigWithUserAgent());
            kinesisClient.setRegion(RegionUtils.getRegion(regionName));

            validateStream(kinesisClient, streamName);

            while (true) {
                sendRecord(kinesisClient, streamName);
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendRecord(AmazonKinesis kinesisClient, String streamName) {
        int anInt = new Random().nextInt();
        PutRecordRequest putRecordRequest = new PutRecordRequest();
        putRecordRequest.setStreamName(streamName);
        putRecordRequest.setData(ByteBuffer.wrap(String.valueOf(anInt).getBytes()));
        putRecordRequest.setPartitionKey(String.valueOf(anInt));
        System.out.println("Putting number: " + anInt);
        kinesisClient.putRecord(putRecordRequest);
    }
}
